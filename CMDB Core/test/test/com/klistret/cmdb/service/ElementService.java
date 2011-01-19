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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.klistret.cmdb.ci.element.system.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.RelationType;
import com.klistret.cmdb.ci.relation.Aggregation;

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
		Element element = elementService.get(new Long(274));

		assertNotNull(element);
	}

	// @Test
	// @Rollback(value = false)
	public void set() {
		ElementType elementType = elementTypeService
				.get("{http://www.klistret.com/cmdb/ci/element/system}Environment");

		Element element = new Element();
		element.setName("Different");
		element.setType(elementType);
		element.setFromTimeStamp(new java.util.Date());
		element.setCreateTimeStamp(new java.util.Date());
		element.setUpdateTimeStamp(new java.util.Date());

		Environment environment = new Environment();
		environment.setName("Different");
		environment.setWatermark("production");
		environment.setState("Online");

		com.klistret.cmdb.ci.commons.Property property1 = new com.klistret.cmdb.ci.commons.Property();
		property1.setName("example");
		property1.setValue("of a property");

		com.klistret.cmdb.ci.commons.Property property2 = new com.klistret.cmdb.ci.commons.Property();
		property2.setName("another test");
		property2.setValue("where a property is created");

		com.klistret.cmdb.ci.commons.Property[] properties = new com.klistret.cmdb.ci.commons.Property[] {
				property1, property2 };
		environment.setProperty(Arrays.asList(properties));

		environment.setTag(Arrays.asList(new String[] { "my litte", "bears" }));

		element.setConfiguration(environment);

		elementService.create(element);

		assertNotNull(element);
	}

	//@Test
	//@Rollback(value = false)
	public void find() {
		String[] expressions = { "declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[matches(sw:Organization,\"att\")]" };

		List<Element> response = elementService.find(
				Arrays.asList(expressions), 0, 10);

		assertNotNull(response);
	}
	
	//@Test
	//@Rollback(value = false)
	public void relate() {
		RelationType type = relationTypeService
		.get("{http://www.klistret.com/cmdb/ci/relation}Aggregation");
		
		Element software = elementService.get(new Long(404));
		Element environment = elementService.get(new Long(364));
		
		Relation relation = new Relation();
		relation.setType(type);
		relation.setSource(environment);
		relation.setDestination(software);
		relation.setFromTimeStamp(new java.util.Date());
		relation.setCreateTimeStamp(new java.util.Date());
		relation.setUpdateTimeStamp(new java.util.Date());
		
		Aggregation aggregation = new Aggregation();
		aggregation.setName("364 against 404");
		
		relation.setConfiguration(aggregation);
		
		relationService.create(relation);
		
		assertNotNull(relation);
	}
	
	//@Test
	//@Rollback(value = false)
	public void getRelation() {
		Relation relation = relationService.get(new Long(1));
		assertNotNull(relation);
	}
	
	@Test
	@Rollback(value = false)
	public void findRelation() {
		String[] expressions = { "declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)]", "declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation/pojo:source[pojo:id eq 3]" };
	
		List<Relation> response = relationService.find(
				Arrays.asList(expressions), 0, 10);

		assertNotNull(response);
	}
}
