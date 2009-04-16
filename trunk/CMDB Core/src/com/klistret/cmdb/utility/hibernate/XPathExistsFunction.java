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

import com.klistret.cmdb.pojo.PropertyXPathExpression;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.utility.xmlbeans.XPathOperators;

@SuppressWarnings("serial")
public class XPathExistsFunction implements Criterion {
	private final String propertyName;
	private final PropertyXPathExpression propertyXPathExpression;

	public XPathExistsFunction(String propertyName,
			PropertyXPathExpression propertyXPathExpression) {
		this.propertyName = propertyName;
		this.propertyXPathExpression = propertyXPathExpression;
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

		PropertyExpression expression = new PropertyExpression(
				propertyXPathExpression.getQName(), propertyXPathExpression
						.getPath());

		if (propertyXPathExpression.isFunction()) {
			if (dialect instanceof DB2Dialect)
				expression
						.setDefaultFunctionPrefix("http://www.ibm.com/xmlns/prod/db2/functions");

			if (dialect instanceof Oracle9iDialect)
				expression.setDefaultFunctionPrefix("");
		}

		switch (propertyXPathExpression.getComparison()) {
		case Equal:
			XPathOperators.equal(expression, "?");
			break;
		}

		return null;
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria,
			CriteriaQuery criteriaQuery) throws HibernateException {
		return new TypedValue[] { criteriaQuery.getTypedValue(criteria,
				propertyName, propertyXPathExpression.getValue().toString()
						.toLowerCase()) };
	}
}
