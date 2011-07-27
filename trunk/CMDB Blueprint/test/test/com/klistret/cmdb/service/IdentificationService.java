package test.com.klistret.cmdb.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.klistret.cmdb.ci.element.context.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;

public class IdentificationService {

	private com.klistret.cmdb.service.IdentificationService identificationService;

	@Before
	public void setUp() throws Exception {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		ctx.getEnvironment().setActiveProfiles("development");
		ctx.load("classpath:Test.cfg.xml");
		ctx.refresh();

		identificationService = ctx
				.getBean(com.klistret.cmdb.service.IdentificationService.class);
	}

	@Test
	public void getCriterion() {
		ElementType type = new ElementType();
		type.setName("{http://www.klistret.com/cmdb/ci/element/context}Environment");

		Element element = new Element();
		element.setId(new Long(0));
		element.setType(type);
		element.setName("Dummy");
		element.setFromTimeStamp(new java.util.Date());
		element.setCreateTimeStamp(new java.util.Date());
		element.setUpdateTimeStamp(new java.util.Date());

		Environment environment = new Environment();
		environment.setName("Dummy");
		environment.setWatermark("Testing");

		element.setConfiguration(environment);

		List<String> criterion = identificationService
				.getFullCriterion(element);
		for (String criteria : criterion)
			System.out.println(criteria);
	}
}
