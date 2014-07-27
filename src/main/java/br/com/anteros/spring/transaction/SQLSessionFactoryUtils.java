package br.com.anteros.spring.transaction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.aopalliance.intercept.Interceptor;
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

public abstract class SQLSessionFactoryUtils {

	public static final int SESSION_SYNCHRONIZATION_ORDER = DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100;

	static final Log logger = LogFactory.getLog(SQLSessionFactoryUtils.class);

	private static final ThreadLocal<Map<SQLSessionFactory, Set<SQLSession>>> deferredCloseHolder = new NamedThreadLocal<Map<SQLSessionFactory, Set<SQLSession>>>(
			"Anteros sessions registered for deferred close");

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
			throw new DataAccessResourceFailureException("Could not open Anteros Session", ex);
		}
	}

	public static SQLSession getSession(SQLSessionFactory sessionFactory)
			throws DataAccessResourceFailureException {

		try {
			return doGetSession(sessionFactory, true);
		} catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not open Anteros Session", ex);
		}
	}


	public static SQLSession doGetSession(SQLSessionFactory sessionFactory,
			boolean allowCreate) throws Exception,
			IllegalStateException {

		Assert.notNull(sessionFactory, "No SessionFactory specified");

		Object resource = TransactionSynchronizationManager.getResource(sessionFactory);
		if (resource instanceof SQLSession) {
			return (SQLSession) resource;
		}
		SQLSessionHolder sessionHolder = (SQLSessionHolder) resource;
		if (sessionHolder != null && !sessionHolder.isEmpty()) {
			// pre-bound Anteros SQLSession
			SQLSession session = null;
			if (TransactionSynchronizationManager.isSynchronizationActive()
					&& sessionHolder.doesNotHoldNonDefaultSession()) {
				// Spring transaction management is active ->
				// register pre-bound Session with it for transactional
				// flushing.
				session = sessionHolder.getValidatedSession();
				if (session != null && !sessionHolder.isSynchronizedWithTransaction()) {
					logger.debug("Registering Spring transaction synchronization for existing Anteros SQLSession");
					TransactionSynchronizationManager.registerSynchronization(new SpringSQLSessionSynchronization(
							sessionHolder, sessionFactory, false));
					sessionHolder.setSynchronizedWithTransaction(true);
				}
			} else {
				// No Spring transaction management active -> try JTA
				// transaction synchronization.
				session = getJtaSynchronizedSession(sessionHolder, sessionFactory);
			}
			if (session != null) {
				return session;
			}
		}

		logger.debug("Opening Anteros SQLSession");
		SQLSession session = sessionFactory.openSession();

		// Use same Session for further Anteros actions within the
		// transaction.
		// Thread object will get removed by synchronization at transaction
		// completion.
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			// We're within a Spring-managed transaction, possibly from
			// JtaTransactionManager.
			logger.debug("Registering Spring transaction synchronization for new Anteros Session");
			SQLSessionHolder holderToUse = sessionHolder;
			if (holderToUse == null) {
				holderToUse = new SQLSessionHolder(session);
			} else {
				holderToUse.addSession(session);
			}
			TransactionSynchronizationManager.registerSynchronization(new SpringSQLSessionSynchronization(holderToUse,
					sessionFactory, true));
			holderToUse.setSynchronizedWithTransaction(true);
			if (holderToUse != sessionHolder) {
				TransactionSynchronizationManager.bindResource(sessionFactory, holderToUse);
			}
		} else {
			// No Spring transaction management active -> try JTA transaction
			// synchronization.
			registerJtaSynchronization(session, sessionFactory, sessionHolder);
		}

		// Check whether we are allowed to return the Session.
		if (!allowCreate && !isSessionTransactional(session, sessionFactory)) {
			closeSession(session);
			throw new IllegalStateException("No Anteros SQLSession bound to thread, "
					+ "and configuration does not allow creation of non-transactional one here");
		}

		return session;
	}

	private static SQLSession getJtaSynchronizedSession(SQLSessionHolder sessionHolder,
			SQLSessionFactory sessionFactory) throws Exception {

		// JTA synchronization is only possible with a
		// javax.transaction.TransactionManager.
		// We'll check the Anteros SQLSessionFactory: If a
		// TransactionManagerLookup is specified
		// in Anteros configuration, it will contain a TransactionManager
		// reference.
		TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, sessionHolder.getAnySession());
		if (jtaTm != null) {
			// Check whether JTA transaction management is active ->
			// fetch pre-bound Session for the current JTA transaction, if any.
			// (just necessary for JTA transaction suspension, with an
			// individual
			// Anteros SQLSession per currently active/suspended transaction)
			try {
				// Look for transaction-specific Session.
				Transaction jtaTx = jtaTm.getTransaction();
				if (jtaTx != null) {
					int jtaStatus = jtaTx.getStatus();
					if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
						SQLSession session = sessionHolder.getValidatedSession(jtaTx);
						if (session == null && !sessionHolder.isSynchronizedWithTransaction()) {
							// No transaction-specific Session found: If not
							// already marked as
							// synchronized with transaction, register the
							// default thread-bound
							// Session as JTA-transactional. If there is no
							// default Session,
							// we're a new inner JTA transaction with an outer
							// one being suspended:
							// In that case, we'll return null to trigger
							// opening of a new Session.
							session = sessionHolder.getValidatedSession();
							if (session != null) {
								logger.debug("Registering JTA transaction synchronization for existing Anteros SQLSession");
								sessionHolder.addSession(jtaTx, session);
								jtaTx.registerSynchronization(new SpringJtaSynchronizationAdapter(
										new SpringSQLSessionSynchronization(sessionHolder, sessionFactory,
												false), jtaTm));
								sessionHolder.setSynchronizedWithTransaction(true);
							}
						}
						return session;
					}
				}
				// No transaction active -> simply return default thread-bound
				// Session, if any
				// (possibly from OpenSessionInViewFilter/Interceptor).
				return sessionHolder.getValidatedSession();
			} catch (Throwable ex) {
				throw new DataAccessResourceFailureException("Could not check JTA transaction", ex);
			}
		} else {
			// No JTA TransactionManager -> simply return default thread-bound
			// Session, if any
			// (possibly from OpenSessionInViewFilter/Interceptor).
			return sessionHolder.getValidatedSession();
		}
	}

	private static void registerJtaSynchronization(SQLSession session, SQLSessionFactory sessionFactory,
			SQLSessionHolder sessionHolder) throws Exception {

		// JTA synchronization is only possible with a
		// javax.transaction.TransactionManager.
		// We'll check the Anteros SQLSessionFactory: If a
		// TransactionManagerLookup is specified
		// in Anteros configuration, it will contain a TransactionManager
		// reference.
		TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, session);
		if (jtaTm != null) {
			try {
				Transaction jtaTx = jtaTm.getTransaction();
				if (jtaTx != null) {
					int jtaStatus = jtaTx.getStatus();
					if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
						logger.debug("Registering JTA transaction synchronization for new Anteros SQLSession");
						SQLSessionHolder holderToUse = sessionHolder;
						// Register JTA Transaction with existing SessionHolder.
						// Create a new SessionHolder if none existed before.
						if (holderToUse == null) {
							holderToUse = new SQLSessionHolder(jtaTx, session);
						} else {
							holderToUse.addSession(jtaTx, session);
						}
						jtaTx.registerSynchronization(new SpringJtaSynchronizationAdapter(
								new SpringSQLSessionSynchronization(holderToUse, sessionFactory, 
										true), jtaTm));
						holderToUse.setSynchronizedWithTransaction(true);
						if (holderToUse != sessionHolder) {
							TransactionSynchronizationManager.bindResource(sessionFactory, holderToUse);
						}
					}
				}
			} catch (Throwable ex) {
				throw new DataAccessResourceFailureException(
						"Could not register synchronization with JTA TransactionManager", ex);
			}
		}
	}

	public static SQLSession getNewSession(SQLSessionFactory sessionFactory) {
		return getNewSession(sessionFactory, null);
	}

	@SuppressWarnings("deprecation")
	public static SQLSession getNewSession(SQLSessionFactory sessionFactory, Interceptor entityInterceptor) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");

		try {
			SQLSessionHolder sessionHolder = (SQLSessionHolder) TransactionSynchronizationManager
					.getResource(sessionFactory);
			if (sessionHolder != null && !sessionHolder.isEmpty()) {
				return sessionFactory.openSession(sessionHolder.getAnySession().getConnection());
			} else {

				return sessionFactory.openSession();
			}
		} catch (Exception ex) {
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
		SQLSessionHolder sessionHolder = (SQLSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		return (sessionHolder != null && !sessionHolder.isEmpty());
	}

	public static boolean isSessionTransactional(SQLSession session, SQLSessionFactory sessionFactory) {
		if (sessionFactory == null) {
			return false;
		}
		SQLSessionHolder sessionHolder = (SQLSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		return (sessionHolder != null && sessionHolder.containsSession(session));
	}

	public static boolean isDeferredCloseActive(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		return (holderMap != null && holderMap.containsKey(sessionFactory));
	}

	public static void initDeferredClose(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");
		logger.debug("Initializing deferred close of Anteros SQLSessions");
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		if (holderMap == null) {
			holderMap = new HashMap<SQLSessionFactory, Set<SQLSession>>();
			deferredCloseHolder.set(holderMap);
		}
		holderMap.put(sessionFactory, new LinkedHashSet<SQLSession>(4));
	}

	public static void processDeferredClose(SQLSessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");
		Map<SQLSessionFactory, Set<SQLSession>> holderMap = deferredCloseHolder.get();
		if (holderMap == null || !holderMap.containsKey(sessionFactory)) {
			throw new IllegalStateException("Deferred close not active for SessionFactory [" + sessionFactory + "]");
		}
		logger.debug("Processing deferred close of Anteros SQLSessions");
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
		// Only close non-transactional Sessions.
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
		} else {
			closeSession(session);
		}
	}

	public static void closeSession(SQLSession session) {
		if (session != null) {
			logger.debug("Closing Anteros SQLSession");
			try {
				session.close();
			} catch (Exception ex) {
				logger.debug("Could not close Anteros SQLSession", ex);
			} catch (Throwable ex) {
				logger.debug("Unexpected exception on closing Anteros SQLSession", ex);
			}
		}
	}

}
