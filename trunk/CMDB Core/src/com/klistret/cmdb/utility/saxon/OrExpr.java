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
import net.sf.saxon.expr.BooleanExpression;

/**
 * OrExpr is a logical expression either true or false. Operands are AndExpr or
 * extensions of AndExpr which are ComparisonExpr.
 * 
 * @author Matthew Young
 * 
 */
public class OrExpr extends LogicalExpr<Expr> {

	public OrExpr(BooleanExpression expression, Configuration configuration) {
		super(expression, configuration);
	}

	public String getXPath() {
		String xpath = null;
		for (Expr operand : operands)
			xpath = xpath == null ? operand.getXPath() : String.format(
					"%s or %s", xpath, operand.getXPath());

		return xpath;
	}

	public Type getType() {
		return Type.Or;
	}
}
