package br.com.anteros.spring.config;

import java.util.Properties;

public class ViburDataSourceConfiguration extends PooledDataSourceConfiguration {

	protected Properties dataSourceProperties = new Properties();
	

	public ViburDataSourceConfiguration addProperty(String name, Object value) {
		this.dataSourceProperties.put(name, value);
		return this;
	}


	public Properties getDataSourceProperties() {
		return dataSourceProperties;
	}	
	
	public static PooledDataSourceConfiguration create() {
		return new ViburDataSourceConfiguration();
	}

	public static PooledDataSourceConfiguration of(String driverClass, String jdbcUrl, String user, String password) {
		return new ViburDataSourceConfiguration().driverClass(driverClass).jdbcUrl(jdbcUrl).user(user)
				.password(password);
	}
	
}
