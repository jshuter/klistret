package com.klistret.cmdb.utility.saxon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.sxpath.IndependentContext;

public class PathExpr {

	private IndependentContext staticContext;

	private String xpath;

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
			.compile("\\s?declare\\s+namespace\\s+(.+)\\s?=\\s?(.+)\\s?;");

	public PathExpr(String xpath) {
		this.xpath = xpath;

		staticContext = new IndependentContext(new Configuration());

		prolog(xpath);
	}

	private void prolog(String xpath) {
		/**
		 * Handle default element name space declarations
		 */
		Matcher demd = defaultElementNamespaceDeclaration.matcher(xpath);
		while (demd.find())
			staticContext.setDefaultElementNamespace(demd.group(2));

		/**
		 * Handle default function name space declarations
		 */
		Matcher dfmd = defaultFunctionNamespaceDeclaration.matcher(xpath);
		while (dfmd.find())
			staticContext.setDefaultFunctionNamespace(dfmd.group(2));

		/**
		 * Handle name space declarations
		 */
		Matcher nd = namespaceDeclaration.matcher(xpath);
		while (nd.find())
			staticContext.declareNamespace(nd.group(1), nd.group(2));
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
}
