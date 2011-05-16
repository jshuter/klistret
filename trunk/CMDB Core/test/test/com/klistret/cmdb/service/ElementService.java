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

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.klistret.cmdb.ci.element.context.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.Relation;

/**
 * Element services are tested directly
 * 
 * @author Matthew Young
 * 
 */
public class ElementService {

	private Element dummyElement;

	/**
	 * Element Service
	 */
	protected com.klistret.cmdb.service.ElementService elementService;

	/**
	 * Element Type Services
	 */
	protected com.klistret.cmdb.service.ElementTypeService elementTypeService;

	/**
	 * Relation Service
	 */
	protected com.klistret.cmdb.service.RelationService relationService;

	/**
	 * Relation Type Services
	 */
	protected com.klistret.cmdb.service.RelationTypeService relationTypeService;

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		ctx.getEnvironment().setActiveProfiles("development");
		ctx.load("classpath:Spring.cfg.xml");
		ctx.refresh();

		elementService = ctx
				.getBean(com.klistret.cmdb.service.ElementService.class);
		relationService = ctx
				.getBean(com.klistret.cmdb.service.RelationService.class);

		elementTypeService = ctx
				.getBean(com.klistret.cmdb.service.ElementTypeService.class);
		relationTypeService = ctx
				.getBean(com.klistret.cmdb.service.RelationTypeService.class);

		ElementType elementType = elementTypeService
				.get("{http://www.klistret.com/cmdb/ci/element/context}Environment");

		dummyElement = new Element();
		dummyElement.setName("Dummy");
		dummyElement.setType(elementType);
		dummyElement.setFromTimeStamp(new java.util.Date());
		dummyElement.setCreateTimeStamp(new java.util.Date());
		dummyElement.setUpdateTimeStamp(new java.util.Date());

		Environment environment = new Environment();
		environment.setName("Dummy");
		environment.setWatermark("Testing");

		com.klistret.cmdb.ci.commons.Property property1 = new com.klistret.cmdb.ci.commons.Property();
		property1.setName("example");
		property1.setValue("of a property");

		com.klistret.cmdb.ci.commons.Property property2 = new com.klistret.cmdb.ci.commons.Property();
		property2.setName("another test");
		property2.setValue("where a property is created");

		com.klistret.cmdb.ci.commons.Property[] properties = new com.klistret.cmdb.ci.commons.Property[] {
				property1, property2 };
		environment.setProperty(Arrays.asList(properties));

		environment.setTag(Arrays.asList(new String[] { "my litte", "ät" }));

		com.klistret.cmdb.ci.commons.Ownership ownership = new com.klistret.cmdb.ci.commons.Ownership();
		com.klistret.cmdb.ci.commons.Contact contact = new com.klistret.cmdb.ci.commons.Contact();
		ownership.setContact(contact);
		environment.setOwnership(ownership);

		dummyElement.setConfiguration(environment);
	}

	/**
	 * Get element
	 * 
	 * @throws JAXBException
	 */
	// @Test
	public void getElement() throws JAXBException {
		Element element = elementService.get(new Long(78941));

		assertNotNull(element);
	}

	/**
	 * Update element
	 */
	// @Test
	public void updateElement() {
		Element element = elementService.get(new Long(78941));
		elementService.update(element);

		assertNotNull(element);
	}

	/**
	 * Create element then delete
	 */
	// @Test
	public void createElement() {
		elementService.create(dummyElement);
		elementService.delete(dummyElement.getId());

		assertNotNull(dummyElement);
	}

	/**
	 * Find elements
	 */
	// @Test
	public void findElement() {
		List<Element> response = elementService
				.find(
						Arrays
								.asList(new String[] {
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/system}Application']",
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:configuration[element:Environment = (\"Ettan\",\"tm639\")]" }),
						0, 10);

		assertNotNull(response);
	}

	/**
	 * Count elements
	 */
	@Test
	public void countElement() {
		Integer response = elementService
				.count(Arrays
						.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/system}Application']",
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:configuration[element:Environment = (\"Ettan\",\"tm639\")]" }));

		System.out.println(String.format("Count [%s]", response));
		assertNotNull(response);
	}

	/**
	 * Get relation
	 */
	// @Test
	public void getRelation() {
		Relation relation = relationService.get(new Long(1));
		assertNotNull(relation);
	}

	/**
	 * Find relations
	 */
	// @Test
	public void findRelation() {
		List<Relation> response = relationService
				.find(
						Arrays
								.asList(new String[] {
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)]",
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]",
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation/pojo:source[pojo:name = \"KUI\"]/pojo:type[pojo:name = \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]",
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Relation/pojo:source/pojo:configuration/element:Environment[text() = \"Production\"]",
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Relation/pojo:destination/pojo:configuration[sw:Type = \"Version\" and sw:Module = \"KUI\" and sw:Organization = \"Försäkringskassan\"]" }),
						0, 25);

		assertNotNull(response);
	}
}
