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
package com.klistret.cmdb.aspect.crud.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.service.RelationService;

/**
 * When an element is deleted all source and destination relationships are
 * deleted (cascade).
 * 
 * @author Matthew Young
 * 
 */
public class CascadeRelations {

	private static final Logger logger = LoggerFactory
			.getLogger(CascadeRelations.class);

	private RelationService relationService;

	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public void receive(Message<Element> message) {
		if (message.getHeaders().get("function").equals("DELETE")) {
			int count = relationService.cascade(message.getPayload().getId());

			logger
					.debug(
							"Cascaded delete of element [id: {}] to source/destination relations [count: {}]",
							message.getPayload().getId(), count);
		}
	}
}
