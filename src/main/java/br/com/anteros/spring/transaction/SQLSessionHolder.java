/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.spring.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.transaction.Transaction;

/**
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class SQLSessionHolder extends ResourceHolderSupport {

	private static final Object DEFAULT_KEY = new Object();

	private final Map<Object, SQLSession> sessionMap = new ConcurrentHashMap<Object, SQLSession>(1);

	private Transaction transaction;


	public SQLSessionHolder(SQLSession session) {
		addSession(session);
	}

	public SQLSessionHolder(Object key, SQLSession session) {
		addSession(key, session);
	}


	public SQLSession getSession() {
		return getSession(DEFAULT_KEY);
	}

	public SQLSession getSession(Object key) {
		return this.sessionMap.get(key);
	}

	public SQLSession getValidatedSession() throws Exception {
		return getValidatedSession(DEFAULT_KEY);
	}

	public SQLSession getValidatedSession(Object key) throws Exception {
		SQLSession session = this.sessionMap.get(key);
		if (session != null && session.isClosed()) {
			this.sessionMap.remove(key);
			session = null;
		}
		return session;
	}

	public SQLSession getAnySession() {
		if (!this.sessionMap.isEmpty()) {
			return this.sessionMap.values().iterator().next();
		}
		return null;
	}

	public void addSession(SQLSession session) {
		addSession(DEFAULT_KEY, session);
	}

	public void addSession(Object key, SQLSession session) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(session, "SQLSession must not be null");
		this.sessionMap.put(key, session);
	}

	public SQLSession removeSession(Object key) {
		return this.sessionMap.remove(key);
	}

	public boolean containsSession(SQLSession session) {
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


	@Override
	public void clear() {
		super.clear();
		this.transaction = null;
	}

}
