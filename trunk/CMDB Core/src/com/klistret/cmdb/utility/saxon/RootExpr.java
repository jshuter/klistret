package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.expr.RootExpression;

public class RootExpr<T extends Expr> extends Expr {

	protected RootExpr(RootExpression expression) {
		super(expression);
	}

	@Override
	public Type getType() {
		return Type.Root;
	}

	@Override
	public boolean equals(Object obj) {
		// TO-DO
		return false;
	}

}
