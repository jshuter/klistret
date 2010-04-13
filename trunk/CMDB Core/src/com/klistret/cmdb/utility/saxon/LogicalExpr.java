package com.klistret.cmdb.utility.saxon;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.Token;
import net.sf.saxon.expr.ValueComparison;

public abstract class LogicalExpr<T extends Expr> extends Expr {

	protected List<Expr> operands;

	public LogicalExpr(Expression expression, Configuration configuration) {
		super(expression, configuration);
	}

	public LogicalExpr(BooleanExpression expression, Configuration configuration) {
		super(expression, configuration);

		operands = new ArrayList<Expr>();

		for (Expression expr : expression.getOperands()) {
			if (expr.getClass().getName().equals(
					BooleanExpression.class.getName())) {
				switch (((BooleanExpression) expr).getOperator()) {
				case Token.AND:
					operands.add(new AndExpr((BooleanExpression) expr,
							configuration));
					break;
				case Token.OR:
					operands.add(new OrExpr((BooleanExpression) expr,
							configuration));
					break;
				default:
					throw new IrresoluteException(
							String
									.format(
											"Boolean expression [%s] must either be an AND or OR operation",
											expr));
				}
			}

			else if (expr.getClass().getName().equals(
					GeneralComparison.class.getName())) {
				operands.add(new ComparisonExpr(expr, configuration));
			}

			else if (expr.getClass().getName().equals(
					ValueComparison.class.getName())) {
				operands.add(new ComparisonExpr(expr, configuration));
			}

			else {
				throw new IrresoluteException(
						String
								.format(
										"Operand [%s] not a boolean, general or value logical expression",
										expr));
			}
		}
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

	public List<Expr> getOperands() {
		return operands;
	}

	public String toString() {
		return String.format("predicate [%s], type [%s], operands [%s]",
				expression.toString(), getType(), operands);
	}
}
