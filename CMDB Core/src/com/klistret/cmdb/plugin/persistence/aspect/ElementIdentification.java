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

package com.klistret.cmdb.plugin.persistence.aspect;

import java.net.URL;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.service.ElementService;

/**
 * 
 * @author Matthew Young
 * 
 */
public class ElementIdentification {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementIdentification.class);

	private CIIdentification ciIdentification;

	private ElementService elementService;
	
	private static final String activeQuery = "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]";

	public void setUrl(URL url) {
		this.ciIdentification = new CIIdentification(url);
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	/**
	 * If the criterion does not exist the method returns (ie. no proceeding).
	 * If the element matches existing then an exception is raised.
	 * 
	 * @param element
	 */
	public void identify(Element element) {
		List<String> criterion = ciIdentification.getCriterion(element);

		if (criterion == null) {
			logger
					.debug("Exiting method because no valid xpath expressions for the criteria list");
			return;
		}

		criterion.add(activeQuery);
		List<Element> results = elementService.find(criterion, 0, 10);

		if (element.getId() == null && !results.isEmpty()) {
			logger
					.debug(
							"Non-persistence element is identical to other elements [count: {}] according to persistence rules ",
							results.size());
			throw new ApplicationException(
					String
							.format(
									"Non-persistence element is identical to other elements [count: %d] according to persistence rules ",
									results.size()),
					new RejectedExecutionException());
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
											element.getId(), other.getId()),
							new RejectedExecutionException());
				}
			}
		}

		logger.debug("Identification allowing set to proceed");

	}
}
