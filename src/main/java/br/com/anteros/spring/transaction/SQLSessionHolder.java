package br.com.anteros.spring.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

import br.com.anteros.persistence.metadata.annotation.type.FlushMode;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.transaction.Transaction;


public class SQLSessionHolder extends ResourceHolderSupport {

	private static final Object DEFAULT_KEY = new Object();

	private final Map<Object, SQLSession> sessionMap = new ConcurrentHashMap<Object, SQLSession>(1);

	private Transaction transaction;

	private FlushMode previousFlushMode;


	public SQLSessionHolder(SQLSession session) {
		addSQLSession(session);
	}

	public SQLSessionHolder(Object key, SQLSession session) {
		addSQLSession(key, session);
	}


	public SQLSession getSQLSession() {
		return getSQLSession(DEFAULT_KEY);
	}

	public SQLSession getSQLSession(Object key) {
		return this.sessionMap.get(key);
	}

	public SQLSession getValidatedSQLSession() {
		return getValidatedSQLSession(DEFAULT_KEY);
	}

	public SQLSession getValidatedSQLSession(Object key) {
		SQLSession session = this.sessionMap.get(key);
		return session;
	}

	public SQLSession getAnySQLSession() {
		if (!this.sessionMap.isEmpty()) {
			return this.sessionMap.values().iterator().next();
		}
		return null;
	}

	public void addSQLSession(SQLSession session) {
		addSQLSession(DEFAULT_KEY, session);
	}

	public void addSQLSession(Object key, SQLSession session) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(session, "Session must not be null");
		this.sessionMap.put(key, session);
	}

	public SQLSession removeSQLSession(Object key) {
		return this.sessionMap.remove(key);
	}

	public boolean containsSQLSession(SQLSession session) {
		return this.sessionMap.containsValue(session);
	}

	public boolean isEmpty() {
		return this.sessionMap.isEmpty();
	}

	public boolean doesNotHoldNonDefaultSession() {
		return this.sessionMap.isEmpty() ||
				(this.sessionMap.size() == 1 && this.sessionMap.containsKey(DEFAULT_KEY));
	}


	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return this.transaction;
	}

	public void setPreviousFlushMode(FlushMode previousFlushMode) {
		this.previousFlushMode = previousFlushMode;
	}

	public FlushMode getPreviousFlushMode() {
		return this.previousFlushMode;
	}


	@Override
	public void clear() {
		super.clear();
		this.transaction = null;
		this.previousFlushMode = null;
	}

}
