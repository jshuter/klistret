package test.com.klistret.cmdb.utility.xmlbeans;

import static org.junit.Assert.assertNotNull;

import org.apache.xmlbeans.SchemaType;
import org.junit.Before;
import org.junit.Test;

public class SchemaTypeHelper {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void getBaseSchemaTypes1() {
		SchemaType[] results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getBaseSchemaTypes("com.klistret.cmdb.xmlbeans.Element");

		assertNotNull(results);
	}

	@Test
	public void getBaseSchemaTypes2() {
		SchemaType[] results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getBaseSchemaTypes("com.klistret.cmdb.xmlbeans.Elemen");

		assertNotNull(results);
	}

	@Test
	public void getBaseSchemaTypes3() {
		SchemaType[] results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getBaseSchemaTypes("com.klistret.cmdb.xmlbeans.Name");

		assertNotNull(results);
	}

	@Test
	public void getBaseSchemaTypes4() {
		SchemaType[] results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getBaseSchemaTypes("com.klistret.cmdb.xmlbeans.ElementDocument");

		assertNotNull(results);
	}

	@Test
	public void getExtendSchemaTypes() {
		SchemaType[] results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getExtendSchemaTypes("com.klistret.cmdb.xmlbeans.Element");

		assertNotNull(results);
	}

	@Test
	public void getRootDocumentType() {
		SchemaType results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getRootDocumentType("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");

		assertNotNull(results);
	}

	@Test
	public void getDocument() {
		SchemaType results = com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper
				.getDocument("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");

		assertNotNull(results);
	}
}
