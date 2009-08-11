package test.com.klistret.cmdb.identification;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.pojo.PropertyCriteria;
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

	@Test
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
				.setType("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");
		bNameAndNamespace.setPropertyCriterion("NameAndNamespace");
		bNameAndNamespace.setOrder(3);

		document.save(new File("C:\\temp\\persistenceRules.xml"), opts);
	}

	//@Test
	public void dummy() throws MalformedURLException {
		com.klistret.cmdb.identification.PersistenceRules rules = new com.klistret.cmdb.identification.PersistenceRules(
				new URL("file:C:\\temp\\persistenceRules.xml"));

		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = com.klistret.cmdb.xmlbeans.element.logical.collection.Environment.Factory
				.newInstance();
		environment.setName("whatever");
		environment.setNamespace("hello");

		PropertyCriteria critera = rules.getPropertyCriteria(environment);
		
	}
}
