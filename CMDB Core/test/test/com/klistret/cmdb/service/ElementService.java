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
		propertyCriteria.setEntityName("Element");

		List<PropertyCriterion> criteria = new ArrayList<PropertyCriterion>();

		PropertyCriterion toTimeStampCriterion = new PropertyCriterion();
		toTimeStampCriterion.setPropertyLocationPath("toTimeStamp");
		toTimeStampCriterion.setValue("2009-01-01-00.00.00.000000");
		toTimeStampCriterion
				.setOperator(PropertyCriterion.operators.greaterThan);
		criteria.add(toTimeStampCriterion);

		propertyCriteria.setPropertyCriteria(criteria);

		elementService.findByCriteria(propertyCriteria);
	}
}
