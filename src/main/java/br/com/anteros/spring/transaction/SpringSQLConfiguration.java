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

import javax.sql.DataSource;

import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.persistence.metadata.configuration.PersistenceModelConfiguration;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceConfiguration;

/**
 * Configuração Anteros usando uma fábrica criada para uso com sistema de transações do Spring.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class SpringSQLConfiguration extends AnterosPersistenceConfiguration {

	public SpringSQLConfiguration(DataSource dataSource, CloudFileManager externalFileManager, boolean enableImageCompression) {
		super(dataSource, externalFileManager,enableImageCompression);
	}

	public SpringSQLConfiguration(PersistenceModelConfiguration modelConfiguration, CloudFileManager externalFileManager, boolean enableImageCompression) {
		super(modelConfiguration, externalFileManager,enableImageCompression);
	}

	public SpringSQLConfiguration(DataSource dataSource, PersistenceModelConfiguration modelConfiguration, CloudFileManager externalFileManager, boolean enableImageCompression) {
		super(dataSource, modelConfiguration, externalFileManager,enableImageCompression);
	}

	@Override
	public SQLSessionFactory buildSessionFactory() throws Exception {
		prepareClassesToLoad();
		buildDataSource();		
		SpringSQLSessionFactoryImpl sessionFactory = new SpringSQLSessionFactoryImpl(entityCacheManager, dataSource,
				this.getSessionFactoryConfiguration(), this.externalFileManager, this.enableImageCompression);
		loadEntities(sessionFactory.getDialect());		
		sessionFactory.generateDDL();
		return sessionFactory;
	}

}
