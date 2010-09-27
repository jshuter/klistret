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

import java.util.Arrays;

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
import com.klistret.cmdb.ci.pojo.QueryResponse;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.pojo.QueryRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:Spring.cfg.xml" })
@TransactionConfiguration
@Transactional
public class ElementService extends
		AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	protected com.klistret.cmdb.service.ElementService elementService;

	@Autowired
	protected com.klistret.cmdb.service.ElementTypeService elementTypeService;

	//@Test
	//@Rollback(value = false)
	public void getById() throws JAXBException {
		Element element = elementService.getById(new Long(81));
		System.out
				.println(element.getName());
	}

	//@Test
	//@Rollback(value = false)
	public void setElement() {
		ElementType elementType = elementTypeService
				.getByCompositeId("com.klistret.cmdb.ci.element.logical.collection.Environment");

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

		elementService.set(element);
	}

	@Test
	@Rollback(value = false)
	public void findByExpr() {
		String[] expressions = { "declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element/pojo:configuration/commons:Name[. = \"Saturnus\"]" };

		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setExpressions(Arrays.asList(expressions));
		queryRequest.setStart(0);
		queryRequest.setLimit(100);

		QueryResponse response = elementService
				.findByExpressions(queryRequest);
		for (Element element : response.getElements())
			System.out.println(element.getName());
	}
}
