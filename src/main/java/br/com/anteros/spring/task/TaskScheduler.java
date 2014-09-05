package br.com.anteros.spring.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;

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
public abstract class TaskScheduler {

	@Autowired
	protected SQLSessionFactory sessionFactory;

	/**
	 * Método responsável por executar a tarefa agendada.
	 */
	public abstract void taskExecutor();

}
