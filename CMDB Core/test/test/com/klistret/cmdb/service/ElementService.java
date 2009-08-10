package test.com.klistret.cmdb.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.klistret.cmdb.pojo.PropertyCriteria;
import com.klistret.cmdb.pojo.PropertyCriterion;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:Spring.cfg.xml" })
@TransactionConfiguration
@Transactional
public class ElementService extends
		AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	protected com.klistret.cmdb.service.ElementService elementService;

	// @Test
	public void getById() {
		elementService.getById(new Long(0));
	}

	@Test
	public void findByCriteria() throws MalformedURLException {
		com.klistret.cmdb.identification.PersistenceRules rules = new com.klistret.cmdb.identification.PersistenceRules(
				new URL("file:C:\\temp\\persistenceRules.xml"));

		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = com.klistret.cmdb.xmlbeans.element.logical.collection.Environment.Factory
				.newInstance();
		environment.setName("whatever");
		environment.setNamespace("hello");

		PropertyCriteria critera = rules.getPropertyCriteria(environment);

		elementService.findByCriteria(critera);
	}

}
