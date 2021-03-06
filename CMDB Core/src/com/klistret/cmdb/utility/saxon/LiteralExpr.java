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
import net.sf.saxon.expr.Expression;
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
public class LiteralExpr implements Expr {

	private static final Logger logger = LoggerFactory
			.getLogger(LiteralExpr.class);

	/**
	 * Saxon expression
	 */
	private Expression expression;

	/**
	 * Saxon configuration
	 */
	private Configuration configuration;

	/**
	 * Internal Value representation (atomic)
	 */
	private com.klistret.cmdb.utility.saxon.Value value;

	/**
	 * Interval Value representation (sequence)
	 */
	private com.klistret.cmdb.utility.saxon.Value[] values;

	/**
	 * An atomic condition
	 */
	private Boolean atomic;

	/**
	 * The value of a string literal is an atomic value whose type is xs:string
	 * and whose value is the string denoted by the characters between the
	 * delimiting apostrophes or quotation marks
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected LiteralExpr(StringLiteral expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;

		setValue(expression);
	}

	/**
	 * Numeric literal
	 * 
	 * @param expression
	 * @param configuration
	 */
	protected LiteralExpr(Literal expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;

		setValue(expression);
	}

	/**
	 * Return Saxon expression
	 */
	public Expression getExpression() {
		return this.expression;
	}

	/**
	 * Return Saxon configuration
	 */
	public Configuration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Generates XPath
	 */
	public String getXPath() {
		return getXPath(false);
	}

	/**
	 * Generates XPath by returning the underlying Value (regardless if atomic
	 * or not) as a string.
	 */
	public String getXPath(boolean maskLiteral) {
		if (!maskLiteral)
			return ((Literal) expression).getValue().toString();

		if (atomic)
			return String.format("$%s", value.getMask());

		String results = null;
		for (com.klistret.cmdb.utility.saxon.Value value : values) {
			results = results == null ? String.format("($%s", value.getMask())
					: String.format("%s, $%s", results, value.getMask());
		}
		results = String.format("%s)", results);

		return results;
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
						String.format(
								"Literal [%s] values are not allowed to be empty sequences",
								expression));

			Value literal = expression.getValue();

			if (literal instanceof AtomicValue) {
				value = new com.klistret.cmdb.utility.saxon.Value();

				value.setText(literal.getStringValue());
				value.setLiteral(literal.toString());
				value.setJavaValue(Value.convertToJava(literal.asItem()));
			}

			if (literal instanceof SequenceExtent) {
				values = new com.klistret.cmdb.utility.saxon.Value[literal
						.getLength()];

				for (int index = 0; index < literal.getLength(); index++) {
					Item item = ((SequenceExtent) literal).itemAt(index);

					if (!(item instanceof Value))
						throw new ApplicationException(
								"Sequence item is not an instance of Value");

					com.klistret.cmdb.utility.saxon.Value value = new com.klistret.cmdb.utility.saxon.Value();

					value.setText(item.getStringValue());
					value.setLiteral(((Value) item).toString());
					value.setJavaValue(Value.convertToJava(item));

					values[index] = value;
				}
			}
		} catch (XPathException e) {
			logger.warn(
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
	 * @return Value
	 */
	public com.klistret.cmdb.utility.saxon.Value getValue() {
		return value;
	}

	/**
	 * Sequence literals only
	 * 
	 * @return Object array
	 */
	public com.klistret.cmdb.utility.saxon.Value[] getValues() {
		return values;
	}

	/**
	 * Is atomic? False means the literal wraps a sequence that happens to
	 * contain a single atomic value (which this class can parse into an array
	 * of values).
	 * 
	 * @return Boolean
	 */
	public Boolean isAtomic() {
		return atomic;
	}

	/**
	 * 
	 */
	public String toString() {
		return String.format("type [%s], comparison [%s], value [%s]",
				getType(), expression, value);
	}
}
