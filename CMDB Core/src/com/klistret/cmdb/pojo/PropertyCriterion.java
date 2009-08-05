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

package com.klistret.cmdb.pojo;

import com.klistret.cmdb.exception.ApplicationException;

public class PropertyCriterion {
	/**
	 * Regular expression for property location paths
	 */
	private final static String propertyLocationPathExpression = "(\\w+)|(\\w+[.]\\w+)*";

	private String propertyLocationPath;

	private String value;

	public enum operators {
		matches, contains, startsWith, endsWith, equal, notEqual, lessThan, lessThanOrEqual, greaterThan, greaterThanOrEqual
	}

	private operators operator;

	public String getPropertyLocationPath() {
		return propertyLocationPath;
	}

	public void setPropertyLocationPath(String propertyLocationPath) {
		if (!propertyLocationPath.matches(propertyLocationPathExpression))

			throw new ApplicationException(String.format(
					"path [%s] does not match expression [%s]",
					propertyLocationPath, propertyLocationPathExpression));

		this.propertyLocationPath = propertyLocationPath;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	};

	public operators getOperator() {
		return operator;
	}

	public void setOperator(operators operator) {
		this.operator = operator;
	}
}
