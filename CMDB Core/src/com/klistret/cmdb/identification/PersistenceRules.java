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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.pojo.Element;
import com.klistret.cmdb.utility.hibernate.XPathExpression;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper;
import com.klistret.cmdb.xmlbeans.PersistenceRulesDocument;
import com.klistret.cmdb.xmlbeans.PropertyCriterion;

public class PersistenceRules {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceRules.class);

	private PersistenceRulesDocument persistenceRulesDocument;

	/**
	 * 
	 * @param url
	 */
	public PersistenceRules(URL url) {
		try {
			this.persistenceRulesDocument = (PersistenceRulesDocument) XmlObject.Factory
					.parse(url);
		} catch (XmlException e) {
			logger.error("URL [{}] failed parsing; {}", url, e);
			throw new InfrastructureException(e.getMessage());
		} catch (IOException e) {
			logger.error("URL [{}] failed parsing: {}", url, e);
			throw new InfrastructureException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param xmlObject
	 * @return Hibernate detached criteria
	 */
	public DetachedCriteria getDetachedCriteria(XmlObject xmlObject) {
		DetachedCriteria query = DetachedCriteria.forClass(Element.class, "e");

		/**
		 * limit by class name through elementType relation
		 */
		query.createAlias("type", "etype");
		query.add(Restrictions.eq("etype.name", xmlObject.schemaType()
				.getFullJavaName()));

		/**
		 * limit to only active elements
		 */
		query.add(Restrictions.isNull("e.toTimeStamp"));

		/**
		 * limit with property expression criterion
		 */
		String[] xpaths = getXPathCriterion(xmlObject);
		for (String xpath : xpaths) {
			query.add(new XPathExpression("e.configuration", xpath, "this"));
		}

		return query;
	}

	/**
	 * 
	 * @param xmlObject
	 * @return XPath criterion array
	 */
	public String[] getXPathCriterion(XmlObject xmlObject) {
		PropertyExpression[] propertyExpressionCriterion = getPropertyExpressionCriterion(xmlObject);

		List<String> xpathCriterion = new ArrayList<String>(
				propertyExpressionCriterion.length);
		for (PropertyExpression propertyExpression : propertyExpressionCriterion) {
			String value = ((XmlAnySimpleType) xmlObject
					.selectPath(propertyExpression.toString())[0])
					.getStringValue();
			xpathCriterion.add(propertyExpression.equal(value));
		}

		return xpathCriterion.toArray(new String[0]);
	}

	/**
	 * 
	 * @param propertyExpressionCriterion
	 * @param xmlObject
	 * @return true/false
	 */
	private boolean validatePropertyExpressionCriterion(
			PropertyExpression[] propertyExpressionCriterion,
			XmlObject xmlObject) {
		for (PropertyExpression propertyExpression : propertyExpressionCriterion) {
			String xpath = propertyExpression.toString();

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
	 * 
	 * @param xmlObject
	 * @return Property Expression array (valid criterion based on order and if
	 *         the XmlObject includes the selected properties)
	 */
	public PropertyExpression[] getPropertyExpressionCriterion(
			XmlObject xmlObject) {
		String classname = xmlObject.schemaType().getFullJavaName();

		List<PropertyExpression[]> propertyExpressionCriteria = getPropertyExpressionCriteria(classname);
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

	/**
	 * 
	 * @param classname
	 * @return Property Expression array list (criteria in order)
	 */
	public List<PropertyExpression[]> getPropertyExpressionCriteria(
			String classname) {
		/**
		 * return ordered list of base types (ascending) based on fully
		 * qualified class-name
		 */
		SchemaType[] baseSchemaTypes = SchemaTypeHelper
				.getBaseSchemaTypes(classname);

		/**
		 * construct schema type list for query
		 */
		String schemaTypesList = String.format("\'%s\'", classname);
		for (SchemaType baseSchemaType : baseSchemaTypes)
			schemaTypesList = schemaTypesList.concat(String.format(",\'%s\'",
					baseSchemaType.getFullJavaName()));

		String namespaces = "declare namespace cmdb=\'http://www.klistret.com/cmdb\';";

		/**
		 * positional variables only allowed for "for" clause and the order
		 * should be ascending to the base class (type). Necessary to order the
		 * returned property criterion by class then the order attribute.
		 */
		String xquery = "for $types at $typesIndex in ("
				+ schemaTypesList
				+ ") "
				+ "for $binding in $this/cmdb:PersistenceRules/cmdb:Binding[not(cmdb:ExclusionType = \'"
				+ classname
				+ "\')] "
				+ "for $criterion in $this/cmdb:PersistenceRules/cmdb:PropertyCriterion "
				+ "where $binding/cmdb:Type = $types "
				+ "and $binding/cmdb:PropertyCriterion = $criterion/@Name "
				+ "order by $typesIndex, $binding/@Order empty greatest "
				+ "return $criterion";

		/**
		 * validate (by order) each property criteria against the passed
		 * xmlObject (can not cast execQuery results to a specific schema type)
		 */
		XmlObject[] propertyCriteria = persistenceRulesDocument
				.execQuery(namespaces + xquery);
		List<PropertyExpression[]> propertyExpressionArrayList = new ArrayList<PropertyExpression[]>(
				propertyCriteria.length);

		for (XmlObject xo : propertyCriteria) {
			try {
				/**
				 * cast XmlObject to PropertyCriterion through PersistenceRules
				 * xml-fragment
				 */
				PropertyCriterion propertyCriterion = com.klistret.cmdb.xmlbeans.PersistenceRules.Factory
						.parse(xo.xmlText()).getPropertyCriterionArray(0);

				/**
				 * construct property expression array
				 */
				List<PropertyExpression> propertyExpressions = new ArrayList<PropertyExpression>(
						propertyCriterion.getPropertyLocationPathArray().length);
				for (String propertyLocationPath : propertyCriterion
						.getPropertyLocationPathArray()) {
					PropertyExpression propertyExpression = new PropertyExpression(
							classname, propertyLocationPath);

					propertyExpressions.add(propertyExpression);
				}

				/**
				 * add property expression array to return list (logging the
				 * criterion name plus the list index)
				 */
				propertyExpressionArrayList.add(propertyExpressions
						.toArray(new PropertyExpression[0]));
				logger
						.debug(
								"Added property expression [criterion name: {}] array to criteria list at [{}]",
								propertyCriterion.getName(),
								propertyExpressionArrayList.size());
			} catch (XmlException e) {
				logger.error(
						"Fail parse XmlObject [{}] to PropertyCriterion: {}",
						xo.xmlText(), e);
				throw new InfrastructureException(e);
			}
		}

		return propertyExpressionArrayList;
	}
}