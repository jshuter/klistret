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

import com.klistret.cmdb.ci.pojo.Element;

public class ElementIntegration {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementIntegration.class);

	private MessageChannel channel;

	public void setChannel(MessageChannel channel) {
		this.channel = channel;
	}

	public MessageChannel getChannel() {
		return this.channel;
	}

	public void delete(Long id, Element element) {
		logger
				.debug(
						"Generating a message with CRUD function Delete on element [id: {}]",
						element.getId());
		Message<Element> message = MessageBuilder.withPayload(element)
				.setHeader("function", "DELETE").build();
		channel.send(message);
	}

	public void create(Element element) {
		logger
				.debug(
						"Generating a message with CRUD function Create on element [id: {}]",
						element.getId());
		Message<Element> message = MessageBuilder.withPayload(element)
				.setHeader("function", "CREATE").build();
		channel.send(message);
	}

	public void update(Element element) {
		logger
				.debug(
						"Generating a message with CRUD function Update on element [id: {}]",
						element.getId());
		Message<Element> message = MessageBuilder.withPayload(element)
				.setHeader("function", "UPDATE").build();
		channel.send(message);
	}

	public void read(Long id, Element element) {
		logger
				.warn(
						"Message for CRUD function Get on element [id: {}] not supported",
						element.getId());
	}

}
