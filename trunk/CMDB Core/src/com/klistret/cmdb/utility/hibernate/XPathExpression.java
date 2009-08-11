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

package com.klistret.cmdb.utility.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.engine.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Hibernate Criterion for XPath expressions
 * 
 * @author Matthew Young
 * 
 */
@SuppressWarnings("serial")
public class XPathExpression implements Criterion {
	private static final Logger logger = LoggerFactory
			.getLogger(XPathExpression.class);

	/**
	 * Binding property for Hibernate criteria (as stated in mapping files)
	 */
	private final String propertyName;

	/**
	 * XPath statement
	 */
	private final String xpath;

	/**
	 * XPath statements have (currently) support for a single context variable
	 * reference
	 */
	private final String variableReference;

	/**
	 * Default Function name-space for DB2 Viper (9 version)
	 */
	private final String DB2DefaultFunctionNamespace = "http://www.ibm.com/xmlns/prod/db2/functions";

	/**
	 * Default Function name-space for Oracle (11g version)
	 */
	private final String OracleDefaultFunctionNamespace = "http://xmlns.oracle.com/xdb";

	/**
	 * Regular Expression matches name-space declarations
	 */
	private final String reDefaultFunctionNamespace = "declare\\s+default\\s+function\\s+namespace\\s+\"(http(s?):\\/\\/|(www.))([a-z0-9\\/_;:%#=&?@\\-.]+[a-z0-9\\/_#=&?\\-])\"";

	/**
	 * Return value for criterion without types values
	 */
	private static final TypedValue[] NO_TYPED_VALUES = new TypedValue[0];

	/**
	 * Constructor transfers over arguments to properties
	 * 
	 * @param propertyName
	 * @param xpath
	 * @param variableReference
	 */
	public XPathExpression(String propertyName, String xpath,
			String variableReference) {
		this.propertyName = propertyName;
		this.xpath = xpath;
		this.variableReference = variableReference;
	}

	/**
	 * Depending on the database dialect (only DB2/Oracle supported currently)
	 * the XmlExists clause is built from the passed XPath and variable
	 * reference.
	 * 
	 * @param criteria
	 * @param criteriaQuery
	 * @return String
	 */
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		Dialect dialect = criteriaQuery.getFactory().getDialect();
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
				propertyName);

		if (columns.length != 1) {
			logger
					.error(
							"xpathExists may only be used with single-column properties [property: {}]",
							propertyName);
			throw new HibernateException(
					"xpathExists may only be used with single-column properties");
		}

		logger.debug("xpath [{}] prior to applying dialect", xpath);

		if (dialect instanceof DB2Dialect) {
			String db2Xpath = xpath.replaceAll(reDefaultFunctionNamespace, "");
			db2Xpath = String.format(
					"declare default function namespace \"%s\";",
					DB2DefaultFunctionNamespace).concat(xpath);

			return String.format("XMLEXISTS(\'%s\' PASSING %s AS \"%s\")",
					db2Xpath, columns[0], variableReference);
		}

		if (dialect instanceof Oracle9iDialect) {
			String oracleXpath = xpath.replaceAll(reDefaultFunctionNamespace,
					"");
			oracleXpath = String.format(
					"declare default function namespace \"%s\";",
					OracleDefaultFunctionNamespace).concat(xpath);

			return String.format("XMLExists(\'%s\' PASSING %s AS \"%s\")",
					oracleXpath, columns[0], variableReference);
		}

		logger.error("dialect [{}] not supported for xpath expression", dialect
				.toString());
		throw new HibernateException(String.format(
				"dialect [%s] not supported for xpath expression", dialect
						.toString()));
	}

	public TypedValue[] getTypedValues(Criteria criteria,
			CriteriaQuery criteriaQuery) throws HibernateException {
		return NO_TYPED_VALUES;
	}

	/**
	 * More information for debugging
	 * 
	 * @return String
	 */
	public String toString() {
		return String
				.format(
						"xpath [%s] against property [%s] with variable reference [%s]",
						xpath, propertyName, variableReference);
	}
}
