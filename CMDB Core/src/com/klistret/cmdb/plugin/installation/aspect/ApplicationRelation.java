package com.klistret.cmdb.plugin.installation.aspect;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.element.component.software.ApplicationSoftware;
import com.klistret.cmdb.ci.element.process.change.Installation;
import com.klistret.cmdb.ci.element.system.Application;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.ci.relation.Composition;
import com.klistret.cmdb.service.ElementService;
import com.klistret.cmdb.service.ElementTypeService;
import com.klistret.cmdb.service.RelationService;
import com.klistret.cmdb.service.RelationTypeService;

public class ApplicationRelation {

	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationRelation.class);

	private RelationService relationService;

	private RelationTypeService relationTypeService;

	private ElementService elementService;

	private ElementTypeService elementTypeService;

	private String state;

	private String elementType;

	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public void setRelationTypeService(RelationTypeService relationTypeService) {
		this.relationTypeService = relationTypeService;
	}

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public void setElementTypeService(ElementTypeService elementTypeService) {
		this.elementTypeService = elementTypeService;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	/**
	 * Two stages. First all existing relationships of software to applications
	 * tied to a particular environment are deleted. Then all applications tied
	 * to an environment and a module name are associated to the assigned
	 * software. If no application exists then one is created.
	 * 
	 * @param element
	 */
	public void relate(Element element) {
		if (element.getType().getName().equals(elementType)) {

			Installation installation = (Installation) element
					.getConfiguration();

			if (installation.getState().equals(state)) {
				logger
						.debug(
								"Element [{}] has same state [{}] as configured in the plugin",
								element, state);

				/**
				 * Find composite relationships where an application named after
				 * the module is associated with an environment and it has
				 * software the has the same organization, module, and type.
				 */
				Element applicationSoftwareElement = elementService
						.get(installation.getDestination().getId());
				ApplicationSoftware applicationSoftware = (ApplicationSoftware) applicationSoftwareElement
						.getConfiguration();

				List<Relation> applicationSoftwareRelations = relationService
						.find(
								Arrays
										.asList(new String[] {
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation/pojo:source/pojo:type[pojo:name = \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Relation/pojo:source[pojo:name = \""
														+ applicationSoftware
																.getModule()
														+ "\"]/pojo:configuration[element:Environment = \""
														+ installation
																.getSource()
																.getName()
														+ "\"]",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Relation/pojo:destination/pojo:configuration[sw:Organization = \""
														+ applicationSoftware
																.getOrganization()
														+ "\" and sw:Module = \""
														+ applicationSoftware
																.getModule()
														+ "\" and sw:Type = \""
														+ applicationSoftware
																.getType()
														+ "\"]" }), 0, 25);

				logger
						.debug(
								"Found {} software relations to the applications [{}] associated to environment [{}]",
								new Object[] {
										applicationSoftwareRelations.size(),
										applicationSoftware.getModule(),
										installation.getSource().getName() });

				/**
				 * Delete similar relationships to application software that are
				 * likely another version
				 */
				for (Relation relation : applicationSoftwareRelations) {
					logger
							.debug(
									"Deleting relation [{}] between software [{}] and application [{}]",
									new Object[] {
											relation.getId(),
											relation.getDestination().getName(),
											relation.getSource().getName() });
					relationService.delete(relation.getId());
				}

				/**
				 * Find all applications tied to a particular environment with a
				 * name corresponding to the software's module.
				 */
				List<Element> applicationElements = elementService
						.find(
								Arrays
										.asList(new String[] {
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name = \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]",
												"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element[pojo:name = \""
														+ applicationSoftware
																.getModule()
														+ "\"]/pojo:configuration[element:Environment = \""
														+ installation
																.getSource()
																.getName()
														+ "\"]" }), 0, 25);
				logger
						.debug(
								"Found {} applications by name [{}] and associated to environment [{}]",
								new Object[] { applicationElements.size(),
										applicationSoftware.getModule(),
										installation.getSource().getName() });

				/**
				 * Create a new application if none exists
				 */
				if (applicationElements.size() == 0) {
					ElementType type = elementTypeService
							.get("{http://www.klistret.com/cmdb/ci/element/system}Application");

					Element applicationElement = new Element();
					applicationElement.setName(applicationSoftware.getModule());
					applicationElement.setType(type);
					applicationElement.setFromTimeStamp(new Date());
					applicationElement.setCreateTimeStamp(new Date());

					Application configuration = new Application();
					configuration.setName(applicationSoftware.getModule());
					configuration.getEnvironment().add(
							installation.getSource().getName());
					configuration.setState("Online");

					applicationElement.setConfiguration(configuration);

					elementService.create(applicationElement);

					applicationElements.add(applicationElement);
				}

				RelationType compositionType = relationTypeService
						.get("{http://www.klistret.com/cmdb/ci/relation}Composition");

				for (Element applicationElement : applicationElements) {
					Composition config = new Composition();
					config.setName(applicationElement.getName() + ", "
							+ applicationSoftwareElement.getName());

					Relation relation = new Relation();
					relation.setType(compositionType);
					relation.setSource(applicationElement);
					relation.setDestination(applicationSoftwareElement);
					relation.setCreateTimeStamp(new java.util.Date());
					relation.setFromTimeStamp(new java.util.Date());
					relation.setConfiguration(config);

					relationService.create(relation);
					logger
							.debug(
									"Created relation [{}] between software [{}] and application [{}]",
									new Object[] {
											relation.getId(),
											relation.getDestination().getName(),
											relation.getSource().getName() });
				}
			}
		}
	}
}
