package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.expr.Expression;

public abstract class Expr {

	protected final Expression expression;

	public enum Type {
		Root, Path, Or, And, Comparison, Irresolute
	};

	public Expr(Expression expression) {
		this.expression = expression;
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
