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

import javax.sql.DataSource;

import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.configuration.SessionFactoryConfiguration;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.session.impl.SQLSessionImpl;
import br.com.anteros.persistence.util.ConnectionUtils;

public class SpringSQLSessionFactoryImpl extends AbstractSQLSessionFactory {

	public SpringSQLSessionFactoryImpl(EntityCacheManager entityCacheManager, DataSource dataSource,
			SessionFactoryConfiguration configuration)
			throws Exception {
		super(entityCacheManager, dataSource, configuration);
	}

	@Override
	public SQLSession getSession() throws Exception {
		SQLSession session = existingSession(this);
		if (session == null) {
			// Ler properties da connection
			session = new SQLSessionImpl(this, this.getDatasource().getConnection(), this.getEntityCacheManager(),
					new SQLQueryRunner(), this.getDialect(), this.isShowSql(), this.isFormatSql(),
					this.getQueryTimeout());
			doBind(session, this);
		}
		return session;
	}

	@Override
	public void beforeGenerateDDL() throws Exception {

	}

	@Override
	public void afterGenerateDDL() throws Exception {

	}

	public Connection validateConnection(Connection conn) throws SQLException {
		if (conn != null) {
			// primeiro tenta usar o método isValid, porém nem todos os JDBC
			// implementam este método
			try {
				if (!conn.isValid(0)) {
					conn = null;
				}
			} catch (AbstractMethodError ex) {
				// se der alguma exceção no isValid usa o isClosed
				if (conn.isClosed()) {
					conn = null;
				}
			}
		}
		if (conn == null) {
			conn = this.getDatasource().getConnection();
		}
		return conn;
	}

}
