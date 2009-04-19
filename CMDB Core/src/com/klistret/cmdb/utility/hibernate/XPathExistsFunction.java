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
import org.hibernate.engine.TypedValue;

import com.klistret.cmdb.utility.xmlbeans.Expression;

@SuppressWarnings("serial")
public class XPathExistsFunction implements Criterion {
	private final String propertyName;
	private final Expression expression;
	private final String xpath;
	private final Object value;
	private final boolean functional;

	public XPathExistsFunction(String propertyName, Expression expression,
			String xpath, Object value, boolean functional) {
		this.propertyName = propertyName;
		this.expression = expression;
		this.xpath = xpath;
		this.value = value;
		this.functional = functional;
	}

	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		Dialect dialect = criteriaQuery.getFactory().getDialect();
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
				propertyName);

		if (columns.length != 1)
			throw new HibernateException(
					"xpathExists may only be used with single-column properties");

		if (dialect instanceof DB2Dialect) {
			if (functional)
				expression
						.setDefaultFunctionPrefix("http://www.ibm.com/xmlns/prod/db2/functions");

			return String.format("XMLEXISTS(\'%s %s\') PASSING %s AS \"%s\"",
					expression.getDeclareClause(), xpath, columns[0],
					expression.getContext());
		}

		throw new HibernateException(String.format(
				"dialect [%s] not supported for xpath exists function", dialect
						.toString()));
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria,
			CriteriaQuery criteriaQuery) throws HibernateException {
		return new TypedValue[] { criteriaQuery.getTypedValue(criteria,
				propertyName, value) };
	}
}
