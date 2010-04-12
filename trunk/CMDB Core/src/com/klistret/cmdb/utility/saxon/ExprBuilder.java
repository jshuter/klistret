package com.klistret.cmdb.utility.saxon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.StaticContext;

public class ExprBuilder {

	private interface SaxonTranslator {
		void explain(StaticContext staticContext, Expression expression,
				List<Expr> relativePath);
	}

	private enum Translator implements SaxonTranslator {
		Slash(SlashExpression.class.getName()) {
			public void explain(StaticContext staticContext,
					Expression expression, List<Expr> relativePath) {
				Expression controlling = ((SlashExpression) expression)
						.getControllingExpression();
				Expression controlled = ((SlashExpression) expression)
						.getControlledExpression();

				getTranslatorByClassName(controlling.getClass().getName())
						.explain(staticContext, controlling, relativePath);

				getTranslatorByClassName(controlled.getClass().getName())
						.explain(staticContext, controlled, relativePath);
			}
		},
		Root(RootExpression.class.getName()) {
			public void explain(StaticContext staticContext,
					Expression expression, List<Expr> relativePath) {
				relativePath.add(new RootExpr<Expr>(
						(RootExpression) expression, staticContext
								.getConfiguration()));
			}
		},
		Axis(AxisExpression.class.getName()) {
			public void explain(StaticContext staticContext,
					Expression expression, List<Expr> relativePath) {
				relativePath.add(new RelativePathExpr<Expr>(
						(AxisExpression) expression, staticContext
								.getConfiguration()));
			}
		},
		Filter(FilterExpression.class.getName()) {
			public void explain(StaticContext staticContext,
					Expression expression, List<Expr> relativePath) {
				relativePath.add(new RelativePathExpr<Expr>(
						(FilterExpression) expression, staticContext
								.getConfiguration()));
			}
		};

		private static Map<String, Translator> requestLookup;

		static {

			requestLookup = new HashMap<String, Translator>();
			requestLookup.put(Slash.getClassName(), Slash);
			requestLookup.put(Root.getClassName(), Root);
			requestLookup.put(Axis.getClassName(), Axis);
			requestLookup.put(Filter.getClassName(), Filter);
		}

		private final String className;

		private Translator(String className) {
			this.className = className;
		}

		public String getClassName() {
			return className;
		}

		public static Translator getTranslatorByClassName(String className) {
			return requestLookup.get(className);
		}
	}

	public static List<Expr> makeRelativePath(StaticContext staticContext,
			Expression expression) {
		List<Expr> relativePath = new ArrayList<Expr>();
		Translator.getTranslatorByClassName(expression.getClass().getName())
				.explain(staticContext, expression, relativePath);

		return relativePath;
	}
}