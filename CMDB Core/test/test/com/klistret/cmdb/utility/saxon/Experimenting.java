package test.com.klistret.cmdb.utility.saxon;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.ci.element.logical.collection.Environment;

public class Experimenting {
	String[] baseTypes = { "com.klistret.cmdb.ci.commons.Base",
			"com.klistret.cmdb.ci.pojo.Element" };
	String[] assignablePackages = { "com/klistret/cmdb/ci" };

	com.klistret.cmdb.utility.jaxb.CIContextHelper helper;

	private Element element;

	@Before
	public void setUp() throws Exception {
		helper = new com.klistret.cmdb.utility.jaxb.CIContextHelper(baseTypes,
				assignablePackages);

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

	// @Test
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
		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void xpath() {
		String xpath = "/pojo:Element[@name='mine']";

		XPathEvaluator xeval = new XPathEvaluator();

		IndependentContext staticContext = new IndependentContext(
				new Configuration());
		staticContext.declareNamespace("pojo",
				"http://www.klistret.com/cmdb/ci/pojo");

		xeval.setStaticContext(staticContext);

		try {
			XPathExpression xpathExpression = xeval.createExpression(xpath);

			Processor processor = new Processor(false);
			DocumentBuilder db = processor.newDocumentBuilder();

			Source source = db.build(new File("C:\\temp\\test.xml")).asSource();
			List<?> results = xpathExpression.evaluate(source);

			if (results.size() == 1) {
				NodeInfo nodeInfo = (NodeInfo) results.get(0);

				nodeInfo.toString();
			}

		} catch (XPathException e) {
			e.printStackTrace();
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
	}
}
