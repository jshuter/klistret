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

package com.klistret.cmdb.pojo;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

public class PropertyCriteria {
	private static final Logger logger = LoggerFactory
			.getLogger(PropertyCriteria.class);

	private int maxResults = 100;

	private int firstResult = 0;

	private String entityName;

	private List<PropertyCriterion> propertyCriteria;

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public List<PropertyCriterion> getPropertyCriteria() {
		return propertyCriteria;
	}

	public void setPropertyCriteria(List<PropertyCriterion> propertyCriteria) {
		this.propertyCriteria = propertyCriteria;
	}

	public Criteria getCriteria(Session session) {
		/**
		 * empty criteria list control
		 */
		if (propertyCriteria == null)
			return null;

		/**
		 * create criteria
		 */
		Criteria criteria = session.createCriteria(entityName);
		if (criteria == null)
			throw new ApplicationException(String.format(
					"unable to create criteria for entity name [%s]",
					entityName));

		/**
		 * evaluate each property
		 */
		for (PropertyCriterion propertyCriterion : propertyCriteria) {
			transformPropertyCriterion(session, criteria, propertyCriterion,
					propertyCriterion.getPropertyLocationPath());
		}

		/**
		 * max/first results
		 */
		criteria.setMaxResults(maxResults);
		criteria.setFirstResult(firstResult);

		return criteria;
	}

	private void transformPropertyCriterion(Session session, Criteria criteria,
			PropertyCriterion propertyCriterion, String propertyLocationPath) {
		/**
		 * split properties between current and remainder to get at the first
		 * property
		 */
		String[] split = propertyLocationPath.split("[.]", 2);
		String property = split[0];

		/**
		 * reset propertyLocationPath as remainder if exists otherwise null
		 */
		if (split.length == 2) {
			propertyLocationPath = split[1];
		} else {
			propertyLocationPath = null;
		}

		/**
		 * determine Hibernate type information
		 */
		Map<?, ?> test = session.getSessionFactory().getAllClassMetadata();
		logger.debug(test.toString());

		ClassMetadata classMetadata = session.getSessionFactory()
				.getClassMetadata("com.klistret.cmdb.pojo." + entityName);
		Type propertyType = classMetadata.getPropertyType(property);

		if (propertyType.isAnyType())
			logger.debug(String.format("property [%s] is any type", property));

		if (propertyType.isAssociationType())
			logger.debug(String.format("property [%s] is association type",
					property));

		if (propertyType.isCollectionType())
			logger.debug(String.format("property [%s] is collection type",
					property));
	}
}
