package test.com.klistret.cmdb.identification;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.xmlbeans.element.logical.collection.Environment;
import com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument;

public class PersistenceRules {

	private EnvironmentDocument document;
	private Environment target;

	@Before
	public void setUp() throws Exception {
		document = EnvironmentDocument.Factory.newInstance();

		target = document.addNewEnvironment();
		target.setName("Saturnus");
		target.setNamespace("Production");
	}

	// @Test
	public void createIdentificationDocument() throws IOException {
		XmlOptions opts = new XmlOptions();
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(4);

		// construct document
		com.klistret.cmdb.xmlbeans.PersistenceRulesDocument document = com.klistret.cmdb.xmlbeans.PersistenceRulesDocument.Factory
				.newInstance();
		com.klistret.cmdb.xmlbeans.PersistenceRules rules = document
				.addNewPersistenceRules();

		// add a property criterion
		com.klistret.cmdb.xmlbeans.PropertyCriterion cNameAndNamespace = rules
				.addNewPropertyCriterion();
		cNameAndNamespace.setName("NameAndNamespace");
		cNameAndNamespace.addPropertyLocationPath("Name");
		cNameAndNamespace.addPropertyLocationPath("Namespace");

		// add a binding
		com.klistret.cmdb.xmlbeans.Binding bNameAndNamespace = rules
				.addNewBinding();
		bNameAndNamespace
				.setType("com.klistret.cmdb.xmlbeans.element.logical.Collection");
		bNameAndNamespace.setPropertyCriterion("NameAndNamespace");
		bNameAndNamespace.setOrder(3);

		document.save(new File("C:\\temp\\persistenceRules.xml"), opts);
	}

	@Test
	public void dummy() throws XmlException, IOException {
		com.klistret.cmdb.xmlbeans.PersistenceRulesDocument document = com.klistret.cmdb.xmlbeans.PersistenceRulesDocument.Factory
				.parse(new File("C:\\temp\\persistenceRules.xml"));

		String namespaces = "declare namespace cmdb=\'http://www.klistret.com/cmdb\';";

		String xquery = "for $pc in $this/cmdb:PersistenceRules/cmdb:PropertyCriterion "
				+ "for $b in $this/cmdb:PersistenceRules/cmdb:Binding "
				+ "where $pc/@Name = $b/cmdb:PropertyCriterion "
				+ "and matches($b/cmdb:Type , \'com.klistret.cmdb.xmlbeans.element.logical.Collection|com.klistret.cmdb.xmlbeans.element.logical.collection.Environment\')"
				+ "return $pc";

		XmlObject[] results = document.execQuery(namespaces + xquery);

		// Print the results.
		if (results.length > 0) {
			System.out.println("The query results: \n");
			System.out.println(results[0].toString() + "\n");
		}
		assertNotNull(results);
	}
}
