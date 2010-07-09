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

package com.klistret.cmdb.aspects.persistence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.service.ElementService;
import com.klistret.cmdb.utility.saxon.PathExpression;

/**
 * AOP invocation to identify elements prior to set calls
 * 
 * @author Matthew Young
 * 
 */
public class ElementIdentification {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementIdentification.class);

	private Identification identification;

	private ElementService elementService;

	public void setIdentification(Identification identification) {
		this.identification = identification;
	}

	public Identification getIdentification() {
		return this.identification;
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public ElementService getElementService() {
		return this.elementService;
	}

	/**
	 * Applies persistence rules against the passed element to determine if a
	 * criterion for the element has results against the database (i.e. uniqueness).
	 * 
	 * @param element
	 */
	public void identify(Element element) {
		logger
				.debug("Applying persistence rules against element [{}]",
						element);

		List<PathExpression[]> criteria = identification
				.getCriteriaByClassname(element.getType().getName());
		if (criteria == null) {
			logger
					.debug("Exiting method because no persistence rules defined to element nor the element's ancestors");
			return;
		}

		String[] criterion = identification.getCriterionByObject(criteria,
				element);
		if (criterion == null) {
			logger.debug("Exiting method because no valid xpath expressions for the criteria list");
			return;
		}

		List<Element> results = elementService.findByExpressions(criterion, 0,
				1);

		if (element.getId() == null && !results.isEmpty()) {
			logger
					.debug(
							"Non-persistence element is identical to other elements [count: {}] according to persistence rules ",
							results.size());
			throw new ApplicationException(
					String
							.format(
									"Non-persistence element is identical to other elements [count: %d] according to persistence rules ",
									results.size()));
		}

		if (element.getId() != null && !results.isEmpty()) {
			for (Element other : results) {
				if (!element.equals(other)) {
					logger
							.debug(
									"Element [{}] is identical to other [{}] according to persistence rules ",
									element.getId(), other.getId());
					throw new ApplicationException(
							String
									.format(
											"Element [%d] is identical to other [%d] according to persistence rules ",
											element.getId(), other.getId()));
				}
			}
		}

		logger.debug("Identification allowing set to proceed");
	}
}
