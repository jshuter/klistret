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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.klistret.cmdb.ci.element.context.Environment;
import com.klistret.cmdb.ci.element.system.Application;
import com.klistret.cmdb.ci.element.process.change.Installation;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.ci.relation.Composition;
import com.klistret.cmdb.utility.jaxb.CIContext;

/**
 * Element services are tested directly
 * 
 * @author Matthew Young
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:Spring.cfg.xml" })
@TransactionConfiguration
@Transactional
public class ElementService extends
		AbstractTransactionalJUnit4SpringContextTests {

	/**
	 * Element Service
	 */
	@Autowired
	protected com.klistret.cmdb.service.ElementService elementService;

	/**
	 * Element Type Services
	 */
	@Autowired
	protected com.klistret.cmdb.service.ElementTypeService elementTypeService;

	/**
	 * Relation Service
	 */
	@Autowired
	protected com.klistret.cmdb.service.RelationService relationService;

	/**
	 * Relation Type Services
	 */
	@Autowired
	protected com.klistret.cmdb.service.RelationTypeService relationTypeService;

	//@Test
	//@Rollback(value = false)
	public void get() throws JAXBException {
		Element element = elementService.get(new Long(224));
		System.out.println(element.getConfiguration().getClass().getName());
		System.out.println(CIContext.getCIContext().getBean(
				QName.valueOf(element.getType().getName())).getJavaClass()
				.getName());

		assertNotNull(element);
	}

	@Test
	@Rollback(value = false)
	public void getAndSet() {
		Element element = elementService.get(new Long(224));

		element.getConfiguration().setTag(
				Arrays.asList(new String[] { "Development" }));
		elementService.update(element);

		assertNotNull(element);
	}

	// @Test
	public void dummy() {
		System.out.println("file.encoding: "
				+ System.getProperty("file.encoding"));
		System.out.println("Charset: " + Charset.defaultCharset());
	}

	// @Test
	// @Rollback(value = false)
	public void set() {
		ElementType elementType = elementTypeService
				.get("{http://www.klistret.com/cmdb/ci/element/context}Environment");

		Element element = new Element();
		element.setName("tm907");
		element.setType(elementType);
		element.setFromTimeStamp(new java.util.Date());
		element.setCreateTimeStamp(new java.util.Date());
		element.setUpdateTimeStamp(new java.util.Date());

		Environment environment = new Environment();
		environment.setName("tm907");
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

		element.setConfiguration(environment);

		elementService.create(element);

		assertNotNull(element);
	}

	// @Test
	// @Rollback(value = false)
	public void set2() {
		ElementType elementType = elementTypeService
				.get("{http://www.klistret.com/cmdb/ci/element/system}Application");

		Element element = new Element();
		element.setName("PRO");
		element.setType(elementType);
		element.setFromTimeStamp(new java.util.Date());
		element.setCreateTimeStamp(new java.util.Date());
		element.setUpdateTimeStamp(new java.util.Date());

		Application application = new Application();
		application.setName("PRO");
		application.setWatermark("Testing");
		application
				.setEnvironment(Arrays.asList(new String[] { "Production" }));
		application.setState("Online");

		element.setConfiguration(application);

		elementService.create(element);

		assertNotNull(element);
	}

	// @Test
	// @Rollback(value = false)
	public void find() {
		List<Element> response = elementService
				.find(
						Arrays
								.asList(new String[] { "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:sourceRelations[empty(pojo:toTimeStamp)]/pojo:destination/pojo:configuration[sw:Version eq \"0001_A02\"]" }),
						0, 10);

		System.out.println("response size: " + response == null ? "empty"
				: response.size());

		assertNotNull(response);
	}

	// @Test
	// @Rollback(value = false)
	public void relate() {
		RelationType type = relationTypeService
				.get("{http://www.klistret.com/cmdb/ci/relation}Composition");

		Element application = elementService.get(new Long(241));
		Element software = elementService.get(new Long(342));

		Relation relation = new Relation();
		relation.setType(type);
		relation.setSource(application);
		relation.setDestination(software);
		relation.setFromTimeStamp(new java.util.Date());
		relation.setCreateTimeStamp(new java.util.Date());
		relation.setUpdateTimeStamp(new java.util.Date());

		Composition composition = new Composition();
		composition.setName(software.getName());

		relation.setConfiguration(composition);

		relationService.create(relation);

		assertNotNull(relation);
	}

	// @Test
	// @Rollback(value = false)
	public void getRelation() {
		Relation relation = relationService.get(new Long(1));
		assertNotNull(relation);
	}

	// @Test
	// @Rollback(value = false)
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

		System.out.println("response size: " + response == null ? "empty"
				: response.size());

		assertNotNull(response);
	}

	// @Test
	// @Rollback(value = false)
	public void cascade() {
		relationService.cascade(new Long(1));
	}

	// @Test
	// @Rollback(value = false)
	public void delete() {
		elementService.delete(new Long(4));
	}

	// @Test
	// @Rollback(value = false)
	public void delete2() {
		relationService.delete(new Long(146));
	}

	// @Test
	// @Rollback(value = false)
	public void settingRelations() {
		Element kui = elementService.get(new Long(241));
		Element kui_0001_a01 = elementService.get(new Long(242));

		RelationType type = relationTypeService
				.get("{http://www.klistret.com/cmdb/ci/relation}Composition");
		Relation relation = new Relation();
		relation.setType(type);
		relation.setDestination(kui_0001_a01);
		relation.setFromTimeStamp(new java.util.Date());
		relation.setCreateTimeStamp(new java.util.Date());
		relation.setUpdateTimeStamp(new java.util.Date());

		Composition composition = new Composition();
		composition.setName("242 against 241");

		relation.setConfiguration(composition);

		kui.setSourceRelations(Arrays.asList(relation));
		elementService.update(kui);
	}

	// @Test
	// @Rollback(value=false)
	public void plugin() {
		Element element = elementService.get(new Long(343));
		((Installation) element.getConfiguration()).setState("Completed");

		elementService.update(element);
	}
}
