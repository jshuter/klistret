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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

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

/**
 * The basic idea is to only handle relative path expressions as defined by the
 * XPath 2.0 specification (http://www.w3.org/TR/xpath20/#id-path-expressions).
 * Restricting the acceptable expressions to a list of steps (ie a slash
 * expression in Saxon terms) maps XPaths (not XQuerys) to JPA mappings. The JPA
 * structure is generally a combination of step with or without predicates. The
 * XPath simplification stops once a XML column/property is reached whereby even
 * other expressions than steps are acceptable (denoted as irresolute).
 * 
 * @author Matthew Young
 * 
 */
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
	 * XPaths split by slash regular expression
	 */
	private String[] xpathSplit;

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
	 * Root expressions are optional to relative expressions
	 */
	private boolean hasRoot = false;

	/**
	 * Place holder for namespaces
	 */
	private List<String> namespaces = new ArrayList<String>();

	/**
	 * Place holder for type mappings
	 */
	private Map<String, String> typeMappings = new HashMap<String, String>();

	/**
	 * Static expressions are formed after the semantics defined at the
	 * following site http://www.w3.org/TR/xquery-semantics and the URI literal
	 * is intentionally left as a wild-card because end-users will likely
	 * defined invalid URIs.
	 */
	static final Pattern defaultElementNamespaceDeclaration = Pattern
			.compile("\\s*declare\\s+default\\s+element\\s+namespace\\s+(\'|\")(((?!\\3).)*)\\3\\s*;");

	static final Pattern defaultFunctionNamespaceDeclaration = Pattern
			.compile("\\s*declare\\s+default\\s+function\\s+namespace\\s+(\'|\")(((?!\\3).)*)\\3\\s*;");

	static final Pattern namespaceDeclaration = Pattern
			.compile("\\s*declare\\s+namespace\\s+(((?!\\s*=\\s*).)*)\\s?=\\s?(\'|\")(((?!\\3).)*)\\3\\s*;");

	/**
	 * Mapping between XML columns (qname) and actual XML documents (type) is
	 * easier to handle as a declaration than adding xsi:type filters to the
	 * XPath.
	 */
	static final Pattern mappingDeclaration = Pattern
			.compile("\\s?declare\\s+mapping\\s+(\\w+):(\\w+)\\s*=(\\w+):(\\w+)\\s*;");

	/**
	 * Regular expression for delimiting path (slash) expressions (look ahead
	 * for single as well double quotes)
	 */
	static final String slashDelimitor = "/(?=([^']*'[^']*')*(?![^']*'))(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))";

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
		 * Length of captured XPath statement (known declarations and data after
		 * the prolog)
		 */
		int xpathLength = 0;

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

			xpathLength = xpathLength + (demd.end() - demd.start());

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

			xpathLength = xpathLength + (dfmd.end() - dfmd.start());

			staticContext.setDefaultFunctionNamespace(dfmd.group(2));
		}

		/**
		 * Handle name space declarations
		 */
		Matcher nd = namespaceDeclaration.matcher(xpath);
		while (nd.find()) {
			logger.debug(
					"Identified namespace declaration [prefix: {}, uri: {}]",
					nd.group(1), nd.group(4));

			if (nd.end() > prologOffset)
				prologOffset = nd.end();

			xpathLength = xpathLength + (nd.end() - nd.start());

			staticContext.declareNamespace(nd.group(1), nd.group(4));
			namespaces.add(xpath.substring(nd.start(), nd.end()).trim());
		}

		/**
		 * Handle mapping declarations
		 */
		Matcher md = mappingDeclaration.matcher(xpath);
		while (md.find()) {
			String key = String.format("%s:%s", md.group(1), md.group(2));
			String value = String.format("%s:%s", md.group(3), md.group(4));

			logger
					.debug(
							"Identified mapping declaration JTA property [{}], document type [{}]",
							key, value);

			if (md.end() > prologOffset)
				prologOffset = md.end();

			xpathLength = xpathLength + (md.end() - md.start());

			if (getNamespace(md.group(1)) == null)
				throw new ApplicationException(
						String
								.format(
										"JTA property prefix [%s] has no cooresponding namespace declaration",
										md.group(1)));

			if (getNamespace(md.group(3)) == null)
				throw new ApplicationException(
						String
								.format(
										"Document type prefix [%s] has no cooresponding namespace declaration",
										md.group(3)));

			typeMappings.put(key, value);
		}

		/**
		 * Capture information
		 */
		xpathLength = xpathLength + getXPathWithoutProlog().length();
		logger.debug("Captured length [{}] and actual XPath length [{}]",
				xpathLength, xpath.length());
		if (xpathLength < xpath.length())
			logger.warn("Uncaptured text prior to declare statements");

		/**
		 * Divide the xpath into individual xpaths delimited by slash
		 */
		xpathSplit = getXPathWithoutProlog().split(slashDelimitor);

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
			logger
					.error(
							"XPathException [{}] creating Saxon expression from xpath [{}]",
							e.getMessage(), xpath);
			throw new ApplicationException(
					String
							.format(
									"Unable to create Saxon expression from xpath [%s], start character [%d] after prolog (ie declarations)",
									xpath, prologOffset), e);
		} catch (ClassCastException e) {
			logger
					.error(
							"ClassCastException [{}] creating Saxon expression from xpath [{}]",
							e.getMessage(), xpath);
			throw new ApplicationException(
					String
							.format(
									"Unable to create Saxon expression from xpath [%s], start character [%d] after prolog (ie declarations)",
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
				hasRoot = true;
			}

			/**
			 * Axis expressions in Saxon have no predicates oddly enough and
			 * those that can't be processed into a simple step are translated
			 * into irresolute expressions
			 */
			else if (expression.getClass().getName().equals(
					AxisExpression.class.getName())) {
				relativePath.add(new StepExpr((AxisExpression) expression,
						staticContext.getConfiguration()));
			}

			/**
			 * Same as axis expressions but Saxon filters also for predicates
			 */
			else if (expression.getClass().getName().equals(
					FilterExpression.class.getName())) {
				relativePath.add(new StepExpr((FilterExpression) expression,
						staticContext.getConfiguration()));
			}

			/**
			 * Default is to capture unresolved expressions as irresolute
			 * exception
			 */
			else {
				throw new IrresoluteException();
			}
		} catch (IrresoluteException e) {
			relativePath.add(new IrresoluteExpr(expression, staticContext
					.getConfiguration()));
		}

		/**
		 * Setup step information (ie only types of root, step or irresolute)
		 */
		int depth = relativePath.size() - 1;
		if (depth >= 0 && depth < xpathSplit.length) {
			Step step = ((Step) relativePath.get(depth));

			step.setPathExpression(this);
			step.setXPath(depth == 0 ? "/" : xpathSplit[depth]);
			step.setDepth(depth);

			if (depth != 0)
				((Step) relativePath.get(depth - 1)).setNext(step);
		}
	}

	/**
	 * Get original XPath statement including prolog
	 * 
	 * @return XPath
	 */
	public String getXPath() {
		return this.xpath;
	}

	/**
	 * Get original XPath statement without prolog
	 * 
	 * @return XPath without prolog
	 */
	public String getXPathWithoutProlog() {
		return this.xpath.substring(prologOffset);
	}

	/**
	 * Get default element namespace
	 * 
	 * @return Default element namespace
	 */
	public String getDefaultElementNamespace() {
		return staticContext.getDefaultElementNamespace();
	}

	/**
	 * Get default function namespace
	 * 
	 * @return Default function namespace
	 */
	public String getDefaultFunctionNamespace() {
		return staticContext.getDefaultFunctionNamespace();
	}

	/**
	 * Every namespace declaration has a prefix
	 * 
	 * @return Namespace prefixes
	 */
	public Iterator<String> getPrefixes() {
		return staticContext.getNamespaceResolver().iteratePrefixes();
	}

	/**
	 * Return namespace by prefix
	 * 
	 * @param prefix
	 * @return Namespace
	 */
	public String getNamespace(String prefix) {
		return staticContext.getNamespaceResolver().getURIForPrefix(prefix,
				false);
	}

	/**
	 * Entire namespace declaration is returned
	 * 
	 * @return List of namespaces
	 */
	public List<String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Mapping between JTA property names and XML document types (key/value
	 * composed of prefix:local name pairs)
	 * 
	 * @return Map
	 */
	public Map<String, String> getTypeMappings() {
		return typeMappings;
	}

	/**
	 * Relative paths are a list of steps (ie root, step, or irresolute)
	 * 
	 * @return Relative path expression
	 */
	public List<Expr> getRelativePath() {
		return relativePath;
	}

	/**
	 * Get a step by index within the relative path
	 * 
	 * @param index
	 * @return Expression
	 */
	public Expr getExpr(int index) {
		return relativePath.get(index);
	}

	/**
	 * Get QName for a particular step expression in the relative path (null for
	 * root or irresolute)
	 * 
	 * @param index
	 * @return QName
	 */
	public QName getQName(int index) {
		if (getExpr(index).getType() == Expr.Type.Step)
			return ((StepExpr) getExpr(index)).getQName();

		return null;
	}

	/**
	 * Get xpath for a particular expression
	 * 
	 * @param index
	 * @return XPath
	 */
	public String getXPath(int index) {
		return xpathSplit[index];
	}

	/**
	 * Existence of a root expression within the relative path
	 * 
	 * @return boolean
	 */
	public boolean hasRoot() {
		return this.hasRoot;
	}

	/**
	 * Get number of expressions within the relative path
	 * 
	 * @return Size of the relative path
	 */
	public int getDepth() {
		return relativePath.size();
	}
}
