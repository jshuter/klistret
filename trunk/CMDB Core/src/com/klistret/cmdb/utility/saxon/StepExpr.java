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

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.sf.saxon.instruct.TraceExpression;

public class StepExpr extends Expr {

	private static final Logger logger = LoggerFactory
			.getLogger(StepExpr.class);

	/**
	 * Steps should declare a predicate list rather than a single predicate
	 * however the implementation would be treated as an AND operator thereby
	 * making it easier to ask end-users to distill multiple predicates into a
	 * single.
	 */
	private Expr predicate;

	/**
	 * Underlying Axis expression
	 */
	private AxisExpression axisExpression;

	/**
	 * XPath 2.0 allows for only elements or attributes as primary nodes
	 */
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
		predicate = explainPredicate(expression.getFilter());
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

	/**
	 * Recursive logic just like the explain Saxon method to build up a tree of
	 * logical expressions (or, and plus comparisons) otherwise the entire
	 * controlling expression is deemed irresolute.
	 * 
	 * @param expression
	 * @return
	 */
	private Expr explainPredicate(Expression expression) {
		logger.debug("Explaining predicate on expression [{}]", expression);

		if (expression.getClass().getName().equals(
				BooleanExpression.class.getName())) {
			switch (((BooleanExpression) expression).getOperator()) {

			case Token.AND:
				AndExpr andExpr = new AndExpr((BooleanExpression) expression,
						configuration);

				for (Expression operand : ((BooleanExpression) expression)
						.getOperands()) {
					andExpr.addOperand(explainPredicate(operand));
				}

				logger.debug("Return AndExpr predicate [{}]", andExpr);

				return andExpr;

			case Token.OR:
				OrExpr orExpr = new OrExpr((BooleanExpression) expression,
						configuration);

				for (Expression operand : ((BooleanExpression) expression)
						.getOperands()) {
					orExpr.addOperand(explainPredicate(operand));
				}

				logger.debug("Return OrExpr predicate [{}]", orExpr);

				return orExpr;

			default:
				throw new IrresoluteException(
						String
								.format(
										"Boolean expression [%s] must either be an AND or OR operation",
										expression));
			}
		}

		else if (expression.getClass().getName().equals(
				GeneralComparison.class.getName())) {
			return new ComparisonExpr((GeneralComparison) expression,
					configuration);
		}

		else if (expression.getClass().getName().equals(
				ValueComparison.class.getName())) {
			return new ComparisonExpr((ValueComparison) expression,
					configuration);
		}

		else if (expression.getClass().getName().equals(
				TraceExpression.class.getName())) {
			return new ComparisonExpr((TraceExpression) expression,
					configuration);
		}

		else {
			throw new IrresoluteException(
					String
							.format(
									"Operand [%s] not a boolean, general or value logical expression",
									expression));
		}
	}

	@Override
	public Type getType() {
		return Type.Step;
	}

	/**
	 * QName identifies the node in a general manner based on a name test but
	 * not allowing for wildcard.
	 * 
	 * @return
	 */
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

		// prefix not used to determine equality
		String suggestedPrefix = configuration.getNamePool()
				.suggestPrefixForURI(parsedClarkName[0]);

		QName qname = new QName(parsedClarkName[0], parsedClarkName[1],
				suggestedPrefix);
		return qname;
	}

	/**
	 * Returns predicate which may be null
	 * 
	 * @return Expr
	 */
	public Expr getPredicate() {
		return predicate;
	}

	/**
	 * Is predicate
	 * 
	 * @return boolean
	 */
	public boolean hasPredicate() {
		if (predicate == null)
			return false;

		return true;
	}

	/**
	 * Valid underlying axis nodes are either an element or attribute
	 * 
	 * @return PrimaryNodeKind
	 */
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

	/**
	 * An axis that only ever contains the context node or nodes that are after
	 * the context node in document order is a forward axis.
	 * 
	 * @return boolean
	 */
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

	/**
	 * An axis that only ever contains the context node or nodes that are before
	 * the context node in document order is a reverse axis.
	 * 
	 * @return boolean
	 */
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

	/**
	 * Axis direction is a single step rather than relative
	 * 
	 * @return boolean
	 */
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
						"type [%s], step [%s], node kind [%s], qname [%s], forward [%b], absolute [%b], predicate [%s]",
						getType(), expression, getPrimaryNodeKind(),
						getQName(), isForward(), isAbsolute(), predicate);
	}
}
