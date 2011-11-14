package test.com.klistret.cmdb.utility.jaxb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.StringList;
import com.sun.org.apache.xerces.internal.xs.XSLoader;
import com.sun.org.apache.xerces.internal.xs.XSModel;

import org.reflections.util.Utils;

public class Experimenting {

	@Before
	public void setUp() throws Exception {

	}


	// @Test
	public void findSchemas() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.getUrlsForCurrentClasspath())
				.setScanners(new ResourcesScanner()));

		Set<String> entries = reflections.getResources(Pattern
				.compile(".*\\.xsd"));

		if (entries != null) {
			System.out.println(String
					.format("Found %d entries", entries.size()));
			for (String entry : entries)
				System.out.println(entry);
		}
	}

	/**
	 * See
	 * http://stackoverflow.com/questions/2603778/how-can-i-unmarshall-in-jaxb-and-enjoy-the-schema-validation-without-using-an-exp
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 * @throws SAXException
	 */
	// @Test
	public void validation() throws JAXBException, IOException, SAXException {
		Sample sample = new Sample();
		sample.setLimit(10);
		sample.setStart(0);

		Choice choice = new Choice();
		choice.setAddress("gatan");
		choice.setFloater(new Float(10.0));
		sample.setChoice(choice);

		JAXBContext context = JAXBContext.newInstance(Sample.class);
		Marshaller m = context.createMarshaller();

		StringWriter sw = new StringWriter();

		final PipedInputStream in = new PipedInputStream();

		context.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri,
					String suggestedFileName) throws IOException {
				StreamResult streamResult = new StreamResult(
						new PipedOutputStream(in));
				streamResult.setSystemId("");
				return streamResult;
			}
		});
		Schema schema = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
				new StreamSource(in, ""));

		m.setSchema(schema);
		m.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				System.out.println(event);
				return false;
			}
		});

		m.marshal(sample, sw);
		sw.close();

		System.out.println(sw.toString());
	}

	// @Test
	public void validation2() throws SAXException, JAXBException, IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		com.klistret.cmdb.ci.element.context.Environment env = new com.klistret.cmdb.ci.element.context.Environment();
		env.setName("hellow");
		env.setNamespace("production");
		com.klistret.cmdb.ci.commons.Ownership ownership = new com.klistret.cmdb.ci.commons.Ownership();
		ownership.setName("production");
		env.setOwnership(ownership);

		com.klistret.cmdb.ci.pojo.ElementType elmType = new com.klistret.cmdb.ci.pojo.ElementType();
		elmType.setId(new Long(1));
		elmType.setName("ele type");
		elmType.setFromTimeStamp(new java.util.Date());
		elmType.setCreateTimeStamp(new java.util.Date());
		elmType.setUpdateTimeStamp(new java.util.Date());

		com.klistret.cmdb.ci.pojo.Element elm = new com.klistret.cmdb.ci.pojo.Element();
		elm.setId(new Long(1));
		elm.setType(elmType);
		elm.setName("eleme");
		elm.setFromTimeStamp(new java.util.Date());
		elm.setCreateTimeStamp(new java.util.Date());
		elm.setUpdateTimeStamp(new java.util.Date());
		elm.setConfiguration(env);

		com.klistret.cmdb.ci.pojo.RelationType relType = new com.klistret.cmdb.ci.pojo.RelationType();
		relType.setId(new Long(1));
		relType.setName("rel type");
		relType.setFromTimeStamp(new java.util.Date());
		relType.setCreateTimeStamp(new java.util.Date());
		relType.setUpdateTimeStamp(new java.util.Date());

		com.klistret.cmdb.ci.pojo.Relation rel = new com.klistret.cmdb.ci.pojo.Relation();
		rel.setId(new Long(1));
		rel.setType(relType);
		rel.setFromTimeStamp(new java.util.Date());
		rel.setCreateTimeStamp(new java.util.Date());
		rel.setUpdateTimeStamp(new java.util.Date());

		JAXBContext context = JAXBContext
				.newInstance(com.klistret.cmdb.ci.element.context.Environment.class);
		Marshaller m = context.createMarshaller();

		final FileInputStream commons = new FileInputStream(
				"C:\\workshop\\klistret\\CMDB CI\\src\\xsd\\commons.cmdb.xsd");
		FileInputStream pojo = new FileInputStream(
				"C:\\workshop\\klistret\\CMDB CI\\src\\xsd\\pojo.cmdb.xsd");
		FileInputStream collection = new FileInputStream(
				"C:\\workshop\\klistret\\CMDB CI\\src\\xsd\\element\\logical\\collection.cmdb.xsd");
		FileInputStream environment = new FileInputStream(
				"C:\\workshop\\klistret\\CMDB CI\\src\\xsd\\element\\logical\\collection\\environment.cmdb.xsd");

		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		factory.setResourceResolver(new LSResourceResolver() {

			public String getResourceName(String path) {
				int lastIndexOf = path.lastIndexOf("/");

				if (lastIndexOf == -1)
					return path;

				if (lastIndexOf == path.length())
					return null;

				return path.substring(lastIndexOf + 1, path.length());
			}

			public LSInput resolveResource(String type, String namespaceURI,
					String publicId, String systemId, String baseURI) {
				DOMImplementationRegistry registry;
				try {

					registry = DOMImplementationRegistry.newInstance();
					DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry
							.getDOMImplementation("LS 3.0");

					LSInput ret = domImplementationLS.createLSInput();

					ret.setSystemId(systemId);
					if (getResourceName(systemId).equals("commons.cmdb.xsd")) {
						ret
								.setByteStream(new FileInputStream(
										"C:\\workshop\\klistret\\CMDB CI\\src\\xsd\\commons.cmdb.xsd"));
					}
					if (getResourceName(systemId).equals("collection.cmdb.xsd")) {
						ret
								.setByteStream(new FileInputStream(
										"C:\\workshop\\klistret\\CMDB CI\\src\\xsd\\element\\logical\\collection.cmdb.xsd"));
					}

					return ret;
				} catch (ClassCastException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}

		});

		Schema schema = factory.newSchema(new Source[] {
				new StreamSource(environment), new StreamSource(pojo),
				new StreamSource(collection), new StreamSource(commons) });

		m.setSchema(schema);
		m.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				System.out.println(event);
				return false;
			}
		});

		StringWriter sw = new StringWriter();
		m.marshal(env, sw);
		sw.close();

		System.out.println(sw.toString());
	}

	// @Test
	public void schemaModel() {
		URL commons = Utils.getContextClassLoader().getResource(
				"xsd/commons.cmdb.xsd");

		XSLoader xsLoader = new XMLSchemaLoader();
		XSModel xsModel = xsLoader.loadURI(commons.toString());

		StringList namespaces = xsModel.getNamespaces();
		if (namespaces != null) {
			for (int index = 0; index < namespaces.getLength(); index++)
				System.out.println(namespaces.item(index));
		}
	}

	@Test
	public void ciModel() {
		com.klistret.cmdb.utility.jaxb.CIContext.getCIContext();
	}

	// @Test
	public void stringTest() {
		String example = "xsd/commons.xsd";

		System.out.println(example.substring(example.lastIndexOf("/") + 1,
				example.length()));
	}

}
