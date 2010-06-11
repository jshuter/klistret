package test.com.klistret.cmdb.utility.jaxb;

import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Before;
import org.junit.Test;

public class JAXBContextHelper {

	String[] baseTypes = { "com.klistret.cmdb.Base",
			"com.klistret.cmdb.pojo.Element" };
	String[] assignablePackages = { "com/klistret/cmdb" };

	com.klistret.cmdb.utility.jaxb.JAXBContextHelper helper;

	com.klistret.cmdb.pojo.Element element;

	com.klistret.cmdb.pojo.ElementType elementType;

	com.klistret.cmdb.element.logical.collection.Environment environment;

	@Before
	public void setUp() throws Exception {
		helper = new com.klistret.cmdb.utility.jaxb.JAXBContextHelper(
				baseTypes, assignablePackages);

		elementType = new com.klistret.cmdb.pojo.ElementType();
		elementType.setId(new Long(1));
		elementType.setName("a type");

		environment = new com.klistret.cmdb.element.logical.collection.Environment();
		environment.setName("a environment");
		environment.setWatermark("1234");

		element = new com.klistret.cmdb.pojo.Element();
		element.setId(new Long(1));
		element.setName("a element");
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

			System.out.println(String.format("Element [%s]",stringWriter));
			
			m.marshal(environment, stringWriter);
			System.out.println(String.format("Environment [%s]", stringWriter));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
