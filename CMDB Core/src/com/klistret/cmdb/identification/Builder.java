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

package com.klistret.cmdb.identification;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlObject;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

public class Builder {

	private SortedMap<CompositeKey, PropertyExpression[]> identifcations = new TreeMap<CompositeKey, PropertyExpression[]>();

	public Builder() {
	}

	public void addIdentifcation(PropertyExpression[] builders, Integer origin) {
		addIdentifcation(builders, origin, null);
	}

	public void addIdentifcation(PropertyExpression[] expressions,
			Integer origin, Integer priority) {
		identifcations.put(new CompositeKey(origin, priority), expressions);
	}

	private boolean validate(PropertyExpression[] expressions,
			XmlObject xmlObject) {
		for (PropertyExpression expression : expressions) {
			String xpath = expression.getXPath();

			XmlObject[] results = xmlObject.selectPath(xpath);
			if (results.length == 0)
				return false;

			if (results.length > 1)
				throw new ApplicationException(
						String
								.format(
										"identification xpath [%s] is not unique for xmlObject [%s]",
										xpath, xmlObject.schemaType()
												.getFullJavaName()));
		}

		return true;
	}

	public PropertyExpression[] getPrimaryIdentification(XmlObject xmlObject) {
		Iterator<Map.Entry<CompositeKey, PropertyExpression[]>> it = identifcations
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<CompositeKey, PropertyExpression[]> pairs = it.next();

			if (validate(pairs.getValue(), xmlObject))
				return pairs.getValue();
		}

		return null;
	}

	private class CompositeKey implements Comparable<CompositeKey> {

		private Integer priority;

		private Integer origin;

		public CompositeKey(int origin, Integer priority) {
			this.origin = origin;
			this.priority = priority;
		}

		public Integer getOrigin() {
			return this.origin;
		}

		public Integer getPriority() {
			return this.priority;
		}

		@Override
		public int compareTo(CompositeKey other) {

			// same base type
			if (this.getOrigin().compareTo(other.getOrigin()) == 0) {
				// descend if this priority is null
				if (this.getPriority() == null)
					return 1;

				// ascend if other priority is null
				if (other.getPriority() == null)
					return -1;

				return this.getPriority().compareTo(other.getPriority());
			}

			return this.getOrigin().compareTo(other.getOrigin());
		}

	}
}
