package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.expr.Expression;

public class AndExpr extends LogicalExpr<Expr> {

	protected AndExpr(Expression expression) {
		super(expression);
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
