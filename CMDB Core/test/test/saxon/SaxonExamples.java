package test.saxon;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import javax.jcr.query.InvalidQueryException;

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
import com.klistret.cmdb.utility.saxon.PathExpression;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	// @Test
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
							"/google:a['hello' = 'hello' and @google:face = 'yes']/google:b/google:c",
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

	@Test
	public void dummy() {
		PathExpression path = new PathExpression(
				"declare namespace google='http://www.google.com'; /google:a[empty(@type)]/google:b/google:c");

		for (Expr expr : path.getRelativePath())
			System.out.println(expr.toString());
	}
}
