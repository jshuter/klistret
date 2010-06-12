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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.Token;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.instruct.TraceExpression;

/**
 * Comparison expressions allow two values to be compared. XPath provides three
 * kinds of comparison expressions, called value comparisons, general
 * comparisons, and node comparisons. The last comparison type is excluded from
 * the framework and the general/value are restricted to 2 operands with the
 * first being a step without predicate. Otherwise the expression will be
 * captured as a irresolute and depending on where in the JTA mapping the axis
 * appears the irresolute may be acceptable but not for JTA entities.
 * 
 * Functions which have a boolean return type are also encapsulated in this
 * class.
 * 
 * @author Matthew Young
 * 
 */
public class ComparisonExpr extends LogicalExpr<Expr> {

	private static final Logger logger = LoggerFactory
			.getLogger(ComparisonExpr.class);

	public enum Operator {
		ValueEquals, ValueNotEquals, ValueLessThan, ValueLessThanOrEquals, ValueGreaterThan, ValueGreaterThanOrEquals, GeneralEquals, GeneralNotEquals, GeneralLessThan, GeneralLessThanOrEquals, GeneralGreaterThan, GeneralGreaterThanOrEquals, Empty, Exists, Matches
	};

	private Operator operator;

	private Boolean functional = false;

	/**
	 * General comparisons are existentially quantified comparisons that may be
	 * applied to operand sequences of any length. The result of a general
	 * comparison that does not raise an error is always true or false.
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected ComparisonExpr(GeneralComparison expression,
			Configuration configuration) {
		super(expression, configuration);

		switch (expression.getOperator()) {

		case (Token.EQUALS):
			operator = Operator.GeneralEquals;
			break;

		case (Token.NE):
			operator = Operator.GeneralNotEquals;
			break;

		case (Token.LT):
			operator = Operator.GeneralLessThan;
			break;

		case (Token.LE):
			operator = Operator.GeneralLessThanOrEquals;
			break;

		case (Token.GT):
			operator = Operator.GeneralGreaterThan;
			break;

		case (Token.GE):
			operator = Operator.GeneralGreaterThanOrEquals;
			break;

		default:
			logger.debug(
					"General expression [{}] using unsupported operator [{}]",
					expression, expression.getOperator());
			throw new IrresoluteException(String.format(
					"General expression [%s] using unsupported operator [%d]",
					expression, expression.getOperator()));
		}

		logger.debug("General operator [{}] prior to explaining operands",
				operator);
		explainGeneralOrValueOperands(expression.getOperands());
	}

	/**
	 * Value comparisons are used for comparing single values.
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected ComparisonExpr(ValueComparison expression,
			Configuration configuration) {
		super(expression, configuration);

		switch (expression.getOperator()) {

		case (Token.FEQ):
			operator = Operator.ValueEquals;
			break;

		case (Token.FNE):
			operator = Operator.ValueNotEquals;
			break;

		case (Token.FLT):
			operator = Operator.ValueLessThan;
			break;

		case (Token.FLE):
			operator = Operator.ValueLessThanOrEquals;
			break;

		case (Token.FGT):
			operator = Operator.ValueGreaterThan;
			break;

		case (Token.FGE):
			operator = Operator.ValueGreaterThanOrEquals;
			break;

		default:
			logger.debug(
					"Value expression [{}] using unsupported operator [{}]",
					expression, expression.getOperator());
			throw new IrresoluteException(String.format(
					"Value expression [%s] using unsupported operator [%d]",
					expression, expression.getOperator()));
		}

		logger.debug("Value operator [{}] prior to explaining operands",
				operator);
		explainGeneralOrValueOperands(expression.getOperands());
	}

	/**
	 * Function with known boolean return types are allowed as comparisons.
	 * Unfortunately the freeware version of Saxon does not provide the
	 * FunctionType class to check the return type.
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected ComparisonExpr(TraceExpression expression,
			Configuration configuration) {
		super(expression, configuration);

		functional = true;

		/**
		 * Get a name identifying the object of the expression, for example a
		 * function name, template name, variable name, key name, element name,
		 * etc.
		 */
		String functionName = expression.getObjectName().getLocalName();

		/**
		 * Property 'expression' keys the function object
		 */
		FunctionCall function = (FunctionCall) expression
				.getProperty("expression");

