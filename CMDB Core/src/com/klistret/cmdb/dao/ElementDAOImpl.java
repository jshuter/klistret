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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.utility.hibernate.XPathCriteria;

/**
 * 
 * @author Matthew Young
 * 
 */
public class ElementDAOImpl extends BaseImpl implements ElementDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementDAOImpl.class);

	/**
	 * Necessary to use the Projections to limit the selected columns to only
	 * those defined to the Element tables (otherwise the returned columns
	 * contains all columns for all associations).
	 * 
	 * @see com.klistret.cmdb.dao.ElementDAO.findByCriteria
	 * @return Collection
	 */
	public List<Element> findByExpressions(List<String> expressions, int start,
			int limit) {
		try {
			if (expressions == null) {
				logger.error("Expressions parameter is null");
				throw new ApplicationException("Expressions parameter is null");
			}

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

			List<Element> elements = new ArrayList<Element>(results.length);
			logger.debug("Results length [{}]", results.length);

			for (int index = 0; index < results.length; index++) {
				Object[] row = (Object[]) results[index];

				Element element = new Element();
				element.setId((Long) row[0]);
				element.setType((ElementType) row[1]);
				element.setName((String) row[2]);
				element.setFromTimeStamp((Date) row[3]);
				element.setToTimeStamp((Date) row[4]);
				element.setCreateId((String) row[5]);
				element.setCreateTimeStamp((Date) row[6]);
				element.setUpdateTimeStamp((Date) row[7]);
				element
						.setConfiguration((com.klistret.cmdb.ci.commons.Element) row[8]);

				elements.add(element);
			}

			results = null;

			return elements;
		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException running criteria [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * Another proxy (shallow copy of the object) is returned to avoid Hibernate
	 * lazy exceptions in the RestEasy layer. A generic approach to transferring
	 * properties would be better/safer to mapping/POJO changes.
	 * 
	 * @param id
	 * @return Element
	 */
	public Element getById(Long id) {
		try {
			Criteria criteria = getSession().createCriteria(Element.class);

			criteria.add(Restrictions.idEq(id));

			logger.debug("getting element [id: {}] by id ", id);
			Element proxy = (Element) criteria.uniqueResult();
			logger.debug("found element [id: {}] by id ", id);

			if (proxy == null) {
				logger.error("element [id: {}] does not exist", id);
				throw new ApplicationException(String.format(
						"element [id: %s] does not exist", id));
			}

			Element element = new Element();
			element.setId(proxy.getId());
			element.setType(proxy.getType());
			element.setName(proxy.getName());
			element.setFromTimeStamp(proxy.getFromTimeStamp());
			element.setToTimeStamp(proxy.getToTimeStamp());
			element.setCreateId(proxy.getCreateId());
			element.setCreateTimeStamp(proxy.getCreateTimeStamp());
			element.setUpdateTimeStamp(proxy.getUpdateTimeStamp());
			element.setConfiguration(proxy.getConfiguration());

			proxy = null;

			return element;

		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException getting Element [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

	/**
	 * Set is a save/update call to Hibernate which only looks at the ID to
	 * determine if the object is currently persisted or not.
	 * 
	 * @param Element
	 * @return Element
	 */
	public Element set(Element element) {
		/**
		 * record current time when updating
		 */
		element.setUpdateTimeStamp(new java.util.Date());

		try {
			getSession().saveOrUpdate("Element", element);
		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException setting Element [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("save/update element [{}]", element.toString());

		return element;
	}
}
