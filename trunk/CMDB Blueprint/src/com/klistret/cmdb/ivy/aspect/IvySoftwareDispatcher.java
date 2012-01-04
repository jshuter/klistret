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
package com.klistret.cmdb.ivy.aspect;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;

import com.klistret.cmdb.ci.element.component.Software;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.service.ElementService;

/**
 * CRUD creates and updates to Software elements may effect the Ivy status tags.
 * A change to the phase or availability properties or both may effect which
 * Software element is considered latest within these aspects. CRUD is atomic
 * and the logic here must take into consideration infinite recursion. To avoid
 * an never ending circular loop an element is not updated if a latest tag per
 * aspect already exists or if other software has a greater version property.
 * The preservation of tags is critical and a weak point. If a Software element
 * does not contain a latest aspect tag and has the greatest version for that
 * aspect only then may it be updated (i.e. with the tag). When that element is
 * tagged the same tag is removed from all other software elements which also
 * have that tag. This causes a CRUD event on these elements as well the current
 * element but neither should pass the conditions for updating.
 * 
 * @author Matthew Young
 * 
 */
public class IvySoftwareDispatcher {
	private static final Logger logger = LoggerFactory
			.getLogger(IvySoftwareDispatcher.class);

	/**
	 * Software type
	 */
	private static final String softwareTypeName = "{http://www.klistret.com/cmdb/ci/element/component}Software";

	/**
	 * Dependency injection
	 */
	private ElementService elementService;

	/**
	 * Set element service
	 * 
	 * @param elementService
	 */
	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	/**
	 * Get element service
	 * 
	 * @return
	 */
	public ElementService getElementService() {
		return this.elementService;
	}

