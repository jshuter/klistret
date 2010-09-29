package test.com.klistret.cmdb.utility.jaxb;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

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

		com.klistret.cmdb.ci.commons.Ownership ownership = new com.klistret.cmdb.ci.commons.Ownership();
		ownership.setName("ITA");
		
		environment = new com.klistret.cmdb.ci.element.logical.collection.Environment();
		environment.setName("a environment");
		environment.setNamespace("development");
		environment.setWatermark("1234");
		environment.setOwnership(ownership);

		element = new com.klistret.cmdb.ci.pojo.Element();
		element.setId(new Long(1));
		element.setName("mine");
		element.setType(elementType);
		element.setConfiguration(environment);
	}

	@Test
	public void marshallAndValidate() throws JAXBException, IOException {
		Marshaller m = helper.getJAXBContext().createMarshaller();

		m.setSchema(helper.getSchemaGrammers());
		m.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				System.out.println(event);
				return false;
			}
		});

		StringWriter sw = new StringWriter();
		m.marshal(element, sw);
		sw.close();

		System.out.println(sw.toString());
	}
}
