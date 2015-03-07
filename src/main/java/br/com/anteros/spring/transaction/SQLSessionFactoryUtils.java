/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.spring.transaction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.jta.SpringJtaSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.query.SQLQuery;
import br.com.anteros.spring.util.AnterosSpringTranslate;

/**
 * Classe utilitária para {@link SQLSessionFactory}.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public abstract class SQLSessionFactoryUtils {

	public static final int SESSION_SYNCHRONIZATION_ORDER =
			DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100;

	static final Log logger = LogFactory.getLog(SQLSessionFactoryUtils.class);

	private static final ThreadLocal<Map<SQLSessionFactory, Set<SQLSession>>> deferredCloseHolder =
			new NamedThreadLocal<Map<SQLSessionFactory, Set<SQLSession>>>("Anteros Sessions registered for deferred close");


	public static DataSource getDataSource(SQLSessionFactory sessionFactory) {
		if (sessionFactory instanceof AbstractSQLSessionFactory) {
			return ((AbstractSQLSessionFactory) sessionFactory).getDatasource();
		}
		return null;
	}


	public static SQLExceptionTranslator newJdbcExceptionTranslator(SQLSessionFactory sessionFactory) {
		DataSource ds = getDataSource(sessionFactory);
		if (ds != null) {
			return new SQLErrorCodeSQLExceptionTranslator(ds);
		}
		return new SQLStateSQLExceptionTranslator();
	}


	public static TransactionManager getJtaTransactionManager(SQLSessionFactory sessionFactory, SQLSession session)
			throws Exception {
		AbstractSQLSessionFactory sessionFactoryImpl = null;
		if (sessionFactory instanceof AbstractSQLSessionFactory) {
			sessionFactoryImpl = ((AbstractSQLSessionFactory) sessionFactory);
		} else if (session != null) {
			SQLSessionFactory internalFactory = session.getSQLSessionFactory();
			if (internalFactory instanceof AbstractSQLSessionFactory) {
				sessionFactoryImpl = (AbstractSQLSessionFactory) internalFactory;
			}
		}
		return (sessionFactoryImpl != null ? sessionFactoryImpl.getTransactionManager() : null);
	}


	public static SQLSession getSession(SQLSessionFactory sessionFactory, boolean allowCreate)
			throws DataAccessResourceFailureException, IllegalStateException {

		try {
			return doGetSession(sessionFactory, allowCreate);
		} catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not open Anteros SQLSession", ex);
		}
	}

	public static SQLSession getSession(SQLSessionFactory sessionFactory)
			throws DataAccessResourceFailureException {

		try {
			return doGetSession(sessionFactory, true);
		} catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not open Anteros SQLSession", ex);
		}
	}

	public static SQLSession doGetSession(SQLSessionFactory sessionFactory, boolean allowCreate)
			throws Exception, IllegalStateException {

		return doGetSession(sessionFactory, null, allowCreate);
	}


	private static SQLSession doGetSession(
			SQLSessionFactory sessionFactory, 
			SQLExceptionTranslator jdbcExceptionTranslator, boolean allowCreate)
			throws Exception, IllegalStateException {

		Assert.notNull(sessionFactory, "No SQLSessionFactory specified");

		Object resource = TransactionSynchronizationManager.getResource(sessionFactory);
		if (resource instanceof SQLSession) {
			return (SQLSession) resource;
		}
		SQLSessionHolder sessionHolder = (SQLSessionHolder) resource;
		if (sessionHolder != null && !sessionHolder.isEmpty()) {
			SQLSession session = null;
			if (TransactionSynchronizationManager.isSynchronizationActive() &&
					sessionHolder.doesNotHoldNonDefaultSession()) {
				session = sessionHolder.getValidatedSession();
				if (session != null && !sessionHolder.isSynchronizedWithTransaction()) {
					logger.debug("Registering Spring transaction synchronization for existing Anteros SQLSession");
					TransactionSynchronizationManager.registerSynchronization(
							new SpringSQLSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, false));
					sessionHolder.setSynchronizedWithTransaction(true);
				}
			}
			else {
				session = getJtaSynchronizedSession(sessionHolder, sessionFactory, jdbcExceptionTranslator);
			}
			if (session != null) {
				return session;
			}
		}

		logger.debug("Opening Anteros Session");
		SQLSession session = sessionFactory.openSession();
		logger.debug("Opened session");

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering Spring transaction synchronization for new Anteros SQLSession");
			SQLSessionHolder holderToUse = sessionHolder;
			if (holderToUse == null) {
				holderToUse = new SQLSessionHolder(session);
			}
			else {
				holderToUse.addSession(session);
			}
			if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
				
			}
			TransactionSynchronizationManager.registerSynchronization(
					new SpringSQLSessionSynchronization(holderToUse, sessionFactory, jdbcExceptionTranslator, true));
			holderToUse.setSynchronizedWithTransaction(true);
			if (holderToUse != sessionHolder) {
				TransactionSynchronizationManager.bindResource(sessionFactory, holderToUse);
			}
		}
		else {
			registerJtaSynchronization(session, sessionFactory, jdbcExceptionTranslator, sessionHolder);
		}

		if (!allowCreate && !isSessionTransactional(session, sessionFactory)) {
			closeSession(session);
            throw new IllegalStateException(AnterosSpringTranslate.getMessage(SQLSessionFactoryUtils.class, "NoSessionBoundToThread"));
		}

		logger.debug("Return session opened");
		return session;
	}

	private static SQLSession getJtaSynchronizedSession(
			SQLSessionHolder sessionHolder, SQLSessionFactory sessionFactory,
			SQLExceptionTranslator jdbcExceptionTranslator) throws Exception {

		TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, sessionHolder.getAnySession());
		if (jtaTm != null) {
			try {
				Transaction jtaTx = jtaTm.getTransaction();
				if (jtaTx != null) {
					int jtaStatus = jtaTx.getStatus();
					if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
						SQLSession session = sessionHolder.getValidatedSession(jtaTx);
						if (session == null && !sessionHolder.isSynchronizedWithTransaction()) {
							session = sessionHolder.getValidatedSession();
							if (session != null) {
								logger.debug("Registering JTA transaction synchronization for existing Anteros SQLSession");
								sessionHolder.addSession(jtaTx, session);
								jtaTx.registerSynchronization(
										new SpringJtaSynchronizationAdapter(
												new SpringSQLSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, false),
												jtaTm));
								sessionHolder.setSynchronizedWithTransaction(true);
							}
						}
						return session;
					}
				}
				return sessionHolder.getValidatedSession();
			}
			catch (Throwable ex) {
				throw new DataAccessResourceFailureException("Could not check JTA transaction", ex);
			}
		}
		else {
			return sessionHolder.getValidatedSession();
		}
	}

	private static void registerJtaSynchronization(SQLSession session, SQLSessionFactory sessionFactory,
			SQLExceptionTranslator jdbcExceptionTranslator, SQLSessionHolder sessionHolder) throws Exception {

		TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, session);
		if (jtaTm != null) {
			try {
				Transaction jtaTx = jtaTm.getTransaction();
				if (jtaTx != null) {
					int jtaStatus = jtaTx.getStatus();
					if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
						logger.debug("Registering JTA transaction synchronization for new Anteros SQLSession");
						SQLSessionHolder holderToUse = sessionHolder;
						if (holderToUse == null) {
							holderToUse = new SQLSessionHolder(jtaTx, session);
						}
						else {
							holderToUse.addSession(jtaTx, session);
						}
						jtaTx.registerSynchronization(
								new SpringJtaSynchronizationAdapter(
										new SpringSQLSessionSynchronization(holderToUse, sessionFactory, jdbcExceptionTranslator, true),
										jtaTm));
						holderToUse.setSynchronizedWithTransaction(true);
						if (holderToUse != sessionHolder) {
							TransactionSynchronizationManager.bindResource(sessionFactory, holderToUse);
						}
					}
				}
			}
			catch (Throwable ex) {
				throw new DataAccessResourceFailureException(
						"Could not register synchronization with JTA TransactionManager", ex);
			}
		}
	}


	@SuppressWarnings("deprecation")
	public static SQLSession getNewSession(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SQLSessionFactory specified");

		try {
			SQLSessionHolder sessionHolder = (SQLSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
			if (sessionHolder != null && !sessionHolder.isEmpty()) {
					return sessionFactory.openSession(sessionHolder.getAnySession().getConnection());
			}
			else {
					return sessionFactory.openSession();
			}
		}
		catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not open Anteros SQLSession", ex);
		}
	}


	public static String toString(SQLSession session) {
		return session.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(session));
	}

	public static boolean hasTransactionalSession(SQLSessionFactory sessionFactory) {
		if (sessionFactory == null) {
			return false;
		}
		SQLSessionHolder sessionHolder =
				(SQLSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		return (sessionHolder != null && !sessionHolder.isEmpty());
	}

	public static boolean isSessionTransactional(SQLSession session, SQLSessionFactory sessionFactory) {
		if (sessionFactory == null) {
			return false;
		}
		SQLSessionHolder sessionHolder =
				(SQLSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		return (sessionHolder != null && sessionHolder.containsSession(session));
	}

	public static void applyTransactionTimeout(SQLQuery query, SQLSessionFactory sessionFactory) {
		Assert.notNull(query, "No SQLQuery object specified");
		if (sessionFactory != null) {
			SQLSessionHolder sessionHolder =
					(SQLSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
			if (sessionHolder != null && sessionHolder.hasTimeout()) {
				//query.setTimeout(sessionHolder.getTimeToLiveInSeconds());
			}
		}
	}

	
	public static boolean isDeferredCloseActive(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SQLSessionFactory specified");
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		return (holderMap != null && holderMap.containsKey(sessionFactory));
	}

	public static void initDeferredClose(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SQLSessionFactory specified");
		logger.debug("Initializing deferred close of Anteros Sessions");
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		if (holderMap == null) {
			holderMap = new HashMap<SQLSessionFactory, Set<SQLSession>>();
			deferredCloseHolder.set(holderMap);
		}
		holderMap.put(sessionFactory, new LinkedHashSet<SQLSession>(4));
	}

	public static void processDeferredClose(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SQLSessionFactory specified");
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		if (holderMap == null || !holderMap.containsKey(sessionFactory)) {
			throw new IllegalStateException("Deferred close not active for SQLSessionFactory [" + sessionFactory + "]");
		}
		logger.debug("Processing deferred close of Anteros Sessions");
		Set<SQLSession> sessions = holderMap.remove(sessionFactory);
		for (SQLSession session : sessions) {
			closeSession(session);
		}
		if (holderMap.isEmpty()) {
			deferredCloseHolder.remove();
		}
	}

	public static void releaseSession(SQLSession session, SQLSessionFactory sessionFactory) {
		if (session == null) {
			return;
		}
		if (!isSessionTransactional(session, sessionFactory)) {
			closeSessionOrRegisterDeferredClose(session, sessionFactory);
		}
	}

	static void closeSessionOrRegisterDeferredClose(SQLSession session, SQLSessionFactory sessionFactory) {
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		if (holderMap != null && sessionFactory != null && holderMap.containsKey(sessionFactory)) {
			logger.debug("Registering Anteros SQLSession for deferred close");
			Set<SQLSession> sessions = holderMap.get(sessionFactory);
			sessions.add(session);
		}
		else {
			closeSession(session);
		}
	}

	public static void closeSession(SQLSession session) {
		if (session != null) {
			logger.debug("Closing Anteros SQLSession");
			try {
				session.close();
			}
			catch (Throwable ex) {
				logger.debug("Unexpected exception on closing Anteros SQLSession", ex);
			}
		}
	}

}
