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

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

/**
 * Expr is the basis for all acceptable XPath expressions as denoted by their
 * type. The only type not specified by the XPath 2.0 specification is what is
 * called Irresolute which is basically an expression within a relative path
 * that is not a Step.
 * 
 * @author Matthew Young
 * 
 */
public interface Expr {

	/**
	 * Basic Expr types starting at the top level as Relative Paths which
	 * consist of Steps that can have predicates comprised of logical
	 * expressions.
	 */
	public enum Type {
		Root, RelativePath, Step, Or, And, Comparison, Literal, Irresolute
	};

	/**
	 * Get Saxon expression
	 * 
	 * @return Saxon expression
	 */
	public Expression getExpression();

	/**
	 * Saxon Configuration that compiled the underlying XPath
	 * 
	 * @return Saxon configuration
	 */
	public Configuration getConfiguration();

	/**
	 * Get a generated XPath for the original expression
	 * 
	 * @return XPath
	 */
	public String getXPath();

	/**
	 * Get Expr type
	 * 
	 * @return Expr type
	 */
	public Type getType();

}
