package br.com.anteros.spring.config;

import java.util.Properties;

public class AnterosDBCPDataSourceConfiguration extends PooledDataSourceConfiguration {

	protected Properties dataSourceProperties = new Properties();
	

	public AnterosDBCPDataSourceConfiguration addProperty(String name, Object value) {
		this.dataSourceProperties.put(name, value);
		return this;
	}


	public Properties getDataSourceProperties() {
		return dataSourceProperties;
	}	
	
	public static PooledDataSourceConfiguration create() {
		return new AnterosDBCPDataSourceConfiguration();
	}

	public static PooledDataSourceConfiguration of(String driverClass, String jdbcUrl, String user, String password) {
		return new AnterosDBCPDataSourceConfiguration().driverClass(driverClass).jdbcUrl(jdbcUrl).user(user)
				.password(password);
	}
	
}
