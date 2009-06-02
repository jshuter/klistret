package test.com.klistret.cmdb.identification;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.xmlbeans.element.logical.collection.Environment;
import com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument;

public class Resolver {

	private EnvironmentDocument document;
	private Environment target;

	@Before
	public void setUp() throws Exception {
		document = EnvironmentDocument.Factory.newInstance();

		target = document.addNewEnvironment();
		target.setName("Saturnus");
		target.setNamespace("Production");
		// target.setWatermark("FK");
	}

	//@Test
	public void useIdentificationBuilder() throws XmlException, IOException {
		com.klistret.cmdb.xmlbeans.IdentificationDocument document = com.klistret.cmdb.xmlbeans.IdentificationDocument.Factory
				.parse(new File("/tmp/identifcation.xml"));

		com.klistret.cmdb.identification.Resolver resolver = new com.klistret.cmdb.identification.Resolver(
				document);

		com.klistret.cmdb.identification.Builder builder = resolver
				.getIdentificationBuilder(target);

		PropertyExpression[] results = builder.getPrimaryIdentification(target);

		for (PropertyExpression expression : results) {
			System.out.println(expression.getXPath());
		}
	}

	@Test
	public void createIdentificationDocument() throws IOException {
		XmlOptions opts = new XmlOptions();
		opts.setSavePrettyPrint();
		opts.setSavePrettyPrintIndent(4);

		com.klistret.cmdb.xmlbeans.IdentificationDocument document = com.klistret.cmdb.xmlbeans.IdentificationDocument.Factory
				.newInstance();

		com.klistret.cmdb.xmlbeans.Identification identification = document
				.addNewIdentification();

		/**
		 * com.klistret.cmdb.xmlbeans.Binding bNameAndNamespace = identification
		 * .addNewBinding(); bNameAndNamespace .setClass1(
		 * "com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");
		 * bNameAndNamespace.setCriteria("NameAndNamespace");
		 * bNameAndNamespace.setPriority(10);
		 */

		com.klistret.cmdb.xmlbeans.Criteria cNameAndNamespace = identification
				.addNewCriteria();
		cNameAndNamespace.setName("NameAndNamespace");
		com.klistret.cmdb.xmlbeans.PropertyComposite pc = cNameAndNamespace
				.addNewPropertyComposite();
		pc.addPath("Name");
		pc.addPath("Namespace");

		/**
		 * com.klistret.cmdb.xmlbeans.Binding bName = identification
		 * .addNewBinding(); bName .setClass1(
		 * "com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");
		 * bName.setCriteria("Name");
		 */

		com.klistret.cmdb.xmlbeans.Criteria cName = identification
				.addNewCriteria();
		cName.setName("Name");
		com.klistret.cmdb.xmlbeans.PropertyComposite pcName = cName
				.addNewPropertyComposite();
		pcName.addPath("Name");

		com.klistret.cmdb.xmlbeans.Binding bName2 = identification
				.addNewBinding();
		bName2
				.setClass1("com.klistret.cmdb.xmlbeans.element.logical.Collection");
		bName2.setCriteria("Name");
		bName2.setPriority(7);

		com.klistret.cmdb.xmlbeans.Binding bName3 = identification
				.addNewBinding();
		bName3
				.setClass1("com.klistret.cmdb.xmlbeans.element.logical.Collection");
		bName3.setCriteria("NameAndNamespace");
		bName3.setPriority(0);
		bName3
				.addExclusion("com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");

		com.klistret.cmdb.xmlbeans.Criteria cWatermark = identification
				.addNewCriteria();
		cWatermark.setName("Watermark");
		com.klistret.cmdb.xmlbeans.PropertyComposite pcWatermark = cWatermark
				.addNewPropertyComposite();
		pcWatermark.addPath("Watermark");

		/**
		 * com.klistret.cmdb.xmlbeans.Binding bWatermark = identification
		 * .addNewBinding(); bWatermark .setClass1(
		 * "com.klistret.cmdb.xmlbeans.element.logical.collection.Environment");
		 * bWatermark.setCriteria("Watermark"); bWatermark.setPriority(0);
		 */

		document.save(new File("/tmp/identifcation.xml"), opts);
	}
}
