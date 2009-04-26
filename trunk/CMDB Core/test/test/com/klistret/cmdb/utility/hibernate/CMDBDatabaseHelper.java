package test.com.klistret.cmdb.utility.hibernate;

import org.junit.Before;
import org.junit.Test;

public class CMDBDatabaseHelper {

	private com.klistret.cmdb.utility.hibernate.CMDBDatabaseHelper helper;

	@Before
	public void setUp() throws Exception {
		helper = com.klistret.cmdb.utility.hibernate.CMDBDatabaseHelper
				.getInstance("Hibernate.cfg.xml");
	}

	@Test
	public void generateDatabaseSchema() {
		helper.generateDatabaseSchema("/tmp/cmdb.ddl", true, false);
	}
}
