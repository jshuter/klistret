package com.klistret.cmdb.aspects.persistence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.service.ElementService;
import com.klistret.cmdb.utility.saxon.PathExpression;

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

	public void identify(Element element) {
		logger
				.debug("Applying persistence rules against element [{}]",
						element);

		List<PathExpression[]> criteria = identification
				.getCriteriaByClassname(element.getType().getName());
		if (criteria == null) {
			logger
					.debug("No persistence rules defined to element nor the element's ancestors");
			return;
		}

		PathExpression[] criterion = identification.getCriterionByObject(
				criteria, element);
		if (criterion == null) {
			logger.debug("No valid xpath expressions for the criteria list");
		}
		
		String[] expressions = new String[criterion.length];
		for (PathExpression pathExpression : criterion)
			expressions[expressions.length]
		elementService.findByExpressions(criterion, null, null);
	}
}
