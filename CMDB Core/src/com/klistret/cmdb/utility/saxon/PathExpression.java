/**
 ** This file is part of Klistret. Klistret is free software: you can
 ** redistribute it and/or modify it under the terms of the GNU General
 ** Public License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.

 ** Klistret is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 ** General Public License for more details. You should have received a
 ** copy of the GNU General Public License along with Klistret. If not,
 ** see <http://www.gnu.org/licenses/>
 */

package com.klistret.cmdb.utility.saxon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory
			.getLogger(PathExpression.class);

	/**
	 * Static context contains information prior to evaluation, usually name
	 * definitions
	 */
	private IndependentContext staticContext;

	/**
	 * Original XPath statement
	 */
	private String xpath;

	/**
	 * Offset within the XPath statement where the actual XPath clause starts
	 * without preceding declare clauses
	 */
	private int prologOffset = 0;

	/**
	 * Saxon representation of the XPath statement
	 */
	private Expression expression;

	/**
	 * Project specific representation of relative paths [StepExpr (("/" | "//")
	 * StepExpr)*]
	 */
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
	 * Regular expression for delimiting path (slash) expressions (currently
	 * only works with single quotes)
	 */
	static final String slashDelimitor = "/(?=([^']*'[^']*')*(?![^']*'))";

	/**
	 * Constructor finds through regular expressions the default
	 * element/function namespaces as well all declared namespaces then creates
	 * a Saxon expression with the ExpressionTool and evaluates the expression
	 * will explain calls.
	 * 
	 * @param xpath
	 */
	public PathExpression(String xpath) {
		logger.debug("Path expression constructed from XPath [{}]", xpath);

		this.xpath = xpath;
		this.staticContext = new IndependentContext(new Configuration());

		if (xpath == null)
			throw new ApplicationException("Unable to resolve xpath as null");

		/**
		 * Handle default element name space declarations
		 */
		Matcher demd = defaultElementNamespaceDeclaration.matcher(xpath);
		while (demd.find()) {
			logger.debug(
					"Identified default element namespace declaration [{}]",
					demd.group(2));

			if (demd.end() > prologOffset)
				prologOffset = demd.end();
			staticContext.setDefaultElementNamespace(demd.group(2));
		}

		/**
		 * Handle default function name space declarations
		 */
		Matcher dfmd = defaultFunctionNamespaceDeclaration.matcher(xpath);
		while (dfmd.find()) {
			logger.debug(
					"Identified default function namespace declaration [{}]",
					dfmd.group(2));

			if (dfmd.end() > prologOffset)
				prologOffset = dfmd.end();
			staticContext.setDefaultFunctionNamespace(dfmd.group(2));
		}

		/**
		 * Handle name space declarations
		 */
		Matcher nd = namespaceDeclaration.matcher(xpath);
		while (nd.find()) {
			logger.debug(
					"Identified namespace declaration [prefix: {}, uri: {}]",
					nd.group(1), nd.group(3));

			if (nd.end() > prologOffset)
				prologOffset = nd.end();
			staticContext.declareNamespace(nd.group(1), nd.group(3));
		}

		/**
		 * Make Saxon representation using the start index to begin translation
		 * after the last declaration.
		 */
		try {
			expression = ExpressionTool.make(xpath, staticContext,
					prologOffset, -1, 1, true);

			relativePath = new ArrayList<Expr>();

			explain(expression);
		} catch (XPathException e) {
			throw new ApplicationException(
					String
							.format(
									"Unable to make Saxon expression from xpath [%s], start character [%d]",
									xpath, prologOffset), e);
		} catch (ClassCastException e) {
			throw new ApplicationException(
					String
							.format(
									"Unable to make Saxon expression from xpath [%s], start character [%d]",
									xpath, prologOffset), e);
		}
	}

	/**
	 * Nearly identical logic defined in the explain methods of the individual
	 * Saxon expression class extensions
	 * 
	 * @param expression
	 */
	private void explain(Expression expression) {
		try {
			/**
			 * Slash expressions always consist of a controlling and controlled
			 * expression that can be further explained.
			 */
			if (expression.getClass().getName().equals(
					SlashExpression.class.getName())) {
				explain(((SlashExpression) expression)
						.getControllingExpression());
				explain(((SlashExpression) expression)
						.getControlledExpression());
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
			 * Axis expressions in Saxon have no predicates oddly enough and
			 * those that can't be processed into a simple step are translated
			 * into irresolute expressions
			 */
			else if (expression.getClass().getName().equals(
					AxisExpression.class.getName())) {
				StepExpr step = new StepExpr((AxisExpression) expression,
						staticContext.getConfiguration());

				step.setXPath(getSteps()[relativePath.size()]);
				relativePath.add(step);
			}

			/**
			 * Same as axis expressions but Saxon filters also for predicates
			 */
			else if (expression.getClass().getName().equals(
					FilterExpression.class.getName())) {
				StepExpr step = new StepExpr((FilterExpression) expression,
						staticContext.getConfiguration());

				step.setXPath(getSteps()[relativePath.size()]);
				relativePath.add(step);
			}

			/**
			 * Default is to capture unresolved expressions as irresolute
			 * exception
			 */
			else {
				throw new IrresoluteException();
			}
		} catch (IrresoluteException e) {
			IrresoluteExpr irresolute = new IrresoluteExpr(expression,
					staticContext.getConfiguration());

			irresolute.setXPath(getSteps()[relativePath.size()]);
			relativePath.add(irresolute);
		}
	}

	public String getXPath() {
		return this.xpath;
	}

	public String getXPathWithoutProlog() {
		return this.xpath.substring(prologOffset);
	}

	public String[] getSteps() {
		return getXPathWithoutProlog().split(slashDelimitor);
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
