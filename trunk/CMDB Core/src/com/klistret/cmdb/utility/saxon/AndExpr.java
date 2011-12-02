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

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.BooleanExpression;

/**
 * AndExpr is a logical expression either true or false made of ComparisonExpr
 * operands.
 * 
 * @author Matthew Young
 * 
 */
public class AndExpr extends LogicalExpr<Expr> {

	public AndExpr(BooleanExpression expression, Configuration configuration) {
		super(expression, configuration);
	}

	public String getXPath() {
		return getXPath(false);
	}

	public String getXPath(boolean maskLiteral) {
		return AndExpr.getXPath(this.getOperands(), maskLiteral);
	}

	protected static String getXPath(List<Expr> operands, boolean maskLiteral) {
		String xpath = null;
		for (Expr operand : operands)
			xpath = xpath == null ? operand.getXPath(maskLiteral) : String
					.format("%s and %s", xpath, operand.getXPath(maskLiteral));

		return xpath;
	}

	public Type getType() {
		return Type.And;
	}

}
