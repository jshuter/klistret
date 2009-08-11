package test.com.klistret.cmdb.service;

import java.net.MalformedURLException;
import java.net.URL;
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

import com.klistret.cmdb.pojo.PropertyCriteria;

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
	public void getById() {
		com.klistret.cmdb.pojo.Element element = elementService
				.getById(new Long(46));

		element.getConfiguration().setNamespace("test");
		elementService.set(element);
	}

	// @Test
	public void findByCriteria() throws MalformedURLException {
		com.klistret.cmdb.identification.PersistenceRules rules = new com.klistret.cmdb.identification.PersistenceRules(
				new URL("file:C:\\temp\\persistenceRules.xml"));

		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = com.klistret.cmdb.xmlbeans.element.logical.collection.Environment.Factory
				.newInstance();
		environment.setName("whatever");
		// environment.setNamespace("hello");
		environment.setNamespace("dev");

		PropertyCriteria critera = rules.getPropertyCriteria(environment);

		elementService.findByCriteria(critera);
	}

	@Test
	@Rollback(value = false)
	public void set() {
		com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument document = com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument.Factory
				.newInstance();
		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = document
				.addNewEnvironment();
		environment.setName("whatever");
		// environment.setNamespace("hello");
		environment.setNamespace("dev");

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
