package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.expr.Expression;

public class ComparisonExpr extends LogicalExpr<Expr> {

	public enum Operator {
		ValueEquals, ValueNotEquals, ValueLessThan, ValueLessThanOrEquals, ValueGreaterThan, ValueGreaterThanOrEquals, GeneralEquals, GeneralNotEquals, GeneralLessThan, GeneralLessThanOrEquals, GeneralGreaterThan, GeneralGreaterThanOrEquals
	};

	private Operator operator;

	protected ComparisonExpr(Expression expression) {
		super(expression);
	}

	@Override
	public Type getType() {
		return Type.Comparison;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComparisonExpr) {
			return super.equals(obj);
		}
		return false;
	}

}
