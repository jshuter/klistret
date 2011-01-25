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

import java.util.List;
import java.util.NoSuchElementException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.hibernate.XPathCriteria;

/**
 * 
 * @author Matthew Young
 * 
 */
public class RelationDAOImpl extends BaseImpl implements RelationDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(RelationDAOImpl.class);

	/**
	 * Finds relations based on XPath expressions
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Relation> find(List<String> expressions, int start, int limit) {
		try {
			logger
					.debug(
							"Finding relations by expression from start position [{}] with limit [{}]",
							start, limit);

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Criteria hcriteria = new XPathCriteria(expressions, getSession())
					.getCriteria();

			hcriteria.setFirstResult(start);
			hcriteria.setMaxResults(limit);

			List<Relation> relations = hcriteria.list();

			return relations;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * Get relation by unique ID
	 * 
	 * @param id
	 * @return Relation
	 */
	public Relation get(Long id) {
		try {
			logger.debug("Getting relation [id: {}] by id ", id);

			Criteria criteria = getSession().createCriteria(Relation.class);
			criteria.add(Restrictions.idEq(id));

			Relation relation = (Relation) criteria.uniqueResult();

			if (relation == null)
				throw new ApplicationException(String.format(
						"Relation [id: %s] not found", id),
						new NoSuchElementException());

			return relation;

		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * Update/save relation
	 * 
	 */
	public Relation set(Relation relation) {
		/**
		 * record current time when updating
		 */
		relation.setUpdateTimeStamp(new java.util.Date());

		try {
			getSession().saveOrUpdate("Relation", relation);
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("Save/update relation [{}]", relation.toString());

		return relation;
	}

	/**
	 * Delete a relation by setting the ToTimeStamp attribute to the current
	 * date
	 */
	public Relation delete(Long id) {
		Criteria criteria = getSession().createCriteria(Relation.class);
		criteria.add(Restrictions.idEq(id));

		Relation relation = (Relation) criteria.uniqueResult();

		if (relation == null)
			throw new ApplicationException(String.format(
					"Relation [id: %s] not found", id),
					new NoSuchElementException());

		if (relation.getToTimeStamp() != null)
			throw new ApplicationException(String.format(
					"Relation [id: %d] has already been deleted", id),
					new NoSuchElementException());

		relation.setToTimeStamp(new java.util.Date());
		relation.setUpdateTimeStamp(new java.util.Date());

		try {
			getSession().update("Relation", relation);
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("Deleted relation [{}]", relation);
		return relation;
	}

	/**
	 * Deletion of element logically result in the deletion of their relations.
	 * DML-style operation written directly in HQL quickens the update process.
	 * 
	 * @param id
	 */
	public int cascade(Long id) {
		String hqlElementDeletion = "update Relation r set r.toTimeStamp = current_timestamp() where (r.source.id = :sourceId or r.destination.id = :destinationId) and r.toTimeStamp is null";

		int count = getSession().createQuery(hqlElementDeletion).setLong(
				"sourceId", id).setLong("destinationId", id).executeUpdate();
		logger.info("Deleted {} relations to element [{}]", count, id);
		
		return count;
	}
}
