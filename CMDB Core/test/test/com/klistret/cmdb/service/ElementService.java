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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.klistret.cmdb.exception.ApplicationException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.klistret.cmdb.ci.element.component.Software;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.ci.relation.Composition;

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
				.get("{http://www.klistret.com/cmdb/ci/element/component}Software");

		dummyElement = new Element();
		dummyElement.setName("core");
		dummyElement.setType(elementType);
		dummyElement.setFromTimeStamp(new java.util.Date());
		dummyElement.setCreateTimeStamp(new java.util.Date());
		dummyElement.setUpdateTimeStamp(new java.util.Date());

		Software configuration = new Software();
		configuration.setName("core");

		configuration.setOrganization("com.klistret");
		configuration.setVersion("0.1");

		dummyElement.setConfiguration(configuration);
	}

	/**
	 * Get element
	 * 
	 * @throws JAXBException
	 */

	public void getElement() throws JAXBException {
		Element element = elementService.get(new Long(577336));

		System.out.println(element.getId());
		assertNotNull(element);
	}

	/**
	 * Update element
	 */

	public void updateElement() {
		Element element = elementService.get(new Long(577336));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");
		System.out.println("get version: "
				+ sdf.format(element.getUpdateTimeStamp()));

		try {
			element.setUpdateTimeStamp(new Date());
			element = elementService.update(element);
			System.out.println("get version: "
					+ sdf.format(element.getUpdateTimeStamp()));
		} catch (ApplicationException e) {
			System.out.println(e.getMessage());
		}

		assertNotNull(element);
	}

	/**
	 * Create element then delete
	 */
	@Test
	public void createElement() {
		elementService.create(dummyElement);
		assertNotNull(dummyElement);
	}

	/**
	 * Find elements
	 */

	public void findElement() {
		List<Element> response = elementService
				.find(Arrays
						.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\" or pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]",
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element[pojo:name eq \"INF\"]/pojo:configuration[\"0069_A06\" gt component:Version]" }),
						0, 10);

		assertNotNull(response);
		System.out.println(response.size());
	}

	public void maxElement() {
		String response = elementService
				.aggregate(
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; max(/pojo:Element/pojo:configuration/component:Version)",
						Arrays.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\" or pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]",
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element[pojo:name eq \"INF\"]/pojo:configuration[matches(component:Version,\"0068\")]" }));

		assertNotNull(response);
		System.out.println(response);
	}

	/**
	 * Count elements
	 */

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
	 * Unique elements
	 */

	public void uniqueElement() {
		Element element = elementService
				.unique(Arrays
						.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/system}Application']",
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:configuration[element:Environment = (\"Ettan\",\"tm639\")][commons:Name eq \"A37\"]" }));
		System.out.println(String.format("Element [name: %s, id: %s]",
				element.getName(), element.getId()));

		assertNotNull(element);
	}

	/**
	 * Get relation
	 */

	public void getRelation() {
		Relation relation = relationService.get(new Long(1));
		assertNotNull(relation);
	}

	/**
	 * Find relations
	 */

	public void findRelation() {
		List<Relation> response = relationService
				.find(Arrays
						.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Relation[empty(pojo:toTimeStamp) and pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]/pojo:source[pojo:name eq \"KUI\" and pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]/pojo:configuration[element:Environment = (\"Test\")]",
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Relation/pojo:destination[pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[commons:Name eq \"KUI\" and component:Organization = (\"Försäkringskassan\")]" }),
						0, 25);

		assertNotNull(response);
	}

	public void findElement2() {
		List<Element> response = elementService
				.find(Arrays
						.asList(new String[] { "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element[pojo:name eq \"KUI\"][empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]/pojo:configuration[element:Environment = \"Produktion\"]" }),
						0, 25);

		assertNotNull(response);
	}

	public void findSoftware() {
		Integer count = elementService
				.count(Arrays
						.asList(new String[] { "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:destinationRelations[empty(pojo:toTimeStamp)]/pojo:source[empty(pojo:toTimeStamp) and pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]/pojo:configuration[element:Environment = (\"Produktion\")]" }));

		assertNotNull(count);
	}

	public void countRelation() {
		List<String> expressions = Arrays
				.asList(new String[] { "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Dependency\"]/pojo:source[pojo:id eq "
						+ new Long(9) + "]" });
		Integer count = relationService.count(expressions);

		assertNotNull(count);
	}

	public void mistake() {
		List<String> expressions = Arrays
				.asList(new String[] {
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]",
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Relation/pojo:source[empty(pojo:toTimeStamp)][pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/system}Application\"][pojo:name eq \"INF\"]/pojo:configuration[element:Environment = (\"Test\")]",
						"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Relation/pojo:destination[empty(pojo:toTimeStamp)][pojo:type/pojo:name = \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]/pojo:configuration[component:Organization eq \"Försäkringskassan\" and component:Type eq \"Version\"]" });

		Integer count = relationService.count(expressions);
		System.out.println("Count: " + count);
		assertNotNull(count);
	}

	public void createRelation() {
		Element application = elementService.get(new Long(412221));

		Element software = elementService.get(new Long(412016));

		RelationType type = relationTypeService
				.get("{http://www.klistret.com/cmdb/ci/relation}Composition");

		Relation relation = new Relation();
		relation.setType(type);
		relation.setFromTimeStamp(new Date());
		relation.setToTimeStamp(null);
		relation.setUpdateTimeStamp(new Date());
		relation.setCreateTimeStamp(new Date());

		relation.setSource(application);
		relation.setDestination(software);

		Composition configuration = new Composition();
		configuration.setName(software.getName());

		relation.setConfiguration(configuration);

		relationService.create(relation);
	}
}
