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

import br.com.anteros.persistence.dsl.osql.types.OrderSpecifier;
import br.com.anteros.persistence.dsl.osql.types.Predicate;
import br.com.anteros.persistence.session.SQLSessionFactory;
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
	public void setSessionFactory(SQLSessionFactory sessionFactory) {
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
	public void refresh(T entity) {
		super.refresh(entity);
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
	public T findOne(ID id) {
		return super.findOne(id);
	}

	@Override
	public boolean exists(ID id) {
		return super.exists(id);
	}

	@Override
	public List<T> findAll() {
		return super.findAll();
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		return super.findAll(pageable);
	}

	@Override
	public List<T> find(String sql) {
		return super.find(sql);
	}

	@Override
	public Page<T> find(String sql, Pageable pageable) {
		return super.find(sql, pageable);
	}

	@Override
	public List<T> find(String sql, Object parameters) {
		return super.find(sql, parameters);
	}

	@Override
	public Page<T> find(String sql, Object parameters, Pageable pageable) {
		return super.find(sql, parameters, pageable);
	}

	@Override
	public List<T> findByNamedQuery(String queryName) {
		return super.findByNamedQuery(queryName);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Pageable pageable) {
		return super.findByNamedQuery(queryName, pageable);
	}

	@Override
	public List<T> findByNamedQuery(String queryName, Object parameters) {
		return super.findByNamedQuery(queryName, parameters);
	}

	@Override
	public Page<T> findByNamedQuery(String queryName, Object parameters, Pageable pageable) {
		return super.findByNamedQuery(queryName, parameters, pageable);
	}

	@Override
	public T findOne(Predicate predicate) {
		return super.findOne(predicate);
	}

	@Override
	public List<T> findAll(Predicate predicate) {
		return super.findAll(predicate);
	}

	@Override
	public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
		return super.findAll(predicate, orders);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {
		return super.findAll(predicate, pageable);
	}

	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... orders) {
		return super.findAll(predicate, pageable, orders);
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

	

}
