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

package com.klistret.cmdb.rules;

import java.util.List;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.rules.cache.PersistenceCache;
import com.klistret.cmdb.utility.annotations.Timer;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

/**
 * Persistence Rules are located in an external XML document. They allow unique
 * identification of global elements like Element/Relation prior to save/update
 * calls so that organizations can specify for themselves what uniquely earmarks
 * CIs. Rules may be ordered and those applied to abstract super classes get
 * inherited (unless excluded). Rules are composed of property criteria and a
 * binding to XmlObjects. Nothing more. Simple.
 * 
 * @author Matthew Young
 * 
 */
public class PersistenceImpl implements Persistence {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceImpl.class);

	private PersistenceCache persistenceCache;

	public PersistenceCache getPersistenceCache() {
		return persistenceCache;
	}

	public void setPersistenceCache(PersistenceCache persistenceCache) {
		this.persistenceCache = persistenceCache;
	}

	/**
	 * Based on the XmlObject the element type is derived automatically from the
	 * SchemaType java class name, only active (toTimeStamp null) objects are
	 * selected, and each property criterion is evaluated. All property
	 * expressions returned by the rules document are evaluated in order,
	 * returning the first positive hit.
	 * 
	 * @param xmlObject
	 * @return PropertyCriteria
	 */
	@Timer
	public com.klistret.cmdb.pojo.PropertyCriteria getPropertyCriteria(
			XmlObject xmlObject) {
		/**
		 * valid expression array?
		 */
		PropertyExpression[] propertyExpressions = getPropertyExpressionCriterion(xmlObject);
		if (propertyExpressions.length == 0) {
			logger.debug("persistence rules not defined for XmlObject [{}]",
					xmlObject.schemaType().getFullJavaName());
			return null;
		}

		/**
		 * construct property criteria
		 */
		com.klistret.cmdb.pojo.PropertyCriteria criteria = new com.klistret.cmdb.pojo.PropertyCriteria();
		criteria.setClassName(xmlObject.schemaType().getFullJavaName());

		/**
		 * limit by class name through elementType relation
		 */
		com.klistret.cmdb.pojo.PropertyCriterion typeCriterion = new com.klistret.cmdb.pojo.PropertyCriterion();
		typeCriterion.setPropertyLocationPath("type.name");
		typeCriterion
				.setOperation(com.klistret.cmdb.pojo.PropertyCriterion.Operation.equal);
		String[] types = { xmlObject.schemaType().getFullJavaName() };
		typeCriterion.setValues(types);
		criteria.addPropertyCriterion(typeCriterion);

		/**
		 * limit to only active elements
		 */
		com.klistret.cmdb.pojo.PropertyCriterion toTimeStampCriterion = new com.klistret.cmdb.pojo.PropertyCriterion();
		toTimeStampCriterion.setPropertyLocationPath("toTimeStamp");
		toTimeStampCriterion
				.setOperation(com.klistret.cmdb.pojo.PropertyCriterion.Operation.isNull);
		criteria.addPropertyCriterion(toTimeStampCriterion);

		/**
		 * limit with property expression criterion
		 */
		for (PropertyExpression propertyExpression : propertyExpressions) {
			String[] values = new String[1];
			values[0] = ((XmlAnySimpleType) xmlObject
					.selectPath(propertyExpression.toString(false))[0])
					.getStringValue();

			com.klistret.cmdb.pojo.PropertyCriterion xpathCriterion = new com.klistret.cmdb.pojo.PropertyCriterion();
			xpathCriterion.setPropertyLocationPath("configuration."
					+ propertyExpression.getPropertyLocationPath());
			xpathCriterion
					.setOperation(com.klistret.cmdb.pojo.PropertyCriterion.Operation.equal);
			xpathCriterion.setValues(values);

			criteria.addPropertyCriterion(xpathCriterion);
		}

		return criteria;
	}

	/**
	 * Determines if a property expression is valid for the passed XmlObject by
	 * performing a select for each XPath with the expectation a non-empty,
	 * simple-type result set.
	 * 
	 * @param propertyExpressionCriterion
	 * @param xmlObject
	 * @return true/false
	 */
	private boolean validatePropertyExpressionCriterion(
			PropertyExpression[] propertyExpressionCriterion,
			XmlObject xmlObject) {
		for (PropertyExpression propertyExpression : propertyExpressionCriterion) {
			String xpath = propertyExpression.toString(false);

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

			if (!results[0].schemaType().isSimpleType())
				throw new ApplicationException(
						String
								.format(
										"selected node [xpath: %s] is not simple type for xmlObject [xml: %s]",
										xpath, xmlObject.xmlText()));

		}

		return true;
	}

	/**
	 * Return first valid property expression for the passed XmlObject
	 * 
	 * @param xmlObject
	 * @return Property Expression array (valid criterion based on order and if
	 *         the XmlObject includes the selected properties)
	 */
	public PropertyExpression[] getPropertyExpressionCriterion(
			XmlObject xmlObject) {
		String classname = xmlObject.schemaType().getFullJavaName();

		List<PropertyExpression[]> propertyExpressionCriteria = persistenceCache
				.getPropertyExpressionCriteria(classname);
		for (int index = 0; index < propertyExpressionCriteria.size(); index++) {
			if (validatePropertyExpressionCriterion(propertyExpressionCriteria
					.get(index), xmlObject)) {
				logger
						.debug(
								"valid property expression criterion at index [{}] in criteria list for xmlObject [xml: {}]",
								index + 1, xmlObject.xmlText());
				return propertyExpressionCriteria.get(index);
			}
		}

		logger.debug(
				"no property expression criterion for xmlObject [xml: {}]",
				xmlObject.xmlText());
		return new PropertyExpression[0];
	}
}
