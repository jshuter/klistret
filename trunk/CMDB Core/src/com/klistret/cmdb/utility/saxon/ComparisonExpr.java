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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.SlashExpression;
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
 * first being a relative path. Otherwise the expression will be captured as a
 * irresolute and depending on where in the JTA mapping the axis appears the
 * irresolute may be acceptable but not for JTA entities.
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

	/**
	 * Types of comparison
	 */
	public enum Operator {
		ValueEquals, ValueNotEquals, ValueLessThan, ValueLessThanOrEquals, ValueGreaterThan, ValueGreaterThanOrEquals, GeneralEquals, Empty, Exists, Matches
	};

	/**
	 * Comparison type
	 */
	private Operator operator;

	/**
	 * Functional comparison
	 */
	private Boolean functional = false;

	/**
	 * General comparisons are existentially quantified comparisons that may be
	 * applied to operand sequences of any length. The result of a general
	 * comparison that does not raise an error is always true or false.
	 * 
	 * @param expression
	 * @param configuration
	 */
	public ComparisonExpr(GeneralComparison expression,
			Configuration configuration) {
		super(expression, configuration);

		switch (expression.getOperator()) {
		case (Token.EQUALS):
			operator = Operator.GeneralEquals;
			break;

		default:
			logger.error("Only the = operator is supported for general comparisons.  Most likely the expression is determined irresolute because a value comparison was intended.");
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
	public ComparisonExpr(ValueComparison expression,
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
	public ComparisonExpr(TraceExpression expression,
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
			throw new IrresoluteException(
					String.format(
							"Trace or functional expression [%s] using unsupported function [%s]",
							expression, functionName));
		}

		logger.debug("Function [{}] prior to explaining arguments",
				functionName);
		explainFunctionOperands(function.getArguments());
	}

	/**
	 * Generate XPath without masking literal values
	 */
	public String getXPath() {
		return getXPath(false);
	}

	/**
	 * Generate XPath
	 */
	public String getXPath(boolean maskLiteral) {
		return ComparisonExpr.getXPath(this.getOperator(), this.getOperands(),
				maskLiteral);
	}

	/**
	 * Static XPath builder to help reuse.
	 * 
	 * @param operator
	 * @param operands
	 * @param maskLiteral
	 * @return
	 */
	public static String getXPath(Operator operator, List<Expr> operands,
			boolean maskLiteral) {
		switch (operator) {
		case ValueEquals:
			return String.format("%s eq %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case ValueNotEquals:
			return String.format("%s nq %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case ValueGreaterThan:
			return String.format("%s gt %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case ValueGreaterThanOrEquals:
			return String.format("%s ge %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case ValueLessThan:
			return String.format("%s lt %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case ValueLessThanOrEquals:
			return String.format("%s le %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case GeneralEquals:
			return String.format("%s = %s",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		case Empty:
			return String.format("empty(%s)",
					operands.get(0).getXPath(maskLiteral));
		case Exists:
			return String.format("exists(%s)",
					operands.get(0).getXPath(maskLiteral));
		case Matches:
			return String.format("matches(%s,%s)",
					operands.get(0).getXPath(maskLiteral), operands.get(1)
							.getXPath(maskLiteral));
		}

		throw new ApplicationException(
				"Operator undefined while generating XPath");
	}

	/**
	 * Controll that the number of expression is 2 with the right being a step
	 * with predicates and left being a literal.
	 * 
	 * @param operands
	 */
	private void explainGeneralOrValueOperands(Expression[] operands) {
		if (operands.length != 2) {
			throw new IrresoluteException(
					String.format(
							"General/Value comparisons are limited to 2 operands only [passed: %d]",
							operands.length));
		}

		Expr literal = null;
		Expr relative = null;

		for (Expression operand : operands) {
			if (operand instanceof Literal) {
				if (operand.getClass().getName()
						.equals(Literal.class.getName()))
					literal = new LiteralExpr((Literal) operand, configuration);

				if (operand.getClass().getName()
						.equals(StringLiteral.class.getName()))
					literal = new LiteralExpr((StringLiteral) operand,
							configuration);
			}

			if (operand.getClass().getName()
					.equals(AxisExpression.class.getName()))
				relative = new RelativePathExpr(operand, configuration);

			if (operand.getClass().getName()
					.equals(FilterExpression.class.getName()))
				relative = new RelativePathExpr(operand, configuration);

			if (operand.getClass().getName()
					.equals(SlashExpression.class.getName()))
				relative = new RelativePathExpr(operand, configuration);

			if (operand.getClass().getName()
					.equals(ContextItemExpression.class.getName()))
				logger.debug("Context [.] not supported, incomplete JTA resolution");
		}

		if (literal == null)
			throw new IrresoluteException(
					"General/Value comparisons are required to have a literal expression");

		if (relative == null)
			throw new IrresoluteException(
					"General/Value comparisons are required to have a Step without predicate or Relative Path");

		addOperand(literal);
		addOperand(relative);
	}

	/**
	 * Control that the first operand is a relative path.
	 * 
	 * TO-DO: Redo to handle multiple function formats (for example where the
	 * Step operand is not forced into the first position)
	 * 
	 * @param operands
	 */
	private void explainFunctionOperands(Expression[] operands) {
		if (operands.length < 1) {
			throw new IrresoluteException(
					String.format(
							"Functional comparisons must have at least one operand not [%d]",
							operands.length));
		}

		Expression stepOperand = operands[0];
		addOperand(new RelativePathExpr(stepOperand, configuration));

		for (int index = 1; index < operands.length; index++) {
			Expression operand = operands[index];

			if (operand.getClass().getName().equals(Literal.class.getName())) {
				addOperand(new LiteralExpr((Literal) operand, configuration));
			}

			else if (operand.getClass().getName()
					.equals(StringLiteral.class.getName())) {
				addOperand(new LiteralExpr((StringLiteral) operand,
						configuration));
			}

			else {
				throw new IrresoluteException(String.format(
						"Functional comparison operand [%s] is unsupported",
						operand));
			}
		}
	}

	/**
	 * Get the expression type
	 */
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

	/**
	 * Display value
	 */
	public String toString() {
		return String.format(
				"type [%s], comparison [%s], operands [%s], operator [%s]",
				getType(), expression, operands, operator);
	}
}
