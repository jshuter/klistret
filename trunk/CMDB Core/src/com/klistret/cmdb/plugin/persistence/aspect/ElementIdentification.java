package com.klistret.cmdb.plugin.persistence.aspect;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.service.ElementService;

public class ElementIdentification {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementIdentification.class);

	private CIIdentification ciIdentification;

	private ElementService elementService;

	public void setURL(URL persistenceURL) {
		this.ciIdentification = new CIIdentification(persistenceURL);
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public void identify(Element element) {
		List<String> criterion = ciIdentification.getCriterion(element);

		if (criterion == null) {
			logger
					.debug("Exiting method because no valid xpath expressions for the criteria list");
			return;
		}

		List<Element> results = elementService.findByExpressions(criterion, 0,
				10);

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