		if (functionName.equals("empty"))
			operator = Operator.Empty;

		else if (functionName.equals("exists"))
			operator = Operator.Exists;

		else if (functionName.equals("matches"))
			operator = Operator.Matches;

		else {
			logger.debug(
					"Trace expression [{}] using unsupported function [{}]",
					expression, functionName);
			throw new IrresoluteException(String.format(
					"Trace expression [%s] using unsupported function [%s]",
					expression, functionName));
		}

		logger.debug("Function [{}] prior to explaining arguments",
				functionName);
		explainFunctionOperands(function.getArguments());
	}

	/**
	 * Controll that the number of expression is 2 with the right being a step
	 * with predicates and left being a literal.
	 * 
	 * @param operands
	 */
	private void explainGeneralOrValueOperands(Expression[] operands) {
		if (operands.length != 2) {
			logger
					.debug(
							"General/Value comparisons are limited to 2 operands only not [{}]",
							operands.length);
			throw new IrresoluteException(
					String
							.format(
									"General/Value comparisons are limited to 2 operands only not [%d]",
									operands.length));
		}

		Expression right = operands[0];
		if (!(right.getClass().getName().equals(AxisExpression.class.getName()))) {
			logger
					.debug(
							"General/Value comparisons require the right operand to be an axis not Saxon expression [{}]",
							right.getClass().getName());
			throw new IrresoluteException(
					String
							.format(
									"General/Value comparisons require the right operand to be an axis not Saxon expression [%s]",
									right.getClass().getName()));
		}
		addOperand(new StepExpr((AxisExpression) right, configuration));

		Expression left = operands[1];
		if (!(left instanceof Literal)) {
			logger
					.debug(
							"General/Value comparisons require the left operand to be a literal not Saxon expression [{}]",
							left.getClass().getName());
			throw new IrresoluteException(
					String
							.format(
									"General/Value comparisons require the left operand to be a literal not Saxon expression [%s]",
									left.getClass().getName()));
		}

		if (left.getClass().getName().equals(Literal.class.getName()))
			addOperand(new LiteralExpr((Literal) left, configuration));

		if (left.getClass().getName().equals(StringLiteral.class.getName()))
			addOperand(new LiteralExpr((StringLiteral) left, configuration));
	}

	/**
	 * Control that the first operand is a step without predicates and the
	 * remaining are literals.
	 * 
	 * @param operands
	 */
	private void explainFunctionOperands(Expression[] operands) {
		if (operands.length < 1) {
			logger
					.debug(
							"Functional comparisons must have at least one operand not [{}]",
							operands.length);
			throw new IrresoluteException(
					String
							.format(
									"Functional comparisons must have at least one operand not [%d]",
									operands.length));
		}

		Expression right = operands[0];
		if (!(right.getClass().getName().equals(AxisExpression.class.getName()))) {
			logger
					.error(
							"Function comparisons require the right most operand to be an axis not Saxon expression [{}]",
							right.getClass().getName());
			throw new IrresoluteException(
					String
							.format(
									"Function comparisons require the right most operand to be an axis not Saxon expression [%s]",
									right.getClass().getName()));
		}
		addOperand(new StepExpr((AxisExpression) right, configuration));

		for (int index = 1; index < operands.length; index++) {
			Expression operand = operands[index];

			if (operand.getClass().getName().equals(Literal.class.getName())) {
				addOperand(new LiteralExpr((Literal) operand, configuration));
			}

			else if (operand.getClass().getName().equals(
					StringLiteral.class.getName())) {
				addOperand(new LiteralExpr((StringLiteral) operand,
						configuration));
			}

			else {
				logger.debug(
						"Functional comparison operand [{}] is unsupported",
						operand);
				throw new IrresoluteException(String.format(
						"Functional comparison operand [%s] is unsupported",
						operand));
			}
		}
	}

	/**
	 * Get the expression type
	 */
	@Override
	public Type getType() {
		return Type.Comparison;
	}

	/**
	 * Get the operator this comparison performs
	 * 
	 * @return
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Is this a functional operation
	 * 
	 * @return Boolean
	 */
	public Boolean isFunctional() {
		return functional;
	}

	public String toString() {
		return String.format(
				"type [%s], comparison [%s], operands [%s], operator [%s]",
				getType(), expression, operands, operator);
	}
}
