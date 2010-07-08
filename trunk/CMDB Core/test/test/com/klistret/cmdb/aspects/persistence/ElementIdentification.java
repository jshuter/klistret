package test.com.klistret.cmdb.aspects.persistence;

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

import com.klistret.cmdb.aspects.persistence.Criterion;
import com.klistret.cmdb.aspects.persistence.PersistenceRules;
import com.klistret.cmdb.aspects.persistence.Rule;
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

	private PersistenceRules persistenceRules;

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

		/**
		 * persistence rules
		 */
		persistenceRules = new PersistenceRules();

		Criterion cName = new Criterion();
		cName.setName("Name");
		cName
				.getExpressions()
				.add(
						"declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element/pojo:configuration/commons:Name");

		persistenceRules.getCriterion().add(cName);

		Criterion cNamespace = new Criterion();
		cNamespace.setName("Namespace");
		cNamespace
				.getExpressions()
				.add(
						"declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element[pojo:name = \"Saturnus\"]/pojo:configuration/commons:Namespace[. = \"whatever\"]");

		persistenceRules.getCriterion().add(cNamespace);

		Rule rEnvironmentName = new Rule();
		rEnvironmentName.setCriterion(cName.getName());
		rEnvironmentName
				.setClassname("com.klistret.cmdb.ci.element.logical.collection.Environment");
		rEnvironmentName.setOrder(2);

		persistenceRules.getRule().add(rEnvironmentName);

		Rule rEnvironmentNamespace = new Rule();
		rEnvironmentNamespace.setCriterion(cNamespace.getName());
		rEnvironmentNamespace
				.setClassname("com.klistret.cmdb.ci.element.logical.Collection");
		rEnvironmentNamespace.setOrder(1);

		persistenceRules.getRule().add(rEnvironmentNamespace);
	}

	@Autowired
	protected ElementService elementService;

	@Autowired
	protected CIContextHelper ciContextHelper;

	@Test
	@Rollback(value = false)
	public void identify() {
		com.klistret.cmdb.aspects.persistence.ElementIdentification aop = new com.klistret.cmdb.aspects.persistence.ElementIdentification();
		com.klistret.cmdb.aspects.persistence.Identification identification = new com.klistret.cmdb.aspects.persistence.Identification();

		identification.setCiContextHelper(ciContextHelper);
		identification.setPersistenceRules(persistenceRules);

		aop.setElementService(elementService);
		aop.setIdentification(identification);

		aop.identify(element);
	}
}
