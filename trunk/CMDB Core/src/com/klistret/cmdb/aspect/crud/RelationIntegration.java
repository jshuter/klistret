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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

import com.klistret.cmdb.ci.pojo.Relation;

/**
 * Publishes messages upon CRUD function calls (using AOP). The payload is the
 * relation object and the header is populated with a function attribute denoting
 * the CRUD type. Currently, reads are not handled prior to checking out the
 * performance impacts of using Spring integration to do EDA.
 * 
 * @author Matthew Young
 * 
 */
public class RelationIntegration {

	private static final Logger logger = LoggerFactory
			.getLogger(RelationIntegration.class);

	private MessageChannel channel;

	public void setChannel(MessageChannel channel) {
		this.channel = channel;
	}

	public MessageChannel getChannel() {
		return this.channel;
	}

	public void delete(Long id, Relation relation) {
		logger
				.debug(
						"Generating a message with CRUD function Delete on relation [id: {}]",
						relation.getId());
		Message<Relation> message = MessageBuilder.withPayload(relation)
				.setHeader("function", "DELETE").build();
		channel.send(message);
	}

	public void create(Relation relation) {
		logger
				.debug(
						"Generating a message with CRUD function Create on relation [id: {}]",
						relation.getId());
		Message<Relation> message = MessageBuilder.withPayload(relation)
				.setHeader("function", "CREATE").build();
		channel.send(message);
	}

	public void update(Relation relation) {
		logger
				.debug(
						"Generating a message with CRUD function Update on relation [id: {}]",
						relation.getId());
		Message<Relation> message = MessageBuilder.withPayload(relation)
				.setHeader("function", "UPDATE").build();
		channel.send(message);
	}

	public void read(Long id, Relation relation) {
		logger
				.debug(
						"Message for CRUD function Get on relation [id: {}] not supported",
						relation.getId());
	}

}
