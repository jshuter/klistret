package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BooleanExpression;

public class AndExpr extends LogicalExpr<Expr> {

	protected AndExpr(BooleanExpression expression, Configuration configuration) {
		super(expression, configuration);
	}

	@Override
	public Type getType() {
		return Type.And;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AndExpr) {
			return super.equals(obj);
		}
		return false;
	}

}
