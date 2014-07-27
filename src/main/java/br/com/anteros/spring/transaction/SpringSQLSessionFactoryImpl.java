package br.com.anteros.spring.transaction;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import br.com.anteros.persistence.log.Logger;
import br.com.anteros.persistence.log.LoggerProvider;
import br.com.anteros.persistence.metadata.EntityCacheManager;
import br.com.anteros.persistence.session.AbstractSQLSessionFactory;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.configuration.AnterosProperties;
import br.com.anteros.persistence.session.configuration.SessionFactoryConfiguration;
import br.com.anteros.persistence.session.context.CurrentSQLSessionContext;
import br.com.anteros.persistence.session.context.JTASQLSessionContext;
import br.com.anteros.persistence.session.context.ManagedSQLSessionContext;
import br.com.anteros.persistence.session.context.ThreadLocalSQLSessionContext;
import br.com.anteros.persistence.session.exception.SQLSessionException;
import br.com.anteros.persistence.session.impl.SQLQueryRunner;
import br.com.anteros.persistence.session.impl.SQLSessionFactoryImpl;
import br.com.anteros.persistence.session.impl.SQLSessionImpl;
import br.com.anteros.persistence.transaction.TransactionFactory;
import br.com.anteros.persistence.transaction.TransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.JDBCTransactionFactory;
import br.com.anteros.persistence.transaction.impl.JNDITransactionManagerLookup;
import br.com.anteros.persistence.transaction.impl.TransactionException;
import br.com.anteros.persistence.util.ReflectionUtils;

public class SpringSQLSessionFactoryImpl extends AbstractSQLSessionFactory {
	private static Logger log = LoggerProvider.getInstance().getLogger(SQLSessionFactoryImpl.class.getName());
	private TransactionFactory transactionFactory;
	private TransactionManagerLookup transactionManagerLookup;
	private TransactionManager transactionManager;
	private CurrentSQLSessionContext currentSessionContext;

	public SpringSQLSessionFactoryImpl(EntityCacheManager entityCacheManager, DataSource dataSource,
			SessionFactoryConfiguration configuration)
			throws Exception {
		super(entityCacheManager, dataSource, configuration);
		
		configuration.addProperty(AnterosProperties.CURRENT_SESSION_CONTEXT, SpringSQLSessionContext.class.getName());
		
		this.currentSessionContext = buildCurrentSessionContext();
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
			transactionFactory = new JDBCTransactionFactory();
		}
		return transactionFactory;
	}

	@Override
	public TransactionManagerLookup getTransactionManagerLookup() throws Exception {
		if (transactionManagerLookup == null) {
			String tmLookupClass = configuration.getProperty(AnterosProperties.TRANSACTION_MANAGER_LOOKUP);
			if (tmLookupClass == null) {
				tmLookupClass = JNDITransactionManagerLookup.class.getName();
			}
			if (tmLookupClass == null) {
				log.info("No TransactionManagerLookup configured (in JTA environment, use of read-write or transactional second-level cache is not recommended)");
				return null;
			} else {
				log.info("instantiating TransactionManagerLookup: " + tmLookupClass);
				try {
					transactionManagerLookup = (TransactionManagerLookup) ReflectionUtils.classForName(tmLookupClass)
							.newInstance();
					log.info("instantiated TransactionManagerLookup");
				} catch (Exception e) {
					log.error("Could not instantiate TransactionManagerLookup", e);
					throw new TransactionException("Could not instantiate TransactionManagerLookup '" + tmLookupClass
							+ "'");
				}
			}
		}
		return transactionManagerLookup;
	}

	@Override
	public TransactionManager getTransactionManager() throws Exception {
		log.info("obtaining TransactionManager");
		if (transactionManager == null)
			transactionManager = getTransactionManagerLookup().getTransactionManager();
		return transactionManager;
	}
	
	private CurrentSQLSessionContext buildCurrentSessionContext() throws Exception {
		String impl = configuration.getProperty( AnterosProperties.CURRENT_SESSION_CONTEXT );
		if ( impl == null && transactionManager != null ) {
			impl = "jta";
		}

		if ( impl == null ) {
			return null;
		}
		else if ( "jta".equals( impl ) ) {
			return new JTASQLSessionContext( this );
		}
		else if ( "thread".equals( impl ) ) {
			return new ThreadLocalSQLSessionContext( this );
		}
		else if ( "managed".equals( impl ) ) {
			return new ManagedSQLSessionContext( this );
		}
		else {
			return new ThreadLocalSQLSessionContext( this );
		}
	}

	@Override
	public SQLSession openSession(Connection connection) throws Exception {
		setConfigurationClientInfo(connection);
		return new SQLSessionImpl(this, connection, this.getEntityCacheManager(),
				new SQLQueryRunner(), this.getDialect(), this.isShowSql(), this.isFormatSql(),
				this.getQueryTimeout(),getTransactionFactory());
	}

}
