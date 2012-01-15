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
package com.klistret.cmdb.utility.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import com.klistret.cmdb.exception.ApplicationException;

public class OptimisticLocking {
	private static final Logger logger = LoggerFactory
			.getLogger(OptimisticLocking.class);

	public Object commitTransaction(ProceedingJoinPoint pjp) throws Throwable {
		try {
			return pjp.proceed();
		} catch (HibernateOptimisticLockingFailureException e) {
			Signature signature = pjp.getSignature();
			logger.error(
					"Stale in declaration:{}, name: {}, long: {} message: {}",
					new Object[] { signature.getDeclaringTypeName(),
							signature.getName(), pjp.toLongString(),
							e.getMessage() });
			throw new ApplicationException("Stale entity captured.");
		}
	}
}