	/**
	 * There are 3 latest aspects (i.e. where the version is greatest) of
	 * importance. In relation to the software lifecycle and software
	 * availability date or a combination of these aspects.
	 * 
	 * @param message
	 */
	public void updateStatus(Message<Element> message) {
		Element element = message.getPayload();
		if (element.getId() == null)
			throw new ApplicationException(
					"Ivy Software dispatcher expects persisted elements only");

		if ((message.getHeaders().get("function").equals("UPDATE") || message
				.getHeaders().get("function").equals("CREATE"))
				&& element.getType().getName().equals(softwareTypeName)) {
			Software configuration = (Software) element.getConfiguration();

			/**
			 * Latest phase metadata
			 */
			if (configuration.getPhase() == null)
				logger.debug(
						"Software [id: {}, organization: {}, name: {}] has no phase set",
						new Object[] { element.getId(),
								configuration.getOrganization(),
								element.getName() });
			else {
				String tag = String.format("latest.%s",
						configuration.getPhase());

				if (configuration.getTag().contains(tag))
					logger.debug(
							"Software [id: {}, organization: {}, name: {}] already has tag {}",
							new Object[] { element.getId(),
									configuration.getOrganization(),
									element.getName(), tag });
				else {
					int count = elementService
							.count(Arrays.asList(new String[] { String
									.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element[empty(pojo:toTimeStamp) and pojo:id ne %d and pojo:name eq \"%s\" and pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization = (\"%s\") and component:Phase = (\"%s\") and component:Version gt \"%s\"]",
											element.getId(), element.getName(),
											configuration.getOrganization(),
											configuration.getPhase(),
											configuration.getVersion()) }));
					if (count > 0)
						logger.debug(
								"Other software have greater versions than software [id: {}, organization: {}, name: {}] for phase {}",
								new Object[] { element.getId(),
										configuration.getOrganization(),
										element.getName(),
										configuration.getPhase() });
					else {
						List<String> expressions = Arrays
								.asList(new String[] { String
										.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element[empty(pojo:toTimeStamp) and pojo:name eq \"%s\" and pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization = (\"%s\") and commons:Tag = (\"%s\")]",
												element.getName(),
												configuration.getOrganization(),
												tag) });

						int limit = elementService.count(expressions);
						if (limit > 0) {
							List<Element> results = elementService.find(
									expressions, 0, limit);

							for (Element other : results) {
								other.getConfiguration().getTag().remove(tag);

								logger.info(
										"Removing tag {} from software [id: {}]",
										tag, other.getId());
								elementService.update(other);
							}
						}

						configuration.getTag().add(tag);
						logger.info("Added tag {} to software [id: {}]", tag,
								element.getId());

						elementService.update(element);
					}
				}
			}

			/**
			 * Latest availability metadata
			 */
			if (configuration.getAvailability() == null)
				logger.debug(
						"Software [id: {}, organization: {}, name: {}] has no availability set",
						new Object[] { element.getId(),
								configuration.getOrganization(),
								element.getName() });
			else {
				String tag = String.format("latest.%s",
						configuration.getAvailability());

				if (configuration.getTag().contains(tag))
					logger.debug(
							"Software [id: {}, organization: {}, name: {}] already has tag {}",
							new Object[] { element.getId(),
									configuration.getOrganization(),
									element.getName(), tag });
				else {
					int count = elementService
							.count(Arrays.asList(new String[] { String
									.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element[empty(pojo:toTimeStamp) and pojo:id ne %d and pojo:name eq \"%s\" and pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization = (\"%s\") and component:Availability = (\"%s\") and component:Version gt \"%s\"]",
											element.getId(), element.getName(),
											configuration.getOrganization(),
											configuration.getAvailability(),
											configuration.getVersion()) }));
					if (count > 0)
						logger.debug(
								"Other software elements have greater versions than this software [id: {}, organization: {}, name: {}] for availability {}",
								new Object[] { element.getId(),
										configuration.getOrganization(),
										element.getName(),
										configuration.getAvailability() });
					else {
						List<String> expressions = Arrays
								.asList(new String[] { String
										.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element[empty(pojo:toTimeStamp) and pojo:name eq \"%s\" and pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization = (\"%s\") and commons:Tag = (\"%s\")]",
												element.getName(),
												configuration.getOrganization(),
												tag) });

						int limit = elementService.count(expressions);
						if (limit > 0) {
							List<Element> results = elementService.find(
									expressions, 0, limit);

							for (Element other : results) {
								other.getConfiguration().getTag().remove(tag);

								logger.info(
										"Removing tag {} from software [id: {}]",
										tag, other.getId());
								elementService.update(other);
							}
						}

						configuration.getTag().add(tag);
						logger.info("Added tag {} to software [id: {}]", tag,
								element.getId());

						elementService.update(element);
					}
				}
			}

			/**
			 * Latest phase.availability metadata
			 */
			/**
			 * Latest availability metadata
			 */
			if (configuration.getAvailability() == null
					|| configuration.getPhase() == null)
				logger.debug(
						"Software [id: {}, organization: {}, name: {}] has no availability or phase set",
						new Object[] { element.getId(),
								configuration.getOrganization(),
								element.getName() });
			else {
				String tag = String.format("latest.%s.%s",
						configuration.getPhase(),
						configuration.getAvailability());

				if (configuration.getTag().contains(tag))
					logger.debug(
							"Software [id: {}, organization: {}, name: {}] already has tag {}",
							new Object[] { element.getId(),
									configuration.getOrganization(),
									element.getName(), tag });
				else {
					int count = elementService
							.count(Arrays.asList(new String[] { String
									.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element[empty(pojo:toTimeStamp) and pojo:id ne %d and pojo:name eq \"%s\" and pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization = (\"%s\") and component:Phase = (\"%s\") and component:Availability = (\"%s\") and component:Version gt \"%s\"]",
											element.getId(), element.getName(),
											configuration.getOrganization(),
											configuration.getPhase(),
											configuration.getAvailability(),
											configuration.getVersion()) }));
					if (count > 0)
						logger.debug(
								"Other software elements have greater versions than this software [id: {}, organization: {}, name: {}] for phase {} plus availability {}",
								new Object[] { element.getId(),
										configuration.getOrganization(),
										element.getName(),
										configuration.getPhase(),
										configuration.getAvailability() });
					else {
						List<String> expressions = Arrays
								.asList(new String[] { String
										.format("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; /pojo:Element[empty(pojo:toTimeStamp) and pojo:name eq \"%s\" and pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization = (\"%s\") and commons:Tag = (\"%s\")]",
												element.getName(),
												configuration.getOrganization(),
												tag) });

						int limit = elementService.count(expressions);
						if (limit > 0) {
							List<Element> results = elementService.find(
									expressions, 0, limit);

							for (Element other : results) {
								other.getConfiguration().getTag().remove(tag);

								logger.info(
										"Removing tag {} from software [id: {}]",
										tag, other.getId());
								elementService.update(other);
							}
						}

						configuration.getTag().add(tag);
						logger.info("Added tag {} to software [id: {}]", tag,
								element.getId());

						elementService.update(element);
					}
				}
			}
		}
	}
}
