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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceIdentification {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceIdentification.class);

	public void doElementPersistenceCheck(com.klistret.cmdb.pojo.Element element) {
		logger.debug("running check prior to persistance [element:{}]", element
				.toString());
	}

}
