package br.com.anteros.spring.service;

import java.lang.reflect.ParameterizedType;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.anteros.persistence.service.AbstractSQLService;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.dao.SQLSessionFactoryDao;

/**
 * 
 * Classe que representa um serviço (@Service) do Spring com controle
 * transacional (@Transaction)
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 *
 */
@Service
@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true)
public abstract class AbstractTransactionSQLService<T> extends AbstractSQLService<T> {

	@Autowired
	protected SQLSessionFactory sqlSessionFactory;

	@PostConstruct
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true)
	public void initDao() {
		try {
			// descobre automaticamente qual a classe do tipo T
			Class<T> clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
					.getActualTypeArguments()[0];
			dao = new SQLSessionFactoryDao<T>(clazz, sqlSessionFactory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = false)
	public void remove(T object) throws Exception {
		super.remove(object);
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = false)
	public T save(T object) throws Exception {
		return super.save(object);
	}

}
