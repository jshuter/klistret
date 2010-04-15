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

public abstract class Expr {

	protected final Expression expression;

	protected final Configuration configuration;

	public enum Type {
		Root, Path, Or, And, Comparison, Literal, Irresolute
	};

	public Expr(Expression expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;
	}

	public Expression getExpression() {
		return expression;
	}

	public abstract Type getType();

	public abstract boolean equals(Object obj);

	public String toString() {
		return String.format("type [%s], saxon expression [%s]", getType(),
				expression);
	}
}
