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

import javax.sql.DataSource;

import br.com.anteros.core.configuration.exception.AnterosConfigurationException;
import br.com.anteros.persistence.metadata.configuration.ModelConfiguration;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceConfiguration;
import br.com.anteros.spring.util.AnterosSpringTranslate;

public class SpringSQLConfiguration extends AnterosPersistenceConfiguration {

	public SpringSQLConfiguration(DataSource dataSource) {
		super(dataSource);
	}

	public SpringSQLConfiguration(ModelConfiguration modelConfiguration) {
		super(modelConfiguration);
	}

	public SpringSQLConfiguration(DataSource dataSource, ModelConfiguration modelConfiguration) {
		super(dataSource, modelConfiguration);
	}

	@Override
	public SQLSessionFactory buildSessionFactory() throws Exception {
		buildDataSource();
		if (dataSource == null)
			throw new AnterosConfigurationException(AnterosSpringTranslate.getMessage(this.getClass(), "datasourceNotConfigured"));
		loadEntities();
		SpringSQLSessionFactoryImpl sessionFactory = new SpringSQLSessionFactoryImpl(entityCacheManager, dataSource,
				this.getSessionFactoryConfiguration());
		sessionFactory.generateDDL();
		return sessionFactory;
	}

}
