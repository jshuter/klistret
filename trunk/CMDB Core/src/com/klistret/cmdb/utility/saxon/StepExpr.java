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
import java.util.List;

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

/**
 * Steps denote an axis step with predicates to filter the underlying sequence
 * of items. A step is either an element or attribute with forward direction
 * plus a valid QName.
 * 
 * @author Matthew Young
 * 
 */
public class StepExpr extends Step {

	private static final Logger logger = LoggerFactory
			.getLogger(StepExpr.class);

	private List<Expr> predicates = new ArrayList<Expr>();

	/**
	 * Underlying Axis expression
	 */
	private AxisExpression axisExpression;

	/**
	 * QName for step
	 */
	private QName qname;

	/**
	 * XPath 2.0 allows for only elements or attributes as primary nodes
	 */
	public enum PrimaryNodeKind {
		Element, Attribute
	}

	/**
	 * Passing a Saxon axis expression means there are no predicates
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected StepExpr(AxisExpression expression, Configuration configuration) {
		super(expression, configuration);
		setAxisExpression(expression);
	}

	/**
	 * Passing a Saxon filter expression means there are predicates plus an
	 * underlying axis expression
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected StepExpr(FilterExpression expression, Configuration configuration) {
		super(expression, configuration);

		/**
		 * Multiple predicates are allowed and according to the XPath 2.0
		 * specification are applied in order. Saxon just wraps predicates
		 * inside individual filter expressions with the context in the axis
		 * expression.
		 */
		Expression controlling = expression.getControllingExpression();
		while (controlling.getClass().getName().equals(
				FilterExpression.class.getName())) {
			logger.debug("Adding a predicate [depth: {}]", predicates.size());
			predicates.add(0, explainPredicate(((FilterExpression) controlling)
					.getFilter()));

			controlling = ((FilterExpression) controlling)
					.getControllingExpression();
		}

		if (!controlling.getClass().getName().equals(
				AxisExpression.class.getName())) {
			throw new IrresoluteException(
					String
							.format(
									"Controlling step [%s] in filter expression is not an axis expression",
									controlling));
		}

		setAxisExpression((AxisExpression) controlling);
		logger.debug("Adding the initial predicate [depth: {}]", predicates
				.size());
		predicates.add(0, explainPredicate(expression.getFilter()));
	}

	/**
	 * Sets the axis without predicate and generates the QName. QName identifies
	 * the node in a general manner based on a name test but not allowing for
	 * wildcard.
	 * 
	 * @param expression
	 */
	private void setAxisExpression(AxisExpression expression) {
		this.axisExpression = expression;

		// Wild cards generate empty node tests
		if (axisExpression.getNodeTest() == null)
			qname = null;

		int fingerprint = axisExpression.getNodeTest().getFingerprint();

		// Finger print = -1 if the node test matches nodes of more than one
		// name
		if (fingerprint == -1)
			throw new IrresoluteException(String
					.format("Axis expression [%s] has wildcard fingerprint",
							expression));

		String clarkName = configuration.getNamePool()
				.getClarkName(fingerprint);

		if (clarkName == null)
			throw new IrresoluteException(String
					.format("Axis expression [%s] has an unknown clarkname",
							expression));

		// URI, local name (suggested prefix is really saved internally)
		String[] parsedClarkName = NamePool.parseClarkName(clarkName);

		// prefix not used to determine equality
		String suggestedPrefix = configuration.getNamePool()
				.suggestPrefixForURI(parsedClarkName[0]);

		qname = new QName(parsedClarkName[0], parsedClarkName[1],
				suggestedPrefix);

		/**
		 * Manageable axis expressions are limited to primary (element or
		 * attribute) nodes only otherwise an irresolute expression is cast.
		 * Notable that Saxon does not formulate expressions into an axis with
		 * predicates.
		 */
		if (getPrimaryNodeKind() == null || !isAbsolute() || !isForward()
				|| getQName() == null) {
			throw new IrresoluteException(
					String
							.format(
									"Axis expression [%s] is either neither not a primary node or is not an absolute step with forward direction or the qname is null (likely a wildcard)",
									expression));
		}
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
		if (expression.getClass().getName().equals(
				BooleanExpression.class.getName())) {
			logger.debug("Predicate is a boolean expression");
			switch (((BooleanExpression) expression).getOperator()) {

			case Token.AND:
				AndExpr andExpr = new AndExpr((BooleanExpression) expression,
						configuration);

				for (Expression operand : ((BooleanExpression) expression)
						.getOperands())
					andExpr.addOperand(explainPredicate(operand));

				logger.debug("Resolved as an AndExpr predicate [{}]", andExpr);

				return andExpr;

			case Token.OR:
				OrExpr orExpr = new OrExpr((BooleanExpression) expression,
						configuration);

				for (Expression operand : ((BooleanExpression) expression)
						.getOperands())
					orExpr.addOperand(explainPredicate(operand));

				logger.debug("Resolved as an OrExpr predicate [{}]", orExpr);

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
			logger.debug("Predicate is a general comparison");

			return new ComparisonExpr((GeneralComparison) expression,
					configuration);
		}

		else if (expression.getClass().getName().equals(
				ValueComparison.class.getName())) {
			logger.debug("Predicate is a value comparison");

			return new ComparisonExpr((ValueComparison) expression,
					configuration);
		}

		else if (expression.getClass().getName().equals(
				TraceExpression.class.getName())) {
			logger.debug("Predicate is a trace or functional expression");

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
	 * Get QName
	 * 
	 * @return
	 */
	public QName getQName() {
		return qname;
	}

	/**
	 * Get Axis name (prefix:local part) without filter
	 * 
	 * @return
	 */
	public String getAxisName() {
		return String.format("%s:%s", qname.getPrefix(), qname.getLocalPart());
	}

	/**
	 * Returns predicate which may be null
	 * 
	 * @return Expr
	 */
	public List<Expr> getPredicates() {
		return predicates;
	}

	/**
	 * Has predicates
	 * 
	 * @return boolean
	 */
	public boolean hasPredicates() {
		if (predicates.size() == 0)
			return false;

		return true;
	}

	/**
	 * Valid underlying axis nodes are either an element or attribute (a
	 * namespace is possible but not allowed)
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
	 * An axis that only contains the context node or nodes that are after the
	 * context node in document order is a forward axis.
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
	 * An axis that only contains the context node or nodes that are before the
	 * context node in document order is a reverse axis.
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
						"type [%s], step [%s], node kind [%s], qname [%s], forward [%b], absolute [%b], predicate count [%d]",
						getType(), expression, getPrimaryNodeKind(),
						getQName(), isForward(), isAbsolute(), predicates
								.size());
	}
}
