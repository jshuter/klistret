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

package com.klistret.cmdb.rules.cache;

import java.util.List;

import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

/**
 * Interface exists solely to allow for AOP proxy [see
 * http://blog.xebia.com/2006/08/18/the-problem-with-proxy-based-aop-frameworks]
 * 
 * Need to cache calls constructing the PropertyExpression list since initially
 * this can take up to 1000 ms and internal method calls inside the
 * PersistenceImpl aren't accessible to AOP proxies (maybe with CGLIB).
 * 
 * @author Matthew Young
 * 
 */
public interface PersistenceCache {
	List<PropertyExpression[]> getPropertyExpressionCriteria(String classname);
}
