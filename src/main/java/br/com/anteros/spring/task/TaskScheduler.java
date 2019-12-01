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
package br.com.anteros.spring.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;

import br.com.anteros.persistence.session.SQLSessionFactory;

/**
 * 
 * Classe para padronizar e com exemplo de anotações necessárias para que o
 * Spring faça o agendamento de tarefas
 * 
 */
@EnableScheduling
public abstract class TaskScheduler {

	@Autowired
	protected SQLSessionFactory sessionFactorySQL;

	/**
	 * Método responsável por executar a tarefa agendada.
	 */
	public abstract void taskExecutor();

}
