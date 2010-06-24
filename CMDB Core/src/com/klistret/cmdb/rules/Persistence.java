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

package com.klistret.cmdb.rules;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Interface exists solely to allow for AOP proxy [see
 * http://blog.xebia.com/2006/08/18/the-problem-with-proxy-based-aop-frameworks]
 * 
 * XmlObjects traveling around aren't suitable for caching (too much variation)
 * but the persistence rules are defined in relation to the schema type. So
 * dynamic caching can be applied to the getCriteriaByType method instead
 * improving performance. Note that the interface's methods do not make internal
 * calls to each other than AOP proxies can not be applied to inter-class
 * communication (might be doable with CGLIB proxies?).
 * 
 * 
 * @author Matthew Young
 * 
 */
public interface Persistence {
	/**
	 * Persistence rules are bound to types (the full java name for schema
	 * types)
	 * 
	 * @param classname
	 * @return Criteria List of criterion (PropertyExpression array)
	 */
	List<String[]> getCriterionByQName(QName qname);

}
