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
				"C:/workshop/klistret/CMDB Core/src/xsd/commons.xsd",
				"C:/workshop/klistret/CMDB Core/src/xsd/xmlschemaNamespace.xsd",
				"C:/workshop/klistret/CMDB Core/src/xsd/element",
				"C:/workshop/klistret/CMDB Core/src/xsd/relation",
				"-Xcollection-setter-injector",
				"-Xinject-code",
				"-XxmlElement",
				"-XxmlElement-bases=com.klistret.cmdb.Base,com.klistret.cmdb.pojo.Element,com.klistret.cmdb.pojo.ElementType,com.klistret.cmdb.pojo.Relation,com.klistret.cmdb.pojo.RelationType",
				"-XxmlNSMap",
				"-XxmlNSMap-targets=com.klistret.cmdb.pojo.Element,com.klistret.cmdb.pojo.Relation" };

		XJCFacade.main(args);
	}

}
