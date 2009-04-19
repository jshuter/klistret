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

package com.klistret.cmdb.service;

import java.util.Collection;

import com.klistret.cmdb.pojo.ElementType;

public interface ElementTypeService {

	ElementType getByCompositeId(String name);

	Collection<ElementType> findByCriteria(
			com.klistret.cmdb.utility.hibernate.Criteria criteria);

	Integer countByCriteria(com.klistret.cmdb.utility.hibernate.Criteria criteria);
}
