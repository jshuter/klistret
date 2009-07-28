package test.com.klistret.cmdb.identification;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.xmlbeans.SchemaTypeHelper;
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
		String classname = "com.klistret.cmdb.xmlbeans.element.logical.collection.Environment";

		// return ordered list of base types ascending
		SchemaType[] baseSchemaTypes = SchemaTypeHelper
				.getBaseSchemaTypes(classname);

		// construct schema type list for query
		String schemaTypesList = String.format("\'%s\'", classname);
		for (SchemaType schemaType : baseSchemaTypes)
			schemaTypesList = schemaTypesList.concat(String.format(",\'%s\'",
					schemaType.getFullJavaName()));

		String namespaces = "declare namespace cmdb=\'http://www.klistret.com/cmdb\';";

		/**
		 * Positional variables only allowed for "for" clause and the order
		 * should be ascending to the base class (type). Necessary to order the
		 * returned property criterion by class then the order attribute.
		 */
		String xquery = "for $types at $typesIndex in ("
				+ schemaTypesList
				+ ") "
				+ "for $binding in $this/cmdb:PersistenceRules/cmdb:Binding[not(cmdb:ExclusionType = \'"
				+ classname
				+ "\')] "
				+ "for $criterion in $this/cmdb:PersistenceRules/cmdb:PropertyCriterion "
				+ "where $binding/cmdb:Type = $types "
				+ "and $binding/cmdb:PropertyCriterion = $criterion/@Name "
				+ "order by $typesIndex, $binding/@Order empty greatest "
				+ "return $criterion";

		// Ordered array of PropertyCriterion XmlBeans
		com.klistret.cmdb.xmlbeans.PersistenceRulesDocument document = com.klistret.cmdb.xmlbeans.PersistenceRulesDocument.Factory
				.parse(new File("C:\\temp\\persistenceRules.xml"));
		XmlObject[] propertyCriteria = document.execQuery(namespaces + xquery);

		for (XmlObject criterion : propertyCriteria) {
			System.out.println(criterion.toString());
		}
	}
}
