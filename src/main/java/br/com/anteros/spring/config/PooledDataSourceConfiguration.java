package br.com.anteros.spring.config;

public class PooledDataSourceConfiguration {

	protected PooledDataSourceConfiguration() {

	}

	public static PooledDataSourceConfiguration create() {
		return new PooledDataSourceConfiguration();
	}

	public static PooledDataSourceConfiguration of(String driverClass, String jdbcUrl, String user, String password) {
		return new PooledDataSourceConfiguration().driverClass(driverClass).jdbcUrl(jdbcUrl).user(user)
				.password(password);
	}

	private String driverClass;

	private String jdbcUrl;

	private String user;

	private String password;

	private int acquireIncrement = 5;

	private int initialPoolSize = 10;

	private int maxPoolSize = 100;

	private int minPoolSize = 10;

	private int maxIdleTime = 5000;

	private int idleConnectionTestPeriod = 5000;

	private String preferredTestQuery;
	
	private boolean testConnectionOnCheckout;
	
	private boolean testConnectionOnCheckin;
	
	private int maxConnectionAge = 14400;
	
	private int acquireRetryAttempts = 10;
	
	private String automaticTestTable = "TST_CONNECTION";

	private int connectionIdleLimitInSeconds = 5;

	private String testConnectionQuery = "isValid";

	public String getDriverClass() {
		return driverClass;
	}

	public PooledDataSourceConfiguration driverClass(String driverClass) {
		this.driverClass = driverClass;
		return this;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public PooledDataSourceConfiguration jdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		return this;
	}

	public String getUser() {
		return user;
	}

	public PooledDataSourceConfiguration user(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public PooledDataSourceConfiguration password(String password) {
		this.password = password;
		return this;
	}

	public int getAcquireIncrement() {
		return acquireIncrement;
	}

	public PooledDataSourceConfiguration acquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
		return this;
	}

	public int getInitialPoolSize() {
		return initialPoolSize;
	}

	public PooledDataSourceConfiguration initialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
		return this;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public PooledDataSourceConfiguration maxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public PooledDataSourceConfiguration minPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
		return this;
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public PooledDataSourceConfiguration maxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
		return this;
	}

	public int getIdleConnectionTestPeriod() {
		return idleConnectionTestPeriod;
	}

	public PooledDataSourceConfiguration idleConnectionTestPeriod(int idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
		return this;
	}

	public String getPreferredTestQuery() {
		return preferredTestQuery;
	}

	public PooledDataSourceConfiguration preferredTestQuery(String preferredTestQuery) {
		this.preferredTestQuery = preferredTestQuery;
		return this;
	}


	public boolean isTestConnectionOnCheckout() {
		return testConnectionOnCheckout;
	}

	public PooledDataSourceConfiguration testConnectionOnCheckout(boolean testConnectionOnCheckout) {
		this.testConnectionOnCheckout = testConnectionOnCheckout;
		return this;
	}

	public boolean isTestConnectionOnCheckin() {
		return testConnectionOnCheckin;
	}

	public PooledDataSourceConfiguration testConnectionOnCheckin(boolean testConnectionOnCheckin) {
		this.testConnectionOnCheckin = testConnectionOnCheckin;
		return this;
	}

	public int getMaxConnectionAge() {
		return maxConnectionAge;
	}

	public PooledDataSourceConfiguration maxConnectionAge(int maxConnectionAge) {
		this.maxConnectionAge = maxConnectionAge;
		return this;
	}

	public int getAcquireRetryAttempts() {
		return acquireRetryAttempts;
	}

	public PooledDataSourceConfiguration acquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
		return this;
	}

	public String getAutomaticTestTable() {
		return automaticTestTable;
	}

	public PooledDataSourceConfiguration automaticTestTable(String automaticTestTable) {
		this.automaticTestTable = automaticTestTable;
		return this;
	}

	public int getConnectionIdleLimitInSeconds() {
		return connectionIdleLimitInSeconds;
	}

	public PooledDataSourceConfiguration connectionIdleLimitInSeconds(int connectionIdleLimitInSeconds) {
		this.connectionIdleLimitInSeconds = connectionIdleLimitInSeconds;
		return this;
	}

	public String getTestConnectionQuery() {
		return testConnectionQuery;
	}

	public PooledDataSourceConfiguration testConnectionQuery(String testConnectionQuery) {
		this.testConnectionQuery = testConnectionQuery;
		return this;
	}
}
