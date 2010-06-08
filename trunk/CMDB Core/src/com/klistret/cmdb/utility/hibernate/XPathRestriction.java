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

import com.klistret.cmdb.utility.saxon.Step;

/**
 * Implements Hibernate Criterion for XPath expressions
 * 
 * Static default function declarations should be edit-ready rather then hard
 * coded.
 * 
 * @author Matthew Young
 * 
 */
@SuppressWarnings("serial")
public class XPathRestriction implements Criterion {
	private static final Logger logger = LoggerFactory
			.getLogger(XPathRestriction.class);

	/**
	 * Binding property for Hibernate criteria (as stated in mapping files)
	 */
	private final String propertyName;

	/**
	 * Expression step
	 */
	private final Step step;

	/**
	 * 
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
	 * Return value for criterion without types values
	 */
	private static final TypedValue[] NO_TYPED_VALUES = new TypedValue[0];

	/**
	 * Constructor transfers over arguments to properties (variable refenese
	 * defaults to "this")
	 * 
	 * @param propertyName
	 * @param step
	 */
	public XPathRestriction(String propertyName, Step step) {
		this.propertyName = propertyName;
		this.step = step;
		this.variableReference = "this";
	}

	/**
	 * Constructor transfers over arguments to properties
	 * 
	 * @param propertyName
	 * @param xpath
	 * @param variableReference
	 */
	public XPathRestriction(String propertyName, Step step,
			String variableReference) {
		this.propertyName = propertyName;
		this.step = step;
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
							"XMLEXISTS may only be used with single-column properties [property: {}]",
							propertyName);
			throw new HibernateException(
					"XMLEXISTS may only be used with single-column properties");
		}

		String xpath = String.format("$%s/%s", variableReference, step
				.getRemainingXPath());
		logger
				.debug(
						"XPath [{}] prior prefixing default function declaration and namespace declarations",
						xpath);

		for (String namespace : step.getPathExpression().getNamespaces())
			xpath = namespace.concat(xpath);

		if (step.getPathExpression().getDefaultElementNamespace() != null)
			xpath = step.getPathExpression().getDefaultElementNamespace()
					.concat(xpath);

		if (dialect instanceof DB2Dialect) {
			xpath = String.format("declare default function namespace \"%s\";",
					DB2DefaultFunctionNamespace).concat(xpath);
			logger.debug("XPath [{}] prior to returning XMLEXISTS clause",
					xpath);

			return String.format("XMLEXISTS(\'%s\' PASSING %s AS \"%s\")",
					xpath, columns[0], variableReference);
		}

		if (dialect instanceof Oracle9iDialect) {
			xpath = String.format("declare default function namespace \"%s\";",
					OracleDefaultFunctionNamespace).concat(xpath);
			logger.debug("XPath [{}] prior to returning XMLEXISTS clause",
					xpath);

			return String.format("XMLExists(\'%s\' PASSING %s AS \"%s\")",
					xpath, columns[0], variableReference);
		}

		throw new HibernateException(String.format(
				"Dialect [%s] not supported for xpath expression", dialect));
	}

	/**
	 * Returns NO_TYPED_VALUES constant
	 */
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
						"XPath [%s] against property [%s] with variable reference [%s]",
						step.getRemainingXPath(), propertyName,
						variableReference);
	}
}
