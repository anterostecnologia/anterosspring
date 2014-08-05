package br.com.anteros.spring.transaction;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import br.com.anteros.core.configuration.SessionFactoryConfiguration;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceProperties;
import br.com.anteros.persistence.session.context.CurrentSQLSessionContext;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.session.impl.SQLSessionFactoryImpl;
import br.com.anteros.persistence.session.impl.SQLSessionImpl;
import br.com.anteros.persistence.transaction.TransactionFactory;
import br.com.anteros.persistence.transaction.TransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.JDBCTransactionFactory;
import br.com.anteros.persistence.transaction.impl.JNDITransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.TransactionException;

public class SpringSQLSessionFactoryImpl extends AbstractSQLSessionFactory {
	private static Logger log = LoggerProvider.getInstance().getLogger(SQLSessionFactoryImpl.class.getName());
	private TransactionFactory transactionFactory;
	private TransactionManagerLookup transactionManagerLookup;
	private TransactionManager transactionManager;

	public SpringSQLSessionFactoryImpl(EntityCacheManager entityCacheManager, DataSource dataSource,
			SessionFactoryConfiguration configuration)
			throws Exception {
		super(entityCacheManager, dataSource, configuration);
		String tmLookupClass = configuration.getProperty(AnterosPersistenceProperties.TRANSACTION_MANAGER_LOOKUP);
		if (tmLookupClass == null) {
			log.info("No TransactionManagerLookup configured (in JTA environment, use of read-write or transactional second-level cache is not recommended)");
		} else {
			log.info("instantiating TransactionManagerLookup: " + tmLookupClass);
			try {
				transactionManagerLookup = (TransactionManagerLookup) ReflectionUtils.classForName(tmLookupClass)
						.newInstance();
				log.info("instantiated TransactionManagerLookup");
				transactionManager = transactionManagerLookup.getTransactionManager();
			} catch (Exception e) {
				log.error("Could not instantiate TransactionManagerLookup", e);
				throw new TransactionException("Could not instantiate TransactionManagerLookup '" + tmLookupClass + "'");
			}
		}
	}
	
	@Override
	protected CurrentSQLSessionContext buildCurrentSessionContext() throws Exception {
		return new SpringSQLSessionContext(this);
	}

	@Override
	public SQLSession getCurrentSession() throws Exception {
		if ( currentSessionContext == null ) {
			throw new SQLSessionException( "No CurrentSessionContext configured!" );
		}
		return currentSessionContext.currentSession();
	}

	@Override
	public void beforeGenerateDDL() throws Exception {

	}

	@Override
	public void afterGenerateDDL() throws Exception {

	}

	public SQLSession openSession() throws Exception {
		return openSession(this.getDatasource().getConnection());
	}
	
	
	@Override
	protected TransactionFactory getTransactionFactory() {
		if (transactionFactory == null) {
			try {
				transactionFactory = buildTransactionFactory();
			} catch (Exception e) {
				throw new TransactionException("Não foi possível criar a fábrica de transações.",e);
			}
		}
		return transactionFactory;
	}

	protected TransactionFactory buildTransactionFactory() throws Exception {
		if (transactionFactory == null) {
			String tfLookupClass = configuration.getProperty(AnterosPersistenceProperties.TRANSACTION_FACTORY);
			if (tfLookupClass == null) {
				tfLookupClass = JDBCTransactionFactory.class.getName();
			}
			log.info("instantiating TransactionFactory: " + tfLookupClass);
			try {
				transactionFactory = (TransactionFactory) ReflectionUtils.classForName(tfLookupClass).newInstance();
				log.info("instantiated TransactionFactory");
			} catch (Exception e) {
				log.error("Could not instantiate TransactionManagerLookup", e);
				throw new TransactionException("Could not instantiate TransactionManagerLookup '" + tfLookupClass + "'");
			}
		}
		return transactionFactory;
	}

	@Override
	public TransactionManagerLookup getTransactionManagerLookup() throws Exception {
		return transactionManagerLookup;
	}

	@Override
	public TransactionManager getTransactionManager() throws Exception {
		return transactionManager;
	}
	
	@Override
	public SQLSession openSession(Connection connection) throws Exception {
		setConfigurationClientInfo(connection);
		return new SQLSessionImpl(this, connection, this.getEntityCacheManager(),
				new SQLQueryRunner(), this.getDialect(), this.isShowSql(), this.isFormatSql(),
				this.getQueryTimeout(),getTransactionFactory());
	}

}
