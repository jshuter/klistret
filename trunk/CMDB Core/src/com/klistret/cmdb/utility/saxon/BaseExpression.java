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

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.trans.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

/**
 * TODO: Adjust String.format to StringBuilder to compensate for extra memory
 * usage: http://stackoverflow.com/questions/513600/should-i-use-javas
 * -string-format-if-performance-is-important
 * 
 * @author Matthew Young
 * 
 */
public abstract class BaseExpression {

	private static final Logger logger = LoggerFactory
			.getLogger(BaseExpression.class);

	/**
	 * Static context contains information prior to evaluation, usually name
	 * definitions
	 */
	protected IndependentContext staticContext;

	/**
	 * Original XPath statement including the prolog.
	 */
	protected String xpath;

	/**
	 * Offset within the XPath statement where the actual XPath clause starts
	 * without preceding declare clauses
	 */
	protected int prologOffset = 0;

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
	public static final Pattern defaultElementNamespaceDeclaration = Pattern
			.compile("\\s*declare\\s+default\\s+element\\s+namespace\\s+(\'|\")(((?!\\3).)*)\\3\\s*;");

	public static final Pattern defaultFunctionNamespaceDeclaration = Pattern
			.compile("\\s*declare\\s+default\\s+function\\s+namespace\\s+(\'|\")(((?!\\3).)*)\\3\\s*;");

	public static final Pattern namespaceDeclaration = Pattern
			.compile("\\s*declare\\s+namespace\\s+(((?!\\s*=\\s*).)*)\\s?=\\s?(\'|\")(((?!\\3).)*)\\3\\s*;");

	/**
	 * Mapping between XML columns (qname) and actual XML documents (type) is
	 * easier to handle as a declaration than adding xsi:type filters to the
	 * XPath.
	 */
	public static final String mappingStatement = "\\s?declare\\s+mapping\\s+(\\w+):(\\w+)\\s*=(\\w+):(\\w+)\\s*;";

	public static final Pattern mappingDeclaration = Pattern
			.compile(mappingStatement);

	/**
	 * Saxon Expression
	 */
	protected Expression expression;

	/**
	 * Default constructor
	 * 
	 * @param xpath
	 */
	public BaseExpression(String xpath) {
		logger.debug("Base expression constructed from XPath [{}]", xpath);

		this.xpath = xpath;
		this.staticContext = new IndependentContext(new Configuration());

		if (xpath == null)
			throw new ApplicationException("Passed XPath value is null");

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

			logger.debug(
					"Identified mapping declaration JTA property [{}], document type [{}]",
					key, value);

			if (md.end() > prologOffset)
				prologOffset = md.end();

			xpathLength = xpathLength + (md.end() - md.start());

			if (getNamespace(md.group(1)) == null) {
				throw new ApplicationException(
						String.format(
								"JTA property prefix [%s] has no cooresponding namespace declaration",
								md.group(1)));
			}

			if (getNamespace(md.group(3)) == null) {
				throw new ApplicationException(
						String.format(
								"Document type prefix [%s] has no cooresponding namespace declaration",
								md.group(3)));
			}

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
		 * Make Saxon representation using the start index to begin translation
		 * after the last declaration.
		 */
		try {
			/**
			 * Reusable XPathExpression (needed by plugins) means an extra
			 * compilation along side the ExpressionTool (generates a
			 * PathExpression)
			 */
			XPathEvaluator evaluator = new XPathEvaluator();
			evaluator.setStaticContext(staticContext);

			/**
			 * Generates a SlashExpression
			 */
			expression = ExpressionTool.make(xpath, staticContext,
					prologOffset, -1, 1, true);
		} catch (XPathException e) {
			throw new ApplicationException(
					String.format(
							"Unable to create Saxon expression from xpath [%s], start character [%d] after prolog (ie declarations)",
							xpath, prologOffset), e);
		}
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
	 * Get original XPath statement including prolog
	 * 
	 * @return XPath
	 */
	public String getXPath() {
		return this.xpath;
	}

	/**
	 * Return Saxon Expression
	 * 
	 * @return XPathExpression
	 */
	public Expression getExpression() {
		return this.expression;
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
	 * Get prolog without XPath directive
	 * 
	 * @return Prolog
	 */
	public String getProlog() {
		return this.xpath.substring(0, prologOffset);
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
}
