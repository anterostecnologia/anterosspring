package br.com.anteros.spring.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.anteros.persistence.session.ExternalFileManager;
import br.com.anteros.persistence.session.configuration.PackageScanEntity;
import br.com.anteros.persistence.session.query.ShowSQLType;

public class SQLSessionFactoryConfiguration {

	private List<Class<?>> entitySourceClasses = new ArrayList<Class<?>>();
	private PackageScanEntity packageScanEntity = null;
	private Boolean includeSecurityModel = true;
	private String dialect;
	private ShowSQLType[] showSql = { ShowSQLType.NONE };
	private Boolean formatSql = true;
	private String jdbcSchema = "";
	private String databaseDDLGeneration = "none";
	private String scriptDDLGeneration = "none";
	private String ddlOutputMode = "none";
	private String applicationLocation = "";
	private String createTablesFileName = "";
	private String dropTablesFileName = "";	
	private Long lockTimeout = 0L;
	private Boolean useBeanValidation = true;
	private ExternalFileManager externalFileManager;
	private Map<Object, Class<?>> entityListeners = new LinkedHashMap<>();

	private SQLSessionFactoryConfiguration() {

	}

	public static SQLSessionFactoryConfiguration create() {
		return new SQLSessionFactoryConfiguration();
	}
	
	public SQLSessionFactoryConfiguration addEntityListener(Class<?> entity, Object listener) {
		entityListeners.put(listener, entity);
		return this;
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

	public String getDatabaseDDLGeneration() {
		return databaseDDLGeneration;
	}

	public SQLSessionFactoryConfiguration databaseDDLGeneration(String databaseDDLGeneration) {
		this.databaseDDLGeneration = databaseDDLGeneration;
		return this;
	}
	
	public String getScriptDDLGeneration() {
		return scriptDDLGeneration;
	}

	public SQLSessionFactoryConfiguration scriptDDLGeneration(String scriptDDLGeneration) {
		this.scriptDDLGeneration = scriptDDLGeneration;
		return this;
	}

	public String getDDLOutputMode() {
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

	public Long getLockTimeout() {
		return lockTimeout;
	}

	public SQLSessionFactoryConfiguration lockTimeout(Long lockTimeout) {
		this.lockTimeout = lockTimeout;
		return this;
	}

	public Boolean getUseBeanValidation() {
		return useBeanValidation;
	}

	public SQLSessionFactoryConfiguration useBeanValidation(Boolean useBeanValidation) {
		this.useBeanValidation = useBeanValidation;
		return this;
	}

	public ExternalFileManager getExternalFileManager() {
		return externalFileManager;
	}

	public SQLSessionFactoryConfiguration externalFileManager(ExternalFileManager externalFileManager) {
		this.externalFileManager = externalFileManager;
		return this;
	}

	public Map<Object, Class<?>> getEntityListeners() {
		return entityListeners;
	}

}
