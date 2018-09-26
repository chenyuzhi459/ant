package io.sugo.services.exception;

/**
 * Created by chenyuzhi on 18-9-5.
 */
public class RemoteException extends RuntimeException {
	private Object remoteMessage;


	public RemoteException(Object remoteMessage) {
		this.remoteMessage = remoteMessage;
	}

	public Object getRemoteMessage() {
		return remoteMessage;
	}
}
