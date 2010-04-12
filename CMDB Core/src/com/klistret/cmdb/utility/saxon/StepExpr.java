package com.klistret.cmdb.utility.saxon;

import java.util.List;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NamePool;

public class StepExpr<T extends Expr> extends Expr {

	private List<Expr> predicates;

	private AxisExpression axisExpression;

	public enum PrimaryNodeKind {
		Element, Attribute
	}

	protected StepExpr(AxisExpression expression, Configuration configuration) {
		super(expression, configuration);
		setAxisExpression(expression);
	}

	protected StepExpr(FilterExpression expression, Configuration configuration) {
		super(expression, configuration);

		/**
		 * Manageable filter expressions are limited to a controlling step being
		 * an axis expression.
		 */
		Expression controlling = expression.getControllingExpression();
		if (!controlling.getClass().getName().equals(
				AxisExpression.class.getName()))
			throw new IrresoluteException(
					String
							.format(
									"Controlling step [%s] in filter expression is not an axis expression",
									controlling));

		setAxisExpression((AxisExpression) controlling);

		/**
		 * Establish predicates
		 */
	}

	private void setAxisExpression(AxisExpression expression) {
		this.axisExpression = expression;

		/**
		 * Manageable axis expressions are limited to primary (element or
		 * attribute) nodes only otherwise an irresolute expression is cast.
		 * Notable that Saxon does not formulate expressions into an axis with
		 * predicates.
		 */
		if (getPrimaryNodeKind() == null || !isAbsolute() || getQName() == null)
			throw new IrresoluteException(
					String
							.format(
									"Axis expression [%s] is either neither not a primary node or is not an absolute step or the qname is null (likely a wildcard)",
									expression));
	}

	@Override
	public boolean equals(Object obj) {
		// TO-DO
		return false;
	}

	@Override
	public Type getType() {
		return Type.Path;
	}

	public QName getQName() {
		// Wild cards generate empty node tests
		if (axisExpression.getNodeTest() == null)
			return null;

		int fingerprint = axisExpression.getNodeTest().getFingerprint();

		// Finger print = -1 if the node test matches nodes of more than one
		// name
		if (fingerprint == -1)
			return null;

		String clarkName = configuration.getNamePool()
				.getClarkName(fingerprint);

		if (clarkName == null)
			return null;

		// URI, local name (suggested prefix is really saved internally)
		String[] parsedClarkName = NamePool.parseClarkName(clarkName);

		QName qname = new QName(parsedClarkName[0], parsedClarkName[1],
				configuration.getNamePool().suggestPrefixForURI(
						parsedClarkName[0]));
		return qname;
	}

	public List<Expr> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<Expr> predicates) {
		this.predicates = predicates;
	}

	public PrimaryNodeKind getPrimaryNodeKind() {
		switch (axisExpression.getAxis()) {
		case Axis.CHILD:
			return PrimaryNodeKind.Element;
		case Axis.ATTRIBUTE:
			return PrimaryNodeKind.Attribute;
		default:
			return null;
		}
	}

	public boolean isForward() {
		switch (axisExpression.getAxis()) {
		case Axis.CHILD:
			return true;
		case Axis.DESCENDANT:
			return true;
		case Axis.ATTRIBUTE:
			return true;
		case Axis.SELF:
			return true;
		case Axis.DESCENDANT_OR_SELF:
			return true;
		case Axis.FOLLOWING_SIBLING:
			return true;
		case Axis.FOLLOWING:
			return true;
		case Axis.NAMESPACE:
			return true;
		default:
			return false;
		}
	}

	public boolean isReverse() {
		switch (axisExpression.getAxis()) {
		case Axis.ANCESTOR:
			return true;
		case Axis.ANCESTOR_OR_SELF:
			return true;
		case Axis.PRECEDING:
			return true;
		case Axis.PRECEDING_SIBLING:
			return true;
		case Axis.PARENT:
			return true;
		default:
			return false;
		}
	}

	public boolean isAbsolute() {
		switch (axisExpression.getAxis()) {
		case Axis.CHILD:
			return true;
		case Axis.ATTRIBUTE:
			return true;
		case Axis.PARENT:
			return true;
		default:
			return false;
		}
	}

	public String toString() {
		return String
				.format(
						"step [%s], node kind [%s], qname [%s], forward [%b], absolute [%b]",
						expression.toString(), getPrimaryNodeKind(),
						getQName(), isForward(), isAbsolute());
	}
}
