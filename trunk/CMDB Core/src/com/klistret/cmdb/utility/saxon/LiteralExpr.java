package com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Value;

public class LiteralExpr extends Expr {

	private String valueAsString;

	private Object value;

	protected LiteralExpr(StringLiteral expression, Configuration configuration) {
		super(expression, configuration);
		setValue(expression);
	}

	protected LiteralExpr(Literal expression, Configuration configuration) {
		super(expression, configuration);
		setValue(expression);
	}

	private void setValue(Literal expression) {
		try {
			valueAsString = expression.getValue().getStringValue();
			value = Value.convertToJava(expression.getValue().asItem());
		} catch (XPathException e) {
		}
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Type.Literal;
	}

	public Object getValue() {
		return value;
	}

	public String getValueAsString() {
		return this.valueAsString;
	}

	public String toString() {
		return String.format("type [%s], comparison [%s], value [%s]",
				getType(), expression, value);
	}
}
