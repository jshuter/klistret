package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

public class IrresoluteExpr<T extends Expr> extends Expr {

	protected IrresoluteExpr(Expression expression, Configuration configuration) {
		super(expression, configuration);
	}

	@Override
	public Type getType() {
		return Type.Irresolute;
	}

	@Override
	public boolean equals(Object obj) {
		// TO-DO
		return false;
	}

}
