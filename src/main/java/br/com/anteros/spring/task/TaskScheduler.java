package br.com.anteros.spring.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.anteros.persistence.session.SQLSessionFactory;

/**
 * 
 * Classe para padronizar e com exemplo de anotações necessárias para que o
 * Spring faça o agendamento de tarefas
 * 
 * @author Douglas Junior <nassifrroma@gmail.com>
 *
 */
@EnableScheduling
@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = true)
public abstract class TaskScheduler {

	@Autowired
	protected SQLSessionFactory sqlSessionFactory;

	/**
	 * Método responsável por executar a tarefa agendada.
	 */
	@Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED, readOnly = false)
	public abstract void taskExecutor();

}
