package test.com.klistret.cmdb.utility.saxon;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.jaxb.reflection.util.QNameMap;

import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	// @Test
	public void another() {
		String[] baseTypes = { "com.klistret.cmdb.Base",
				"com.klistret.cmdb.pojo.Element" };
		String[] assignablePackages = { "com/klistret/cmdb" };

		JAXBContextHelper helper = new JAXBContextHelper(baseTypes,
				assignablePackages);
		for (QNameMap.Entry<XMLBean> entry : helper.getXMLBeans().entrySet()) {
			System.out.println(entry.getValue().toString());
		}
	}

	@Test
	public void dummy() {
		PathExpression path = new PathExpression(
				"declare namespace google='http://www.google.com'; /google:a[matches(@type,\"what/ever\")]/google:b[matches(@type,'ano\thor')]/google:c");

		for (Expr expr : path.getRelativePath())
			System.out.println(String.format("%s, %s", expr.toString(), expr
					.getXPath()));
	}
}