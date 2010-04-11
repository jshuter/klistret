package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

public class OrExpr extends LogicalExpr<Expr> {

	protected OrExpr(Expression expression, Configuration configuration) {
		super(expression, configuration);
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
