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

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

public abstract class LogicalExpr<T extends Expr> extends Expr {

	protected List<Expr> operands;

	public LogicalExpr(Expression expression, Configuration configuration) {
		super(expression, configuration);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LogicalExpr) {
			LogicalExpr other = (LogicalExpr) obj;
			return operands == null ? other.operands == null : operands
					.equals(other.operands);
		}
		return false;
	}

	public List<Expr> getOperands() {
		return operands;
	}

	protected void addOperand(Expr expression) {
		if (operands == null)
			operands = new ArrayList<Expr>();

		operands.add(expression);
	}

	public String toString() {
		return String.format("type [%s], logical [%s], operands [%s]",
				getType(), expression, operands);
	}
}
