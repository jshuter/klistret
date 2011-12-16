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

import com.klistret.cmdb.ci.pojo.Relation;

/**
 * 
 * @author Matthew Young
 * 
 */
public interface RelationDAO {
	/**
	 * CRUD get by unique ID
	 * 
	 * @param id
	 * @return Relation
	 */
	Relation get(Long id);

	/**
	 * CRUD find by criteria where the criteria is a list of XPath expressions
	 * and start/limit parameters.
	 * 
	 * @param XPath[]
	 * @param start
	 * @param limit
	 * @return List
	 */
	List<Relation> find(List<String> expressions, int start, int limit);

	/**
	 * Identical to the find method except a unique Relation is returned
	 * 
	 * @param expressions
	 * @param start
	 * @param limit
	 * @return
	 */
	Relation unique(List<String> expressions);
	
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
	 * @param relation
	 * @return Relation
	 */
	Relation set(Relation relation);

	/**
	 * CRUD delete
	 * 
	 * @param id
	 */
	Relation delete(Long id);

	/**
	 * DML-style cascade deletion of an element's relations
	 * 
	 * @param id
	 */
	int cascade(Long id);
	
	/**
	 * DML-style cascade deletion of an element's relations
	 * 
	 * @param id
	 * @param source
	 * @param destination
	 * @return
	 */
	int cascade(Long id, boolean source, boolean destination);
}
