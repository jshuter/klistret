package test.com.klistret.cmdb.utility.saxon;

import javax.xml.namespace.QName;

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

		QName qname = new QName("http://www.klistret.com/cmdb/pojo", "Element",
				"pojo");
		helper.getXMLBeans().get(qname);
	}

	@Test
	public void dummy() {
		String xpath = "declare namespace cmdb='http://www.klistret.com/cmdb'; declare namespace pojo='http://www.klistret.com/cmdb/pojo';/pojo:Element[matches(@id,\"43423\") and exists(@toTimeStamp)]";

		PathExpression path = new PathExpression(xpath);

		for (Expr expr : path.getRelativePath())
			System.out.println(String.format("%s, %s", expr.toString(), expr
					.getXPath()));
	}
}
