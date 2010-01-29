package test.saxon;

import org.junit.Before;
import org.junit.Test;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public class SaxonExamples {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void tokenize() {
		Configuration config = new Configuration();
		IndependentContext context = new IndependentContext(config);
		ExpressionPresenter presenter = new ExpressionPresenter(config,
				System.out);

		String xpath = "/a[@name='hello' and @type='whatever']/b/c";

		try {
			Expression expression = ExpressionTool.make(xpath, context, 0, -1,
					1, true);

			expression.explain(presenter);
			presenter.close();

		} catch (XPathException e) {
			e.printStackTrace();
		}
	}
}
