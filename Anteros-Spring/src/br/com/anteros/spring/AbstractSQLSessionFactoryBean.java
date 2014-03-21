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

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import br.com.anteros.persistence.session.SQLSessionFactory;

public abstract class AbstractSQLSessionFactoryBean implements FactoryBean, InitializingBean{

	protected SQLSessionFactory sessionFactory;
	protected Class<?>[] annotatedClasses;
	protected Properties properties;
	protected DataSource dataSource;
	
	public void setAnnotatedClasses(Class<?>[] annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		buildSessionFactory();
	}

	@Override
	public Object getObject() throws Exception {
		return sessionFactory;
	}

	@Override
	public Class getObjectType() {
		return (this.sessionFactory != null ? this.sessionFactory.getClass() : SQLSessionFactory.class);
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		if (this.properties == null) {
			this.properties = new Properties();
		}
		return this.properties;
	}
	
	protected abstract void buildSessionFactory() throws Exception;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Class[] getAnnotatedClasses() {
		return annotatedClasses;
	}

}
