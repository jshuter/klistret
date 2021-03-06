package test.com.klistret.cmdb.utility.saxon;

import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.element.context.Environment;

public class Experimenting {

	com.klistret.cmdb.utility.jaxb.CIContext ciContext;

	private Element element;

	@Before
	public void setUp() throws Exception {
		ciContext = com.klistret.cmdb.utility.jaxb.CIContext.getCIContext();

		ElementType elementType = new ElementType();
		elementType.setId(new Long(1));
		elementType.setName("a type");

		Environment environment = new Environment();
		environment.setName("a environment");
		environment.setWatermark("1234");

		element = new com.klistret.cmdb.ci.pojo.Element();
		element.setId(new Long(1));
		element.setName("mine");
		element.setType(elementType);
		element.setConfiguration(environment);
	}


	public void xquery() {
		String xquery = String
				.format(
						"declare namespace persistence=\'http://www.klistret.com/cmdb/ci/persistence\'; declare variable $this external; for $qnames at $qnameIndex in (%s) "
								+ "for $rule in $this/persistence:PersistenceRules/persistence:Rule[not(persistence:Exclusions = \'"
								+ "%s\')] "
								+ "for $criterion in $this/persistence:PersistenceRules/persistence:Criterion "
								+ "where $rule/persistence:QName = $qnames "
								+ "and $rule/persistence:Criterion = $criterion/@Name "
								+ "order by $qnameIndex, $rule/@Order empty greatest "
								+ "return $criterion",
						"com.klistret.cmdb.ci.logical.collection.Environment",
						"");

		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();

		try {
			XQueryExecutable xqexec = xqc.compile(xquery);
			xqexec.getClass();
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
	}

	public void xpath() {
		String xpath = "/pojo:Element/pojo:configuration/@Watermark[. = \"1234\"]";

		XPathEvaluator xeval = new XPathEvaluator();

		IndependentContext staticContext = new IndependentContext(
				new Configuration());
		staticContext.declareNamespace("pojo",
				"http://www.klistret.com/cmdb/ci/pojo");
		staticContext.declareNamespace("commons",
				"http://www.klistret.com/cmdb/ci/commons");

		xeval.setStaticContext(staticContext);

		try {
			XPathExpression xpathExpression = xeval.createExpression(xpath);

			JAXBSource source = new JAXBSource(ciContext.getJAXBContext(),
					element);
			List<?> results = xpathExpression.evaluate(source);

			if (results.size() == 1) {
				ValueRepresentation valueR = (ValueRepresentation) results
						.get(0);

				System.out.println(valueR.getStringValue());
			}

		} catch (XPathException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}


	public void third() {
		Processor processor = new Processor(false);

		DocumentBuilder db = processor.newDocumentBuilder();
		XPathCompiler xcompiler = processor.newXPathCompiler();

		try {
			xcompiler.declareNamespace("pojo",
					"http://www.klistret.com/cmdb/ci/pojo");
			XPathExecutable xexecutable = xcompiler
					.compile("/pojo:Element/pojo:type[name=\"a type\"]");
			XPathSelector xselector = xexecutable.load();

			JAXBSource source = new JAXBSource(ciContext.getJAXBContext(),
					element);

			xselector.setContextItem(db.build(source));
			XdmValue results = xselector.evaluate();

			results.size();
		} catch (SaxonApiException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}


	public void unmarshaller() {
		StringWriter stringWriter = new StringWriter();

		try {
			Marshaller m = ciContext.getJAXBContext().createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(element, stringWriter);

			System.out.println(String.format("Element [%s]", stringWriter));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void dummy() {
		//Pattern p = Pattern.compile("(\'|\")(((?!\\1).)*)\\1");
		Pattern p = Pattern.compile("([\"\'])(?:\\\\\\1|.)*?(\\1)");

		Matcher m = p.matcher("\"hello\"");
		if (m.matches())
			System.out.println("corret");
	}
}
