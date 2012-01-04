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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.commons.Property;
import com.klistret.cmdb.ci.element.component.Publication;
import com.klistret.cmdb.ci.element.component.Software;
import com.klistret.cmdb.ci.element.context.Organization;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.ci.relation.Dependency;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.ivy.pojo.IvyModule;

/**
 * 
 * @author Matthew Young
 * 
 */
public class IvyServiceImpl implements IvyService {

	private static final Logger logger = LoggerFactory
			.getLogger(IvyServiceImpl.class);

	/**
	 * A marker linking together the dependencies at the time of registration.
	 */
	private final static String markerPrefix = "IvyRegistration";

	/**
	 * A Commons property added to dependencies during registration to denote an
	 * implicit revision was used and the value of the property is the implicit
	 * variable.
	 */
	private final static String implicitPropertyName = "ivy.implicit";

	/**
	 * Pattern for implicit revision variables
	 */
	public static final Pattern implicitStatusPattern = Pattern
			.compile("^latest(\\.\\w+){1,2}");
	/**
	 * Element service (dependency injection)
	 */
	private ElementService elementService;

	/**
	 * Element Type service (dependency injection)
	 */
	private ElementTypeService elementTypeService;

	/**
	 * Relation service (dependency injection)
	 */
	private RelationService relationService;

	/**
	 * Relation Type service (dependency injection)
	 */
	private RelationTypeService relationTypeService;

	/**
	 * Mark generator for this extension
	 */
	private static Random generator = new Random(19580427);

	/**
	 * Singleton
	 */
	private ElementType softareType;

	/**
	 * Singleton
	 */
	private RelationType dependencyType;

	/**
	 * Singleton
	 */
	private ElementType publicationType;

	/**
	 * Singleton
	 */
	private ElementType organizationType;

	/**
	 * Singleton
	 */
	private ElementType softwareContextType;

	/**
	 * Singleton
	 */
	private ElementType publicationContextType;

	/**
	 * Singleton
	 */
	private ElementType publicationTypeType;

	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	public void setElementTypeService(ElementTypeService elementTypeService) {
		this.elementTypeService = elementTypeService;
	}

	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public void setRelationTypeService(RelationTypeService relationTypeService) {
		this.relationTypeService = relationTypeService;
	}

	/**
	 * Ivy module descriptor have at a minimum 3 major sections: 1) information,
	 * 2) publications and 3) dependencies. This method builds the corresponding
	 * CIs based on the 3 primary Ivy descriptors. The information section
	 * because the Software, publications become Publication CI plus an
	 * associated dependency, and dependencies become Software CIs with a
	 * dependency.
	 * 
	 * @param moduleDescriptor
	 */
	public void register(IvyModule moduleDescriptor) {
		/**
		 * Unique find otherwise fail, no element then create and an existing is
		 * deleted then a new created.
		 */
		Element software = getSoftware(moduleDescriptor);

		/**
		 * Loop through artifacts creating a Publication CI (existing
		 * publications are deleted handling them like software) plus a
		 * Dependency CI to the Software CI
		 */
		List<Element> publications = getPublications(moduleDescriptor, software);
		logger.debug("Processed {} publications inclusive dependencies",
				publications.size());

		/**
		 * Loop through dependencies
		 */
		List<Element> dependencies = getDependencies(moduleDescriptor, software);
		logger.debug("Processed {} software dependencies", dependencies.size());
	}

	/**
	 * Get an Ivy module document for a particular Software element either
	 * masking implicit statuses or not plus giving a transient view of the Ivy
	 * descriptor or at the time of registeration.
	 * 
	 * @param organization
	 * @param module
	 * @param revision
	 * @return IvyModule
	 */
	public IvyModule get(String organization, String module, String revision,
			boolean mask, boolean trans) {
		IvyModule moduleDescriptor = new IvyModule();

		List<String> softwareExpression = new ArrayList<String>();
		softwareExpression
				.add(String
						.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
								module));

