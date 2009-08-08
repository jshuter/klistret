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

import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.NullableType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.hibernate.XPathExpression;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

public class PropertyCriteria {
	private static final Logger logger = LoggerFactory
			.getLogger(PropertyCriteria.class);

	private int maxResults = 100;

	private int firstResult = 0;

	private String className;

	private String xmlClassName;

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

	public String getXmlClassName() {
		return xmlClassName;
	}

	public void setXmlClassName(String xmlClassName) {
		this.xmlClassName = xmlClassName;
	}

	public List<PropertyCriterion> getPropertyCriteria() {
		return propertyCriteria;
	}

	public void setPropertyCriteria(List<PropertyCriterion> propertyCriteria) {
		this.propertyCriteria = propertyCriteria;
	}

	/**
	 * 
	 * @param session
	 * @return Hibernate criteria
	 */
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
		 * create criteria
		 */
		Criteria criteria;
		try {
			criteria = session.createCriteria(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new ApplicationException(String.format(
					"classname [%s] not found: %s", className, e.getMessage()));
		}
		if (criteria == null)
			throw new ApplicationException(String.format(
					"unable to create criteria for classname [%s]", className));

		/**
		 * evaluate each property
		 */
		for (PropertyCriterion propertyCriterion : propertyCriteria) {
			transformPropertyCriterion(session, criteria, propertyCriterion,
					propertyCriterion.getPropertyLocationPath(), className);
		}

		/**
		 * max/first results
		 */
		criteria.setMaxResults(maxResults);
		criteria.setFirstResult(firstResult);

		return criteria;
	}

	/**
	 * 
	 * @param session
	 * @param property
	 * @param currentClassName
	 * @return Hibernate Type
	 */
	private Type resolvePropertyType(Session session, String property,
			String currentClassName) {
		/**
		 * determine Hibernate type information (works only with className, not
		 * sure if a bug but the entityName method returns null)
		 */
		ClassMetadata classMetadata;
		try {
			classMetadata = session.getSessionFactory().getClassMetadata(
					currentClassName);
		} catch (HibernateException e) {
			throw new ApplicationException(String.format(
					"unable to get class metadata for class name [%s]",
					className));
		}

		try {
			return classMetadata.getPropertyType(property);
		} catch (HibernateException e) {
			throw new ApplicationException(String.format(
					"unable to get hibernate type for property [%s]", property));
		}
	}

	/**
	 * 
	 * @param session
	 * @param criteria
	 * @param propertyCriterion
	 * @param propertyLocationPath
	 * @param currentClassName
	 */
	private void transformPropertyCriterion(Session session, Criteria criteria,
			PropertyCriterion propertyCriterion, String propertyLocationPath,
			String currentClassName) {
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
				currentClassName);
		logger.debug(String.format(
				"processing property [%s], type return class [%s]", property,
				propertyType.getReturnedClass().getName()));

		/**
		 * handling associated collections
		 */
		if (propertyType.isAssociationType() && propertyType.isCollectionType()) {
			String aen = ((CollectionType) propertyType)
					.getAssociatedEntityName(((SessionFactoryImplementor) session
							.getSessionFactory()));
			logger.debug(String.format(
					"traversing into associated collection [%s]", aen));

			transformPropertyCriterion(session, criteria
					.createCriteria(property), propertyCriterion,
					propertyLocationPath, aen);
		}

		/**
		 * handling associated entities
		 */
		if (propertyType.isAssociationType() && propertyType.isEntityType()) {
			logger.debug(String.format("traversing into associated entity"));
			transformPropertyCriterion(session, criteria
					.createCriteria(property), propertyCriterion,
					propertyLocationPath, propertyType.getReturnedClass()
							.getName());
		}

		/**
		 * handle XmlObject properties
		 */
		if (propertyType.getReturnedClass().equals(XmlObject.class)) {
			if (propertyLocationPath == null)
				throw new ApplicationException(
						"property expression for XmlObject empty");

			if (xmlClassName == null)
				throw new ApplicationException(
						"xmlObject class name not specified");

			logger.debug(String.format("adding criteria for XmlObject"));

			PropertyExpression propertyExpression = new PropertyExpression(
					xmlClassName, propertyLocationPath);

			switch (propertyCriterion.getOperation()) {
			case matches:
				criteria.add(new XPathExpression(property, propertyExpression
						.matches(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case contains:
				criteria.add(new XPathExpression(property, propertyExpression
						.contains(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case startsWith:
				criteria.add(new XPathExpression(property, propertyExpression
						.startsWith(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case endsWith:
				criteria.add(new XPathExpression(property, propertyExpression
						.endsWith(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case equal:
				criteria.add(new XPathExpression(property, propertyExpression
						.equal(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case notEqual:
				criteria.add(new XPathExpression(property, propertyExpression
						.notEqual(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case lessThan:
				criteria.add(new XPathExpression(property, propertyExpression
						.lessThan(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case lessThanOrEqual:
				criteria.add(new XPathExpression(property, propertyExpression
						.lessThanOrEqual(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case greaterThan:
				criteria.add(new XPathExpression(property, propertyExpression
						.greaterThan(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			case greaterThanOrEqual:
				criteria.add(new XPathExpression(property, propertyExpression
						.greaterThanOrEqual(propertyCriterion.getValue()),
						propertyExpression.getVariableReference()));
				break;
			default:
				throw new ApplicationException(String.format(
						"operation [%s] not valid for xpath", propertyCriterion
								.getOperation().toString()));
			}
		}

		/**
		 * handle simple property
		 */
		if (propertyLocationPath == null) {
			logger.debug(String.format("adding criteria for simple property"));

			Object value = null;
			if (propertyCriterion.getValue() != null) {
				try {
					value = ((NullableType) propertyType)
							.fromStringValue(propertyCriterion.getValue());
				} catch (HibernateException e) {
					throw new ApplicationException(String.format(
							"error converting string [%s] to object: %s",
							propertyCriterion.getValue(), e.getMessage()));
				}
			}

			switch (propertyCriterion.getOperation()) {
			case matches:
				criteria.add(Restrictions.ilike(property, value));
				break;
			case contains:
				criteria.add(Restrictions.ilike(property, value.toString(),
						MatchMode.ANYWHERE));
				break;
			case startsWith:
				criteria.add(Restrictions.ilike(property, value.toString(),
						MatchMode.START));
				break;
			case endsWith:
				criteria.add(Restrictions.ilike(property, value.toString(),
						MatchMode.END));
				break;
			case equal:
				criteria.add(Restrictions.eq(property, value));
				break;
			case notEqual:
				criteria.add(Restrictions.ne(property, value));
				break;
			case lessThan:
				criteria.add(Restrictions.lt(property, value));
				break;
			case lessThanOrEqual:
				criteria.add(Restrictions.le(property, value));
				break;
			case greaterThan:
				criteria.add(Restrictions.gt(property, value));
				break;
			case greaterThanOrEqual:
				criteria.add(Restrictions.ge(property, value));
				break;
			case isNull:
				criteria.add(Restrictions.isNull(property));
				break;
			case isNotNull:
				criteria.add(Restrictions.isNotNull(property));
				break;
			}

		}

	}

}
