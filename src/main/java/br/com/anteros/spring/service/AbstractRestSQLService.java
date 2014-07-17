package br.com.anteros.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.anteros.persistence.session.SQLSessionFactory;

/**
 * 
 * Classe que representa um serviço (@Service) do Spring com controle
 * transacional (@Transaction) e REST (@RestController)
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 *
 */
@RestController
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = true)
public abstract class AbstractRestSQLService<T> extends AbstractTransactionSQLService<T> {

	@Autowired
	public AbstractRestSQLService(SQLSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	/**
	 * Insert or Update object in database via POST or PUT methods;
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = false)
	@RequestMapping(value = "/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
	public abstract T save(@PathVariable(value = "id") String... id) throws Exception;

	/**
	 * Remove object in database via DELETE method
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = false)
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public abstract T remove(@PathVariable(value = "id") String... id) throws Exception;

	/**
	 * Get object in database via GET method
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public abstract T get(@PathVariable(value = "id") String... id) throws Exception;

}
