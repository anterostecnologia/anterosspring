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
package br.com.anteros.spring.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.lock.LockOptions;
import br.com.anteros.persistence.session.repository.Page;
import br.com.anteros.persistence.session.repository.Pageable;
import br.com.anteros.persistence.session.service.GenericSQLService;

/**
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 * @param <T>
 * @param <ID>
 */
public class SpringSQLService<T, ID extends Serializable> extends GenericSQLService<T, ID> {

	@Override
	@Autowired 
	public void setSessionFactory(@Qualifier("sessionFactorySQL") SQLSessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}
	
	@Override
	public <S extends T> S save(S entity) {
		return super.save(entity);
	}
	
	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		return super.save(entities);
	}
	
	@Override
	public <S extends T> S saveAndFlush(S entity) {
		return super.saveAndFlush(entity);
	}

	@Override
	public void flush() {
		super.flush();
	}
	
	@Override
	public void refresh(T entity, String fieldsToForceLazy) {
		super.refresh(entity, fieldsToForceLazy);
	}
	
	@Override
	public void remove(ID id) {
		super.remove(id);
	}

	@Override
	public void remove(T entity) {
		super.remove(entity);
	}

	@Override
	public void remove(Iterable<? extends T> entities) {
		super.remove(entities);
	}

	@Override
	public void removeAll() {
		super.removeAll();
	}
	

	@Override
	public T findOne(ID id, String fieldsToForceLazy) {
		return super.findOne(id, fieldsToForceLazy);
	}

	@Override
	public boolean exists(ID id) {
		return super.exists(id);
	}

	@Override
	public List<T> findAll(String fieldsToForceLazy) {
		return super.findAll(fieldsToForceLazy);
	}

	@Override
	public Page<T> findAll(Pageable pageable, String fieldsToForceLazy) {
		return super.findAll(pageable, fieldsToForceLazy);
	}

	@Override
	public List<T> find(String sql, String fieldsToForceLazy) {
		return super.find(sql, fieldsToForceLazy);
	}

	@Override
	public Page<T> find(String sql, Pageable pageable, String fieldsToForceLazy) {
		return super.find(sql, pageable, fieldsToForceLazy);
	}

	@Override
	public List<T> find(String sql, Object parameters, String fieldsToForceLazy) {
		return super.find(sql, parameters, fieldsToForceLazy);
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable, String fieldsToForceLazy) {
		return super.find(sql, parameters, pageable, fieldsToForceLazy);
	}

	@Override
	public List<T> findByNamedQuery(String queryName, String fieldsToForceLazy) {
		return super.findByNamedQuery(queryName, fieldsToForceLazy);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Pageable pageable, String fieldsToForceLazy) {
		return super.findByNamedQuery(queryName, pageable, fieldsToForceLazy);
	}

	@Override
	public List<T> findByNamedQuery(String queryName, Object parameters, String fieldsToForceLazy) {
		return super.findByNamedQuery(queryName, parameters, fieldsToForceLazy);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable, String fieldsToForceLazy) {
		return super.findByNamedQuery(queryName, parameters, pageable, fieldsToForceLazy);
	}

	@Override
	public T findOne(Predicate predicate, String fieldsToForceLazy) {
		return super.findOne(predicate, fieldsToForceLazy);
	}

	@Override
	public List<T> findAll(Predicate predicate, String fieldsToForceLazy) {
		return super.findAll(predicate, fieldsToForceLazy);
	}

	@Override
	public Iterable<T> findAll(Predicate predicate, String fieldsToForceLazy, OrderSpecifier<?>... orders) {
		return super.findAll(predicate, fieldsToForceLazy, orders);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable, String fieldsToForceLazy) {
		return super.findAll(predicate, pageable, fieldsToForceLazy);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable, String fieldsToForceLazy, OrderSpecifier<?>... orders) {
		return super.findAll(predicate, pageable, fieldsToForceLazy, orders);
	}

	@Override
	public long count() {
		return super.count();
	}

	@Override
	public long count(Predicate predicate) {
		return super.count(predicate);
	}
	
	@Override
	public String getTableName() throws Exception {
		return super.getTableName();
	}

	@Override
	public T findByCode(String code, boolean readOnly, String fieldsToForceLazy) {
		return super.findByCode(code, readOnly, fieldsToForceLazy);
	}
	
	@Override
	public T findByCode(String code, LockOptions lockOptions, boolean readOnly, String fieldsToForceLazy) {
		return super.findByCode(code, lockOptions, readOnly, fieldsToForceLazy);
	}
	
	@Override
	public T findByCode(String code, LockOptions lockOptions, String fieldsToForceLazy) {
		return super.findByCode(code, lockOptions, fieldsToForceLazy);
	}
	
	@Override
	public T findByCode(String code, String fieldsToForceLazy) {
		return super.findByCode(code, fieldsToForceLazy);
	}

}
