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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.hibernate.XPathExpression;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

public class PropertyCriterion {

	public enum Operation {
		matches {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.ilike(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.matches(value), propertyExpression
						.getVariableReference());
			}
		},
		contains {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.ilike(property, value
						.toString(), MatchMode.ANYWHERE);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.contains(value), propertyExpression
						.getVariableReference());
			}
		},
		startsWith {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.ilike(property, value
						.toString(), MatchMode.START);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.startsWith(value), propertyExpression
						.getVariableReference());
			}
		},
		endsWith {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.ilike(property, value
						.toString(), MatchMode.END);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.endsWith(value), propertyExpression
						.getVariableReference());
			}
		},
		equal {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.eq(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.equal(value), propertyExpression
						.getVariableReference());
			}
		},
		notEqual {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.ne(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.notEqual(value), propertyExpression
						.getVariableReference());
			}
		},
		lessThan {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.lt(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.lessThan(value), propertyExpression
						.getVariableReference());
			}
		},
		lessThanOrEqual {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.le(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.lessThanOrEqual(value), propertyExpression
						.getVariableReference());
			}
		},
		greaterThan {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.gt(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.greaterThan(value), propertyExpression
						.getVariableReference());
			}
		},
		greaterThanOrEqual {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.ge(property, value);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return new XPathExpression(property, propertyExpression
						.greaterThanOrEqual(value), propertyExpression
						.getVariableReference());
			}
		},
		isNull {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.isNull(property);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return null;
			}
		},
		isNotNull {
			protected Criterion restriction(String property, Object value) {
				return (Criterion) Restrictions.isNotNull(property);
			}

			protected Criterion xpathRestriction(
					PropertyExpression propertyExpression, String property,
					String value) {
				return null;
			}
		};

		protected abstract Criterion restriction(String property, Object value);

		protected abstract Criterion xpathRestriction(
				PropertyExpression propertyExpression, String property,
				String value);
	}

	private final static String propertyLocationPathExpression = "(\\w+)|(\\w+[.]\\w+)*";

	private String propertyLocationPath;

	private String value;

	private Operation operation;

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

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}
