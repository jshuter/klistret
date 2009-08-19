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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;

/**
 * Borrowed code from a Java Boutique article on method caching with EhCache:
 * http://javaboutique.internet.com/tutorials/ehcache/index-2.html
 * 
 * @author Matthew Young
 * 
 */
public class MethodCachingAOP implements MethodInterceptor {

	private static final Logger logger = LoggerFactory
			.getLogger(MethodCachingAOP.class);

	private CacheManager cacheManager;

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		final String cacheName = String.format("%s.%s", methodInvocation
				.getMethod().getName(), methodInvocation.getMethod()
				.getDeclaringClass().toString());

		Cache cache = cacheManager.getCache(cacheName);

		if (cache == null) {
			logger.debug("cache manager has no cache named {}", cacheName);

		}

		// TODO Auto-generated method stub
		return null;
	}

}
