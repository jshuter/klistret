package com.klistret.cmdb.exception;

import java.util.List;

@SuppressWarnings("serial")
public class NonUniqueObjectException extends ApplicationException {

	private List<String> expressions;

	public NonUniqueObjectException(String message) {
		super(message);
	}

	public NonUniqueObjectException(String message, List<String> expressions) {
		super(message);
		this.expressions = expressions;
	}

	public List<String> getExpressions() {
		return this.expressions;
	}
}
