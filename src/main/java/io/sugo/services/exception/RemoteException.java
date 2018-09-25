package io.sugo.services.exception;

/**
 * Created by chenyuzhi on 18-9-5.
 */
public class RemoteException extends RuntimeException {
	private Object originalMessage;


	public RemoteException(Object originalMessage) {
		this.originalMessage = originalMessage;
	}


	public Object getOriginalMessage() {
		return originalMessage;
	}
}
