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
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Value;

public class LiteralExpr extends Expr {

	private static final Logger logger = LoggerFactory
			.getLogger(LiteralExpr.class);

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
			logger
					.debug(
							"Literal [{}] either had no string representation or could not be converted to Java",
							expression);
		}
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
