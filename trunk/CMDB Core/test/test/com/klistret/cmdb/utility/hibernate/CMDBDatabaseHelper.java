package test.com.klistret.cmdb.utility.hibernate;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper;

public class CMDBDatabaseHelper {

	private com.klistret.cmdb.utility.hibernate.CMDBDatabaseHelper helper;

	@Before
	public void setUp() throws Exception {
		helper = com.klistret.cmdb.utility.hibernate.CMDBDatabaseHelper
				.getInstance("Hibernate.cfg.xml");
	}

	// @Test
	public void generateDatabaseSchema() {
		helper.generateDatabaseSchema("C:\\temp\\cmdb.ddl", true, false);
	}

	@Test
	public void dummy() {
		SchemaType environment = XmlBeans
				.getContextTypeLoader()
				.typeForClassname(
						"com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");

		XmlObject document = SchemaTypeHelper
				.getDocument("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");

		SchemaProperty property = document.schemaType().getElementProperty(
				environment.getName());
		System.out.println(property.getName());
	}
}
