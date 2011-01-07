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

package com.klistret.cmdb.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.dao.RelationTypeDAO;
import com.klistret.cmdb.ci.pojo.RelationType;

/**
 * 
 * @author Matthew Young
 * 
 */
public class RelationTypeServiceImpl implements RelationTypeService {

	private static final Logger logger = LoggerFactory
			.getLogger(RelationTypeServiceImpl.class);

	private RelationTypeDAO relationTypeDAO;

	public void setRelationTypeDAO(RelationTypeDAO relationTypeDAO) {
		this.relationTypeDAO = relationTypeDAO;
	}

	public RelationType get(String name) {
		return this.relationTypeDAO.get(name);
	}

	public List<RelationType> find(String name) {
		return relationTypeDAO.find(name);
	}

	/**
	 * Dummy method for pre-flighted calls
	 */
	public String preflighted() {
		logger.debug("Entered preflighted method (ie handled HTTP OPTIONS");

		return "HTTP OPTIONS accepted";
	}
}
