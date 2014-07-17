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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

public class SQLConnectionHolder extends ResourceHolderSupport {

	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";

	private ConnectionHandle connectionHandle;

	private Connection currentConnection;

	private boolean transactionActive = false;

	private Boolean savepointsSupported;

	private int savepointCounter = 0;


	public SQLConnectionHolder(ConnectionHandle connectionHandle) {
		Assert.notNull(connectionHandle, "ConnectionHandle must not be null");
		this.connectionHandle = connectionHandle;
	}

	public SQLConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}

	public SQLConnectionHolder(Connection connection, boolean transactionActive) {
		this(connection);
		this.transactionActive = transactionActive;
	}


	public ConnectionHandle getConnectionHandle() {
		return this.connectionHandle;
	}

	protected boolean hasConnection() {
		return (this.connectionHandle != null);
	}

	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	protected void setConnection(Connection connection) {
		if (this.currentConnection != null) {
			this.connectionHandle.releaseConnection(this.currentConnection);
			this.currentConnection = null;
		}
		if (connection != null) {
			this.connectionHandle = new SimpleConnectionHandle(connection);
		}
		else {
			this.connectionHandle = null;
		}
	}

	public Connection getConnection() {
		Assert.notNull(this.connectionHandle, "Active Connection is required");
		if (this.currentConnection == null) {
			this.currentConnection = this.connectionHandle.getConnection();
		}
		return this.currentConnection;
	}

	public boolean supportsSavepoints() throws SQLException {
		if (this.savepointsSupported == null) {
			this.savepointsSupported = new Boolean(getConnection().getMetaData().supportsSavepoints());
		}
		return this.savepointsSupported.booleanValue();
	}

	public Savepoint createSavepoint() throws SQLException {
		this.savepointCounter++;
		return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}
	
	@Override
	public void released() {
		super.released();
		if (!isOpen() && this.currentConnection != null) {
			this.connectionHandle.releaseConnection(this.currentConnection);
			this.currentConnection = null;
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
		this.savepointsSupported = null;
		this.savepointCounter = 0;
	}

}
