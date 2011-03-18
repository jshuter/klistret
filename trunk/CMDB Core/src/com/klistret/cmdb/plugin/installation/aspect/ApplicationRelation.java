package com.klistret.cmdb.plugin.installation.aspect;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.element.component.software.ApplicationSoftware;
import com.klistret.cmdb.ci.element.process.change.Installation;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.service.ElementService;
import com.klistret.cmdb.service.RelationService;

public class ApplicationRelation {

	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationRelation.class);

	private RelationService relationService;

	private ElementService elementService;

	private String state;

	private String elementType;

	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	public void relate(Element element) {
		if (element.getType().getName().equals(elementType)) {
			Installation installation = (Installation) element
					.getConfiguration();

			if (installation.getState().equals(state)) {
				/**
				 * Find applications with a matching environment and application
				 * software relation that has the same organization plus module
				 * properties.
				 */
				Element applicationSoftwareElement = elementService
						.get(installation.getDestination().getId());
				ApplicationSoftware as = (ApplicationSoftware) applicationSoftwareElement
						.getConfiguration();

				List<Element> results = elementService
						.find(
								Arrays
										.asList(new String[] {
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:configuration/element:Environment[text() = \""
														+ installation
																.getSource()
																.getName()
														+ "\"]",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:sourceRelations[empty(pojo:toTimeStamp)]/pojo:destination/pojo:configuration[sw:Organization = \""
														+ as.getOrganization()
														+ "\" and sw:Module = \""
														+ as.getModule()
														+ "\"]" }), 0, 100);
				for (Element application : results) {
					relationService.find(null, 0, 100);
				}
			}
		}
	}
}
