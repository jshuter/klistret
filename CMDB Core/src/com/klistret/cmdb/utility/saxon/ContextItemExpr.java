package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ContextItemExpression;

public class ContextItemExpr extends Expr {

	protected ContextItemExpr(ContextItemExpression expression,
			Configuration configuration) {
		super(expression, configuration);
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Type.ContextItem;
	}

}
