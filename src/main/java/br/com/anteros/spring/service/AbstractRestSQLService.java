package br.com.anteros.spring.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

/**
 * 
 * Classe que representa um serviço (@Service) do Spring com controle
 * transacional (@Transaction) e REST (@RestController)
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 *
 */
@RestController
public abstract class AbstractRestSQLService<T> extends AbstractTransactionSQLService<T> {

	/**
	 * Insert or Update object in database via POST or PUT methods;
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = false)
	@RequestMapping(value = "/", method = { RequestMethod.POST, RequestMethod.PUT })
	public abstract T save(HttpServletRequest request, HttpServletResponse response, @RequestBody T object) throws Exception;

	/**
	 * Remove object in database via DELETE method
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, readOnly = false)
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public abstract T remove(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "id") String... id) throws Exception;

	/**
	 * Get object in database via GET method
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public abstract T get(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "id") String... id) throws Exception;

	/**
	 * Método para tratamento de exceções ocorridas durante a requisição REST.
	 * 
	 * @param ex
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(HttpClientErrorException.class)
	public @ResponseBody String handleUncaughtException(HttpClientErrorException ex, WebRequest request,
			HttpServletResponse response)
			throws IOException {
		response.sendError(ex.getStatusCode().value(), ex.getMessage());
		ex.printStackTrace();
		return ex.getMessage();
	}

}
