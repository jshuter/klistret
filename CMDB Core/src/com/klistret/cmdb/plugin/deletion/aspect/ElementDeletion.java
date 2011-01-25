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

package com.klistret.cmdb.plugin.deletion.aspect;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.service.RelationService;

public class ElementDeletion {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementDeletion.class);

	private RelationService relationService;

	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public void remove(Long id) {
		int count = relationService.cascade(id);

		logger.debug("Deletion of {} relations complete", count);
	}
}
