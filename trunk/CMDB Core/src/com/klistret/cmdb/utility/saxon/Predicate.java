package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Token;

public class Predicate {

	private Expression expression;

	private Configuration configuration;

	protected Predicate(Expression expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;

		explain(expression);
	}

	private void explain(Expression expression) {
		if (expression.getClass().getName().equals(
				BooleanExpression.class.getName())) {
			switch (((BooleanExpression) expression).getOperator()) {
			case Token.AND:
				new AndExpr((BooleanExpression) expression, configuration);
				break;
			case Token.OR:
				new OrExpr((BooleanExpression) expression, configuration);
				break;
			default:
				throw new IrresoluteException(
						String
								.format(
										"Boolean expression [%s] must either be an AND or OR operation",
										expression));
			}
		}
	}

	public Expression getExpression() {
		return this.expression;
	}

	public Expr getLogicalExpression() {
		return null;
	}
}
