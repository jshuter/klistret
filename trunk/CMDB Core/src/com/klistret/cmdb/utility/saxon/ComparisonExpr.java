package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.Token;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.ContextItemExpression;

public class ComparisonExpr extends LogicalExpr<Expr> {

	public enum Operator {
		ValueEquals, ValueNotEquals, ValueLessThan, ValueLessThanOrEquals, ValueGreaterThan, ValueGreaterThanOrEquals, GeneralEquals, GeneralNotEquals, GeneralLessThan, GeneralLessThanOrEquals, GeneralGreaterThan, GeneralGreaterThanOrEquals
	};

	private Operator operator;

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
			throw new IrresoluteException(String.format(
					"General expression [%s] using unsupported operator [%d]",
					expression, expression.getOperator()));
		}

		setOperands(expression.getOperands());
	}

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
			throw new IrresoluteException(String.format(
					"Value expression [%s] using unsupported operator [%d]",
					expression, expression.getOperator()));
		}

		setOperands(expression.getOperands());
	}

	private void setOperands(Expression[] operands) {
		for (Expression operand : operands) {
			if (operand.getClass().getName().equals(
					AxisExpression.class.getName())) {
				addOperand(new StepExpr((AxisExpression) operand, configuration));
			}

			else if (operand.getClass().getName().equals(
					Literal.class.getName())) {
				addOperand(new LiteralExpr((Literal) operand, configuration));
			}

			else if (operand.getClass().getName().equals(
					StringLiteral.class.getName())) {
				addOperand(new LiteralExpr((StringLiteral) operand,
						configuration));
			}

			else if (operand.getClass().getName().equals(
					ContextItemExpression.class.getName())) {
				addOperand(new ContextItemExpr((ContextItemExpression) operand,
						configuration));
			}

			else {
				throw new IrresoluteException(String.format(
						"Comparison operand [%s] is unsupported", operand));
			}
		}
	}

	@Override
	public Type getType() {
		return Type.Comparison;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComparisonExpr) {
			return super.equals(obj);
		}
		return false;
	}

	public String toString() {
		return String.format(
				"type [%s], comparison [%s], operands [%s], operator [%s]",
				getType(), expression, operands, operator);
	}
}
