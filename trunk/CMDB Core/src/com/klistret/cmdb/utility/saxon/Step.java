package com.klistret.cmdb.utility.saxon;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

/**
 * Root, step and irresolute Expr extend this abstract extension of Expr to
 * store XPath strings for each step, next step pointers, depth within the
 * relative path, and the relative path. This added information is not necessary
 * for general Expr objects.
 * 
 * @author Matthew Young
 * 
 */
public abstract class Step extends Expr {

	/**
	 * Applicable to root, step and irresolute expressions
	 */
	protected String xpath;

	/**
	 * Depth within the regular expression
	 */
	protected int depth;

	/**
	 * Next step in the regular expression
	 */
	protected Step next;

	/**
	 * Owning path expression
	 */
	protected PathExpression pathExpression;

	/**
	 * 
	 * @param expression
	 * @param configuration
	 */
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
	 * Set xpath for this step
	 * 
	 * @param xpath
	 */
	protected void setXPath(String xpath) {
		this.xpath = xpath;
	}

	/**
	 * Get depth within regular expression
	 * 
	 * @return
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * Set depth
	 * 
	 * @param depth
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Next step in the regular expression
	 * 
	 * @return Step
	 */
	public Step getNext() {
		return this.next;
	}

	/**
	 * Set next step
	 * 
	 * @param next
	 */
	public void setNext(Step next) {
		this.next = next;
	}

	/**
	 * Concatenation descending XPaths
	 * 
	 * @return
	 */
	public String getRemainingXPath() {
		return next == null ? null : next.getRemainingXPath() == null ? next
				.getXPath() : next.getXPath().concat("/").concat(
				next.getRemainingXPath());
	}

	/**
	 * Owning path expression
	 * 
	 * @return
	 */
	public PathExpression getPathExpression() {
		return this.pathExpression;
	}

	/**
	 * Set owning path expression
	 * 
	 * @param pathExpression
	 */
	public void setPathExpression(PathExpression pathExpression) {
		this.pathExpression = pathExpression;
	}

	/**
	 * QName associated with the step minus the filter
	 * 
	 * @return
	 */
	public abstract QName getQName();
}