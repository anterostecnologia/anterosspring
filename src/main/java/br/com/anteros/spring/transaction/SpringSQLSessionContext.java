package br.com.anteros.spring.transaction;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.context.CurrentSQLSessionContext;

public class SpringSQLSessionContext implements CurrentSQLSessionContext {

	private static final long serialVersionUID = 1L;
	private final SQLSessionFactory sessionFactory;


	public SpringSQLSessionContext(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	@Override
	public SQLSession currentSession() throws Exception {
		try {
			return (SQLSession) SQLSessionFactoryUtils.doGetSession(this.sessionFactory, false);
		}
		catch (IllegalStateException ex) {
			throw new Exception(ex.getMessage());
		}
	}
}
