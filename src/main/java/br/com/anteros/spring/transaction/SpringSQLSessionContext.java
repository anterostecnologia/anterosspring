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

import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.context.CurrentSQLSessionContext;

/**
 * Implementação de CurrentSQLSession para Spring.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class SpringSQLSessionContext implements CurrentSQLSessionContext {

	private static final long serialVersionUID = 1L;
	private final SQLSessionFactory sessionFactory;


	public SpringSQLSessionContext(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	@Override
	public SQLSession currentSession() throws Exception {
		try {
			return (SQLSession) SQLSessionFactoryUtils.doGetSession(this.sessionFactory, true);
		}
		catch (IllegalStateException ex) {
			throw new Exception(ex.getMessage());
		}
	}
}
