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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.klistret.cmdb.utility.hibernate.HibernateUTC;

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
	@Rollback(value = false)
	public void getById() {
		com.klistret.cmdb.xmlbeans.pojo.Element element = elementService
				.getById(new Long(44));

		element.getConfiguration().setNamespace("test");
		elementService.set(element);
	}

	//@Test
	//@Rollback(value = false)
	public void set() {
		com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument document = com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument.Factory
				.newInstance();
		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = document
				.addNewEnvironment();
		environment.setName("whatever");
		environment.setNamespace("development");

		com.klistret.cmdb.xmlbeans.pojo.ElementType type = elementTypeService
				.getByCompositeId(environment.schemaType().getFullJavaName());

		com.klistret.cmdb.xmlbeans.pojo.Element element = com.klistret.cmdb.xmlbeans.pojo.Element.Factory
				.newInstance();
		element.setName("whatever");
		element.setType(type);
		element.setFromTimeStamp(HibernateUTC.getCurrentCalendar());
		element.setCreateTimeStamp(HibernateUTC.getCurrentCalendar());
		element.setConfiguration(environment);

		elementService.set(element);

		environment.setName("production");
		elementService.set(element);
	}
}
