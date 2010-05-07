package test.com.klistret.cmdb.utility.saxon;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
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

		JAXBContextHelper helper = new JAXBContextHelper(baseTypes,
				assignablePackages);
		helper.getElementNodes();
	}

	// @Test
	public void dummy() {
		PathExpression path = new PathExpression(
				"declare namespace google='http://www.google.com'; /google:a[matches(@type,\"what/ever\")]/google:b[matches(@type,'ano\thor')]/google:c");

		for (Expr expr : path.getRelativePath())
			System.out.println(String.format("%s, %s", expr.toString(), expr
					.getXPath()));

		System.out.println(String.format("qname [%s]", path.getQName(2)));
	}
}
