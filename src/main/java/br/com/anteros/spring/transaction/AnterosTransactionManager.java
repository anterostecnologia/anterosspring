package br.com.anteros.spring.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
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

public class AnterosTransactionManager extends AbstractPlatformTransactionManager implements
		ResourceTransactionManager, BeanFactoryAware, InitializingBean {

	private static final long serialVersionUID = 1L;

	private SQLSessionFactory sessionFactory;

	private DataSource dataSource;

	private boolean autodetectDataSource = true;

	private boolean prepareConnection = true;

	private boolean managedSession = false;

	private boolean earlyFlushBeforeCommit = false;

	private SQLExceptionTranslator jdbcExceptionTranslator;
	
	@SuppressWarnings("unused")
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

	public SQLSessionFactory getSQLSessionFactory() {
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

	public void setHibernateManagedSession(boolean hibernateManagedSession) {
		this.managedSession = hibernateManagedSession;
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
	public void afterPropertiesSet() {
		if (getSQLSessionFactory() == null) {
			throw new IllegalArgumentException("Property 'sessionFactory' is required");
		}

		if (this.autodetectDataSource && getDataSource() == null) {
			DataSource sfds = SQLSessionFactoryUtils.getDataSource(getSQLSessionFactory());
			if (sfds != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Using DataSource [" + sfds
							+ "] of Hibernate SessionFactory for HibernateTransactionManager");
				}
				setDataSource(sfds);
			}
		}
	}

	@Override
	public Object getResourceFactory() {
		return getSQLSessionFactory();
	}

	@Override
	protected Object doGetTransaction() {
		AnterosTransactionObject txObject = new AnterosTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());

		SQLSessionHolder SQLSessionHolder = (SQLSessionHolder) TransactionSynchronizationManager
				.getResource(getSQLSessionFactory());
		if (SQLSessionHolder != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found thread-bound Session ["
						+ SQLSessionFactoryUtils.toString(SQLSessionHolder.getSQLSession())
						+ "] for Hibernate transaction");
			}
			txObject.setSQLSessionHolder(SQLSessionHolder);
		} else if (this.managedSession) {
			try {
				SQLSession session = getSQLSessionFactory().getCurrentSession();
				if (logger.isDebugEnabled()) {
					logger.debug("Found Hibernate-managed Session [" + SQLSessionFactoryUtils.toString(session)
							+ "] for Spring-managed transaction");
				}
				txObject.setExistingSession(session);
			} catch (Exception ex) {
				throw new DataAccessResourceFailureException(
						"Could not obtain Hibernate-managed Session for Spring-managed transaction", ex);
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
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;

		if (txObject.hasConnectionHolder() && !txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
			throw new IllegalTransactionStateException(
					"Pre-bound JDBC Connection found! HibernateTransactionManager does not support "
							+ "running within DataSourceTransactionManager if told to manage the DataSource itself. "
							+ "It is recommended to use a single HibernateTransactionManager for all transactions "
							+ "on a single DataSource, no matter whether Hibernate or JDBC access.");
		}

		SQLSession session = null;

		try {
			if (txObject.getSQLSessionHolder() == null
					|| txObject.getSQLSessionHolder().isSynchronizedWithTransaction()) {
				SQLSession newSession = getSQLSessionFactory().openSession();
				if (logger.isDebugEnabled()) {
					logger.debug("Opened new Session [" + SQLSessionFactoryUtils.toString(newSession)
							+ "] for Hibernate transaction");
				}
				txObject.setSession(newSession);
			}

			session = txObject.getSQLSessionHolder().getSQLSession();

			if (this.prepareConnection) {
				if (logger.isDebugEnabled()) {
					logger.debug("Preparing JDBC Connection of Hibernate Session ["
							+ SQLSessionFactoryUtils.toString(session) + "]");
				}
				Connection con = session.getConnection();
				Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
				txObject.setPreviousIsolationLevel(previousIsolationLevel);
			} else {
				if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
					throw new InvalidIsolationLevelException(
							"HibernateTransactionManager is not allowed to support custom isolation levels: "
									+ "make sure that its 'prepareConnection' flag is on (the default) and that the "
									+ "Hibernate connection release mode is set to 'on_close' (SpringTransactionFactory's default). "
									+ "Make sure that your LocalSessionFactoryBean actually uses SpringTransactionFactory: Your "
									+ "Hibernate properties should *not* include a 'hibernate.transaction.factory_class' property!");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Not preparing JDBC Connection of Hibernate Session ["
							+ SQLSessionFactoryUtils.toString(session) + "]");
				}
			}

			Transaction hibTx = session.getTransaction();
			hibTx.begin();

			txObject.getSQLSessionHolder().setTransaction(hibTx);

			if (getDataSource() != null) {
				Connection con = session.getConnection();
				ConnectionHolder conHolder = new ConnectionHolder(con);
				if (logger.isDebugEnabled()) {
					logger.debug("Exposing Hibernate transaction as JDBC transaction [" + con + "]");
				}
				TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
				txObject.setConnectionHolder(conHolder);
			}

			if (txObject.isNewSQLSessionHolder()) {
				TransactionSynchronizationManager.bindResource(getSQLSessionFactory(), txObject.getSQLSessionHolder());
			}
			txObject.getSQLSessionHolder().setSynchronizedWithTransaction(true);
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
			throw new CannotCreateTransactionException("Could not open Hibernate Session for transaction", ex);
		}
	}

	@Override
	protected Object doSuspend(Object transaction) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;
		txObject.setSQLSessionHolder(null);
		SQLSessionHolder SQLSessionHolder = (SQLSessionHolder) TransactionSynchronizationManager
				.unbindResource(getSQLSessionFactory());
		txObject.setConnectionHolder(null);
		ConnectionHolder connectionHolder = null;
		if (getDataSource() != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
		}
		return new SuspendedResourcesHolder(SQLSessionHolder, connectionHolder);
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(getSQLSessionFactory())) {
			TransactionSynchronizationManager.unbindResource(getSQLSessionFactory());
		}
		TransactionSynchronizationManager.bindResource(getSQLSessionFactory(), resourcesHolder.getSQLSessionHolder());
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
			logger.debug("Committing Hibernate transaction on Session ["
					+ SQLSessionFactoryUtils.toString(txObject.getSQLSessionHolder().getSQLSession()) + "]");
		}
		try {
			txObject.getSQLSessionHolder().getTransaction().commit();
		} catch (Exception ex) {
			throw new TransactionSystemException("Could not commit Hibernate transaction", ex);
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back Hibernate transaction on Session ["
					+ SQLSessionFactoryUtils.toString(txObject.getSQLSessionHolder().getSQLSession()) + "]");
		}
		try {
			txObject.getSQLSessionHolder().getTransaction().rollback();
		} catch (Exception ex) {
			throw new TransactionSystemException("Could not roll back Hibernate transaction", ex);
		} finally {
			if (!txObject.isNewSession() && !this.managedSession) {
				try {
					txObject.getSQLSessionHolder().getSQLSession().clear();
				} catch (Exception e) {
					throw new TransactionSystemException("Could not roll back Hibernate transaction", e);
				}
			}
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting Hibernate transaction on Session ["
					+ SQLSessionFactoryUtils.toString(txObject.getSQLSessionHolder().getSQLSession())
					+ "] rollback-only");
		}
		txObject.setRollbackOnly();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		AnterosTransactionObject txObject = (AnterosTransactionObject) transaction;

		if (txObject.isNewSQLSessionHolder()) {
			TransactionSynchronizationManager.unbindResource(getSQLSessionFactory());
		}

		if (getDataSource() != null) {
			TransactionSynchronizationManager.unbindResource(getDataSource());
		}

		SQLSession session = txObject.getSQLSessionHolder().getSQLSession();
		if (this.prepareConnection) {
			try {
				Connection con = session.getConnection();
				DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
			} catch (Exception ex) {
				logger.debug("Could not access JDBC Connection of Hibernate Session", ex);
			}
		}

		if (txObject.isNewSession()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing Hibernate Session [" + SQLSessionFactoryUtils.toString(session)
						+ "] after transaction");
			}
			SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, getSQLSessionFactory());
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not closing pre-bound Hibernate Session [" + SQLSessionFactoryUtils.toString(session)
						+ "] after transaction");
			}
			if (!this.managedSession) {
				try {
					session.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		txObject.getSQLSessionHolder().clear();
	}

	/**
	 * Hibernate transaction object, representing a SQLSessionHolder. Used as
	 * transaction object by HibernateTransactionManager.
	 */
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

		public void setSQLSessionHolder(SQLSessionHolder sessionHolder) {
			this.sessionHolder = sessionHolder;
			this.newSessionHolder = false;
			this.newSession = false;
		}

		public SQLSessionHolder getSQLSessionHolder() {
			return this.sessionHolder;
		}

		public boolean isNewSQLSessionHolder() {
			return this.newSessionHolder;
		}

		public boolean isNewSession() {
			return this.newSession;
		}

		public boolean hasSpringManagedTransaction() {
			return (this.sessionHolder != null && this.sessionHolder.getTransaction() != null);
		}

		public boolean hasManagedTransaction() throws Exception {
			return (this.sessionHolder != null && this.sessionHolder.getSQLSession().getTransaction().isActive());
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
				this.sessionHolder.getSQLSession().flush();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private static class SuspendedResourcesHolder {

		private final SQLSessionHolder SQLSessionHolder;

		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(SQLSessionHolder SQLSessionHolder, ConnectionHolder conHolder) {
			this.SQLSessionHolder = SQLSessionHolder;
			this.connectionHolder = conHolder;
		}

		private SQLSessionHolder getSQLSessionHolder() {
			return this.SQLSessionHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return this.connectionHolder;
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;		
	}

}
