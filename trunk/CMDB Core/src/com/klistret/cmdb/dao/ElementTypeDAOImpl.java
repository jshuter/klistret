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

package com.klistret.cmdb.dao;

import java.util.Collection;
import java.util.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.klistret.cmdb.exception.InfrastructureException;

public class ElementTypeDAOImpl extends BaseImpl implements ElementTypeDAO {

	private final static Logger logger = Logger.getLogger(ElementTypeDAOImpl.class
			.getName());

	public Integer countByCriteria(com.klistret.cmdb.pojo.PropertyCriteria criteria) {
		try {
			Criteria hcriteria = criteria.getCriteria(getSession());
			hcriteria.setProjection(Projections.rowCount());

			return (Integer) hcriteria.list().iterator().next();
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<com.klistret.cmdb.pojo.ElementType> findByCriteria(
			com.klistret.cmdb.pojo.PropertyCriteria criteria) {
		try {
			Criteria hcriteria = criteria.getCriteria(getSession());

			return hcriteria.list();
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	public com.klistret.cmdb.pojo.ElementType getByCompositeId(String name) {
		logger.fine("getting element type by composite id [" + name + "]");

		Criteria criteria = getSession().createCriteria(
				com.klistret.cmdb.pojo.ElementType.class);

		criteria.add(Restrictions.isNull("toTimeStamp"));
		criteria.add(Restrictions.eq("name", name));

		try {
			com.klistret.cmdb.pojo.ElementType elementType = (com.klistret.cmdb.pojo.ElementType) criteria
					.uniqueResult();

			if (elementType != null) {
				logger.fine("found element type [" + elementType.toString()
						+ "]");
			}

			return elementType;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

}
