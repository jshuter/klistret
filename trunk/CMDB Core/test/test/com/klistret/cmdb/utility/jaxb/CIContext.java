/**
 ** This file is part of Klistret. Klistret is free software: you can
 ** redistribute it and/or modify it under the terms of the GNU General
 ** Public License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.

 ** Klistret is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 ** General Public License for more details. You should have received a
 ** copy of the GNU General Public License along with Klistret. If not,
 ** see <http://www.gnu.org/licenses/>
 */

package test.com.klistret.cmdb.utility.jaxb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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

	//@Test
	/**
	 * Marshall then validate the environment object
	 */
	public void marshallAndValidate() throws Exception {
		Marshaller m = helper.getJAXBContext().createMarshaller();

		m.setSchema(helper.getSchema());
		m.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				fail(String.format("Validation failed: %s", event));
				return false;
			}
		});

		StringWriter sw = new StringWriter();
		m.marshal(environment, sw);
		sw.close();

		System.out.println(sw.toString());
		assertNotNull(String.format(
				"Environment marshalled and validated [xml: %s]", sw), sw);
	}

	//@Test
	/**
	 * Get CI beans
	 */
	public void getBeans() {
		Set<CIBean> beans = helper.getBeans();

		for (CIBean bean : beans) {
			System.out.println(String.format(
					"Bean [class: %s, namespace: %s, name: %s]", bean
							.getJavaClass().getName(), bean.getType()
							.getNamespaceURI(), bean.getType().getLocalPart()));
		}

		assertNotNull(String.format("CI beans [%d] generated", beans.size()),
				beans);
	}

	@Test
	public void marshall() throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(new Class[] { com.klistret.cmdb.utility.jaxb.CIBean.class });
		Marshaller m = context.createMarshaller();

		Set<CIBean> beans = helper.getBeans();

		for (CIBean bean : beans) {
			StringWriter sw = new StringWriter();
			m.marshal(bean, sw);
			System.out.println(sw.toString());
		}
	}
}
