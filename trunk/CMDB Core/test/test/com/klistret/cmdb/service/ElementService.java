package test.com.klistret.cmdb.service;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.spring.ApplicationContextHelper;

public class ElementService {

	private com.klistret.cmdb.service.ElementService service;

	@Before
	public void setUp() throws Exception {
		service = (com.klistret.cmdb.service.ElementService) ApplicationContextHelper
				.getInstance("Spring.cfg.xml").getContext().getBean(
						"elementService");
	}

	@Test
	public void testGetId() {
		com.klistret.cmdb.pojo.ElementType type = new com.klistret.cmdb.pojo.ElementType();
		type.setId(new Long(1));
		type.setName("qname");

		com.klistret.cmdb.pojo.Element element = new com.klistret.cmdb.pojo.Element();
		element.setId(new Long(1));
		element.setName("dummy");
		element.setType(type);

		service.set(element);
	}
}
