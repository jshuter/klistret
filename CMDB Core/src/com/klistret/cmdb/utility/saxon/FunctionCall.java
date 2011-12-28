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

import com.klistret.cmdb.exception.ApplicationException;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.instruct.TraceExpression;

/**
 * Support aggregate function calls taking only regular path expressions.
 * 
 * @author Matthew Young
 * 
 */
public class FunctionCall extends BaseExpression {

	/**
	 * Relative Path expression
	 */
	private RelativePathExpr relativePathExpr;

	/**
	 * Supported functions
	 *
	 */
	public enum Function {
		max, min, avg, sum
	}

	/**
	 * Function parsed from passed expression
	 */
	private Function function;

	/**
	 * Constructor
	 * 
	 * @param xpath
	 */
	public FunctionCall(String xpath) {
		super(xpath);

		if (!expression.getClass().getName()
				.equals(TraceExpression.class.getName()))
			throw new ApplicationException(
					String.format(
							"Function calls (top level) map only Saxon trace expressions [name: %s]",
							expression.getClass().getName()));

		String functionName = expression.getObjectName().getLocalName();

		if (functionName.equals("max"))
			function = Function.max;

		else if (functionName.equals("min"))
			function = Function.min;

		else if (functionName.equals("avg"))
			function = Function.avg;

		else if (functionName.equals("sum"))
			function = Function.sum;

		else {
			throw new IrresoluteException(
					String.format(
							"Function (trace) expression [%s] using unsupported function [%s]",
							expression, functionName));
		}

		net.sf.saxon.expr.FunctionCall function = (net.sf.saxon.expr.FunctionCall) expression
				.getProperty("expression");

		Expression[] arguments = function.getArguments();

		if (arguments == null)
			throw new ApplicationException(
					String.format(
							"Function (trace) expression [%s] had no argument for Saxon property expression",
							expression));

		if (arguments.length != 1)
			throw new ApplicationException(
					String.format(
							"Function (trace) expression [%s] should only have one argument [length: %d]",
							expression, arguments.length));

		this.relativePathExpr = new RelativePathExpr(arguments[0],
				this.staticContext.getConfiguration());
	}

	/**
	 * Get relative path expression
	 * 
	 * @return
	 */
	public RelativePathExpr getRelativePath() {
		return this.relativePathExpr;
	}

	/**
	 * Get function enumeration
	 * 
	 * @return
	 */
	public Function getFunction() {
		return this.function;
	}
}
