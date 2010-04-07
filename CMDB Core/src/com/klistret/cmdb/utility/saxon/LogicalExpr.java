package com.klistret.cmdb.utility.saxon;

import java.util.List;

import net.sf.saxon.expr.Expression;

public abstract class LogicalExpr<T extends Expr> extends Expr {

	protected List<T> operands;

	public LogicalExpr(Expression expression) {
		super(expression);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LogicalExpr) {
			LogicalExpr other = (LogicalExpr) obj;
			return operands == null ? other.operands == null : operands
					.equals(other.operands);
		}
		return false;
	}

	public List<T> getOperands() {
		return operands;
	}
}
