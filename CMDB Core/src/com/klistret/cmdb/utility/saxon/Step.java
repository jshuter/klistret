package com.klistret.cmdb.utility.saxon;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

public abstract class Step extends Expr {

	/**
	 * Applicable to root, step and irresolute expressions
	 */
	protected String xpath;

	protected int depth;

	public Step(Expression expression, Configuration configuration) {
		super(expression, configuration);
	}

	/**
	 * Get xpath strig representation
	 * 
	 * @return
	 */
	public String getXPath() {
		return this.xpath;
	}

	/**
	 * Internal use only
	 * 
	 * @param xpath
	 */
	protected void setXPath(String xpath) {
		this.xpath = xpath;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public abstract QName getQName();
}
