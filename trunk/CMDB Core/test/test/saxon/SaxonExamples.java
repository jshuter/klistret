package test.saxon;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void dummy() {
		PathExpression path = new PathExpression(
				"declare namespace google='http://www.google.com'; /google:a[matches(@type,'whatev/er*')]/google:b/google:c");

		for (String step : path.getSteps())
			System.out.println(step);

		for (Expr expr : path.getRelativePath())
			System.out.println(expr.toString());
	}
}
