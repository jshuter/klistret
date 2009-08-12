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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.identification.PersistenceRules;
import com.klistret.cmdb.pojo.PropertyCriteria;
import com.klistret.cmdb.service.ElementService;

/**
 * AOP class
 * 
 * @author Matthew Young
 * 
 */
public class PersistenceRulesControl {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceRulesControl.class);

	/**
	 * Persistence Rules
	 */
	private PersistenceRules persistenceRules;

	/**
	 * Element service (transaction ready)
	 */
	private ElementService elementService;

	/**
	 * 
	 * @return PersistenceRules
	 */
	public PersistenceRules getPersistenceRules() {
		return persistenceRules;
	}

	/**
	 * 
	 * @param persistenceRules
	 */
	public void setPersistenceRules(PersistenceRules persistenceRules) {
		this.persistenceRules = persistenceRules;
	}

	/**
	 * 
	 * @return ElementService
	 */
	public ElementService getElementService() {
		return elementService;
	}

	/**
	 * 
	 * @param elementService
	 */
	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	/**
	 * Persistent rules (for uniqueness) are applied to the passed Element
	 * generating a PropertyCriteria that determines if other elements in the
	 * database are similar. Hits generate an exception.
	 * 
	 * @param element
	 */
	public void applyElementPersistenceRules(
			com.klistret.cmdb.pojo.Element element) throws ApplicationException {
		logger.debug("apply persistence rules to element [{}]", element
				.toString());

		PropertyCriteria criteria = persistenceRules
				.getPropertyCriteria(element.getConfiguration());

		if (criteria != null) {
			Collection<com.klistret.cmdb.pojo.Element> results = elementService
					.findByCriteria(criteria);

			for (com.klistret.cmdb.pojo.Element other : results)
				logger.debug("criteria selected other element [{}]", other
						.toString());

			if (element.getId() == null && !results.isEmpty())
				throw new ApplicationException(
						String
								.format(
										"new element is identical to other elements [count: %d] according to persistence rules ",
										results.size()));

			if (element.getId() != null && !results.isEmpty()) {
				for (com.klistret.cmdb.pojo.Element other : results) {
					if (!element.equals(other))
						throw new ApplicationException(
								String
										.format(
												"element [%s] is identical to other [%s] according to persistence rules ",
												element.toString(), other
														.toString()));
				}
			}
		}
	}
}