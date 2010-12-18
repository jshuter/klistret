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

import com.klistret.cmdb.ci.element.logical.collection.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;

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

	// @Test
	// @Rollback(value = false)
	public void getById() throws JAXBException {
		Element element = elementService.get(new Long(81));
		
		assertNotNull(element);
	}

	// @Test
	// @Rollback(value = false)
	public void setElement() {
		ElementType elementType = elementTypeService
				.get("{http://www.klistret.com/cmdb/ci/element/logical/collection}Environment");

		Element element = new Element();
		element.setId(new Long(81));
		element.setName("Saturnus");
		element.setType(elementType);
		element.setFromTimeStamp(new java.util.Date());
		element.setCreateTimeStamp(new java.util.Date());
		element.setUpdateTimeStamp(new java.util.Date());

		Environment environment = new Environment();
		environment.setName("Saturnus");
		environment.setWatermark("production");

		element.setConfiguration(environment);

		elementService.create(element);
		
		assertNotNull(element);
	}

	@Test
	@Rollback(value = false)
	public void findByExpr() {
		String[] expressions = { "declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element[empty(pojo:toTimeStamp) and exists(pojo:fromTimeStamp)]/pojo:type[pojo:name=\"{http://www.klistret.com/cmdb/ci/element/logical/collection}Environment\"]" };

		List<Element> response = elementService.find(
				Arrays.asList(expressions), 0, 10);
		
		assertNotNull(response);
	}
}
