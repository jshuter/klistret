package test.com.klistret.cmdb.utility.jaxb;


import org.junit.Before;
import org.junit.Test;


public class CIContextHelper {

	com.klistret.cmdb.utility.jaxb.CIContextHelper helper;

	com.klistret.cmdb.ci.pojo.Element element;

	com.klistret.cmdb.ci.pojo.ElementType elementType;

	com.klistret.cmdb.ci.element.logical.collection.Environment environment;

	@Before
	public void setUp() throws Exception {
		helper = new com.klistret.cmdb.utility.jaxb.CIContextHelper();

		elementType = new com.klistret.cmdb.ci.pojo.ElementType();
		elementType.setId(new Long(1));
		elementType.setName("a type");

		environment = new com.klistret.cmdb.ci.element.logical.collection.Environment();
		environment.setName("a environment");
		environment.setWatermark("1234");

		element = new com.klistret.cmdb.ci.pojo.Element();
		element.setId(new Long(1));
		element.setName("mine");
		element.setType(elementType);
		element.setConfiguration(environment);
	}
	
	@Test
	public void dummy() {
		
	}
}
