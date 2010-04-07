package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.expr.Expression;

public class OrExpr extends LogicalExpr<Expr> {

	protected OrExpr(Expression expression) {
		super(expression);
	}

	@Override
	public Type getType() {
		return Type.Or;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OrExpr) {
			return super.equals(obj);
		}
		return false;
	}
}
