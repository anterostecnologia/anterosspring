package br.com.anteros.spring.util;

import br.com.anteros.core.utils.AbstractCoreTranslate;

public class AnterosSpringTranslate extends AbstractCoreTranslate {

	
	public AnterosSpringTranslate(String messageBundleName) {
		super(messageBundleName);
	}

	private static AnterosSpringTranslate translate;
	
	public static AnterosSpringTranslate getInstance(){
		if (translate==null){
			translate = new AnterosSpringTranslate("anterosspring_messages");
		}
		return translate;
	}
}
