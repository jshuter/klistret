package test.com.klistret.cmdb.utility;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.spring.ApplicationContextHelper;

public class Identification {

	private com.klistret.cmdb.utility.Identification identification;

	@Before
	public void setUp() throws Exception {
		identification = (com.klistret.cmdb.utility.Identification) ApplicationContextHelper
				.getInstance("Spring.cfg.xml").getContext().getBean(
						"identification");
	}

	@Test
	public void dummy() {

	}
}
