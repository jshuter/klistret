package test.com.klistret.cmdb.utility.jaxb;

import java.io.StringWriter;
import java.util.Set;

import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.jaxb.CIBean;

public class CIContext {
	com.klistret.cmdb.ci.pojo.Element element;

	com.klistret.cmdb.ci.pojo.ElementType elementType;

	com.klistret.cmdb.ci.element.context.Environment environment;

	com.klistret.cmdb.utility.jaxb.CIContext helper;

	@Before
	public void setUp() throws Exception {
		elementType = new com.klistret.cmdb.ci.pojo.ElementType();
		elementType.setId(new Long(1));
		elementType.setName("a type");

		com.klistret.cmdb.ci.commons.Ownership ownership = new com.klistret.cmdb.ci.commons.Ownership();
		ownership.setName("ITA");

		environment = new com.klistret.cmdb.ci.element.context.Environment();
		environment.setName("a environment");
		environment.setNamespace("development");
		environment.setWatermark("1234");
		environment.setOwnership(ownership);

		element = new com.klistret.cmdb.ci.pojo.Element();
		element.setId(new Long(1));
		element.setName("mine");
		element.setType(elementType);
		element.setConfiguration(environment);

		helper = com.klistret.cmdb.utility.jaxb.CIContext.getCIContext();
	}

	@Test
	public void marshallAndValidate() throws Exception {
		Marshaller m = helper.getJAXBContext().createMarshaller();

		m.setSchema(helper.getSchema());
		m.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				System.out.println(event);
				return false;
			}
		});

		StringWriter sw = new StringWriter();
		m.marshal(environment, sw);
		sw.close();

		System.out.println(sw.toString());
	}

	//@Test
	public void checkBeans() {
		Set<CIBean> beans = helper.getBeans();
		for (CIBean bean : beans) {
			System.out.println(String.format(
					"Bean [class: %s, namespace: %s, name: %s]", bean
							.getJavaClass().getName(), bean.getType()
							.getNamespaceURI(), bean.getType().getLocalPart()));
		}
	}
}
