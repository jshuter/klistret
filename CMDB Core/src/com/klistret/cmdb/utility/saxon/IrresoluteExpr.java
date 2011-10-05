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

import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;

/**
 * Bucket for any Saxon expression that is not a root or step Expr.
 * 
 * @author Matthew Young
 * 
 */
public class IrresoluteExpr extends Step {

	protected IrresoluteExpr(Expression expression, Configuration configuration) {
		super(expression, configuration);
	}

	public Type getType() {
		return Type.Irresolute;
	}

	public String getXPath() {
		return IrresoluteExpr.getXPath("unknown");
	}

	public static String getXPath(String value) {
		return value;
	}

	public QName getQName() {
		return null;
	}

	public String toString() {
		return String.format("Irresolute expression [%s]", getType(),
				expression);
	}
}
