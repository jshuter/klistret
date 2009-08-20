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

package com.klistret.cmdb.aspects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.io.Serializable;

/**
 * Borrowed code from a Java Boutique article on method caching with EhCache:
 * http://javaboutique.internet.com/tutorials/ehcache/index-2.html
 * 
 * @author Matthew Young
 * 
 */
public class PersistenceCachingAOP implements MethodInterceptor {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceCachingAOP.class);

	private Cache cache;

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Object[] arguments = methodInvocation.getArguments();
		String cacheKey = (String) arguments[0];

		Element element = cache.get(cacheKey);
		if (element == null) {
			Object value = methodInvocation.proceed();

			element = new Element(cacheKey, (Serializable) value);
			cache.put(element);
			logger
					.debug(
							"cached element for getCriteriaByType method with classname [{}]",
							cacheKey);
		}

		logger.debug("using AOP cache for persistence rules");
		return element.getValue();
	}

}
