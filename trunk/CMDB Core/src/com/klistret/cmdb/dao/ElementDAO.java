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

import java.util.Collection;

/**
 * Dealing with Elements is limited to CRUD methods while the business logic is
 * applied through AOP (like persistence rules for uniqueness)
 * 
 * @author Matthew Young
 * 
 */
public interface ElementDAO {

	/**
	 * CRUD get by unique identifier (associations are fetched automatically)
	 * 
	 * @param id
	 * @return Element
	 */
	com.klistret.cmdb.xmlbeans.pojo.Element getById(Long id);

	/**
	 * CRUD get by unique identifier with option to toggle inclusion of
	 * associations
	 * 
	 * @param id
	 * @param fetchAssociations
	 * @return Element
	 */
	com.klistret.cmdb.xmlbeans.pojo.Element getById(Long id, boolean fetchAssociations);

	/**
	 * CRUD find by criteria where the criteria is a list of property
	 * expressions, operators, and values to be applied into the passed
	 * operation.
	 * 
	 * @param criteria
	 * @return Collection
	 */
	Collection<com.klistret.cmdb.xmlbeans.pojo.Element> findByCriteria(
			com.klistret.cmdb.pojo.PropertyCriteria criteria);

	/**
	 * CRUD count by criteria (under the hood the findByCriteria method)
	 * 
	 * @param criteria
	 * @return Integer
	 */
	Integer countByCriteria(com.klistret.cmdb.pojo.PropertyCriteria criteria);

	/**
	 * CRUD save/update
	 * 
	 * @param element
	 * @return Element
	 */
	com.klistret.cmdb.xmlbeans.pojo.Element set(com.klistret.cmdb.xmlbeans.pojo.Element element);
}
