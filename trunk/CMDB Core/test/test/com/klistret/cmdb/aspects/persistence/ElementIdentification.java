package test.com.klistret.cmdb.aspects.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.klistret.cmdb.ci.element.logical.collection.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.service.ElementService;
import com.klistret.cmdb.utility.jaxb.CIContextHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:Spring.cfg.xml" })
@TransactionConfiguration
@Transactional
public class ElementIdentification {

	private Element element;

	@Before
	public void setUp() throws Exception {
		/**
		 * element
		 */
		ElementType elementType = new ElementType();
		elementType.setId(new Long(1));
		elementType
				.setName("com.klistret.cmdb.ci.element.logical.collection.Environment");
		elementType.setCreateTimeStamp(new Date());

		Environment environment = new Environment();
		environment.setName("Saturnus");
		environment.setNamespace("whatever");

		element = new Element();
		element.setName("Saturnus");
		element.setType(elementType);
		element.setConfiguration(environment);
	}

	@Autowired
	protected ElementService elementService;

	@Autowired
	protected CIContextHelper ciContextHelper;

	@Test
	@Rollback(value = false)
	public void identify() throws MalformedURLException {
		com.klistret.cmdb.aspects.persistence.ElementIdentification aop = new com.klistret.cmdb.aspects.persistence.ElementIdentification();
		com.klistret.cmdb.aspects.persistence.Identification identification = new com.klistret.cmdb.aspects.persistence.Identification();

		identification.setCiContextHelper(ciContextHelper);
		identification.setPersistenceRules(new URL("classpath:persistence.rules.xml"));

		aop.setElementService(elementService);
		aop.setIdentification(identification);

		aop.identify(element);
	}
}
