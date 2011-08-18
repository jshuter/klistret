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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.saxon.Step;

/**
 * Implements Hibernate Criterion for XPath expressions acting on a property
 * given a Step. The initial step's name is switched out with a wildcard since
 * the underlying schema type is unknown.
 * 
 * 
 * TODO Postgresql/Oracle have to be integrated soon.
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
	 * Variable reference that associates the database column to the XPath.
	 */
	private final String variableReference;

	/**
	 * Return value for criterion without types values
	 */
	private static final TypedValue[] NO_TYPED_VALUES = new TypedValue[0];

	/**
	 * 
	 */
	private static final Pattern singleQuotes = Pattern
			.compile("'((?:[^']+|'')*)'");

	/**
	 * Constructor transfers over arguments to properties (variable refenese
	 * defaults to "this")
	 * 
	 * @param propertyName
	 * @param step
	 */
	public XPathRestriction(String propertyName, Step step) {
		this(propertyName, step, "this");
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
			logger.error(
					"XMLEXISTS may only be used with single-column properties [property: {}]",
					propertyName);
			throw new HibernateException(
					"XMLEXISTS may only be used with single-column properties");
		}

		/**
		 * A variable reference from SQL into XQuery is necessary
		 */
		String xpath = String.format("$%s", variableReference);

		/**
		 * Property type is generalized to a "*" and the user is left to add
		 * type attributes to the predicate
		 */
		String axis = String.format("%s:%s", step.getQName().getPrefix(), step
				.getQName().getLocalPart());
		if (step.getRemainingXPath() != null)
			xpath = String.format("%s/%s/%s", xpath, step.getXPath()
					.replaceFirst(axis, "*"), step.getRemainingXPath());
		else
			xpath = String.format("%s/%s", xpath,
					step.getXPath().replaceFirst(axis, "*"));

		logger.debug(
				"XPath [{}] prior prefixing default function declaration and namespace declarations",
				xpath);

		/**
		 * Concatenate namespace declarations
		 */
		for (String namespace : step.getPathExpression().getNamespaces())
			xpath = namespace.concat(xpath);

		/**
		 * Concatenate default element namespace declaration
		 */
		if (step.getPathExpression().getDefaultElementNamespace() != null)
			xpath = step.getPathExpression().getDefaultElementNamespace()
					.concat(xpath);

		/**
		 * Depending on the database dialect concatenate the specific default
		 * funtion namespace
		 */
		if (dialect instanceof DB2Dialect) {
			/**
			 * DB2 only allows SQL with double quotes (or at least that is the
			 * extend of my knowledge)
			 */
			Matcher sq = singleQuotes.matcher(xpath);
			if (sq.find()) {
				throw new ApplicationException(
						String.format(
								"XPath [%s] contains surrounding single quotes which DB2 does not allow",
								xpath), new UnsupportedOperationException());
			}

			/**
			 * Passing clause
			 */
			String passing = String.format("PASSING %s AS \"%s\"", columns[0],
					variableReference);

			/**
			 * Return the XMLEXISTS predicate
			 */
			return String.format("XMLEXISTS(\'%s\' %s)", xpath, passing);
		}

		if (dialect instanceof Oracle9iDialect) {
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
				.format("XPath [%s] against property [%s] with variable reference [%s]",
						step.getRemainingXPath(), propertyName,
						variableReference);
	}
}
