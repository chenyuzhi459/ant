package io.sugo.server.usergroup.exception;

/**
 * Created by chenyuzhi on 18-9-5.
 */
public class UserGroupException extends Exception {
	public UserGroupException() {
	}

	public UserGroupException(String message) {
		super(message);
	}

	public UserGroupException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserGroupException(Throwable cause) {
		super(cause);
	}
}
