package br.com.anteros.spring.service;

import java.lang.reflect.ParameterizedType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.anteros.persistence.service.AbstractSQLService;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.dao.SQLDao;

/**
 * 
 * Classe que representa um serviço (@Service) do Spring com controle
 * transacional (@Transaction)
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 *
 */
@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = true)
public abstract class AbstractTransactionSQLService<T> extends AbstractSQLService<T> {

	@Autowired
	public AbstractTransactionSQLService(SQLSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = false)
	public void remove(T object) throws Exception {
		super.remove(object);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = false)
	public T save(T object) throws Exception {
		return super.save(object);
	}

}
