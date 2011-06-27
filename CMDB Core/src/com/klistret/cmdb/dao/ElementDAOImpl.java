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
					"Finding elements by expression from start position [{}] with limit [{}]",
					start, limit);

			if (expressions == null)
				throw new ApplicationException("Expressions parameter is null",
						new IllegalArgumentException());

			Criteria hcriteria = new XPathCriteria(expressions, getSession())
					.getCriteria();
			String alias = hcriteria.getAlias();

			hcriteria.setProjection(Projections.projectionList()
					.add(Projections.property(alias + ".id"))
					.add(Projections.property(alias + ".type"))
					.add(Projections.property(alias + ".name"))
					.add(Projections.property(alias + ".fromTimeStamp"))
					.add(Projections.property(alias + ".toTimeStamp"))
					.add(Projections.property(alias + ".createId"))
					.add(Projections.property(alias + ".createTimeStamp"))
					.add(Projections.property(alias + ".updateTimeStamp"))
					.add(Projections.property(alias + ".configuration")));

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
				element.setConfiguration((com.klistret.cmdb.ci.commons.Element) row[8]);

				elements.add(element);
			}

			results = null;

			return elements;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
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
		} catch (HibernateException he) {
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
	public Element get(Long id) {
		try {
			logger.debug("Getting element [id: {}] by id ", id);

			Criteria criteria = getSession().createCriteria(Element.class);
			criteria.add(Restrictions.idEq(id));

			Element element = (Element) criteria.uniqueResult();

			if (element == null)
				throw new ApplicationException(String.format(
						"Element [id: %s] not found", id),
						new NoSuchElementException());

			return clean(element);

		} catch (HibernateException he) {
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
		 * Validate length of name field
		 */
		if (element.getName().length() > 100)
			throw new ApplicationException(
					String.format(
							"Element name [%s] greater than 100 characters [length: %d]",
							element.getName(), element.getName().length()));

		/**
		 * record current time when updating
		 */
		element.setUpdateTimeStamp(new java.util.Date());

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

		try {
			if (element.getId() != null)
				getSession().merge("Element", element);
			else
				getSession().saveOrUpdate("Element", element);
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("Save/update element [{}]", element.toString());
		return clean(element);
	}

	/**
	 * Delete an element by setting the ToTimeStamp attribute to the current
	 * date
	 */
	public Element delete(Long id) {
		Criteria criteria = getSession().createCriteria(Element.class);
		criteria.add(Restrictions.idEq(id));

		Element element = (Element) criteria.uniqueResult();

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
			getSession().merge("Element", element);
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}

		logger.info("Deleted element [{}]", element);
		return clean(element);
	}

	/**
	 * Necessary to eliminate relation associations which only available to
	 * query through relationships
	 * 
	 * @param other
	 * @return
	 */
	private Element clean(Element other) {
		Element element = new Element();
		element.setId(other.getId());
		element.setType(other.getType());
		element.setName(other.getName());
		element.setFromTimeStamp(other.getFromTimeStamp());
		element.setToTimeStamp(other.getToTimeStamp());
		element.setCreateId(other.getCreateId());
		element.setCreateTimeStamp(other.getCreateTimeStamp());
		element.setUpdateTimeStamp(other.getUpdateTimeStamp());
		element.setConfiguration(other.getConfiguration());

		other = null;

		return element;
	}
}
