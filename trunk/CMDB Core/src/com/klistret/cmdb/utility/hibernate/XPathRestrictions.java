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

package com.klistret.cmdb.utility.hibernate;

import org.hibernate.criterion.Criterion;

import com.klistret.cmdb.utility.xmlbeans.Expression;
import com.klistret.cmdb.utility.xmlbeans.XPathFunctions;
import com.klistret.cmdb.utility.xmlbeans.XPathOperators;

public class XPathRestrictions {

	XPathRestrictions() {
		// not possible to create instance
	}

	public static Criterion equal(String propertyName, Expression expression,
			Object value) {
		String xpath = XPathOperators.equal(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				false);
	}

	public static Criterion notEqual(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathOperators.notEqual(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				false);
	}

	public static Criterion greaterThan(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathOperators.greaterThan(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				false);
	}

	public static Criterion greaterThanOrEqualTo(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathOperators.greaterThanOrEqualTo(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				false);
	}

	public static Criterion lessThan(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathOperators.lessThan(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				false);
	}

	public static Criterion lessThanOrEqualTo(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathOperators.lessThanOrEqualTo(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				false);
	}

	public static Criterion matches(String propertyName, Expression expression,
			Object value) {
		String xpath = XPathFunctions.matches(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				true);
	}

	public static Criterion contains(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathFunctions.contains(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				true);
	}

	public static Criterion endsWith(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathFunctions.endsWith(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				true);
	}

	public static Criterion startsWith(String propertyName,
			Expression expression, Object value) {
		String xpath = XPathFunctions.startsWith(expression, "?");
		return new XPathExistsFunction(propertyName, expression, xpath, value,
				true);
	}
}
