package test.saxon;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.jcr.query.InvalidQueryException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;

import org.apache.jackrabbit.core.query.lucene.FileBasedNamespaceMappings;
import org.apache.jackrabbit.core.query.lucene.NamePathResolverImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.query.DefaultQueryNodeFactory;
import org.apache.jackrabbit.spi.commons.query.LocationStepQueryNode;
import org.apache.jackrabbit.spi.commons.query.QueryNode;
import org.apache.jackrabbit.spi.commons.query.QueryNodeFactory;
import org.apache.jackrabbit.spi.commons.query.QueryRootNode;
import org.apache.jackrabbit.spi.commons.query.xpath.QueryBuilder;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.ExprBuilder;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathVariable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.xqj.SaxonXQDataSource;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void evenDummer() {
		Configuration config = new Configuration();
		IndependentContext context = new IndependentContext(config);
		context.declareNamespace("google", "http://www.google.com");
		context
				.setDefaultFunctionNamespace("http://www.w3.org/2005/xpath-functions");
		XPathVariable evariable = context.declareVariable("", "e");
		evariable.setRequiredType(SequenceType.SINGLE_ELEMENT_NODE);

		String xpath = "//google:d[. = @face]/(google:b|google:f)/@c";
		try {
			Expression expr = ExpressionTool.make(xpath, context, 0, -1, 1,
					true);

			List<Expr> relativePath = ExprBuilder.makeRelativePath(context,
					expr);

			for (Expr step : relativePath) {
				System.out.println(String.format("step [%s]", step));
			}
		} catch (XPathException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void xqj() {
		String expr = "declare namespace google='http://www.google.com'; /(google:a | google:d)[@name='hello' and @type='whatever' and @big='small']/google:b/google:c";

		SaxonXQDataSource xqds = new SaxonXQDataSource();
		try {
			XQConnection xqc = xqds.getConnection();

			XQPreparedExpression e = xqc.prepareExpression(expr);
			XQStaticContext xqsc = e.getStaticContext();

			String[] np = xqsc.getNamespacePrefixes();
			for (String prefix : np) {
				System.out.println(prefix);
			}
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//@Test
	public void somebody() {
		NameFactory namefactory = NameFactoryImpl.getInstance();

		Collection<Name> namespaces = new HashSet<Name>();
		namespaces.add(namefactory.create("http://www.google.com", "google"));

		QueryNodeFactory nodefactory = new DefaultQueryNodeFactory(namespaces);

		QueryBuilder builder = new QueryBuilder();
		try {
			NamePathResolver nameresolver = NamePathResolverImpl
					.create(new FileBasedNamespaceMappings(new File(
							"C:\\temp\\namespaces.txt")));

			QueryRootNode rootnode = builder
					.createQueryTree(
							"/google:a[@google:name = 'hello' and @google:face = 'yes']/google:b/google:c",
							nameresolver, nodefactory);
			LocationStepQueryNode[] steps = rootnode.getLocationNode()
					.getPathSteps();

			for (LocationStepQueryNode step : steps) {
				System.out.println(String.format(
						"step class: %s, name: %s, include descendents: %b",
						step.getClass().getName(), step.getNameTest()
								.toString(), step.getIncludeDescendants()));
				QueryNode[] predicates = step.getPredicates();
				for (QueryNode predicate : predicates) {
					System.out
							.println(String.format(
									"predicate class: %s, type: %d ", predicate
											.getClass().getName(), predicate
											.getType()));
				}

				QueryNode[] operands = step.getOperands();
				for (QueryNode operand : operands) {
					System.out.println(String.format(
							"operand class: %s, type: %d ", operand.getClass()
									.getName(), operand.getType()));
				}
			}

		} catch (InvalidQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
