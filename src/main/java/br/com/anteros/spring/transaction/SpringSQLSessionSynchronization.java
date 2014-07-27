/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.anteros.spring.transaction;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;


class SpringSQLSessionSynchronization implements TransactionSynchronization, Ordered {

	private final SQLSessionHolder sQLSessionHolder;

	private final SQLSessionFactory sessionFactory;

	private final boolean newSession;

	private boolean transactionCompletion = false;

	private Transaction jtaTransaction;

	private boolean holderActive = true;


	public SpringSQLSessionSynchronization(
			SQLSessionHolder sQLSessionHolder, SQLSessionFactory sessionFactory,
			boolean newSession) {

		this.sQLSessionHolder = sQLSessionHolder;
		this.sessionFactory = sessionFactory;
		this.newSession = newSession;

		// Check whether the SessionFactory has a JTA TransactionManager.
		TransactionManager jtaTm;
		try {
			jtaTm = SQLSessionFactoryUtils.getJtaTransactionManager(sessionFactory, sQLSessionHolder.getAnySession());
		} catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not access JTA transaction", ex);
		}
		
		if (jtaTm != null) {
			this.transactionCompletion = true;
			// Fetch current JTA Transaction object
			// (just necessary for JTA transaction suspension, with an individual
			// Anteros SQLSession per currently active/suspended transaction).
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
			session = this.sQLSessionHolder.getSession(this.jtaTransaction);
		}
		if (session == null) {
			session = this.sQLSessionHolder.getSession();
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
			// Eagerly disconnect the Session here, to make release mode "on_close" work on JBoss.
			try {
				getCurrentSession().close();
			} catch (Exception e) {
				new RuntimeException(e);
			}
		}
	}

	@Override
	public void resume() {
		if (this.holderActive) {
			TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sQLSessionHolder);
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
			// Typically in case of a suspended JTA transaction:
			// Remove the Session for the current JTA transaction, but keep the holder.
			SQLSession session = this.sQLSessionHolder.removeSession(this.jtaTransaction);
			if (session != null) {
				if (this.sQLSessionHolder.isEmpty()) {
					// No Sessions for JTA transactions bound anymore -> could remove it.
					TransactionSynchronizationManager.unbindResourceIfPossible(this.sessionFactory);
					this.holderActive = false;
				}
				// Do not close a pre-bound Session. In that case, we'll find the
				// transaction-specific Session the same as the default Session.
				if (session != this.sQLSessionHolder.getSession()) {
					SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, this.sessionFactory);
				}
				else {
					// Eagerly disconnect the Session here, to make release mode "on_close" work nicely.
					try {
						session.close();
					} catch (Exception e) {
						new RuntimeException(e);
					}
				}
				return;
			}
		}
		// We'll only get here if there was no specific JTA transaction to handle.
		if (this.newSession) {
			// Default behavior: unbind and close the thread-bound Anteros SQLSession.
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
			this.holderActive = false;
			if (this.transactionCompletion) {
				// Close the Anteros SQLSession here in case of a Anteros TransactionManagerLookup:
				// Anteros will automatically defer the actual closing until JTA transaction completion.
				// Else, the Session will be closed in the afterCompletion method, to provide the
				// correct transaction status for releasing the Session's cache locks.
				SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(this.sQLSessionHolder.getSession(), this.sessionFactory);
			}
		}
		else  {
			SQLSession session = this.sQLSessionHolder.getSession();
			if (this.transactionCompletion) {
				// Eagerly disconnect the Session here, to make release mode "on_close" work nicely.
				// We know that this is appropriate if a TransactionManagerLookup has been specified.
				try {
					session.close();
				} catch (Exception e) {
					new RuntimeException(e);
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
				// No Anteros TransactionManagerLookup: apply afterTransactionCompletion callback.
				// Always perform explicit afterTransactionCompletion callback for pre-bound Session,
				// even with Anteros TransactionManagerLookup (which only applies to new Sessions).
				SQLSession session = this.sQLSessionHolder.getSession();
				// Provide correct transaction status for releasing the Session's cache locks,
				// if possible. Else, closing will release all cache locks assuming a rollback.
					// Close the Anteros SQLSession here if necessary
					// (closed in beforeCompletion in case of TransactionManagerLookup).
					if (this.newSession) {
						SQLSessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, this.sessionFactory);
					}
			}
			if (!this.newSession && status != STATUS_COMMITTED) {
				// Clear all pending inserts/updates/deletes in the Session.
				// Necessary for pre-bound Sessions, to avoid inconsistent state.
				try {
					this.sQLSessionHolder.getSession().clear();
				} catch (Exception e) {
					new RuntimeException(e);
				}
			}
		}
		finally {
			if (this.sQLSessionHolder.doesNotHoldNonDefaultSession()) {
				this.sQLSessionHolder.setSynchronizedWithTransaction(false);
			}
		}
	}

}
