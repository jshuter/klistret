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

package com.klistret.cmdb.utility.hibernate;

import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

import com.klistret.cmdb.pojo.Element;
import com.klistret.cmdb.pojo.PropertyCriterion;

public class PropertyCriteria implements Criteria {

	private Collection<PropertyCriterion> propertyCriterions;

	private Integer maxResults = 100;
	private Integer firstResult = 0;
	private String orderBy;

	protected DetachedCriteria getDetachedCriteria() {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(
				Element.class, "element");

		for (PropertyCriterion propertyCriterion : propertyCriterions)
			addPropertyCriterion(propertyCriterion, detachedCriteria);

		return detachedCriteria;
	}

	@Override
	public org.hibernate.Criteria getCriteria(Session session) {
		return getDetachedCriteria().getExecutableCriteria(session);
	}

	@Override
	public Integer getFirstResult() {
		return firstResult;
	}

	@Override
	public Integer getMaxResults() {
		return maxResults;
	}

	@Override
	public String getOrderBy() {
		return orderBy;
	}

	public Collection<PropertyCriterion> getPropertyCriterions() {
		return propertyCriterions;
	}

	@Override
	public void setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
	}

	@Override
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	@Override
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void setPropertyCriterions(
			Collection<PropertyCriterion> propertyCriterions) {
		this.propertyCriterions = propertyCriterions;
	}

	private void addPropertyCriterion(PropertyCriterion propertyCriterion,
			DetachedCriteria criteria) {
		// to-do: determine property type and resolution

		// three variations: properties to Element, Xml expressions, and
		// relations by identification
	}
}
