package com.klistret.cmdb.utility.saxon;

@SuppressWarnings("serial")
public class IrresoluteException extends RuntimeException {
	public IrresoluteException() {
	}

	public IrresoluteException(String message) {
		super(message);
	}

	public IrresoluteException(String message, Throwable cause) {
		super(message, cause);
	}

	public IrresoluteException(Throwable cause) {
		super(cause);
	}
}
