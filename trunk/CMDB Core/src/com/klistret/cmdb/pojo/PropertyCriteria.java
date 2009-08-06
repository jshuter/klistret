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
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
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

	public Criteria getCriteria(Session session) {
		/**
		 * empty criteria list control
		 */
		if (propertyCriteria == null) {
			logger.debug("empty property criterion list - return null");
			return null;
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
					propertyCriterion.getPropertyLocationPath());
		}

		/**
		 * max/first results
		 */
		criteria.setMaxResults(maxResults);
		criteria.setFirstResult(firstResult);

		return criteria;
	}

	private Type resolvePropertyType(Session session, String property) {
		/**
		 * determine Hibernate type information (works only with className, not
		 * sure if a bug but the entityName method returns null)
		 */
		ClassMetadata classMetadata;
		try {
			classMetadata = session.getSessionFactory().getClassMetadata(
					className);
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

	private void transformPropertyCriterion(Session session, Criteria criteria,
			PropertyCriterion propertyCriterion, String propertyLocationPath) {
		/**
		 * split properties between current and remainder to get at the first
		 * property plus convert first letter in property to lower case (since
		 * XML properties need to handle lower/upper case names)
		 */
		String[] split = propertyLocationPath.split("[.]", 2);
		String property = split[0].substring(0).toLowerCase();

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
		Type propertyType = resolvePropertyType(session, property);

		logger.debug(String.format(
				"processing property [%s], type return class [%s]", property,
				propertyType.getReturnedClass().toString()));

		// handle associated collections
		if (propertyType.isAssociationType() && propertyType.isCollectionType()) {
			logger.debug(String.format("handle associated collection"));
		}

		// handle associated entities
		if (propertyType.isAssociationType() && propertyType.isEntityType()) {
			logger.debug(String.format("handle associated entity"));
			transformPropertyCriterion(session, criteria
					.createCriteria(property), propertyCriterion,
					propertyLocationPath);
		}

		// handle XmlObject (requires remainder path to transform to a xpath)
		if (propertyType.getReturnedClass().equals(XmlObject.class)
				&& propertyLocationPath != null) {
			logger.debug(String.format("handle XmlObject"));
			PropertyExpression pe = new PropertyExpression(xmlClassName,
					propertyLocationPath);

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.equal))
				criteria.add(new XPathExpression(property, pe
						.equal(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.notEqual))
				criteria.add(new XPathExpression(property, pe
						.notEqual(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.startsWith))
				criteria.add(new XPathExpression(property, pe
						.startsWith(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.endsWith))
				criteria.add(new XPathExpression(property, pe
						.endsWith(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.matches))
				criteria.add(new XPathExpression(property, pe
						.matches(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.contains))
				criteria.add(new XPathExpression(property, pe
						.contains(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.lessThan))
				criteria.add(new XPathExpression(property, pe
						.lessThan(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.lessThanOrEqual))
				criteria.add(new XPathExpression(property, pe
						.lessThanOrEqual(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.greaterThan))
				criteria.add(new XPathExpression(property, pe
						.greaterThan(propertyCriterion.getValue()), pe
						.getVariableReference()));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.greaterThanOrEqual))
				criteria.add(new XPathExpression(property, pe
						.greaterThanOrEqual(propertyCriterion.getValue()), pe
						.getVariableReference()));

		}

		// hand simple property
		if (propertyLocationPath == null) {
			logger.debug(String.format("handle simple property"));

			if (propertyCriterion.getOperator().equals(
					PropertyCriterion.operators.equal))
				criteria.add(Restrictions.eq(property, propertyCriterion
						.getValue()));
		}
	}
}
