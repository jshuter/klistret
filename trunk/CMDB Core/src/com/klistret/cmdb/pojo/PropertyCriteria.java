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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.NullableType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.hibernate.XPathExpression;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper;

public class PropertyCriteria {
	private static final Logger logger = LoggerFactory
			.getLogger(PropertyCriteria.class);

	private int maxResults = 100;

	private int firstResult = 0;

	private String className;

	private List<PropertyCriterion> propertyCriteria;

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<PropertyCriterion> getPropertyCriteria() {
		return propertyCriteria;
	}

	public void setPropertyCriteria(List<PropertyCriterion> propertyCriteria) {
		this.propertyCriteria = propertyCriteria;
	}

	public boolean addPropertyCriterion(PropertyCriterion propertyCriterion) {
		if (this.propertyCriteria == null)
			this.propertyCriteria = new ArrayList<PropertyCriterion>();

		return this.propertyCriteria.add(propertyCriterion);
	}

	public Criteria getCriteria(Session session) {
		/**
		 * empty criteria list control
		 */
		if (propertyCriteria == null) {
			logger
					.error("empty property criterion list within property criteria object");
			throw new ApplicationException(
					"empty property criterion list within property criteria object");
		}

		/**
		 * get entity name from passed className based on the XmlObject root
		 * element name
		 */
		SchemaType root = SchemaTypeHelper.getRootDocumentType(className);
		if (root == null)
			throw new ApplicationException(
					String
							.format(
									"unable to determine root element for XmlObject class [%s]",
									className));
		String persistentEntityName = root.getName().getLocalPart();

		/**
		 * create criteria
		 */
		Criteria criteria = session.createCriteria(persistentEntityName);
		if (criteria == null)
			throw new ApplicationException(String.format(
					"unable to create criteria for class [%s]",
					persistentEntityName));

		/**
		 * evaluate each property
		 */
		for (PropertyCriterion propertyCriterion : propertyCriteria) {
			transformPropertyCriterion(session, criteria, propertyCriterion,
					propertyCriterion.getPropertyLocationPath(),
					persistentEntityName);
		}

		/**
		 * max/first results
		 */
		criteria.setMaxResults(maxResults);
		criteria.setFirstResult(firstResult);

		return criteria;
	}

	private Type resolvePropertyType(Session session, String property,
			String persistentEntityName) {
		/**
		 * determine Hibernate type information (works only with className, not
		 * sure if a bug but the entityName method returns null)
		 */
		ClassMetadata classMetadata;
		try {
			classMetadata = session.getSessionFactory().getClassMetadata(
					persistentEntityName);
		} catch (HibernateException e) {
			throw new ApplicationException(String.format(
					"unable to get class metadata for entity name [%s]",
					persistentEntityName));
		}

		try {
			return classMetadata.getPropertyType(property);
		} catch (HibernateException e) {
			throw new ApplicationException(String.format(
					"unable to get hibernate type for property [%s]", property));
		}
	}

