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

import java.sql.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;


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

	@Test
	public void getById() {
		com.klistret.cmdb.pojo.Element element = elementService
				.getById(new Long(46));

		element.getConfiguration().setNamespace("test");
		elementService.set(element);
	}

	@Test
	@Rollback(value = false)
	public void set() {
		com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument document = com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument.Factory
				.newInstance();
		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = document
				.addNewEnvironment();
		environment.setName("whatever");
		environment.setNamespace("production");

		com.klistret.cmdb.pojo.ElementType type = elementTypeService
				.getByCompositeId(environment.schemaType().getFullJavaName());

		Timestamp currentTimeStamp = new Timestamp(new java.util.Date()
				.getTime());

		com.klistret.cmdb.pojo.Element element = new com.klistret.cmdb.pojo.Element();
		element.setName("whatever");
		element.setType(type);
		element.setFromTimeStamp(currentTimeStamp);
		element.setCreateTimeStamp(currentTimeStamp);
		element.setConfiguration(environment);

		elementService.set(element);
	}
}
