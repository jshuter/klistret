package test.com.klistret.cmdb.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Ivy {

	private com.klistret.cmdb.service.ElementService elementService;

	@Before
	public void setUp() throws Exception {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		ctx.getEnvironment().setActiveProfiles("development", "ivy");
		ctx.load("classpath:Spring.cfg.xml");
		ctx.refresh();

		elementService = ctx
				.getBean(com.klistret.cmdb.service.ElementService.class);
	}

	@Test
	public void update() {
		com.klistret.cmdb.ci.pojo.Element element = elementService
				.get(new Long(577335));

		((com.klistret.cmdb.ci.element.component.Software) element
				.getConfiguration()).setAvailability("Feb2011R");
		((com.klistret.cmdb.ci.element.component.Software) element
				.getConfiguration()).setPhase("integration");

		elementService.update(element);
	}
}
