package br.com.anteros.spring.transaction;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import br.com.anteros.persistence.session.SQLSessionFactory;

public class SpringSQLSessionFactoryBean implements FactoryBean<SQLSessionFactory>, InitializingBean {

	protected SQLSessionFactory sessionFactory;
	protected Class<?>[] annotatedClasses;
	protected Properties properties;
	protected DataSource dataSource;

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
		List<Class<?>> result = Arrays.asList(getAnnotatedClasses());
		for (Class<?> sourceClass : result) {
			configuration.addAnnotatedClass(sourceClass);
		}
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

}
