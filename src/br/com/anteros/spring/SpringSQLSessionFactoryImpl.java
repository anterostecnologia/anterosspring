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
package br.com.anteros.spring;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.configuration.SessionFactoryConfiguration;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.session.impl.SQLSessionImpl;

public class SpringSQLSessionFactoryImpl extends AbstractSQLSessionFactory {

	public SpringSQLSessionFactoryImpl(EntityCacheManager entityCacheManager, DataSource dataSource, SessionFactoryConfiguration configuration)
			throws Exception {
		super(entityCacheManager, dataSource, configuration);
	}

	@Override
	public SQLSession getSession() throws Exception {
		if (localSession.get() == null) {
			// Ler properties da connection
			localSession.set(new SQLSessionImpl(this, this.getDatasource().getConnection(), this.getEntityCacheManager(), new SQLQueryRunner(), this
					.getDialect(), this.isShowSql(), this.isFormatSql()));
		}
		return localSession.get();
	}

	@Override
	public Connection getCurrentConnection() throws Exception {
		return DataSourceUtils.getConnection(this.getDatasource());
	}

	@Override
	public SQLSession getNewSession() throws Exception {
		return new SQLSessionImpl(this, this.getDatasource().getConnection(), this.getEntityCacheManager(), new SQLQueryRunner(), this.getDialect(),
				this.isShowSql(), this.isFormatSql());
	}

	@Override
	public void beforeGenerateDDL() throws Exception {

	}

	@Override
	public void afterGenerateDDL() throws Exception {

	}

}
