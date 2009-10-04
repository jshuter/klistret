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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;

/**
 * 
 * @author Matthew Young
 * 
 */
public class ElementTypeDAOImpl extends BaseImpl implements ElementTypeDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementTypeDAOImpl.class);

	/**
	 * Uses ILike expression to match by name
	 * 
	 * @param name
	 * @return Integer
	 */
	public Integer countByName(String name) {
		try {
			Criteria query = getSession().createCriteria("ElementType");

			query.add(Restrictions.ilike("name", name));
			query.add(Restrictions.isNull("toTimeStamp"));

			query.setProjection(Projections.rowCount());

			return (Integer) query.list().iterator().next();
		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException running count query by name [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * Uses ILike expression to match by name
	 * 
	 * @param name
	 * @return Collection
	 */
	@SuppressWarnings("unchecked")
	public Collection<com.klistret.cmdb.pojo.ElementType> findByName(
			String name) {
		try {
			Criteria query = getSession().createCriteria("ElementType");

			query.add(Restrictions.ilike("name", name));
			query.add(Restrictions.isNull("toTimeStamp"));

			return query.list();
		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException running criteria [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * 
	 * @param name
	 * @return ElementType
	 */
	public com.klistret.cmdb.pojo.ElementType getByCompositeId(
			String name) {
		logger.debug("getting element type by composite id [{}]", name);

		Criteria criteria = getSession().createCriteria(
				com.klistret.cmdb.pojo.ElementType.class);

		criteria.add(Restrictions.isNull("toTimeStamp"));
		criteria.add(Restrictions.eq("name", name));

		try {
			com.klistret.cmdb.pojo.ElementType elementType = (com.klistret.cmdb.pojo.ElementType) criteria
					.uniqueResult();

			if (elementType != null) {
				logger.debug("found element type [{}]", elementType.toString());
			}

			return elementType;
		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException getting by id [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

}