package test.com.klistret.cmdb.utility.xmlbeans;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class PropertyExpression {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void construction1() {
		com.klistret.cmdb.utility.xmlbeans.PropertyExpression pe = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				"com.klistret.cmdb.xmlbeans.element.logical.collection.Environment",
				"Name");

		assertNotNull(pe);
	}

	@Test
	public void construction2() {
		com.klistret.cmdb.utility.xmlbeans.PropertyExpression pe = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				"com.klistret.cmdb.xmlbeans.element.logical.collection.Envir",
				"Name");

		assertNotNull(pe);
	}

	@Test
	public void getXPath1() {
		com.klistret.cmdb.utility.xmlbeans.PropertyExpression pe = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				"com.klistret.cmdb.xmlbeans.element.logical.collection.Environment",
				"Name");

		assertNotNull(pe.toString());
	}

	@Test
	public void getXPath2() {
		com.klistret.cmdb.utility.xmlbeans.PropertyExpression pe = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				"com.klistret.cmdb.xmlbeans.element.logical.collection.Environment",
				"Name");

		assertNotNull(pe.toString(false));
	}

	@Test
	public void matching() {
		com.klistret.cmdb.utility.xmlbeans.PropertyExpression pe = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				"com.klistret.cmdb.xmlbeans.element.logical.collection.Environment",
				"Name");

		assertNotNull(pe.matches("hello"));
	}
}
