package br.com.anteros.spring.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceProperties;
import br.com.anteros.persistence.session.query.ShowSQLType;
import br.com.anteros.spring.transaction.SpringSQLConfiguration;


public abstract class AnterosSpringPersistenceConfiguration {

	public abstract PooledDataSourceConfiguration getPooledDataSourceConfiguration();

	public abstract SingleDataSourceConfiguration getSingleDataSourceConfiguration();

	public abstract SQLSessionFactoryConfiguration getSQLSessionFactoryConfiguration();
	
	@Bean
	public SQLSessionFactory sessionFactory() throws Exception {
		SQLSessionFactoryConfiguration sqlSessionFactoryConfiguration = getSQLSessionFactoryConfiguration();
		if (sqlSessionFactoryConfiguration != null) {
			DataSource dataSource = dataSource();
			SpringSQLConfiguration configuration = new SpringSQLConfiguration(dataSource, sqlSessionFactoryConfiguration.getExternalFileManager(),sqlSessionFactoryConfiguration.isEnableImageCompression());
			for (Class<?> sourceClass : sqlSessionFactoryConfiguration.getEntitySourceClasses()) {
				configuration.addAnnotatedClass(sourceClass);
			}
			configuration.getSessionFactoryConfiguration()
					.setPackageToScanEntity(sqlSessionFactoryConfiguration.getPackageScanEntity());
			configuration.getSessionFactoryConfiguration().setIncludeSecurityModel(sqlSessionFactoryConfiguration.isIncludeSecurityModel());
			configuration.addProperty(AnterosPersistenceProperties.DIALECT, sqlSessionFactoryConfiguration.getDialect());
			configuration.addProperty(AnterosPersistenceProperties.SHOW_SQL,
					ShowSQLType.parse(sqlSessionFactoryConfiguration.getShowSql()));
			configuration.addProperty(AnterosPersistenceProperties.FORMAT_SQL,
					String.valueOf(sqlSessionFactoryConfiguration.isFormatSql()));
			configuration.addProperty(AnterosPersistenceProperties.JDBC_SCHEMA,
					sqlSessionFactoryConfiguration.getJdbcSchema());
			configuration.addProperty(AnterosPersistenceProperties.DATABASE_DDL_GENERATION,
					sqlSessionFactoryConfiguration.getDatabaseDDLGeneration());
			configuration.addProperty(AnterosPersistenceProperties.SCRIPT_DDL_GENERATION,
					sqlSessionFactoryConfiguration.getScriptDDLGeneration());
			configuration.addProperty(AnterosPersistenceProperties.DDL_OUTPUT_MODE,
					sqlSessionFactoryConfiguration.getDDLOutputMode());
			configuration.addProperty(AnterosPersistenceProperties.APPLICATION_LOCATION,
					sqlSessionFactoryConfiguration.getApplicationLocation());
			configuration.addProperty(AnterosPersistenceProperties.CREATE_TABLES_FILENAME,
					sqlSessionFactoryConfiguration.getCreateTablesFileName());
			configuration.addProperty(AnterosPersistenceProperties.DROP_TABLES_FILENAME,
					sqlSessionFactoryConfiguration.getDropTablesFileName());
			configuration.addProperty(AnterosPersistenceProperties.LOCK_TIMEOUT, sqlSessionFactoryConfiguration.getLockTimeout()+"");
			configuration.addProperty(AnterosPersistenceProperties.USE_BEAN_VALIDATION, sqlSessionFactoryConfiguration.getUseBeanValidation()+"");
			return configuration.buildSessionFactory();
		}
		return null;
	}


	@Bean
	public DataSource dataSource() throws Exception {
		if (getPooledDataSourceConfiguration() != null) {
			PooledDataSourceConfiguration pooledDataSourceConfiguration = getPooledDataSourceConfiguration();
			ComboPooledDataSource dataSource = new ComboPooledDataSource();
			dataSource.setDriverClass(pooledDataSourceConfiguration.getDriverClass());
			dataSource.setAcquireIncrement(Integer.valueOf(pooledDataSourceConfiguration.getAcquireIncrement()));
			dataSource.setJdbcUrl(pooledDataSourceConfiguration.getJdbcUrl());
			dataSource.setUser(pooledDataSourceConfiguration.getUser());
			dataSource.setPassword(pooledDataSourceConfiguration.getPassword());
			dataSource.setInitialPoolSize(Integer.valueOf(pooledDataSourceConfiguration.getInitialPoolSize()));
			dataSource.setMaxPoolSize(Integer.valueOf(pooledDataSourceConfiguration.getMaxPoolSize()));
			dataSource.setMinPoolSize(Integer.valueOf(pooledDataSourceConfiguration.getMinPoolSize()));
			dataSource.setMaxIdleTime(Integer.valueOf(pooledDataSourceConfiguration.getMaxIdleTime()));
			dataSource.setIdleConnectionTestPeriod(
					Integer.valueOf(pooledDataSourceConfiguration.getIdleConnectionTestPeriod()));
			dataSource.setAcquireRetryAttempts(pooledDataSourceConfiguration.getAcquireRetryAttempts());
			dataSource.setMaxConnectionAge(pooledDataSourceConfiguration.getMaxConnectionAge());
			dataSource.setAutomaticTestTable(pooledDataSourceConfiguration.getAutomaticTestTable());
			dataSource.setPreferredTestQuery(pooledDataSourceConfiguration.getPreferredTestQuery());
			dataSource.setTestConnectionOnCheckin(pooledDataSourceConfiguration.isTestConnectionOnCheckin());
			dataSource.setTestConnectionOnCheckout(pooledDataSourceConfiguration.isTestConnectionOnCheckout());
			return dataSource;
		} else if (getSingleDataSourceConfiguration() != null) {
			SingleDataSourceConfiguration singleDataSourceConfiguration = getSingleDataSourceConfiguration();
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(singleDataSourceConfiguration.getDriverClass());
			dataSource.setUsername(singleDataSourceConfiguration.getUser());
			dataSource.setPassword(singleDataSourceConfiguration.getPassword());
			dataSource.setUrl(singleDataSourceConfiguration.getJdbcUrl());
		}
		return null;
	}
	
}
