package com.klistret.cmdb.utility.saxon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.klistret.cmdb.exception.ApplicationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.XPathException;

public class PathExpression {

	private IndependentContext staticContext;

	private String xpath;

	private Expression expression;

	private List<Expr> relativePath;

	/**
	 * Statics expressions are formed after the semantics defined at the
	 * following site http://www.w3.org/TR/xquery-semantics and the URI literal
	 * is intentionally left as a wild-card because end-users will likely
	 * defined invalid URIs.
	 */
	static final Pattern defaultElementNamespaceDeclaration = Pattern
			.compile("\\s?declare\\s+default\\s+element\\s+namespace\\s+(\'|\")(.+)(\'|\")\\s?;");

	static final Pattern defaultFunctionNamespaceDeclaration = Pattern
			.compile("\\s?declare\\s+default\\s+function\\s+namespace\\s+(\'|\")(.+)(\'|\")\\s?;");

	static final Pattern namespaceDeclaration = Pattern
			.compile("\\s?declare\\s+namespace\\s+(.+)\\s?=\\s?(\'|\")(.+)(\'|\")\\s?;");

	/**
	 * Constructor finds through regular expressions the default
	 * element/function namespaces as well all declared namespaces then creates
	 * a Saxon expression with the ExpressionTool and evaluates the expression
	 * will explain calls.
	 * 
	 * @param xpath
	 */
	public PathExpression(String xpath) {
		this.xpath = xpath;
		this.staticContext = new IndependentContext(new Configuration());

		if (xpath == null)
			throw new ApplicationException(
					"Unable to resolve null value for xpath");

		int start = 0;

		/**
		 * Handle default element name space declarations
		 */
		Matcher demd = defaultElementNamespaceDeclaration.matcher(xpath);
		while (demd.find()) {
			if (demd.end() > start)
				start = demd.end();
			staticContext.setDefaultElementNamespace(demd.group(2));
		}

		/**
		 * Handle default function name space declarations
		 */
		Matcher dfmd = defaultFunctionNamespaceDeclaration.matcher(xpath);
		while (dfmd.find()) {
			if (dfmd.end() > start)
				start = dfmd.end();
			staticContext.setDefaultFunctionNamespace(dfmd.group(2));
		}

		/**
		 * Handle name space declarations
		 */
		Matcher nd = namespaceDeclaration.matcher(xpath);
		while (nd.find()) {
			if (nd.end() > start)
				start = nd.end();
			staticContext.declareNamespace(nd.group(1), nd.group(3));
		}

		/**
		 * Make Saxon expression
		 */
		try {
			expression = ExpressionTool.make(xpath, staticContext, start, -1,
					1, true);

			relativePath = new ArrayList<Expr>();

			explain(expression);
		} catch (XPathException e) {
			throw new ApplicationException(
					String
							.format(
									"Unable to make Saxon expression from xpath [%s], start character [%d]",
									xpath, start));
		}
	}

	/**
	 * Nearly identical logic defined in the explain methods of the individual
	 * Saxon expression class extensions
	 * 
	 * @param expression
	 */
	private void explain(Expression expression) {
		/**
		 * Slash expressions always consist of a controlling and controlled
		 * expression that can be further explained.
		 */
		if (expression.getClass().getName().equals(
				SlashExpression.class.getName())) {
			explain(((SlashExpression) expression).getControllingExpression());
			explain(((SlashExpression) expression).getControlledExpression());
		}

		/**
		 * Root expression ("/") automatically end up as the first step
		 */
		else if (expression.getClass().getName().equals(
				RootExpression.class.getName())) {
			relativePath.add(new RootExpr((RootExpression) expression,
					staticContext.getConfiguration()));
		}

		/**
		 * Axis expressions in Saxon have no predicates oddly enough and those
		 * that can't be processed into a simple step are translated into
		 * irresolute expressions
		 */
		else if (expression.getClass().getName().equals(
				AxisExpression.class.getName())) {
			try {
				relativePath.add(new StepExpr(
						(AxisExpression) expression, staticContext
								.getConfiguration()));
			} catch (IrresoluteException e) {
				relativePath.add(new IrresoluteExpr<Expr>(expression,
						staticContext.getConfiguration()));
			}
		}

		/**
		 * Same as axis expressions but Saxon filters also for predicates
		 */
		else if (expression.getClass().getName().equals(
				FilterExpression.class.getName())) {
			try {
				relativePath.add(new StepExpr(
						(FilterExpression) expression, staticContext
								.getConfiguration()));
			} catch (IrresoluteException e) {
				relativePath.add(new IrresoluteExpr<Expr>(expression,
						staticContext.getConfiguration()));
			}
		}

		/**
		 * Default is to capture unresolved expressions as irresolute
		 */
		else {
			relativePath.add(new IrresoluteExpr<Expr>(expression, staticContext
					.getConfiguration()));
		}
	}

	public String getXPath() {
		return this.xpath;
	}

	public String getDefaultElementNamespace() {
		return staticContext.getDefaultElementNamespace();
	}

	public String getDefaultFunctionNamespace() {
		return staticContext.getDefaultFunctionNamespace();
	}

	public Iterator<String> getPrefixes() {
		return staticContext.getNamespaceResolver().iteratePrefixes();
	}

	public String getNamespace(String prefix) {
		return staticContext.getNamespaceResolver().getURIForPrefix(prefix,
				false);
	}

	public List<Expr> getRelativePath() {
		return relativePath;
	}
}
