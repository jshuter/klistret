package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

public abstract class Expr {

	protected final Expression expression;

	protected final Configuration configuration;

	public enum Type {
		Root, Path, Or, And, Comparison, Irresolute
	};

	public Expr(Expression expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;
	}

	public Expression getExpression() {
		return expression;
	}

	public abstract Type getType();

	public abstract boolean equals(Object obj);

	public String toString() {
		return String.format("Saxon expression [%s], type [%s]", expression
				.toString(), getType());
	}
}