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
 * 
 * @author Matthew Young
 * 
 */
public interface ElementTypeDAO {

	/**
	 * CRUD get by composite unique id
	 * 
	 * @param name
	 * @return ElementType
	 */
	com.klistret.cmdb.xmlbeans.pojo.ElementType getByCompositeId(String name);

	/**
	 * CRUD find by name used in an ILike expression
	 * 
	 * @param name
	 * @return Collection
	 */
	Collection<com.klistret.cmdb.xmlbeans.pojo.ElementType> findByName(
			String name);

	/**
	 * CRUD find count by name used in an ILike expression
	 * 
	 * @param name
	 * @return Integer
	 */
	Integer countByName(String name);
}
