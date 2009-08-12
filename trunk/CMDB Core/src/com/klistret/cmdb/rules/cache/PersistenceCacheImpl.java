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

package com.klistret.cmdb.rules.cache;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.annotations.Timer;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper;
import com.klistret.cmdb.xmlbeans.PersistenceRulesDocument;
import com.klistret.cmdb.xmlbeans.PropertyCriterion;

/**
 * 
 * @set com.klistret.cmdb.rules.PersistenceImpl
 * @author Matthew Young
 * 
 */
public class PersistenceCacheImpl implements PersistenceCache {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceCacheImpl.class);
	/**
	 * XmlObject persistence rules document
	 */
	private PersistenceRulesDocument persistenceRulesDocument;

	/**
	 * Constructor parses the URL into a XmlObject document
	 * 
	 * @param url
	 */
	public PersistenceCacheImpl(URL url) {
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
	 * List all property expressions for the passed java class representing a
	 * SchemaType
	 * 
	 * Candidate for method caching instead of running FLOWR query every time
	 * 
	 * @param classname
	 * @return Property Expression array list (criteria in order)
	 */
	@Timer
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
