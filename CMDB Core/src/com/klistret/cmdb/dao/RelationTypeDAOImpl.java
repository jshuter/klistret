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

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.StaleStateException;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;

/**
 * 
 * @author Matthew Young
 * 
 */
public class RelationTypeDAOImpl extends BaseImpl implements RelationTypeDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(RelationTypeDAOImpl.class);

	/**
	 * Get relation type by composite ID (name) and the to-timestamp is forced
	 * to be null.
	 * 
	 * @param name
	 *            relation type
	 * @return RelationType
	 * @throws InfrastructureException
	 *             when Hibernate criteria does not return a unique result
	 */
	public RelationType get(String name) {
		logger.debug("Getting relation type by composite id [{}]", name);

		Criteria criteria = getSession().createCriteria(RelationType.class);
		criteria.add(Restrictions.isNull("toTimeStamp"));
		criteria.add(Restrictions.eq("name", name));

		try {
			RelationType relationType = (RelationType) criteria.uniqueResult();

			if (relationType != null)
				logger.debug("Found relation type [{}]",
						relationType.toString());

			return relationType;
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Uses ILike expression to match by name and the to-timestamp is forced to
	 * be null.
	 * 
	 * @param name
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	public List<RelationType> find(String name) {
		logger.debug("Finding relation type by composite id [{}]", name);

		if (name == null)
			throw new ApplicationException("Name parameter is null",
					new IllegalArgumentException());

		try {
			Criteria query = getSession().createCriteria(RelationType.class);

			query.add(Restrictions.ilike("name", name));
			query.add(Restrictions.isNull("toTimeStamp"));

			return query.list();
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	public RelationType set(RelationType relationType) {
		relationType.setUpdateTimeStamp(new Date());

		try {
			if (relationType.getId() != null)
				relationType = (RelationType) getSession().merge(
						"RelationType", relationType);
			else
				getSession().saveOrUpdate("RelationType", relationType);
		} catch (StaleStateException e) {
			throw new ApplicationException(e.getMessage(), e);
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}

		logger.info("Save/update relation type [{}]", relationType.toString());
		return relationType;
	}
}
