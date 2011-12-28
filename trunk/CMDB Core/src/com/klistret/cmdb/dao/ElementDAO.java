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

import com.klistret.cmdb.ci.pojo.Element;

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
	Element get(Long id);

	/**
	 * CRUD find by criteria where the criteria is a list of XPath expressions
	 * and start/limit parameters.
	 * 
	 * @param XPath
	 *            []
	 * @param start
	 * @param limit
	 * @return List
	 */
	List<Element> find(List<String> expressions, int start, int limit);

	/**
	 * Aggregate (max, min, sum, avg) off of criteria
	 * 
	 * @param projection
	 * @param expressions
	 * @return
	 */
	String aggregate(String projection, List<String> expressions);

	/**
	 * Identical to the find method except only a single Element is returned if
	 * unique otherwise an exception is raised.
	 * 
	 * @param expressions
	 * @param start
	 * @param limit
	 * @return
	 */
	Element unique(List<String> expressions);

	/**
	 * Identical to the find method except the row count is returned
	 * 
	 * @param expressions
	 * @param start
	 * @param limit
	 * @return
	 */
	Integer count(List<String> expressions);

	/**
	 * CRUD save/update
	 * 
	 * @param element
	 * @return Element
	 */
	Element set(Element element);

	/**
	 * CRUD delete
	 * 
	 * @param id
	 */
	Element delete(Long id);
}
