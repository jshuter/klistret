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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
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
	 * 
	 */
	public List<Relation> find(List<String> expressions, int start, int limit) {
		try {
			logger
					.debug(
							"Finding relations by expression from start position [{}] with limit [{}]",
							start, limit);

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null");

			Criteria hcriteria = new XPathCriteria(expressions, getSession())
					.getCriteria();
			String alias = hcriteria.getAlias();

			hcriteria.setProjection(Projections.projectionList().add(
					Projections.property(alias + ".id")).add(
					Projections.property(alias + ".type")).add(
					Projections.property(alias + ".name")).add(
					Projections.property(alias + ".fromTimeStamp")).add(
					Projections.property(alias + ".toTimeStamp")).add(
					Projections.property(alias + ".createId")).add(
					Projections.property(alias + ".createTimeStamp")).add(
					Projections.property(alias + ".updateTimeStamp")).add(
					Projections.property(alias + ".configuration")));

			hcriteria.setFirstResult(start);
			hcriteria.setMaxResults(limit);

			Object[] results = hcriteria.list().toArray();

			List<Relation> relations = new ArrayList<Relation>(results.length);
			logger.debug("Results length [{}]", results.length);

			for (int index = 0; index < results.length; index++) {
				Object[] row = (Object[]) results[index];

				Relation relation = new Relation();
				relation.setId((Long) row[0]);
				relation.setType((RelationType) row[1]);
				relation.setName((String) row[2]);
				relation.setFromTimeStamp((Date) row[3]);
				relation.setToTimeStamp((Date) row[4]);
				relation.setCreateId((String) row[5]);
				relation.setCreateTimeStamp((Date) row[6]);
				relation.setUpdateTimeStamp((Date) row[7]);
				relation
						.setConfiguration((com.klistret.cmdb.ci.commons.Relation) row[8]);

				relations.add(relation);
			}

			results = null;

			return relations;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * 
	 * @param id
	 * @return Relation
	 */
	public Relation get(Long id) {
		try {
			logger.debug("Getting relation [id: {}] by id ", id);

			Criteria criteria = getSession().createCriteria(Relation.class);
			criteria.add(Restrictions.idEq(id));

			Relation proxy = (Relation) criteria.uniqueResult();
			logger.debug("Found relation [id: {}] by id ", id);

			if (proxy == null)
				throw new ApplicationException(String.format(
						"relation [id: %s] does not exist", id));

			Relation relation = new Relation();
			relation.setId(proxy.getId());
			relation.setType(proxy.getType());
			relation.setName(proxy.getName());
			relation.setFromTimeStamp(proxy.getFromTimeStamp());
			relation.setToTimeStamp(proxy.getToTimeStamp());
			relation.setCreateId(proxy.getCreateId());
			relation.setCreateTimeStamp(proxy.getCreateTimeStamp());
			relation.setUpdateTimeStamp(proxy.getUpdateTimeStamp());
			relation.setConfiguration(proxy.getConfiguration());

			proxy = null;

			return relation;

		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
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
	 * Delete an element by setting the ToTimeStamp attribute to the current
	 * date
	 */
	public void delete(Long id) {
		Relation relation = get(id);

		if (relation.getToTimeStamp() != null)
			throw new ApplicationException(String.format(
					"Relation [id: %d] has already been deleted", id));

		relation.setToTimeStamp(new java.util.Date());
		relation.setUpdateTimeStamp(new java.util.Date());

		try {
			getSession().update("Relation", relation);
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("Deleted relation [{}]", relation);
	}
}
