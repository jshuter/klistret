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

import java.util.List;

/**
 * Dealing with Elements is limited to CRUD methods while the business logic is
 * applied through AOP (like persistence rules for uniqueness)
 * 
 * @author Matthew Young
 * 
 */
public interface ElementDAO {

	/**
	 * CRUD get by unique identifier (associations are never fetched
	 * automatically, instead all properties from the Hibernate proxy are moved
	 * into a new Element object and the proxy destroyed to avoid lazy loading
	 * exceptions plus the find method doesn't rely relations either and the
	 * number could potentially be quite high)
	 * 
	 * @param id
	 * @return Element
	 */
	com.klistret.cmdb.ci.pojo.Element getById(Long id);

	/**
	 * CRUD find by criteria where the criteria is a list of property
	 * expressions, operators, and values to be applied into the passed
	 * operation.
	 * 
	 * @param expressions
	 * @return Collection
	 */
	List<com.klistret.cmdb.ci.pojo.Element> findByExpressions(
			String[] expressions, Integer start, Integer limit);

	/**
	 * CRUD save/update
	 * 
	 * @param element
	 * @return Element
	 */
	com.klistret.cmdb.ci.pojo.Element set(com.klistret.cmdb.ci.pojo.Element element);
}
