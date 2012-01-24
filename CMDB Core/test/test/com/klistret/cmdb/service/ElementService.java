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

package test.com.klistret.cmdb.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ActiveProfiles;

import com.klistret.cmdb.ci.pojo.Element;

/**
 * Element services are tested directly
 * 
 * @author Matthew Young
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:Spring.cfg.xml" })
@ActiveProfiles("development")
public class ElementService {

	@Autowired
	protected com.klistret.cmdb.service.ElementService elementService;

	@Autowired
	protected com.klistret.cmdb.service.ElementTypeService elementTypeService;

	@Autowired
	protected com.klistret.cmdb.service.RelationService relationService;

	@Autowired
	protected com.klistret.cmdb.service.RelationTypeService relationTypeService;

	private static final String softwareType = "{http://www.klistret.com/cmdb/ci/element/component}Software";

	private static final String organization = "com.test";

	private static final String version = "0.1";

	private static final String name = "demo";

	private Long createId = new Long(701120);

	/**
	 * Create a software element
	 */
	@Test
	public void createElement() {
		com.klistret.cmdb.ci.pojo.ElementType type = elementTypeService
				.get(softwareType);

		Element element = new Element();
		element.setName(name);
		element.setType(type);
		element.setFromTimeStamp(new Date());
		element.setCreateTimeStamp(new Date());

		com.klistret.cmdb.ci.element.component.Software configuration = new com.klistret.cmdb.ci.element.component.Software();
		configuration.setName(name);
		configuration.setOrganization(organization);
		configuration.setVersion(version);
		configuration.getTag().add("eclipse");

		element.setConfiguration(configuration);

		elementService.create(element);

		assertNotNull(element.getId());
	}

	/**
	 * Get element
	 * 
	 */
	@Test
	public void get() {
		Element element = elementService.get(createId);
		assertNotNull(element);
	}

	/**
	 * Expected failure
	 */
	@Test(expected = com.klistret.cmdb.exception.ApplicationException.class)
	public void failGet() {
		Element element = elementService.get(new Long(0));
		assertNull(element);
	}

	/**
	 * Update element
	 */
	@Test
	public void update() {
		Element element = elementService.get(createId);
		elementService.update(element);
		assertNotNull(element);
	}

	/**
	 * Find (software)
	 */
	@Test
	public void find() {
		List<Element> response = elementService
				.find((Arrays.asList(new String[] {
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
								organization, version) })),

				0, 10);

		assertNotNull(response);
	}

	/**
	 * Use like wildcards
	 */
	@Test
	public void like() {
		List<Element> response = elementService
				.find((Arrays
						.asList(new String[] { "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,'ASS%') and empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']" })),

				0, 10);

		boolean starting = true;
		for (Element element : response)
			if (!element.getName().startsWith("ASS"))
				starting = false;

		assertTrue(starting);
	}

	/**
	 * Max software version
	 */
	@Test
	public void max() {
		String response = elementService
				.aggregate(
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; max(/pojo:Element/pojo:configuration/component:Version)",
						Arrays.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
								String.format(
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
										organization, version) }));

		assertNotNull(response);
	}

	/**
	 * Count software elements
	 */
	@Test
	public void count() {
		Integer response = elementService
				.count(Arrays.asList(new String[] {
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
								organization, version) }));

		assertNotNull(response);
	}

	/**
	 * Unique software element
	 */
	@Test(expected = com.klistret.cmdb.exception.ApplicationException.class)
	public void unique() {
		Element element = elementService
				.unique(Arrays.asList(new String[] {
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element[pojo:name eq \"%s\"]/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
								name, organization, version) }));
		assertNotNull(element);
	}

	/**
	 * Delete created element
	 */
	@Test(expected = com.klistret.cmdb.exception.ApplicationException.class)
	public void delete() {
		Element element = elementService.delete(createId);
		assertNotNull(element);
	}

	/**
	 * Multiple deletes
	 */
	@Test
	public void deletes() {
		Integer count = elementService
				.count(Arrays.asList(new String[] {
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
								organization, version) }));

		List<Element> results = elementService
				.find((Arrays.asList(new String[] {
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
						String.format(
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
								organization, version) })),

				0, count);

		for (Element element : results)
			elementService.delete(element.getId());
	}
}
