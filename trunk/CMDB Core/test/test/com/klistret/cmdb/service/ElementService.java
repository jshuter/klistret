package test.com.klistret.cmdb.service;

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
	public void findByCriteria() {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setClassName("com.klistret.cmdb.pojo.Element");
		propertyCriteria
				.setXmlClassName("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");

		List<PropertyCriterion> criteria = new ArrayList<PropertyCriterion>();

		PropertyCriterion typeCriterion = new PropertyCriterion();
		typeCriterion.setPropertyLocationPath("type.name");
		typeCriterion.setValue("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");
		typeCriterion.setOperator(PropertyCriterion.operators.equal);
		criteria.add(typeCriterion);

		PropertyCriterion configurationCriterion = new PropertyCriterion();
		configurationCriterion.setPropertyLocationPath("configuration.Name");
		configurationCriterion.setValue("whatever");
		configurationCriterion.setOperator(PropertyCriterion.operators.matches);
		criteria.add(configurationCriterion);

		propertyCriteria.setPropertyCriteria(criteria);

		elementService.findByCriteria(propertyCriteria);
	}
}
