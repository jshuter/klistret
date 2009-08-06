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

import java.sql.Date;
import java.sql.Timestamp;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;

public class ElementDAOImpl extends BaseImpl implements ElementDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementDAOImpl.class);

	public Integer countByCriteria(
			com.klistret.cmdb.pojo.PropertyCriteria criteria) {
		try {
			Criteria hcriteria = criteria.getCriteria(getSession());
			hcriteria.setProjection(Projections.rowCount());

			return (Integer) hcriteria.list().iterator().next();
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<com.klistret.cmdb.pojo.Element> findByCriteria(
			com.klistret.cmdb.pojo.PropertyCriteria criteria) {
		try {
			Criteria hcriteria = criteria.getCriteria(getSession());
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

			hcriteria.setMaxResults(criteria.getMaxResults());
			hcriteria.setFirstResult(criteria.getFirstResult());

			Object[] results = hcriteria.list().toArray();

			Set<com.klistret.cmdb.pojo.Element> elements = new LinkedHashSet(
					results.length);

			for (int index = 0; index < results.length; index++) {
				Object[] row = (Object[]) results[index];

				com.klistret.cmdb.pojo.Element element = new com.klistret.cmdb.pojo.Element();
				element.setId((Long) row[0]);
				element.setType((com.klistret.cmdb.pojo.ElementType) row[1]);
				element.setName((String) row[2]);
				element.setFromTimeStamp((Date) row[3]);
				element.setToTimeStamp((Date) row[4]);
				element.setCreateId((String) row[5]);
				element.setCreateTimeStamp((Date) row[6]);
				element.setUpdateTimeStamp((Date) row[7]);
				element
						.setConfiguration((com.klistret.cmdb.xmlbeans.Element) row[8]);

				elements.add(element);
			}

			return elements;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	public com.klistret.cmdb.pojo.Element getById(Long id) {
		return getById(id, true);
	}

	public com.klistret.cmdb.pojo.Element getById(Long id,
			boolean fetchAssociations) {
		try {
			Criteria criteria = getSession().createCriteria(
					com.klistret.cmdb.pojo.Element.class);

			if (fetchAssociations) {
				criteria.setFetchMode("sourceRelations", FetchMode.JOIN);
				criteria.setFetchMode("destinationRelations", FetchMode.JOIN);
			}

			criteria.add(Restrictions.idEq(id));

			logger.debug("getting element [id: {}] by id ", id);
			com.klistret.cmdb.pojo.Element element = (com.klistret.cmdb.pojo.Element) criteria
					.uniqueResult();

			if (element == null) {
				throw new ApplicationException(String.format(
						"element [id: %s] does not exist", id));
			}

			return element;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	public com.klistret.cmdb.pojo.Element set(
			com.klistret.cmdb.pojo.Element element) {
		Timestamp currentTimeStamp = new Timestamp(new java.util.Date()
				.getTime());
		element.setUpdateTimeStamp(currentTimeStamp);

		try {
			getSession().saveOrUpdate(element);
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("save/update element [{}]", element.toString());

		return element;
	}

}
