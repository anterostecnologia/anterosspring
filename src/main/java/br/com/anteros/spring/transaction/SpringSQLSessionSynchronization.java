package br.com.anteros.spring.transaction;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;


class SpringSQLSessionSynchronization implements TransactionSynchronization, Ordered {

	private final SQLSessionHolder sessionHolder;

	private final SQLSessionFactory sessionFactory;

	private final boolean newSession;

	private boolean transactionCompletion = false;

	private Transaction jtaTransaction;

	private boolean holderActive = true;


	public SpringSQLSessionSynchronization(
			SQLSessionHolder sessionHolder, SQLSessionFactory sessionFactory,
			SQLExceptionTranslator jdbcExceptionTranslator, boolean newSession) throws Exception {

		this.sessionHolder = sessionHolder;
		this.sessionFactory = sessionFactory;
		this.newSession = newSession;

		TransactionManager jtaTm =
				SQLSessionFactoryUtils.getJtaTransactionManager(sessionFactory, sessionHolder.getAnySession());
		if (jtaTm != null) {
			this.transactionCompletion = true;
			try {
				this.jtaTransaction = jtaTm.getTransaction();
			}
			catch (SystemException ex) {
				throw new DataAccessResourceFailureException("Could not access JTA transaction", ex);
			}
		}
	}

	private SQLSession getCurrentSession() {
		SQLSession session = null;
		if (this.jtaTransaction != null) {
			session = this.sessionHolder.getSession(this.jtaTransaction);
		}
		if (session == null) {
			session = this.sessionHolder.getSession();
		}
		return session;
	}


	@Override
	public int getOrder() {
		return SQLSessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER;
	}


	@Override
	public void suspend() {
		if (this.holderActive) {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
			try {
				getCurrentSession().close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void resume() {
		if (this.holderActive) {
			TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
		}
	}

	@Override
	public void flush() {
		try {
			SQLSessionFactoryUtils.logger.debug("Flushing Anteros SQLSession on explicit request");
			getCurrentSession().flush();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void beforeCommit(boolean readOnly) throws DataAccessException {
		if (!readOnly) {
		}
	}


	@Override
	public void beforeCompletion() {
		if (this.jtaTransaction != null) {
			SQLSession session = this.sessionHolder.removeSession(this.jtaTransaction);
			if (session != null) {
				if (this.sessionHolder.isEmpty()) {
					TransactionSynchronizationManager.unbindResourceIfPossible(this.sessionFactory);
					this.holderActive = false;
				}
				if (session != this.sessionHolder.getSession()) {
					SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, this.sessionFactory);
				}
				else {
					try {
						session.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				return;
			}
		}
		if (this.newSession) {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
			this.holderActive = false;
			if (this.transactionCompletion) {
				SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(this.sessionHolder.getSession(), this.sessionFactory);
			}
		}
		else  {
			SQLSession session = this.sessionHolder.getSession();
			if (this.transactionCompletion) {
				try {
					session.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void afterCommit() {
	}

	@Override
	public void afterCompletion(int status) {
		try {
			if (!this.transactionCompletion || !this.newSession) {
				SQLSession session = this.sessionHolder.getSession();
					if (this.newSession) {
						SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, this.sessionFactory);
					}
			}
			if (!this.newSession && status != STATUS_COMMITTED) {
				try {
					this.sessionHolder.getSession().clear();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		finally {
			if (this.sessionHolder.doesNotHoldNonDefaultSession()) {
				this.sessionHolder.setSynchronizedWithTransaction(false);
			}
		}
	}

}
