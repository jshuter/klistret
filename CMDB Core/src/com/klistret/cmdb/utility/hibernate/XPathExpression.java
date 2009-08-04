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

@SuppressWarnings("serial")
public class XPathExpression implements Criterion {
	/**
	 * 
	 */
	private final String propertyName;

	/**
	 * 
	 */
	private final String xpath;

	/**
	 * 
	 */
	private final String variableReference;

	/**
	 * 
	 */
	private final String DB2DefaultFunctionNamespace = "http://www.ibm.com/xmlns/prod/db2/functions";

	/**
	 * 
	 */
	private final String OracleDefaultFunctionNamespace = "http://xmlns.oracle.com/xdb";

	/**
	 * 
	 */
	private final String reDefaultFunctionNamespace = "declare\\s+default\\s+function\\s+namespace\\s+\\'(http(s?):\\/\\/|(www.))([a-z0-9\\/_;:%#=&?@\\-.]+[a-z0-9\\/_#=&?\\-])\\'";

	/**
	 * 
	 */
	private static final TypedValue[] NO_TYPED_VALUES = new TypedValue[0];

	public XPathExpression(String propertyName, String xpath,
			String variableReference) {
		this.propertyName = propertyName;
		this.xpath = xpath;
		this.variableReference = variableReference;
	}

	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		Dialect dialect = criteriaQuery.getFactory().getDialect();
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
				propertyName);

		if (columns.length != 1)
			throw new HibernateException(
					"xpathExists may only be used with single-column properties");

		if (dialect instanceof DB2Dialect) {
			String db2Xpath = xpath.replaceAll(reDefaultFunctionNamespace,
					String.format("declare default function namespace \'%s\';",
							DB2DefaultFunctionNamespace));

			return String.format("XMLEXISTS(\'%s\') PASSING %s AS \"%s\"",
					db2Xpath, columns[0], variableReference);
		}

		if (dialect instanceof Oracle9iDialect) {
			String oracleXpath = xpath.replaceAll(reDefaultFunctionNamespace,
					String.format("declare default function namespace \'%s\';",
							OracleDefaultFunctionNamespace));

			return String.format("XMLExists(\'%s\') PASSING %s AS \"%s\"",
					oracleXpath, columns[0], variableReference);
		}

		throw new HibernateException(String.format(
				"dialect [%s] not supported for xpath expression", dialect
						.toString()));
	}

	public TypedValue[] getTypedValues(Criteria criteria,
			CriteriaQuery criteriaQuery) throws HibernateException {
		return NO_TYPED_VALUES;
	}

	public String toString() {
		return String
				.format(
						"xpath [%s] against property [%s] with variable reference [%s]",
						xpath, propertyName, variableReference);
	}
}
