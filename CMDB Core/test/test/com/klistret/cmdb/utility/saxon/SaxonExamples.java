package test.com.klistret.cmdb.utility.saxon;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.jaxb.ContextHelper;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void another() {
		String[] baseTypes = { "com.klistret.cmdb.Base" };
		String[] assignablePackages = { "com/klistret/cmdb" };

		ContextHelper helper = new ContextHelper(baseTypes, assignablePackages);
		System.out.println(helper.getClass(new QName(
				"http://www.klistret.com/cmdb", "Element")));
	}

	// @Test
	public void dummy() {
		PathExpression path = new PathExpression(
				"declare namespace google='http://www.google.com'; /google:a[matches(@type,\"what/ever\")]/google:b[matches(@type,'ano\thor')]/google:c");

		for (String step : path.getSteps())
			System.out.println(step);

		for (Expr expr : path.getRelativePath())
			System.out.println(expr.toString());
	}
}
