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

import java.util.Arrays;
import java.util.List;

public class SpringSQLSessionFactoryBean extends AbstractSQLSessionFactoryBean {

	@Override
	protected void buildSessionFactory() throws Exception {
		SpringSQLConfiguration configuration = new SpringSQLConfiguration(this.getDataSource());
		List<Class> result = Arrays.asList(getAnnotatedClasses());
		for (Class<?> sourceClass : result) {
			configuration.addAnnotatedClass(sourceClass);
		}
		configuration.setProperties(this.getProperties());
		sessionFactory = configuration.buildSessionFactory();
	}

}
