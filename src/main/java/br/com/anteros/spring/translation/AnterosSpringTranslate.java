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
package br.com.anteros.spring.translation;

import br.com.anteros.core.translation.AbstractCoreTranslate;
import br.com.anteros.core.translation.TranslateMessage;
import br.com.anteros.persistence.translation.AnterosPersistenceCoreTranslateMessages;

/**
 * Classe de tradução/internacionalização de mensagens AnterosSpring.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class AnterosSpringTranslate extends AbstractCoreTranslate {


	private static AnterosSpringTranslate singleton;

	public static AnterosSpringTranslate getInstance() {
        if ( singleton == null )
            singleton = new AnterosSpringTranslate(AnterosPersistenceCoreTranslateMessages.class);

        return (AnterosSpringTranslate) singleton;
    }    
	
	public AnterosSpringTranslate(Class<? extends TranslateMessage> translateClass) {
		super(translateClass);
	}

}