		if (implicitStatusPattern.matcher(revision).matches())
			softwareExpression
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization eq \"%s\"][component:Tag = (\"%s\")]",
									organization, revision));
		else
			softwareExpression
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization eq \"%s\"][component:Version eq \"%s\"]",
									organization, revision));

		Element software = elementService.unique(softwareExpression);

		if (software == null)
			throw new ApplicationException(
					String.format(
							"No module [name: %s, organization: %s, revision: %s] registered",
							module, organization, revision));

		/**
		 * Info
		 */
		IvyModule.Info info = new IvyModule.Info();

		info.setModule(software.getName());
		info.setOrganisation(((Software) software.getConfiguration())
				.getOrganization());
		info.setRevision(((Software) software.getConfiguration()).getVersion());
		info.setStatus(((Software) software.getConfiguration()).getPhase());
		info.setPublication(((Software) software.getConfiguration())
				.getAvailability());

		moduleDescriptor.setInfo(info);

		/**
		 * Common marker is necessary
		 */
		String marker = null;
		for (String mark : ((Software) software.getConfiguration()).getMark())
			if (mark.matches("IvyRegistration\\d*"))
				marker = mark;
		if (marker == null)
			return moduleDescriptor;

		/**
		 * Publications
		 */
		List<String> findPublications = new ArrayList<String>();
		if (!trans)
			findPublications
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Relation[pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Dependency\"][pojo:source/pojo:id eq %s][pojo:destination/pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]/pojo:configuration[commons:Mark = (\"%s\")]",
									software.getId(), marker));
		else
			findPublications
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Relation[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Dependency\"][pojo:source/pojo:id eq %s][pojo:destination/pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]",
									software.getId()));

		Integer countPublicationDependencies = relationService
				.count(findPublications);

		if (countPublicationDependencies > 0) {
			List<Relation> publicationDependencies = relationService.find(
					findPublications, 0, countPublicationDependencies);
			IvyModule.Publications publications = new IvyModule.Publications();

			for (Relation relation : publicationDependencies) {
				IvyModule.Publications.Artifact artifact = new IvyModule.Publications.Artifact();

				artifact.setName(relation.getDestination().getName());
				artifact.setType(((Publication) relation.getDestination()
						.getConfiguration()).getType());
				artifact.setExt(((Publication) relation.getDestination()
						.getConfiguration()).getExtension());

				for (String use : relation.getConfiguration().getUsage())
					artifact.setArtifactAttributeConf(use);

				publications.getArtifact().add(artifact);
			}

			moduleDescriptor.setPublications(publications);
		}

		/**
		 * Dependencies
		 */
		List<String> findDependencies = new ArrayList<String>();
		if (!trans)
			findDependencies
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Relation[pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Dependency\"][pojo:source/pojo:id eq %s][pojo:destination/pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[commons:Mark = (\"%s\")]",
									software.getId(), marker));
		else
			findDependencies
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Relation[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Dependency\"][pojo:source/pojo:id eq %s][pojo:destination/pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
									software.getId()));

		Integer countSoftwareDependencies = relationService
				.count(findDependencies);

		if (countSoftwareDependencies > 0) {
			List<Relation> softwareDependencies = relationService.find(
					findDependencies, 0, countSoftwareDependencies);
			IvyModule.Dependencies dependencies = new IvyModule.Dependencies();
			for (Relation relation : softwareDependencies) {
				IvyModule.Dependencies.Dependency dependency = new IvyModule.Dependencies.Dependency();

				dependency.setName(relation.getDestination().getName());
				dependency.setOrg(((Software) relation.getDestination()
						.getConfiguration()).getOrganization());

				if (mask) {
					String value = null;
					for (Property prop : relation.getConfiguration()
							.getProperty())
						if (prop.getName().equals(implicitPropertyName))
							value = prop.getValue();

					if (value == null)
						dependency.setRev(((Software) relation.getDestination()
								.getConfiguration()).getVersion());
					else
						dependency.setRev(value);
				} else
					dependency.setRev(((Software) relation.getDestination()
							.getConfiguration()).getVersion());

				for (String use : relation.getConfiguration().getUsage())
					dependency.setDependencyAttributeConf(use);

				dependencies.getDependency().add(dependency);
			}

			moduleDescriptor.setDependencies(dependencies);
		}

		return moduleDescriptor;
	}

	/**
	 * Implicit/explicit view of the Ivy module descriptor at registration.
	 * 
	 * @param organization
	 * @param module
	 * @param revision
	 * @return
	 */
	public IvyModule get(String organization, String module, String revision) {
		return get(organization, module, revision, true, false);
	}

	/**
	 * Transient or current view of the Ivy module descriptor with explicit
	 * revisions for dependencies.
	 * 
	 * @param organization
	 * @param module
	 * @param revision
	 * @return
	 */
	public IvyModule getTransient(String organization, String module,
			String revision) {
		return get(organization, module, revision, false, true);
	}

	/**
	 * View of the Ivy module descriptor during registration with explicit
	 * revisions only.
	 * 
	 * @param organization
	 * @param module
	 * @param revision
	 * @return
	 */
	public IvyModule getExplicit(String organization, String module,
			String revision) {
		return get(organization, module, revision, false, false);
	}

	/**
	 * Get Software type
	 * 
	 * @return ElementType
	 */
	protected ElementType getSoftwareType() {
		if (softareType == null)
			softareType = elementTypeService
					.get("{http://www.klistret.com/cmdb/ci/element/component}Software");

		return softareType;
	}

	/**
	 * Get Publication type
	 * 
	 * @return ElementType
	 */
	protected ElementType getPublicationType() {
		if (publicationType == null)
			publicationType = elementTypeService
					.get("{http://www.klistret.com/cmdb/ci/element/component}Publication");

		return publicationType;
	}

	/**
	 * Get Organization type
	 * 
	 * @return ElementType
	 */
	protected ElementType getOrganizationType() {
		if (organizationType == null)
			organizationType = elementTypeService
					.get("{http://www.klistret.com/cmdb/ci/element/context}Organization");

		return organizationType;
	}

	/**
	 * Get Software (context) type
	 * 
	 * @return ElementType
	 */
	protected ElementType getSoftwareContextType() {
		if (softwareContextType == null)
			softwareContextType = elementTypeService
					.get("{http://www.klistret.com/cmdb/ci/element/context}Software");

		return softwareContextType;
	}

	/**
	 * Get Software (context) type
	 * 
	 * @return ElementType
	 */
	protected ElementType getPublicationContextType() {
		if (publicationContextType == null)
			publicationContextType = elementTypeService
					.get("{http://www.klistret.com/cmdb/ci/element/context}Publication");

		return publicationContextType;
	}

	/**
	 * Get Publication Type type
	 * 
	 * @return ElementType
	 */
	protected ElementType getPublicationTypeType() {
		if (publicationTypeType == null)
			publicationTypeType = elementTypeService
					.get("{http://www.klistret.com/cmdb/ci/element/context}PublicationType");

		return publicationTypeType;
	}

	/**
	 * Get Dependency type
	 * 
	 * @return RelatonType
	 */
	protected RelationType getDependencyType() {
		if (dependencyType == null)
			dependencyType = relationTypeService
					.get("{http://www.klistret.com/cmdb/ci/relation}Dependency");

		return dependencyType;
	}

	/**
	 * Existing Softwre CIs are updated but all source dependencies are deleted.
	 * 
	 * @param moduleDescriptor
	 * @return
	 */
	protected Element getSoftware(IvyModule moduleDescriptor) {
		String name = moduleDescriptor.getInfo().getModule();
		String organization = moduleDescriptor.getInfo().getOrganisation();
		String version = moduleDescriptor.getInfo().getRevision();

		if (name == null)
			throw new ApplicationException(
					"Ivy module name not set (flatted path ivy-module.info.@module)");
		getSoftwareContext(name);

		if (organization == null)
			throw new ApplicationException(
					"Ivy organization not set (flatted path ivy-module.info.@organization)");
		getOrganization(organization);

		if (version == null)
			throw new ApplicationException(
					"Ivy revision not set (flatted path ivy-module.info.@revision)");

		logger.debug(
				"Registering module [name: {}, organization: {}, explicit revision: {}]",
				new Object[] { name, organization, version });

		Element element = elementService
				.unique(Arrays.asList(new String[] {
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
								name),
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization eq \"%s\"][component:Version eq \"%s\"]",
								organization, version) }));
		if (element == null) {
			element = new Element();
			element.setType(getSoftwareType());
			element.setName(name);
			element.setCreateTimeStamp(new Date());
			element.setCreateId(null);
			element.setFromTimeStamp(new Date());
			element.setUpdateTimeStamp(new Date());

			Software configuration = new Software();
			element.setConfiguration(configuration);
		}

		Software configuration = (Software) element.getConfiguration();

		/**
		 * Composite key
		 */
		configuration.setOrganization(organization);
		configuration.setVersion(version);
		configuration.setName(name);

		/**
		 * Marker tag
		 */
		String marker = null;
		for (String mark : configuration.getMark())
			if (mark.matches(markerPrefix + "\\d*"))
				marker = mark;

		if (marker == null) {
			marker = markerPrefix + generator.nextInt();
			configuration.getMark().add(marker);
		}

		/**
		 * Phase
		 */
		if (moduleDescriptor.getInfo().getStatus() != null)
			configuration.setPhase(moduleDescriptor.getInfo().getStatus());

		/**
		 * Availability
		 */
		if (moduleDescriptor.getInfo().getPublication() != null)
			configuration.setAvailability(moduleDescriptor.getInfo()
					.getPublication());

		/**
		 * License (not handled but a warning is generated)
		 */
		if (moduleDescriptor.getInfo().getLicense().size() > 0)
			logger.warn(
					"flattened path ivy-module.info.license not handled ({} instances)",
					moduleDescriptor.getInfo().getLicense().size());

		element.setConfiguration(configuration);

		/**
		 * Persistent then update but delete owning dependencies otherwise
		 * create
		 */
		if (element.getId() == null)
			elementService.create(element);
		else {
			elementService.update(element);
			relationService.cascade(element.getId(), true, false);
		}

		/**
		 * Eliminate owned dependencies (everything)
		 */
		return element;
	}

	/**
	 * Update existing publication CIs with the same composite key inclusive the
	 * type (should be unique otherwise an exception is thrown) otherwise create
	 * a new publication CI. Dependencies owned (sourced) by the publication are
	 * deleted then recreated.
	 * 
	 * @param moduleDescriptor
	 * @param software
	 * @return
	 */
	protected List<Element> getPublications(IvyModule moduleDescriptor,
			Element software) {
		String organization = moduleDescriptor.getInfo().getOrganisation();
		String version = moduleDescriptor.getInfo().getRevision();

		List<Element> publications = new ArrayList<Element>();
		for (com.klistret.cmdb.ivy.pojo.IvyModule.Publications.Artifact artifact : moduleDescriptor
				.getPublications().getArtifact()) {
			String name = artifact.getName();
			String type = artifact.getType();

			if (name == null)
				throw new ApplicationException(
						"Ivy artifact name not set (flatted path ivy-module.publications.artifact.name)");
			getPublicationContext(name);

			if (type == null)
				throw new ApplicationException(
						"Ivy artifact type not set (flatted path ivy-module.publications.artifact.type)");
			getPublicationTypeContext(type);

			Element publication = elementService
					.unique(Arrays.asList(new String[] {
							String.format(
									"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]",
									name),
							String.format(
									"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization eq \"%s\"][component:Version eq \"%s\"][component:Type eq \"%s\"]",
									organization, version, type) }));

			if (publication == null) {
				publication = new Element();
				publication.setType(getPublicationType());
				publication.setName(name);
				publication.setCreateTimeStamp(new Date());
				publication.setCreateId(null);
				publication.setFromTimeStamp(new Date());
				publication.setUpdateTimeStamp(new Date());

				Publication publicationCI = new Publication();
				publication.setConfiguration(publicationCI);
			}

			Publication publicationCI = (Publication) publication
					.getConfiguration();

			/**
			 * Composite key
			 */
			publicationCI.setOrganization(organization);
			publicationCI.setVersion(version);
			publicationCI.setName(name);
			publicationCI.setType(type);

			/**
			 * Extension
			 */
			if (artifact.getExt() != null)
				publicationCI.setExtension(artifact.getExt());

			publication.setConfiguration(publicationCI);

			/**
			 * Persistent updated deleting all owned dependencies otherwise
			 * create
			 */
			if (publication.getId() == null)
				elementService.create(publication);
			else {
				elementService.update(publication);
				relationService.cascade(publication.getId(), true, false);
			}

			publications.add(publication);

			/**
			 * Create dependency automatically since all of the software
			 * dependencies are already deleted.
			 */
			Relation dependency = new Relation();
			dependency.setType(getDependencyType());
			dependency.setSource(software);
			dependency.setDestination(publication);
			dependency.setFromTimeStamp(new Date());
			dependency.setCreateId(null);
			dependency.setCreateTimeStamp(new Date());
			dependency.setFromTimeStamp(new Date());
			dependency.setUpdateTimeStamp(new Date());

			Dependency dependencyCI = new Dependency();
			dependencyCI.setName(String.format("%s,%s", software.getName(),
					publication.getName()));

			/**
			 * Configuration attribute into usage (the entire attribute)
			 */
			String conf = null;
			if (moduleDescriptor.getPublications().getDefaultconf() != null)
				conf = moduleDescriptor.getPublications().getDefaultconf();

			if (artifact.getArtifactAttributeConf() != null)
				conf = artifact.getArtifactAttributeConf();

			if (conf != null)
				dependencyCI.getUsage().add(conf);

			/**
			 * Warn on configuration elements
			 */
			if (artifact.getArtifactElementConf().size() > 0)
				logger.warn(
						"flattened path ivy-module.publications.artifact.conf not handled ({} instances)",
						artifact.getArtifactElementConf().size());

			/**
			 * Add all software markers
			 */
			dependencyCI.getMark().addAll(
					((Software) software.getConfiguration()).getMark());

			dependency.setConfiguration(dependencyCI);
			relationService.create(dependency);
		}

		return publications;
	}

	/**
	 * Only explicit software dependencies are created if not already existing.
	 * 
	 * @param moduleDescriptor
	 * @param software
	 * @return
	 */
	protected List<Element> getDependencies(IvyModule moduleDescriptor,
			Element software) {

		List<Element> dependencies = new ArrayList<Element>();
		for (com.klistret.cmdb.ivy.pojo.IvyModule.Dependencies.Dependency module : moduleDescriptor
				.getDependencies().getDependency()) {
			String name = module.getName();
			String org = module.getOrg();
			String rev = module.getRev();

			/**
			 * Is revision implicit or explicit
			 */
			List<String> expressions = new ArrayList<String>();
			expressions
					.add(String
							.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
									name));

			boolean implicit = implicitStatusPattern.matcher(rev).matches();
			if (implicit)
				expressions
						.add(String
								.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element/pojo:configuration[component:Organization eq \"%s\"][commons:Tag = (\"%s\")]",
										org, rev));
			else
				expressions
						.add(String
								.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization eq \"%s\"][component:Version eq \"%s\"]",
										org, rev));

			Element other = elementService.unique(expressions);
			if (other == null && !implicit) {
				other = new Element();
				other.setType(getSoftwareType());
				other.setName(name);
				other.setCreateTimeStamp(new Date());
				other.setCreateId(null);
				other.setFromTimeStamp(new Date());
				other.setUpdateTimeStamp(new Date());

				Software otherCI = new Software();

				/**
				 * Composite key
				 */
				otherCI.setOrganization(org);
				otherCI.setVersion(rev);
				otherCI.setName(name);

				other.setConfiguration(otherCI);
				elementService.create(other);
			}

			if (other == null && implicit)
				throw new ApplicationException(
						String.format(
								"Implicit dependencies must be resolved. No software dependency [organization: %s, name: %s, implicit rev: %s] ",
								org, name, rev));

			dependencies.add(other);

			/**
			 * Create dependency
			 */
			Relation dependency = new Relation();
			dependency.setType(getDependencyType());
			dependency.setSource(software);
			dependency.setDestination(other);
			dependency.setFromTimeStamp(new Date());
			dependency.setCreateId(null);
			dependency.setCreateTimeStamp(new Date());
			dependency.setFromTimeStamp(new Date());
			dependency.setUpdateTimeStamp(new Date());

			Dependency dependencyCI = new Dependency();
			dependencyCI.setName(String.format("%s,%s", software.getName(),
					other.getName()));

			/**
			 * Configuration
			 */
			String conf = null;
			if (moduleDescriptor.getDependencies().getDefaultconf() != null)
				conf = moduleDescriptor.getDependencies().getDefaultconf();
			if (module.getDependencyAttributeConf() != null)
				conf = module.getDependencyAttributeConf();

			if (conf != null)
				dependencyCI.getUsage().add(conf);

			/**
			 * Warn on configuration elements
			 */
			if (module.getDependencyElementConf().size() > 0)
				logger.warn(
						"flattened path ivy-module.dependencies.dependency.conf not handled ({} instances)",
						module.getDependencyElementConf().size());

			/**
			 * Add all software markers
			 */
			dependencyCI.getMark().addAll(
					((Software) software.getConfiguration()).getMark());

			/**
			 * Add implicit property
			 */
			if (implicit) {
				Property implicitProperty = new Property();
				implicitProperty.setName(implicitPropertyName);
				implicitProperty.setValue(rev);

				dependencyCI.getProperty().add(implicitProperty);
			}

			dependency.setConfiguration(dependencyCI);
			relationService.create(dependency);
		}

		return dependencies;
	}

	/**
	 * Get organization
	 * 
	 * @param name
	 * @return
	 */
	protected Element getOrganization(String name) {
		Element element = elementService
				.unique(Arrays.asList(new String[] { String
						.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Organization\"]",
								name) }));

		if (element == null) {
			element = new Element();
			element.setName(name);
			element.setType(getOrganizationType());
			element.setName(name);
			element.setCreateTimeStamp(new Date());
			element.setCreateId(null);
			element.setFromTimeStamp(new Date());
			element.setUpdateTimeStamp(new Date());

			Organization configuration = new Organization();
			configuration.setName(name);

			element.setConfiguration(configuration);
			elementService.create(element);
		}

		return element;
	}

	/**
	 * Get software (context)
	 * 
	 * @param name
	 * @return
	 */
	protected Element getSoftwareContext(String name) {
		Element element = elementService
				.unique(Arrays.asList(new String[] { String
						.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Software\"]",
								name) }));

		if (element == null) {
			element = new Element();
			element.setName(name);
			element.setType(getSoftwareContextType());
			element.setName(name);
			element.setCreateTimeStamp(new Date());
			element.setCreateId(null);
			element.setFromTimeStamp(new Date());
			element.setUpdateTimeStamp(new Date());

			com.klistret.cmdb.ci.element.context.Software configuration = new com.klistret.cmdb.ci.element.context.Software();
			configuration.setName(name);

			element.setConfiguration(configuration);
			elementService.create(element);
		}

		return element;
	}

	/**
	 * Get publication (context)
	 * 
	 * @param name
	 * @return
	 */
	protected Element getPublicationContext(String name) {
		Element element = elementService
				.unique(Arrays.asList(new String[] { String
						.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}Publication\"]",
								name) }));

		if (element == null) {
			element = new Element();
			element.setName(name);
			element.setType(getPublicationContextType());
			element.setName(name);
			element.setCreateTimeStamp(new Date());
			element.setCreateId(null);
			element.setFromTimeStamp(new Date());
			element.setUpdateTimeStamp(new Date());

			com.klistret.cmdb.ci.element.context.Publication configuration = new com.klistret.cmdb.ci.element.context.Publication();
			configuration.setName(name);

			element.setConfiguration(configuration);
			elementService.create(element);
		}

		return element;
	}

	/**
	 * Get publication type (context)
	 * 
	 * @param name
	 * @return
	 */
	protected Element getPublicationTypeContext(String name) {
		Element element = elementService
				.unique(Arrays.asList(new String[] { String
						.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name eq \"%s\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/context}PublicationType\"]",
								name) }));

		if (element == null) {
			element = new Element();
			element.setName(name);
			element.setType(getPublicationTypeType());
			element.setName(name);
			element.setCreateTimeStamp(new Date());
			element.setCreateId(null);
			element.setFromTimeStamp(new Date());
			element.setUpdateTimeStamp(new Date());

			com.klistret.cmdb.ci.element.context.PublicationType configuration = new com.klistret.cmdb.ci.element.context.PublicationType();
			configuration.setName(name);

			element.setConfiguration(configuration);
			elementService.create(element);
		}

		return element;
	}
}
