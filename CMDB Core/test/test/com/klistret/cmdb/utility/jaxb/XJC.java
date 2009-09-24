package test.com.klistret.cmdb.utility.jaxb;

import org.junit.Before;
import org.junit.Test;

import com.sun.tools.xjc.XJCFacade;

public class XJC {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void execute() throws Throwable {
		String[] args = {
				"-extension",
				"-d",
				"C:/temp",
				"-b",
				"C:/workshop/klistret/CMDB Core/configuration/build/jaxb.binding.xml",
				"C:/workshop/klistret/CMDB Core/src/xsd/pojo.xsd",
				"-Xcollection-setter-injector", "-XxmlRootElement",
				"-XxmlNSMap"};

		XJCFacade.main(args);
	}
}
