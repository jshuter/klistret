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
import org.hibernate.engine.TypedValue;

import com.klistret.cmdb.pojo.PropertyXPathExpression;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

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
	public TypedValue[] getTypedValues(Criteria args, CriteriaQuery factory)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toSqlString(Criteria args, CriteriaQuery factory)
			throws HibernateException {

		PropertyExpression expression = new PropertyExpression(
				propertyXPathExpression.getQName(), propertyXPathExpression
						.getPath());

		return null;
	}

}