	private void transformPropertyCriterion(Session session, Criteria criteria,
			PropertyCriterion propertyCriterion, String propertyLocationPath,
			String persistentEntityName) {
		/**
		 * split properties between current and remainder to get at the first
		 * property plus convert first letter in property to lower case (since
		 * XML properties need to handle lower/upper case names)
		 */
		String[] split = propertyLocationPath.split("[.]", 2);
		String property = split[0];
		property = property.substring(0, 1).toLowerCase().concat(
				property.substring(1));

		/**
		 * reset propertyLocationPath as remainder if exists otherwise null
		 */
		if (split.length == 2) {
			propertyLocationPath = split[1];
		} else {
			propertyLocationPath = null;
		}

		/**
		 * determine Hibernate type information (works only with className, not
		 * sure if a bug but the entityName method returns null)
		 */
		Type propertyType = resolvePropertyType(session, property,
				persistentEntityName);
		logger.debug(String.format(
				"processing property [%s], type return class [%s]", property,
				propertyType.getReturnedClass().getName()));

		/**
		 * handling associated collections
		 */
		if (propertyType.isAssociationType() && propertyType.isCollectionType()) {
			String associatedEntityName = ((CollectionType) propertyType)
					.getAssociatedEntityName(((SessionFactoryImplementor) session
							.getSessionFactory()));

			logger.debug(String.format(
					"traversing into associated collection [%s]",
					associatedEntityName));

			transformPropertyCriterion(session, criteria
					.createCriteria(property), propertyCriterion,
					propertyLocationPath, associatedEntityName);
		}

		/**
		 * handling associated entities
		 */
		if (propertyType.isAssociationType() && propertyType.isEntityType()) {
			String associatedEntityName = ((AssociationType) propertyType)
					.getAssociatedEntityName(((SessionFactoryImplementor) session
							.getSessionFactory()));

			logger.debug(String.format(
					"traversing into associated entity [%s]",
					associatedEntityName));

			transformPropertyCriterion(session, criteria
					.createCriteria(property), propertyCriterion,
					propertyLocationPath, associatedEntityName);
		}

		/**
		 * handle XmlObject properties
		 */
		if (propertyType.getReturnedClass().equals(XmlObject.class)) {
			if (propertyLocationPath == null)
				throw new ApplicationException(
						"property expression for XmlObject empty");

			logger.debug(String.format("adding criteria for XmlObject"));

			PropertyExpression propertyExpression = new PropertyExpression(
					className, propertyLocationPath);

			switch (propertyCriterion.getOperation()) {
			case matches:
				criteria.add(new XPathExpression(property, propertyExpression
						.matches(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case contains:
				criteria.add(new XPathExpression(property, propertyExpression
						.contains(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case startsWith:
				criteria.add(new XPathExpression(property, propertyExpression
						.startsWith(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case endsWith:
				criteria.add(new XPathExpression(property, propertyExpression
						.endsWith(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case equal:
				criteria.add(new XPathExpression(property, propertyExpression
						.equal(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case notEqual:
				criteria.add(new XPathExpression(property, propertyExpression
						.notEqual(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case lessThan:
				criteria.add(new XPathExpression(property, propertyExpression
						.lessThan(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case lessThanOrEqual:
				criteria.add(new XPathExpression(property, propertyExpression
						.lessThanOrEqual(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case greaterThan:
				criteria.add(new XPathExpression(property, propertyExpression
						.greaterThan(getValueAsString(propertyCriterion)),
						propertyExpression.getVariableReference()));
				break;
			case greaterThanOrEqual:
				criteria
						.add(new XPathExpression(
								property,
								propertyExpression
										.greaterThanOrEqual(getValueAsString(propertyCriterion)),
								propertyExpression.getVariableReference()));
				break;
			default:
				throw new ApplicationException(String.format(
						"operation [%s] not valid for xpath restriction",
						propertyCriterion.getOperation().toString()));
			}
		}

		/**
		 * handle simple property
		 */
		if (propertyLocationPath == null) {
			logger.debug(String.format("adding criteria for simple property"));

			switch (propertyCriterion.getOperation()) {
			case matches:
				criteria.add(Restrictions.ilike(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case contains:
				criteria.add(Restrictions
						.ilike(property, getValueAsString(propertyCriterion),
								MatchMode.ANYWHERE));
				break;
			case startsWith:
				criteria.add(Restrictions.ilike(property,
						getValueAsString(propertyCriterion), MatchMode.START));
				break;
			case endsWith:
				criteria.add(Restrictions.ilike(property,
						getValueAsString(propertyCriterion), MatchMode.END));
				break;
			case equal:
				criteria.add(Restrictions.eq(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case notEqual:
				criteria.add(Restrictions.ne(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case lessThan:
				criteria.add(Restrictions.lt(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case lessThanOrEqual:
				criteria.add(Restrictions.le(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case greaterThan:
				criteria.add(Restrictions.gt(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case greaterThanOrEqual:
				criteria.add(Restrictions.ge(property, getValueAsObject(
						propertyCriterion, propertyType)));
				break;
			case isNull:
				criteria.add(Restrictions.isNull(property));
				break;
			case isNotNull:
				criteria.add(Restrictions.isNotNull(property));
				break;
			case in:
				criteria.add(Restrictions.in(property, getValuesAsObject(
						propertyCriterion, propertyType)));
				break;
			default:
				throw new ApplicationException(
						String
								.format(
										"operation [%s] not valid for simple property restriction",
										propertyCriterion.getOperation()
												.toString()));
			}
		}
	}

	private Object[] getValuesAsObject(PropertyCriterion propertyCriterion,
			Type propertyType) {
		Object[] values = null;

		if (propertyCriterion.getValues() != null) {
			values = new Object[propertyCriterion.getValues().length];

			for (int index = 0; index < propertyCriterion.getValues().length; index++) {
				try {
					values[index] = ((NullableType) propertyType)
							.fromStringValue(propertyCriterion.getValues()[index]);
				} catch (HibernateException e) {
					throw new ApplicationException(String.format(
							"error converting values [%s] to object: %s",
							propertyCriterion.getValues(), e.getMessage()));
				}
			}
		}

		return values;
	}

	private Object getValueAsObject(PropertyCriterion propertyCriterion,
			Type propertyType) {
		Object value = null;

		if (propertyCriterion.getValues() != null) {
			try {
				String valueOfString = propertyCriterion.getValues()[0];

				value = ((NullableType) propertyType)
						.fromStringValue(valueOfString);
			} catch (HibernateException e) {
				throw new ApplicationException(String.format(
						"error converting values [%s] to object: %s",
						propertyCriterion.getValues(), e.getMessage()));
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ApplicationException(
						String
								.format(
										"unable to access value at index 0 in value array [%s]: %s",
										propertyCriterion.getValues()
												.toString(), e.getMessage()));
			}
		}

		return value;
	}

	private String getValueAsString(PropertyCriterion propertyCriterion) {
		String value = null;

		if (propertyCriterion.getValues() != null) {
			try {
				value = propertyCriterion.getValues()[0];
			} catch (Exception e) {
				throw new ArrayIndexOutOfBoundsException(
						String
								.format(
										"unable to access value at index 0 in value array [%s]: %s",
										propertyCriterion.getValues()
												.toString(), e.getMessage()));
			}
		}

		return value;
	}
}