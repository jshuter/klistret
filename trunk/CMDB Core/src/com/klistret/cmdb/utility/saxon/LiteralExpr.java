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

import com.klistret.cmdb.exception.ApplicationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.Value;

/**
 * A literal is a direct syntactic representation of an atomic value. However
 * the syntax really supports sequences and atomics. XPath supports two kinds of
 * literals: numeric literals and string literals.
 * 
 * @author Matthew Young
 * 
 */
public class LiteralExpr extends Expr {

	private static final Logger logger = LoggerFactory
			.getLogger(LiteralExpr.class);

	private String valueAsString;

	private String[] valueAsStringArray;

	private Object value;

	private Object[] valueAsArray;

	private Boolean atomic;

	protected LiteralExpr(StringLiteral expression, Configuration configuration) {
		super(expression, configuration);
		setValue(expression);
	}

	protected LiteralExpr(Literal expression, Configuration configuration) {
		super(expression, configuration);
		setValue(expression);
	}

	/**
	 * Executed directly in the constructors. First check is whether the literal
	 * is atomic. Empty sequences are not allowed.
	 * 
	 * @param expression
	 */
	private void setValue(Literal expression) {
		try {
			atomic = Literal.isAtomic(expression);

			if (Literal.isEmptySequence(expression))
				throw new ApplicationException(
						String
								.format(
										"Literal [%s] values are not allowed to be empty sequences",
										expression));

			Value literal = expression.getValue();

			if (literal instanceof AtomicValue) {
				valueAsString = literal.getStringValue();
				value = Value.convertToJava(literal.asItem());
			}

			if (literal instanceof SequenceExtent) {
				valueAsStringArray = new String[literal.getLength()];
				valueAsArray = new Object[literal.getLength()];

				for (int index = 0; index < literal.getLength(); index++) {
					Item item = ((SequenceExtent) literal).itemAt(index);

					valueAsStringArray[index] = item.getStringValue();
					valueAsArray[index] = Value.convertToJava(item);
				}
			}
		} catch (XPathException e) {
			logger
					.warn(
							"Literal [{}] either had no string representation or could not be converted to Java: {}",
							expression, e.getCause());
		}
	}

	@Override
	public Type getType() {
		return Type.Literal;
	}

	/**
	 * Atomic literals only
	 * 
	 * @return Object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sequence literals only
	 * 
	 * @return Object array
	 */
	public Object[] getValueAsArray() {
		return valueAsArray;
	}

	/**
	 * String representation of only atomic literals
	 * 
	 * @return String
	 */
	public String getValueAsString() {
		return valueAsString;
	}

	/**
	 * String array representation of only sequential literals
	 * 
	 * @return String array
	 */
	public String[] getValueAsStringArray() {
		return valueAsStringArray;
	}

	/**
	 * Is atomic?
	 * 
	 * @return Boolean
	 */
	public Boolean isAtomic() {
		return atomic;
	}

	public String toString() {
		return String.format("type [%s], comparison [%s], value [%s]",
				getType(), expression, value);
	}
}
