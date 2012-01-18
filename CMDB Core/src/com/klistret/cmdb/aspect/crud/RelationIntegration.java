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
package com.klistret.cmdb.aspect.crud;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.exception.ApplicationException;

/**
 * Publishes messages upon CRUD function calls (using AOP). The payload is the
 * relation object and the header is populated with a function attribute
 * denoting the CRUD type. Currently, reads are not handled prior to checking
 * out the performance impacts of using Spring integration to do EDA.
 * 
 * @author Matthew Young
 * 
 */
public class RelationIntegration {

	private static final Logger logger = LoggerFactory
			.getLogger(RelationIntegration.class);

	private MessageChannel channel;

	private enum SignatureMethod {
		CREATE, UPDATE, DELETE, READ
	}

	public void setChannel(MessageChannel channel) {
		this.channel = channel;
	}

	public MessageChannel getChannel() {
		return this.channel;
	}

	public Object transmit(ProceedingJoinPoint pjp) throws Throwable {
		try {
			Relation relation = (Relation) pjp.proceed();

			Signature signature = pjp.getSignature();
			String name = signature.getName().toUpperCase();
			Object[] args = pjp.getArgs();

			if (name.equals("GET"))
				name = "READ";
			try {
				SignatureMethod method = SignatureMethod.valueOf(name);

				switch (method) {
				case CREATE:
				case DELETE:
				case UPDATE:
				case READ:
					Relation precedent = null;

					for (Object arg : args)
						if (arg instanceof Relation)
							precedent = (Relation) arg;

					logger.debug(
							"Generating a message with CRUD function {} on relation [id: {}, version: {}]",
							new Object[] { name, relation.getId(),
									relation.getVersion() });

					Message<Relation> message = MessageBuilder
							.withPayload(relation)
							.setHeader("function", name)
							.setHeader(
									"precedent",
									precedent == null ? null : precedent
											.getVersion()).build();
					channel.send(message);
					break;
				}
			} catch (IllegalArgumentException e) {
				logger.debug("Method {} is not trasmitted", name);
			}

			return relation;
		} catch (HibernateOptimisticLockingFailureException e) {
			throw new ApplicationException("Stale relation.",
					new StaleStateException(e.getMessage()));
		} catch (Exception e) {
			logger.error("Unknown exception: {}", e.getMessage());
			throw e;
		}
	}
}
