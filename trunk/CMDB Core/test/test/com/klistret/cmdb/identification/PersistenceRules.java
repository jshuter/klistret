package test.com.klistret.cmdb.identification;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper;
import com.klistret.cmdb.xmlbeans.PropertyCriterion;
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
				.setType("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");
		bNameAndNamespace.setPropertyCriterion("NameAndNamespace");
		bNameAndNamespace.setOrder(3);

		document.save(new File("C:\\temp\\persistenceRules.xml"), opts);
	}

	@Test
	public void dummy() throws XmlException, IOException {
		com.klistret.cmdb.identification.PersistenceRules persistenceRules = new com.klistret.cmdb.identification.PersistenceRules(
				new URL("file:C:\\temp\\persistenceRules.xml"));

		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = com.klistret.cmdb.xmlbeans.element.logical.collection.Environment.Factory
				.newInstance();
		environment.setName("whaever");
		environment.setNamespace("hello");

		String[] xpathCriterion = persistenceRules
				.getXPathCriterion(environment);

		for (String xpath : xpathCriterion) {
			System.out.println(xpath);
		}
	}
}
