package test.saxon;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeClassInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;

import com.klistret.cmdb.pojo.Element;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void another() {
		try {
			RuntimeTypeInfoSet info = JAXBModelFactory.create(Element.class);

			Map<Class, ? extends RuntimeClassInfo> beans = info.beans();

			for (Map.Entry<Class, ? extends RuntimeClassInfo> entry : beans
					.entrySet()) {
				RuntimeClassInfo classInfo = entry.getValue();

				System.out.println(String.format("bean qname [%s]", classInfo
						.getElementName()));
			}

		} catch (IllegalAnnotationsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void dummy() {
		PathExpression path = new PathExpression(
				"declare namespace google='http://www.google.com'; /google:a[matches(@type,'whatev/er*')]/google:b/google:c");

		for (String step : path.getSteps())
			System.out.println(step);

		for (Expr expr : path.getRelativePath())
			System.out.println(expr.toString());
	}
}
