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

import org.hibernate.Criteria;
import org.hibernate.Session;

public class PropertyCriteria {

	private int maxResults = 100;

	private int firstResult = 0;

	private String className;

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

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<PropertyCriterion> getPropertyCriteria() {
		return propertyCriteria;
	}

	public void setPropertyCriteria(List<PropertyCriterion> propertyCriteria) {
		this.propertyCriteria = propertyCriteria;
	}

	public Criteria getCriteria(Session session) {
		try {
			Criteria query = session.createCriteria(Class.forName(className));

			/**
			 * evaluate each property
			 */
			for (PropertyCriterion propertyCriterion : propertyCriteria) {

			}

			/**
			 * max/first results
			 */
			query.setMaxResults(maxResults);
			query.setFirstResult(firstResult);

			return query;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
