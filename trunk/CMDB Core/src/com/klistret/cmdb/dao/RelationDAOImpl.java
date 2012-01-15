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
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.StaleStateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.hibernate.XPathCriteria;
import com.klistret.cmdb.utility.jaxb.CIContext;

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
	public List<Relation> find(List<String> expressions, int start, int limit) {
		try {
			logger.debug(
					"Finding relations by expression from start position [{}] with limit [{}]",
					start, limit);

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Criteria criteria = new XPathCriteria(expressions, getSession())
					.getCriteria();
			String alias = criteria.getAlias();

			criteria.setProjection(Projections.projectionList()
					.add(Projections.property(alias + ".id"))
					.add(Projections.property(alias + ".type"))
					.add(Projections.property(alias + ".source"))
					.add(Projections.property(alias + ".destination"))
					.add(Projections.property(alias + ".fromTimeStamp"))
					.add(Projections.property(alias + ".toTimeStamp"))
					.add(Projections.property(alias + ".createId"))
					.add(Projections.property(alias + ".createTimeStamp"))
					.add(Projections.property(alias + ".updateTimeStamp"))
					.add(Projections.property(alias + ".version"))
					.add(Projections.property(alias + ".configuration")));

			criteria.setFirstResult(start);
			criteria.setMaxResults(limit);
			criteria.setFetchSize(limit);

			Object[] results = criteria.list().toArray();

			List<Relation> relations = new ArrayList<Relation>(results.length);
			logger.debug("Results length [{}]", results.length);

			for (int index = 0; index < results.length; index++) {
				Object[] row = (Object[]) results[index];

				Relation relation = new Relation();
				relation.setId((Long) row[0]);
				relation.setType((RelationType) row[1]);
				relation.setSource((Element) row[2]);
				relation.setDestination((Element) row[3]);
				relation.setFromTimeStamp((Date) row[4]);
				relation.setToTimeStamp((Date) row[5]);
				relation.setCreateId((String) row[6]);
				relation.setCreateTimeStamp((Date) row[7]);
				relation.setUpdateTimeStamp((Date) row[8]);
				relation.setVersion((Long) row[9]);
				relation.setConfiguration((com.klistret.cmdb.ci.commons.Relation) row[9]);

				relations.add(relation);
			}

			results = null;

			return relations;
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Relation(s) found are stale which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Query unique over XPath expressions
	 */
	public Relation unique(List<String> expressions) {
		try {
			logger.debug("Query unique over XPath expressions");

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Integer count = count(expressions);
			if (count == 0)
				return null;

			if (count > 1)
				throw new ApplicationException(String.format(
						"Expressions criteria was not unique: %d", count));

			List<Relation> results = find(expressions, 0, 1);
			Relation relation = results.get(0);

			logger.debug("Returning unique relation [id: {},  version: {}]",
					new Object[] { relation.getId(), relation.getVersion() });
			return relation;
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Unique relation stale which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Query count of XPath expressions
	 */
	public Integer count(List<String> expressions) {
		try {
			logger.debug("Query count of XPath expressions");

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Criteria hcriteria = new XPathCriteria(expressions, getSession())
					.getCriteria();
			hcriteria.setProjection(Projections.rowCount());

			return ((Long) hcriteria.list().get(0)).intValue();
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Relation(s) counted are stale which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
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

			logger.debug("Returning element by id [id: {},  version: {}]",
					new Object[] { relation.getId(), relation.getVersion() });

			return relation;
		} catch (StaleStateException e) {
			throw new ApplicationException(
					String.format(
							"Got relation [id: %s] is stale which means newer version exists (Hibernate).",
							id));
		} catch (HibernateOptimisticLockingFailureException e) {
			throw new ApplicationException(
					String.format(
							"Got relation [id: %s] is stale which means newer version exists (Spring).",
							id));
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Update/save relation
	 * 
	 */
	public Relation set(Relation relation) {
		/**
		 * pojo type matches the configuration qname
		 */
		String typeClassName = CIContext.getCIContext()
				.getBean(QName.valueOf(relation.getType().getName()))
				.getJavaClass().getName();
		if (!typeClassName.equals(relation.getConfiguration().getClass()
				.getName()))
			throw new ApplicationException(
					String.format(
							"Pojo element type [%s] does not match the configuration type [%s]",
							typeClassName, relation.getConfiguration()
									.getClass().getName()));

		/**
		 * Change update date time
		 */
		relation.setUpdateTimeStamp(new java.util.Date());

		try {
			logger.debug("Setting relation [id: {}, version: {}]",
					new Object[] { relation.getId(), relation.getVersion() });

			if (relation.getId() != null)
				relation = (Relation) getSession().merge("Relation", relation);
			else
				getSession().saveOrUpdate("Relation", relation);

			getSession().flush();
		} catch (StaleStateException e) {
			throw new ApplicationException(
					String.format(
							"Set relation [id: %s] is stale which means newer version exists (Hibernate).",
							relation.getId()));
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}

		logger.info("Saved/updated relation [id: {}, version: {}]",
				new Object[] { relation.getId(), relation.getVersion() });

		return relation;
	}

	/**
	 * Delete a relation by setting the ToTimeStamp attribute to the current
	 * date
	 */
	public Relation delete(Long id) {
		Relation relation = get(id);

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
			logger.debug("Deleting relation [id: {}, version: {}]",
					new Object[] { relation.getId(), relation.getVersion() });

			relation = (Relation) getSession().merge("Relation", relation);
			getSession().flush();
		} catch (StaleStateException e) {
			throw new ApplicationException(
					String.format(
							"Deletion stale relation [id: %s] which means newer version exists (Hibernate).",
							relation.getId()));
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}

		logger.info("Deleted relation [id: {}, version: {}]", new Object[] {
				relation.getId(), relation.getVersion() });
		return relation;
	}

	/**
	 * Deletion of an element's relations (both source and destination).
	 * DML-style operation written directly in HQL quickens the update process.
	 * 
	 * @param id
	 */
	public int cascade(Long id) {
		return cascade(id, true, true);
	}

	/**
	 * Deletion of an element's relations
	 * 
	 * @param id
	 * @param source
	 * @param destination
	 * @return
	 */
	public int cascade(Long id, boolean source, boolean destination) {
		int count = 0;

		try {
			if (source) {
				int results = getSession()
						.createQuery(
								"update Relation r set r.toTimeStamp = current_timestamp() where r.source.id = :sourceId and r.toTimeStamp is null")
						.setLong("sourceId", id).executeUpdate();

				count = count + results;
				logger.info("Deleted {}  relations with source [id: {}]",
						results, id);
			}

			if (destination) {
				int results = getSession()
						.createQuery(
								"update Relation r set r.toTimeStamp = current_timestamp() where r.destination.id = :destinationId and r.toTimeStamp is null")
						.setLong("destinationId", id).executeUpdate();

				count = count + results;
				logger.info("Deleted {} relations with destination [id: {}]",
						results, id);
			}
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Cascade deletion of stale entities which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}

		return count;
	}
}
