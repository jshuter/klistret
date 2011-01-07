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

import com.klistret.cmdb.dao.ElementTypeDAO;
import com.klistret.cmdb.ci.pojo.ElementType;

/**
 * 
 * @author Matthew Young
 * 
 */
public class ElementTypeServiceImpl implements ElementTypeService {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementTypeServiceImpl.class);

	/**
	 * Element Type DAO
	 */
	private ElementTypeDAO elementTypeDAO;

	/**
	 * Dependency injection
	 * 
	 * @param elementTypeDAO
	 */
	public void setElementTypeDAO(ElementTypeDAO elementTypeDAO) {
		this.elementTypeDAO = elementTypeDAO;
	}

	public ElementType get(String name) {
		return elementTypeDAO.get(name);
	}

	public List<ElementType> find(String name) {
		return elementTypeDAO.find(name);
	}

	/**
	 * Dummy method for pre-flighted calls
	 */
	public String preflighted() {
		logger.debug("Entered preflighted method (ie handled HTTP OPTIONS");

		return "HTTP OPTIONS accepted";
	}
}
