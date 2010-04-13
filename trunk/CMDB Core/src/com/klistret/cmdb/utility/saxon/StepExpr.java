package com.klistret.cmdb.utility.saxon;

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.expr.Token;

public class StepExpr<T extends Expr> extends Expr {

	private Expr predicate;

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
		 * an axis expression which means the predicate may not be a predicate
		 * list since Saxon builds the controlling step as a filter. Again, the
		 * goal here is to allow for simple relative paths with a single, binary
		 * predicate.
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
		 * Establish predicates looking for Saxon boolean, general, and value
		 * expressions
		 */
		Expression filter = expression.getFilter();
		if (filter.getClass().getName().equals(
				BooleanExpression.class.getName())) {
			switch (((BooleanExpression) filter).getOperator()) {
			case Token.AND:
				predicate = new AndExpr((BooleanExpression) filter,
						configuration);
				break;
			case Token.OR:
				predicate = new OrExpr((BooleanExpression) filter,
						configuration);
				break;
			default:
				throw new IrresoluteException(
						String
								.format(
										"Boolean expression [%s] must either be an AND or OR operation",
										filter));
			}
		}

		else if (filter.getClass().getName().equals(
				GeneralComparison.class.getName())) {
			predicate = new ComparisonExpr(filter, configuration);
		}

		else if (filter.getClass().getName().equals(
				ValueComparison.class.getName())) {
			predicate = new ComparisonExpr(filter, configuration);
		}

		else {
			throw new IrresoluteException(
					String
							.format(
									"Filter expression [%s] is not a boolean, general or value logical expression",
									filter));
		}
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

	public Expr getPredicate() {
		return predicate;
	}

	public void setPredicate(Expr predicate) {
		this.predicate = predicate;
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
						"step [%s], node kind [%s], qname [%s], forward [%b], absolute [%b], predicate [%s]",
						expression.toString(), getPrimaryNodeKind(),
						getQName(), isForward(), isAbsolute(), predicate);
	}
}
