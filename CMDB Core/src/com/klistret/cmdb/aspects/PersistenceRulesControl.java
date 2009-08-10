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

public class PersistenceRulesControl {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceRulesControl.class);

	private PersistenceRules persistenceRules;

	private ElementService elementService;

	public PersistenceRules getPersistenceRules() {
		return persistenceRules;
	}

	public void setPersistenceRules(PersistenceRules persistenceRules) {
		this.persistenceRules = persistenceRules;
	}

	public ElementService getElementService() {
		return elementService;
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public void applyElementPersistenceRules(
			com.klistret.cmdb.pojo.Element element) {
		logger.debug("apply persistence rules to element [{}]", element
				.toString());

		PropertyCriteria criteria = persistenceRules
				.getPropertyCriteria(element.getConfiguration());

		if (criteria != null) {
			Collection<com.klistret.cmdb.pojo.Element> results = elementService
					.findByCriteria(criteria);

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
