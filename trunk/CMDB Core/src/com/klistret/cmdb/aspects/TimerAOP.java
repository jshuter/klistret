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

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timer AOP mainly used for measuring response times for methods earmarked with
 * the Timer annotation.
 * 
 * @author Matthew Young
 * 
 */
public class TimerAOP {

	private static final Logger logger = LoggerFactory
			.getLogger(TimerAOP.class);

	/**
	 * Used in conjunction with an around aspect so that the method may be told
	 * to proceed.
	 * 
	 * @param pjp
	 * @return Object
	 * @throws Throwable
	 */
	public Object timeMethod(ProceedingJoinPoint pjp) throws Throwable {
		try {
			long start = System.currentTimeMillis();
			logger.debug("method timer start for class {}.{}", pjp
					.getSignature().getDeclaringTypeName(), pjp.getSignature()
					.getName());
			Object value = pjp.proceed();

			long end = System.currentTimeMillis();
			long differenceMs = end - start;

			Object[] information = { differenceMs,
					pjp.getSignature().getDeclaringTypeName(),
					pjp.getSignature().getName() };
			logger.debug("execution time {} ms for class {}.{}", information);
			return value;
		} catch (Throwable e) {
			logger.error("timer failed: {}", e.getMessage());
			throw e;
		}
	}

}
