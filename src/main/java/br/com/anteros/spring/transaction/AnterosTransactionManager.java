package br.com.anteros.spring.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.transaction.Transaction;

@SuppressWarnings("serial")
public class AnterosTransactionManager extends AbstractPlatformTransactionManager implements
		ResourceTransactionManager, BeanFactoryAware, InitializingBean {

	private SQLSessionFactory sessionFactory;

	private DataSource dataSource;

	private boolean autodetectDataSource = true;

	private boolean prepareConnection = true;

	private boolean managedSession = false;

	private boolean earlyFlushBeforeCommit = false;

	private SQLExceptionTranslator jdbcExceptionTranslator;

	private SQLExceptionTranslator defaultJdbcExceptionTranslator;

	private BeanFactory beanFactory;

	public AnterosTransactionManager() {
	}

	public AnterosTransactionManager(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		afterPropertiesSet();
	}

	public void setSessionFactory(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SQLSessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	public void setDataSource(DataSource dataSource) {
		if (dataSource instanceof TransactionAwareDataSourceProxy) {
			this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
		} else {
			this.dataSource = dataSource;
		}
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public void setAutodetectDataSource(boolean autodetectDataSource) {
		this.autodetectDataSource = autodetectDataSource;
	}

	public void setPrepareConnection(boolean prepareConnection) {
		this.prepareConnection = prepareConnection;
	}

	public void setManagedSession(boolean managedSession) {
		this.managedSession = managedSession;
	}

	public void setEarlyFlushBeforeCommit(boolean earlyFlushBeforeCommit) {
		this.earlyFlushBeforeCommit = earlyFlushBeforeCommit;
	}

	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		return this.jdbcExceptionTranslator;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() {
		if (getSessionFactory() == null) {
			throw new IllegalArgumentException("Property 'sessionFactory' is required");
		}

		if (this.autodetectDataSource && getDataSource() == null) {
			DataSource sfds = SQLSessionFactoryUtils.getDataSource(getSessionFactory());
			if (sfds != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Using DataSource [" + sfds
							+ "] of Anteros SQLSessionFactory for AnterosTransactionManager");
				}
				setDataSource(sfds);
			}
		}
	}

	@Override
	public Object getResourceFactory() {
		return getSessionFactory();
	}

	@Override
	protected Object doGetTransaction() {
		AnterosTransactionObject txObject = new AnterosTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());

		SQLSessionHolder sessionHolder = (SQLSessionHolder) TransactionSynchronizationManager
				.getResource(getSessionFactory());
		if (sessionHolder != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found thread-bound SQLSession [" + SQLSessionFactoryUtils.toString(sessionHolder.getSession())
						+ "] for Anteros transaction");
			}
			txObject.setSessionHolder(sessionHolder);
		} else if (this.managedSession) {
			try {
				SQLSession session = getSessionFactory().getCurrentSession();
				if (logger.isDebugEnabled()) {
					logger.debug("Found Anteros-managed SQLSession [" + SQLSessionFactoryUtils.toString(session)
							+ "] for Spring-managed transaction");
				}
				txObject.setExistingSession(session);
			} catch (Exception ex) {
				throw new DataAccessResourceFailureException(
						"Could not obtain Anteros-managed SQLSession for Spring-managed transaction", ex);
			}
		}

		if (getDataSource() != null) {
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager
					.getResource(getDataSource());
			txObject.setConnectionHolder(conHolder);
		}

		return txObject;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;
		try {
			return (txObject.hasSpringManagedTransaction() || (this.managedSession && txObject
					.hasManagedTransaction()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;

		if (txObject.hasConnectionHolder() && !txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
			throw new IllegalTransactionStateException(
					"Pre-bound JDBC Connection found! AnterosTransactionManager does not support "
							+ "running within DataSourceTransactionManager if told to manage the DataSource itself. "
							+ "It is recommended to use a single AnterosTransactionManager for all transactions "
							+ "on a single DataSource, no matter whether Anteros or JDBC access.");
		}

		SQLSession session = null;

		try {
			if (txObject.getSessionHolder() == null || txObject.getSessionHolder().isSynchronizedWithTransaction()) {
				SQLSession newSession = getSessionFactory().openSession();
				if (logger.isDebugEnabled()) {
					logger.debug("Opened new SQLSession [" + SQLSessionFactoryUtils.toString(newSession)
							+ "] for Anteros transaction");
				}
				txObject.setSession(newSession);
			}

			session = txObject.getSessionHolder().getSession();

			if (this.prepareConnection) {
				if (logger.isDebugEnabled()) {
					logger.debug("Preparing JDBC Connection of Anteros SQLSession ["
							+ SQLSessionFactoryUtils.toString(session) + "]");
				}
				Connection con = session.getConnection();
				Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
				txObject.setPreviousIsolationLevel(previousIsolationLevel);
			} else {
				if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
					throw new InvalidIsolationLevelException(
							"AnterosTransactionManager is not allowed to support custom isolation levels: "
									+ "make sure that its 'prepareConnection' flag is on (the default) and that the "
									+ "Anteros connection release mode is set to 'on_close' (SpringTransactionFactory's default). "
									+ "Make sure that your LocalSessionFactoryBean actually uses SpringTransactionFactory: Your "
									+ "Anteros properties should *not* include a 'anteros.transaction.factory_class' property!");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Not preparing JDBC Connection of Anteros Session ["
							+ SQLSessionFactoryUtils.toString(session) + "]");
				}
			}

			Transaction hibTx = session.getTransaction();
			hibTx.begin();
			
			int timeout = determineTimeout(definition);

			txObject.getSessionHolder().setTransaction(hibTx);

			if (getDataSource() != null) {
				Connection con = session.getConnection();
				ConnectionHolder conHolder = new ConnectionHolder(con);
				if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(timeout);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Exposing Anteros transaction as JDBC transaction [" + con + "]");
				}
				TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
				txObject.setConnectionHolder(conHolder);
			}

			if (txObject.isNewSessionHolder()) {
				TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject.getSessionHolder());
			}
			txObject.getSessionHolder().setSynchronizedWithTransaction(true);
		}

		catch (Throwable ex) {
			if (txObject.isNewSession()) {
				try {
					if (session.getTransaction().isActive()) {
						session.getTransaction().rollback();
					}
				} catch (Throwable ex2) {
					logger.debug("Could not rollback Session after failed transaction begin", ex);
				} finally {
					SQLSessionFactoryUtils.closeSession(session);
				}
			}
			throw new CannotCreateTransactionException("Could not open Anteros SQLSession for transaction", ex);
		}
	}

	@Override
	protected Object doSuspend(Object transaction) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;
		txObject.setSessionHolder(null);
		SQLSessionHolder sessionHolder = (SQLSessionHolder) TransactionSynchronizationManager
				.unbindResource(getSessionFactory());
		txObject.setConnectionHolder(null);
		ConnectionHolder connectionHolder = null;
		if (getDataSource() != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
		}
		return new SuspendedResourcesHolder(sessionHolder, connectionHolder);
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
		}
		TransactionSynchronizationManager.bindResource(getSessionFactory(), resourcesHolder.getSessionHolder());
		if (getDataSource() != null) {
			TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
		}
	}

	@Override
	protected void prepareForCommit(DefaultTransactionStatus status) {
		if (this.earlyFlushBeforeCommit && status.isNewTransaction()) {
		}
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing Anteros transaction on SQLSession ["
					+ SQLSessionFactoryUtils.toString(txObject.getSessionHolder().getSession()) + "]");
		}
		try {
			txObject.getSessionHolder().getTransaction().commit();
		} catch (Exception ex) {
			throw new TransactionSystemException("Could not commit Anteros transaction", ex);
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back Anteros transaction on SQLSession ["
					+ SQLSessionFactoryUtils.toString(txObject.getSessionHolder().getSession()) + "]");
		}
		try {
			txObject.getSessionHolder().getTransaction().rollback();
		} catch (Exception ex) {
			throw new TransactionSystemException("Could not roll back Anteros transaction", ex);
		} finally {
			if (!txObject.isNewSession() && !this.managedSession) {
				try {
					txObject.getSessionHolder().getSession().clear();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting Anteros transaction on SQLSession ["
					+ SQLSessionFactoryUtils.toString(txObject.getSessionHolder().getSession()) + "] rollback-only");
		}
		txObject.setRollbackOnly();
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void doCleanupAfterCompletion(Object transaction) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;

		if (txObject.isNewSessionHolder()) {
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
		}

		if (getDataSource() != null) {
			TransactionSynchronizationManager.unbindResource(getDataSource());
		}

		SQLSession session = txObject.getSessionHolder().getSession();
		boolean closed;
		try {
			closed = session.isClosed();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		if (this.prepareConnection && !closed) {
			try {
				Connection con = session.getConnection();
				DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
			} catch (Exception ex) {
				logger.debug("Could not access JDBC Connection of Anteros SQLSession", ex);
			}
		}

		if (txObject.isNewSession()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing Anteros SQLSession [" + SQLSessionFactoryUtils.toString(session)
						+ "] after transaction");
			}
			SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, getSessionFactory());
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not closing pre-bound Anteros SQLSession [" + SQLSessionFactoryUtils.toString(session)
						+ "] after transaction");
			}
			if (!this.managedSession) {
				/*
				 * Quando implementar método para desconectar session chamar aqui.
				 */
			}
		}
		txObject.getSessionHolder().clear();
	}

	protected synchronized SQLExceptionTranslator getDefaultJdbcExceptionTranslator() {
		if (this.defaultJdbcExceptionTranslator == null) {
			if (getDataSource() != null) {
				this.defaultJdbcExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(getDataSource());
			} else {
				this.defaultJdbcExceptionTranslator = SQLSessionFactoryUtils
						.newJdbcExceptionTranslator(getSessionFactory());
			}
		}
		return this.defaultJdbcExceptionTranslator;
	}

	private class AnterosTransactionObject extends JdbcTransactionObjectSupport {

		private SQLSessionHolder sessionHolder;

		private boolean newSessionHolder;

		private boolean newSession;

		public void setSession(SQLSession session) {
			this.sessionHolder = new SQLSessionHolder(session);
			this.newSessionHolder = true;
			this.newSession = true;
		}

		public void setExistingSession(SQLSession session) {
			this.sessionHolder = new SQLSessionHolder(session);
			this.newSessionHolder = true;
			this.newSession = false;
		}

		public void setSessionHolder(SQLSessionHolder sessionHolder) {
			this.sessionHolder = sessionHolder;
			this.newSessionHolder = false;
			this.newSession = false;
		}

		public SQLSessionHolder getSessionHolder() {
			return this.sessionHolder;
		}

		public boolean isNewSessionHolder() {
			return this.newSessionHolder;
		}

		public boolean isNewSession() {
			return this.newSession;
		}


		public boolean hasSpringManagedTransaction() {
				return (this.sessionHolder != null && this.sessionHolder.getTransaction() != null);
		}

		public boolean hasManagedTransaction() throws Exception {
			return (this.sessionHolder != null && this.sessionHolder.getSession().getTransaction().isActive());
		}

		public void setRollbackOnly() {
			this.sessionHolder.setRollbackOnly();
			if (hasConnectionHolder()) {
				getConnectionHolder().setRollbackOnly();
			}
		}

		@Override
		public boolean isRollbackOnly() {
			return this.sessionHolder.isRollbackOnly()
					|| (hasConnectionHolder() && getConnectionHolder().isRollbackOnly());
		}

		@Override
		public void flush() {
			try {
				this.sessionHolder.getSession().flush();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	
	private static class SuspendedResourcesHolder {

		private final SQLSessionHolder sessionHolder;

		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(SQLSessionHolder sessionHolder, ConnectionHolder conHolder) {
			this.sessionHolder = sessionHolder;
			this.connectionHolder = conHolder;
		}

		private SQLSessionHolder getSessionHolder() {
			return this.sessionHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return this.connectionHolder;
		}
	}

}
