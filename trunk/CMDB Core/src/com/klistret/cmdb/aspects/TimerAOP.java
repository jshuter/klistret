package com.klistret.cmdb.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerAOP {

	private static final Logger logger = LoggerFactory
			.getLogger(TimerAOP.class);

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
