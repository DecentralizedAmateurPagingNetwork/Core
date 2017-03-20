package org.dapnet.core;

public class CoreStartupException extends RuntimeException {

	private static final long serialVersionUID = -4703872721790107028L;

	public CoreStartupException() {
	}

	public CoreStartupException(String message) {
		super(message);
	}

	public CoreStartupException(Throwable cause) {
		super(cause);
	}

	public CoreStartupException(String message, Throwable cause) {
		super(message, cause);
	}

}
