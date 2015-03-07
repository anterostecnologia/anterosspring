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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import br.com.anteros.core.configuration.PackageScanEntity;
import br.com.anteros.core.scanner.ClassFilter;
import br.com.anteros.core.scanner.ClassPathScanner;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.exception.SQLSessionFactoryException;
import br.com.anteros.security.model.Security;

/**
 * Implementação de FactoryBean do Spring para criação da fábrica de sessões {@link SQLSessionFactory} do Anteros.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class SpringSQLSessionFactoryBean implements FactoryBean<SQLSessionFactory>, InitializingBean {

	protected SQLSessionFactory sessionFactory;
	protected Class<?>[] annotatedClasses;
	protected Properties properties;
	protected DataSource dataSource;
	protected String packageToScanEntity;
	protected boolean includeSecurityModel = false;

	public void setAnnotatedClasses(Class<?>[] annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	public void afterPropertiesSet() throws Exception {
		buildSessionFactory();
	}

	public SQLSessionFactory getObject() throws Exception {
		return sessionFactory;
	}

	public Class<?> getObjectType() {
		return (this.sessionFactory != null ? this.sessionFactory.getClass() : SQLSessionFactory.class);
	}

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

	protected void buildSessionFactory() throws Exception {
		SpringSQLConfiguration configuration = new SpringSQLConfiguration(this.getDataSource());
		List<Class<?>> result = new ArrayList<Class<?>>();
		if (getAnnotatedClasses() != null) {
			result.addAll(Arrays.asList(getAnnotatedClasses()));
		}

		for (Class<?> sourceClass : result) {
			configuration.addAnnotatedClass(sourceClass);
		}
		configuration.getSessionFactoryConfiguration().setPackageToScanEntity(
				new PackageScanEntity().setPackageName(packageToScanEntity));
		configuration.getSessionFactoryConfiguration().setIncludeSecurityModel(includeSecurityModel);
		configuration.setProperties(this.getProperties());
		sessionFactory = configuration.buildSessionFactory();
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Class<?>[] getAnnotatedClasses() {
		return annotatedClasses;
	}

	public SQLSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public String getPackageToScanEntity() {
		return packageToScanEntity;
	}

	public void setPackageToScanEntity(String packageToScanEntity) {
		this.packageToScanEntity = packageToScanEntity;
	}

	public boolean isIncludeSecurityModel() {
		return includeSecurityModel;
	}

	public void setIncludeSecurityModel(boolean includeSecurityModel) {
		this.includeSecurityModel = includeSecurityModel;
	}

}
