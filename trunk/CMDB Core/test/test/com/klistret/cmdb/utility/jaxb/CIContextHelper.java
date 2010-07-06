package test.com.klistret.cmdb.utility.jaxb;

import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Before;
import org.junit.Test;

public class CIContextHelper {

	String[] baseTypes = { "com.klistret.cmdb.ci.commons.Base",
			"com.klistret.cmdb.ci.pojo.Element" };
	String[] assignablePackages = { "com/klistret/cmdb/ci" };

	com.klistret.cmdb.utility.jaxb.CIContextHelper helper;

	com.klistret.cmdb.ci.pojo.Element element;

	com.klistret.cmdb.ci.pojo.ElementType elementType;

	com.klistret.cmdb.ci.element.logical.collection.Environment environment;

	@Before
	public void setUp() throws Exception {
		helper = new com.klistret.cmdb.utility.jaxb.CIContextHelper(baseTypes,
				assignablePackages);

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
	public void unmarshaller() {
		StringWriter stringWriter = new StringWriter();

		try {
			Marshaller m = helper.getJAXBContext().createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(element, stringWriter);

			System.out.println(String.format("Element [%s]", stringWriter));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
