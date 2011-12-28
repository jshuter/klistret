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

/**
 * The basic idea is to only handle relative path expressions as defined by the
 * XPath 2.0 specification (http://www.w3.org/TR/xpath20/#id-path-expressions).
 * Restricting the acceptable expressions to a list of steps (ie a slash
 * expression in Saxon terms) maps XPaths (not XQuerys) to JPA mappings. The JPA
 * structure is generally a combination of step with or without predicates. The
 * XPath simplification stops once a XML column/property is reached whereby even
 * other expressions than steps are acceptable (denoted as irresolute).
 * 
 * @author Matthew Young
 * 
 */
public class PathExpression extends BaseExpression {

	/**
	 * Relative Path expression
	 */
	private RelativePathExpr relativePathExpr;

	/**
	 * Constructor
	 * 
	 * @param xpath
	 */
	public PathExpression(String xpath) {
		super(xpath);

		this.relativePathExpr = new RelativePathExpr(this.expression,
				this.staticContext.getConfiguration(), this);
	}

	/**
	 * Get relative path expression
	 * 
	 * @return
	 */
	public RelativePathExpr getRelativePath() {
		return this.relativePathExpr;
	}
}
