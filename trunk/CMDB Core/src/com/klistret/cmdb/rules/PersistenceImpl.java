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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.sxpath.IndependentContext;

import org.jvnet.jaxb.reflection.util.QNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.pojo.PersistenceRules;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.annotations.Timer;
import com.klistret.cmdb.utility.jaxb.CIContextHelper;

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

	private PersistenceRules persistenceRules;

	private CIContextHelper ciContextHelper;

	public PersistenceRules getPersistenceRules() {
		return persistenceRules;
	}

	public CIContextHelper getCIContextHelper() {
		return ciContextHelper;
	}

	public void setCIContextHelper(CIContextHelper ciContextHelper) {
		this.ciContextHelper = ciContextHelper;
	}

	public PersistenceImpl(URL url) {
		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();
		xqc.compile("");
	}

	/**
	 * List all property expressions for the passed java class representing a
	 * SchemaType
	 * 
	 * Candidate for method caching instead of running FLOWR query every time
	 * 
	 * @param classname
	 * @return Criteria
	 */
	@Timer
	public List<String[]> getCriterionByQName(QName qname) {

		String qnames = String.format("\'%s\'", qname);
		
		XMLBean xmlBean = ciContextHelper.getXMLBean(qname);
		while (xmlBean.getExtended() != null) {
			qnames = qnames.concat(String.format(",\'%s\'", xmlBean
					.getExtended()));
			xmlBean = ciContextHelper.getXMLBean(xmlBean.getExtended());
		}

		String namespaces = "declare namespace persistence=\'http://www.klistret.com/cmdb/ci/persistence\';";

		String xquery = String.format("for $qnames at $qnameIndex in (%s) "
				+ "for $rule in $this/persistence:PersistenceRules/persistence:Rule[not(persistence:Exclusions = \'"
				+ "%s\')] "
				+ "for $criterion in $this/persistence:PersistenceRules/persistence:Criterion "
				+ "where $rule/persistence:QName = $qnames "
				+ "and $rule/persistence:Criterion = $criterion/@Name "
				+ "order by $qnameIndex, $rule/@Order empty greatest "
				+ "return $criterion", qnames, qname);
		
		ciContextHelper.getJAXBContext().createBinder().getXMLNode(persistenceRules);

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

	/**
	 * Return first valid criterion for the passed XmlObject
	 * 
	 * @param xmlObject
	 * @return Criterion
	 */
	@Timer
	public PropertyExpression[] getCriterionByXmlObject(XmlObject xmlObject,
			List<PropertyExpression[]> criteria) {

		for (int index = 0; index < criteria.size(); index++) {
			if (validateCriterion(criteria.get(index), xmlObject)) {
				logger
						.debug(
								"valid property expression criterion at index [{}] in criteria list for xmlObject [xml: {}]",
								index + 1, xmlObject.xmlText());
				return criteria.get(index);
			}
		}

		logger.debug(
				"no property expression criterion for xmlObject [xml: {}]",
				xmlObject.xmlText());
		return new PropertyExpression[0];
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
	private boolean validateCriterion(PropertyExpression[] criterion,
			XmlObject xmlObject) {
		for (PropertyExpression propertyExpression : criterion) {
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
}
