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

package com.klistret.cmdb.aspects;

import java.util.Collection;
import java.util.List;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.rules.Persistence;
import com.klistret.cmdb.pojo.PropertyCriteria;
import com.klistret.cmdb.pojo.PropertyCriterion;
import com.klistret.cmdb.service.ElementService;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

/**
 * AOP class applies persistence rules against services
 * 
 * @author Matthew Young
 * 
 */
public class PersistenceAOP {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceAOP.class);

	/**
	 * Persistence Rules
	 */
	private Persistence persistenceRules;

	/**
	 * Element service (transaction ready)
	 */
	private ElementService elementService;

	/**
	 * 
	 * @return PersistenceRules
	 */
	public Persistence getPersistenceRules() {
		return persistenceRules;
	}

	/**
	 * 
	 * @param persistenceRules
	 */
	public void setPersistenceRules(Persistence persistenceRules) {
		this.persistenceRules = persistenceRules;
	}

	/**
	 * 
	 * @return ElementService
	 */
	public ElementService getElementService() {
		return elementService;
	}

	/**
	 * 
	 * @param elementService
	 */
	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	/**
	 * Persistent rules (for uniqueness) are applied to the passed Element
	 * generating a PropertyCriteria that determines if other elements in the
	 * database are similar. Hits generate an exception.
	 * 
	 * @param element
	 */
	public void applyElementPersistenceRules(
			com.klistret.cmdb.pojo.Element element) throws ApplicationException {
		logger.debug("apply persistence rules to element [{}]", element
				.toString());

		String classname = element.getConfiguration().schemaType()
				.getFullJavaName();

		List<PropertyExpression[]> criteria = persistenceRules
				.getCriteriaByType(classname);
		if (criteria == null) {
			logger.debug("persistence criteria not defined for type [{}]",
					classname);
			return;
		}

		PropertyExpression[] criterion = persistenceRules
				.getCriterionByXmlObject(element.getConfiguration(), criteria);
		if (criterion == null) {
			logger.debug("persistence criterion not defined for element [{}]",
					element.toString());
			return;
		}

		PropertyCriteria pCriteria = getElementPropertyCriteria(element
				.getConfiguration(), criterion);
		if (pCriteria != null) {
			Collection<com.klistret.cmdb.xmlbeans.pojo.Element> results = elementService
					.findByCriteria(pCriteria);

			for (com.klistret.cmdb.xmlbeans.pojo.Element other : results)
				logger.debug("criteria selected other element [{}]", other
						.toString());

			if (element.getId() == null && !results.isEmpty())
				throw new ApplicationException(
						String
								.format(
										"new element is identical to other elements [count: %d] according to persistence rules ",
										results.size()));

			if (element.getId() != null && !results.isEmpty()) {
				for (com.klistret.cmdb.xmlbeans.pojo.Element other : results) {
					if (!element.equals(other))
						throw new ApplicationException(
								String
										.format(
												"element [%s] is identical to other [%s] according to persistence rules ",
												element.toString(), other
														.toString()));
				}
			}
		}
	}

	/**
	 * 
	 * @param xmlObject
	 * @param criterion
	 * @return
	 */
	private PropertyCriteria getElementPropertyCriteria(XmlObject xmlObject,
			PropertyExpression[] criterion) {
		// criteria construction
		PropertyCriteria criteria = new PropertyCriteria();
		criteria.setClassName(xmlObject.schemaType().getFullJavaName());

		// match element type
		PropertyCriterion typeCriterion = new PropertyCriterion();
		typeCriterion.setPropertyLocationPath("type.name");
		typeCriterion.setOperation(PropertyCriterion.Operation.equal);
		String[] types = { xmlObject.schemaType().getFullJavaName() };
		typeCriterion.setValues(types);
		criteria.addPropertyCriterion(typeCriterion);

		// active elements only
		PropertyCriterion toTimeStampCriterion = new PropertyCriterion();
		toTimeStampCriterion.setPropertyLocationPath("toTimeStamp");
		toTimeStampCriterion.setOperation(PropertyCriterion.Operation.isNull);
		criteria.addPropertyCriterion(toTimeStampCriterion);

		// add each property expression as XPath
		for (PropertyExpression propertyExpression : criterion) {
			String[] values = new String[1];
			values[0] = ((XmlAnySimpleType) xmlObject
					.selectPath(propertyExpression.toString(false))[0])
					.getStringValue();

			com.klistret.cmdb.pojo.PropertyCriterion xpathCriterion = new com.klistret.cmdb.pojo.PropertyCriterion();
			xpathCriterion.setPropertyLocationPath(String.format(
					"configuration.%s", propertyExpression
							.getPropertyLocationPath()));
			xpathCriterion.setOperation(PropertyCriterion.Operation.equal);
			xpathCriterion.setValues(values);

			criteria.addPropertyCriterion(xpathCriterion);
		}

		return criteria;
	}
}
