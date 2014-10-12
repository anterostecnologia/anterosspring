package br.com.anteros.spring.util;

import br.com.anteros.core.utils.AbstractCoreTranslate;

public class AnterosSpringTranslate extends AbstractCoreTranslate {

	
	private AnterosSpringTranslate(String messageBundleName) {
		super(messageBundleName);
	}
	

	static {
		setInstance(new AnterosSpringTranslate("anterosspring_messages"));
	}

}
