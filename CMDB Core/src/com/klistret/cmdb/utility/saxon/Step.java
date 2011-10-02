package com.klistret.cmdb.utility.saxon;

import javax.xml.namespace.QName;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.Configuration;

/**
 * Step and Irresolute Expr extend this abstract extension of Expr to generate
 * XPath strings for each step, next step pointers, depth within the relative
 * path, and the relative path. This added information is not necessary for
 * general Expr objects.
 * 
 * @author Matthew Young
 * 
 */
public abstract class Step implements Expr {

	/**
	 * Saxon expression
	 */
	protected Expression expression;

	/**
	 * Saxon configuration
	 */
	protected Configuration configuration;

	/**
	 * Depth within the regular expression
	 */
	protected int depth;

	/**
	 * Next step in the regular expression
	 */
	protected Step next;

	/**
	 * Previous step in the regular expression
	 */
	protected Step previous;

	/**
	 * Owning relative path expression
	 */
	protected RelativePathExpr relativePathExpr;

	/**
	 * 
	 * @param expression
	 * @param configuration
	 */
	public Step(Expression expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;
	}

	/**
	 * Return Saxon expression
	 */
	public Expression getExpression() {
		return this.expression;
	}

	/**
	 * Return Saxon configuration
	 */
	public Configuration getConfiguration() {
		return this.configuration;
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
	 * Previous step in the regular expression
	 * 
	 * @return Step
	 */
	public Step getPrevious() {
		return this.previous;
	}

	/**
	 * Set previous step
	 * 
	 * @param next
	 */
	public void setPrevious(Step previous) {
		this.previous = previous;
	}

	/**
	 * Concatenation descending XPaths
	 * 
	 * @return
	 */
	public String getDescendingXPath() {
		return next == null ? null : next.getDescendingXPath() == null ? next
				.getXPath() : next.getXPath().concat("/")
				.concat(next.getDescendingXPath());
	}

	/**
	 * Owning path expression
	 * 
	 * @return
	 */
	public RelativePathExpr getRelativePath() {
		return this.relativePathExpr;
	}

	/**
	 * Set owning path expression
	 * 
	 * @param pathExpression
	 */
	public void setRelativePath(RelativePathExpr relativePathExpr) {
		this.relativePathExpr = relativePathExpr;
	}

	/**
	 * QName associated with the step minus the filter
	 * 
	 * @return
	 */
	public abstract QName getQName();

}
