package br.com.anteros.spring.config;

public class SingleDataSourceConfiguration {

	private String driverClass;

	private String jdbcUrl;

	private String user;

	private String password;

	private SingleDataSourceConfiguration() {

	}

	public static SingleDataSourceConfiguration create() {
		return new SingleDataSourceConfiguration();
	}

	public static SingleDataSourceConfiguration of(String driverClass, String jdbcUrl, String user, String password) {
		return new SingleDataSourceConfiguration().driverClass(driverClass).jdbcUrl(jdbcUrl).user(user)
				.password(password);
	}

	public String getDriverClass() {
		return driverClass;
	}

	public SingleDataSourceConfiguration driverClass(String driverClass) {
		this.driverClass = driverClass;
		return this;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public SingleDataSourceConfiguration jdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		return this;
	}

	public String getUser() {
		return user;
	}

	public SingleDataSourceConfiguration user(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public SingleDataSourceConfiguration password(String password) {
		this.password = password;
		return this;
	}

}
