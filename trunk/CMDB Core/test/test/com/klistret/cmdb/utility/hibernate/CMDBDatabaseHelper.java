package test.com.klistret.cmdb.utility.hibernate;

import org.apache.xmlbeans.SchemaType;
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

		SchemaType results = SchemaTypeHelper
				.getRoot("com.klistret.cmdb.xmlbeans.Element");

		System.out.println(results.toString());
	}
}
