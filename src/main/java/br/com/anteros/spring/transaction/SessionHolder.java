package br.com.anteros.spring.transaction;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

import br.com.anteros.persistence.metadata.annotation.type.FlushMode;
import br.com.anteros.persistence.session.SQLSession;

public class SessionHolder extends ResourceHolderSupport {

	private SQLSession session;

	private FlushMode previousFlushMode;

	public SessionHolder(SQLSession session) {
		Assert.notNull(session, "Session must not be null");
		this.session = session;
	}

	public SQLSession getSession() {
		return this.session;
	}

	public void setPreviousFlushMode(FlushMode previousFlushMode) {
		this.previousFlushMode = previousFlushMode;
	}

	public FlushMode getPreviousFlushMode() {
		return this.previousFlushMode;
	}

	@Override
	public void clear() {
		super.clear();
		this.previousFlushMode = null;
	}

}
