/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.com.anteros.spring.transaction;

import java.sql.Savepoint;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.SmartTransactionObject;

import br.com.anteros.persistence.log.Logger;
import br.com.anteros.persistence.log.LoggerProvider;

public abstract class SQLJdbcTransactionObjectSupport implements SavepointManager, SmartTransactionObject {

	private static final Logger log = LoggerProvider.getInstance().getLogger(
			SQLJdbcTransactionObjectSupport.class.getName());

	private ConnectionHolder connectionHolder;

	private Integer previousIsolationLevel;

	private boolean savepointAllowed = false;

	public void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public ConnectionHolder getConnectionHolder() {
		return this.connectionHolder;
	}

	public boolean hasConnectionHolder() {
		return (this.connectionHolder != null);
	}

	public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return this.previousIsolationLevel;
	}

	public void setSavepointAllowed(boolean savepointAllowed) {
		this.savepointAllowed = savepointAllowed;
	}

	public boolean isSavepointAllowed() {
		return this.savepointAllowed;
	}

	public void flush() {
		// no-op
	}

	public Object createSavepoint() throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			if (!conHolder.supportsSavepoints()) {
				throw new NestedTransactionNotSupportedException(
						"Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
			}
		} catch (Throwable ex) {
			throw new NestedTransactionNotSupportedException(
					"Cannot create a nested transaction because your JDBC driver is not a JDBC 3.0 driver", ex);
		}
		try {
			return conHolder.createSavepoint();
		} catch (Throwable ex) {
			throw new CannotCreateTransactionException("Could not create JDBC savepoint", ex);
		}
	}

	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionHolderForSavepoint().getConnection().rollback((Savepoint) savepoint);
		} catch (Throwable ex) {
			throw new TransactionSystemException("Could not roll back to JDBC savepoint", ex);
		}
	}

	public void releaseSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionHolderForSavepoint().getConnection().releaseSavepoint((Savepoint) savepoint);
		} catch (Throwable ex) {
			log.debug("Could not explicitly release JDBC savepoint", ex);
		}
	}

	protected ConnectionHolder getConnectionHolderForSavepoint() throws TransactionException {
		if (!isSavepointAllowed()) {
			throw new NestedTransactionNotSupportedException(
					"Transaction manager does not allow nested transactions");
		}
		if (!hasConnectionHolder()) {
			throw new TransactionUsageException(
					"Cannot create nested transaction if not exposing a JDBC transaction");
		}
		return getConnectionHolder();
	}

}
