package br.com.anteros.spring.config;

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.persistence.session.configuration.PackageScanEntity;
import br.com.anteros.persistence.session.query.ShowSQLType;

public class SQLSessionFactoryConfiguration {

	private List<Class<?>> entitySourceClasses = new ArrayList<Class<?>>();
	private PackageScanEntity packageScanEntity = null;
	private boolean includeSecurityModel = true;
	private String dialect;
	private ShowSQLType[] showSql = { ShowSQLType.NONE };
	private boolean formatSql = true;
	private String jdbcSchema = "";
	private String databaseDdlGeneration = "none";
	private String ddlOutputMode = "none";
	private String applicationLocation = "";
	private String createTablesFileName = "";
	private String dropTablesFileName = "";	

	private SQLSessionFactoryConfiguration() {

	}

	public static SQLSessionFactoryConfiguration create() {
		return new SQLSessionFactoryConfiguration();
	}

	public List<Class<?>> getEntitySourceClasses() {
		return entitySourceClasses;
	}

	public SQLSessionFactoryConfiguration addEntitySourceClass(Class<?> entitySourceClass) {
		this.entitySourceClasses.add(entitySourceClass);
		return this;
	}

	public PackageScanEntity getPackageScanEntity() {
		return packageScanEntity;
	}

	public SQLSessionFactoryConfiguration packageScanEntity(PackageScanEntity packageScanEntity) {
		this.packageScanEntity = packageScanEntity;
		return this;
	}

	public boolean isIncludeSecurityModel() {
		return includeSecurityModel;
	}

	public SQLSessionFactoryConfiguration includeSecurityModel(boolean includeSecurityModel) {
		this.includeSecurityModel = includeSecurityModel;
		return this;
	}

	public String getDialect() {
		return dialect;
	}

	public SQLSessionFactoryConfiguration dialect(String dialect) {
		this.dialect = dialect;
		return this;
	}

	public ShowSQLType[] getShowSql() {
		return showSql;
	}

	public SQLSessionFactoryConfiguration showSql(ShowSQLType... showSql) {
		this.showSql = showSql;
		return this;
	}

	public boolean isFormatSql() {
		return formatSql;
	}

	public SQLSessionFactoryConfiguration formatSql(boolean formatSql) {
		this.formatSql = formatSql;
		return this;
	}

	public String getJdbcSchema() {
		return jdbcSchema;
	}

	public SQLSessionFactoryConfiguration jdbcSchema(String jdbcSchema) {
		this.jdbcSchema = jdbcSchema;
		return this;
	}

	public String getDatabaseDdlGeneration() {
		return databaseDdlGeneration;
	}

	public SQLSessionFactoryConfiguration databaseDdlGeneration(String databaseDdlGeneration) {
		this.databaseDdlGeneration = databaseDdlGeneration;
		return this;
	}

	public String getDdlOutputMode() {
		return ddlOutputMode;
	}

	public SQLSessionFactoryConfiguration ddlOutputMode(String ddlOutputMode) {
		this.ddlOutputMode = ddlOutputMode;
		return this;
	}

	public String getApplicationLocation() {
		return applicationLocation;
	}

	public SQLSessionFactoryConfiguration applicationLocation(String applicationLocation) {
		this.applicationLocation = applicationLocation;
		return this;
	}

	public String getCreateTablesFileName() {
		return createTablesFileName;
	}

	public SQLSessionFactoryConfiguration createTablesFileName(String createTablesFileName) {
		this.createTablesFileName = createTablesFileName;
		return this;
	}

	public String getDropTablesFileName() {
		return dropTablesFileName;
	}

	public SQLSessionFactoryConfiguration dropTablesFileName(String dropTablesFileName) {
		this.dropTablesFileName = dropTablesFileName;
		return this;
	}

}
