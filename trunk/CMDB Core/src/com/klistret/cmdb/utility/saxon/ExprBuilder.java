package com.klistret.cmdb.utility.saxon;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.StaticContext;

public class ExprBuilder {

	public static List<Expr> makeRelativePath(StaticContext staticContext,
			Expression expression) {
		List<Expr> relativePath = new ArrayList<Expr>();
		explain(staticContext, expression, relativePath);

		return relativePath;
	}

	private static void explain(StaticContext staticContext,
			Expression expression, List<Expr> relativePath) {
		/** Slash expressions are basically relative paths in Saxon terms */
		if (expression.getClass().getName().equals(
				SlashExpression.class.getName())) {
			explain(staticContext, ((SlashExpression) expression)
					.getControllingExpression(), relativePath);
			explain(staticContext, ((SlashExpression) expression)
					.getControlledExpression(), relativePath);

			return;
		}

		/** Root expression */
		if (expression.getClass().getName().equals(
				RootExpression.class.getName())) {
			relativePath.add(new RootExpr<Expr>((RootExpression) expression));

			return;
		}

		/**
		 * Axis expressions are valid axis steps but usually within Saxon do not
		 * contain predicates (handled by filter expressions)
		 */
		if (expression.getClass().getName().equals(
				AxisExpression.class.getName())) {
			PathExpr<Expr> path = new PathExpr<Expr>(
					(AxisExpression) expression, staticContext
							.getConfiguration());

			// only forward, absolute paths
			if (path.isAbsolute() && path.isForward()) {
				relativePath.add(path);

				return;
			}
		}

		/**
		 * Filter expressions are usually a typical axis with predicates
		 */
		if (expression.getClass().getName().equals(
				net.sf.saxon.expr.FilterExpression.class.getName())) {

			Expression controlling = ((FilterExpression) expression)
					.getControllingExpression();
			// Expression filter = ((FilterExpression) expression).getFilter();

			// Continue if controlling expression is an axis expression
			if (controlling.getClass().getName().equals(
					AxisExpression.class.getName())) {
				PathExpr<Expr> path = new PathExpr<Expr>(
						(AxisExpression) controlling, staticContext
								.getConfiguration());

				// add predicates with acceptable expression types
				relativePath.add(path);

				return;
			}
		}

		relativePath.add(new IrresoluteExpr<Expr>(expression));
	}
}