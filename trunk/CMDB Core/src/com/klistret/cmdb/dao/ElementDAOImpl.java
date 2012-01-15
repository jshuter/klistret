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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.StaleStateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.utility.hibernate.XPathCriteria;
import com.klistret.cmdb.utility.jaxb.CIContext;

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
	public List<Element> find(List<String> expressions, int start, int limit) {
		try {
			logger.debug(
					"Finding elements by expression from start position [{}] with limit [{}] for session [{}]",
					new Object[] { start, limit, getSession().hashCode() });

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Criteria criteria = new XPathCriteria(expressions, getSession())
					.getCriteria();

			criteria.setProjection(Projections.projectionList()
					.add(Projections.property("id"))
					.add(Projections.property("type"))
					.add(Projections.property("name"))
					.add(Projections.property("fromTimeStamp"))
					.add(Projections.property("toTimeStamp"))
					.add(Projections.property("createId"))
					.add(Projections.property("createTimeStamp"))
					.add(Projections.property("updateTimeStamp"))
					.add(Projections.property("version"))
					.add(Projections.property("configuration")));

			criteria.addOrder(Order.asc("name"));

			criteria.setFirstResult(start);
			criteria.setFetchSize(limit);
			criteria.setMaxResults(limit);

			Object[] results = criteria.list().toArray();

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
				element.setVersion((Long) row[8]);
				element.setConfiguration((com.klistret.cmdb.ci.commons.Element) row[8]);

				elements.add(element);
			}

			results = null;

			return elements;
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Element(s) found are stale which means newer version exists.");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Does a projection on the result set for the passed criteria.
	 * 
	 */
	public String aggregate(String projection, List<String> expressions) {
		try {
			logger.debug("Aggregating elements by expression [session: {}]",
					getSession().hashCode());

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			XPathCriteria xpc = new XPathCriteria(expressions, getSession());
			Criteria criteria = xpc.getCriteria();

			Projection aggregation = xpc.aggregate(projection);
			criteria.setProjection(aggregation);

			List<?> results = criteria.list();

			if (results.size() == 0)
				return null;

			if (results.get(0) == null)
				return null;

			return results.get(0).toString();
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Element(s) aggregated are stale which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Unique get off of XPath expressions
	 * 
	 */
	public Element unique(List<String> expressions) {
		try {
			logger.debug("Query unique over XPath expressions (session: {})",
					getSession().hashCode());

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Integer count = count(expressions);
			if (count == 0)
				return null;

			if (count > 1)
				throw new ApplicationException(String.format(
						"Expressions criteria was not unique: %d", count));

			List<Element> results = find(expressions, 0, 1);
			Element element = results.get(0);

			logger.debug(
					"Returning unique element [id: {}, name: {}, version: {}, session: {}]",
					new Object[] { element.getId(), element.getName(),
							element.getVersion(), getSession().hashCode() });
			return element;
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Unique element stale which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}
	}

	/**
	 * Query count of XPath expressions
	 */
	public Integer count(List<String> expressions) {
		try {
			logger.debug("Query count of XPath expressions [session: {}]",
					getSession().hashCode());

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Criteria hcriteria = new XPathCriteria(expressions, getSession())
					.getCriteria();
			hcriteria.setProjection(Projections.rowCount());

			return ((Long) hcriteria.list().get(0)).intValue();
		} catch (StaleStateException e) {
			throw new ApplicationException(
					"Element(s) counted are stale which means newer version exists (Hibernate).");
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
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
	public Element get(Long id) {
		try {
			logger.debug("Getting element [id: {}] by id [session: {}]", id,
					getSession().hashCode());

			Criteria criteria = getSession().createCriteria(Element.class);
			criteria.add(Restrictions.idEq(id));

			Element element = (Element) criteria.uniqueResult();

			if (element == null)
				throw new ApplicationException(String.format(
						"Element [id: %s] not found", id),
						new NoSuchElementException());

			logger.debug(
					"Returning element by id [id: {}, name: {}, version: {}, session: {}]",
					new Object[] { element.getId(), element.getName(),
							element.getVersion(), getSession().hashCode() });

			return element;
		} catch (StaleStateException e) {
			throw new ApplicationException(
					String.format(
							"Got element [id: %d] is stale which means newer version exists (Hibernate).",
							id));
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
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
		 * Validate length of name field
		 */
		if (element.getName().length() > 100)
			throw new ApplicationException(
					String.format(
							"Element name [%s] greater than 100 characters [length: %d]",
							element.getName(), element.getName().length()));

		/**
		 * pojo type matches the configuration qname
		 */
		String typeClassName = CIContext.getCIContext()
				.getBean(QName.valueOf(element.getType().getName()))
				.getJavaClass().getName();
		if (!typeClassName.equals(element.getConfiguration().getClass()
				.getName()))
			throw new ApplicationException(
					String.format(
							"Pojo element type [%s] does not match the configuration type [%s]",
							typeClassName, element.getConfiguration()
									.getClass().getName()));

		/**
		 * Change update date time
		 */
		element.setUpdateTimeStamp(new java.util.Date());

		try {
			logger.debug(
					"Setting element [id: {}, name: {}, version: {}: session: {}]",
					new Object[] { element.getId(), element.getName(),
							element.getVersion(), getSession().hashCode() });

			if (element.getId() != null)
				element = (Element) getSession().merge("Element", element);
			else
				getSession().saveOrUpdate("Element", element);
		} catch (StaleStateException e) {
			throw new ApplicationException(
					String.format(
							"Set element [id: %s, name: %s, version: %s] is stale which means newer version exists (Hibernate).",
							element.getId(), element.getName(),
							element.getVersion()), e);
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}

		logger.info(
				"Saved/updated element [id: {}, name: {}, version: {}, session: {}]",
				new Object[] { element.getId(), element.getName(),
						element.getVersion(), getSession().hashCode() });
		return element;
	}

	/**
	 * Delete an element by setting the ToTimeStamp attribute to the current
	 * date. 
	 */
	public Element delete(Long id) {
		Element element = get(id);

		if (element == null)
			throw new ApplicationException(String.format(
					"Element [id: %s] not found", id),
					new NoSuchElementException());

		if (element.getToTimeStamp() != null)
			throw new ApplicationException(String.format(
					"Element [id: %d] has already been deleted", id),
					new NoSuchElementException());

		element.setToTimeStamp(new java.util.Date());
		element.setUpdateTimeStamp(new java.util.Date());

		try {
			logger.debug(
					"Deleting element [id: {}, name: {}, version: {}, session: {}]",
					new Object[] { element.getId(), element.getName(),
							element.getVersion(), getSession().hashCode() });

			element = (Element) getSession().merge("Element", element);
		} catch (StaleStateException e) {
			throw new ApplicationException(
					String.format(
							"Deleted element [id: %s, name: %s, version: %s] is stale which means newer version exists (Hibernate).",
							element.getId(), element.getName(),
							element.getVersion()), e);
		} catch (HibernateException e) {
			throw new InfrastructureException(e.getMessage(), e);
		}

		logger.info(
				"Deleted element [id: {}, name: {}, version: {}, session: {}]",
				new Object[] { element.getId(), element.getName(),
						element.getVersion(), getSession().hashCode() });
		return element;
	}
}
