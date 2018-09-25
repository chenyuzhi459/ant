package io.sugo.services.usergroup.exception;

/**
 * Created by chenyuzhi on 18-9-5.
 */
public class UserGroupException extends RuntimeException {
	private Object originalMessage;


	public UserGroupException(Object originalMessage, String message) {
		super(message);
		this.originalMessage = originalMessage;
	}

	public Object getOriginalMessage() {
		return originalMessage;
	}
}
