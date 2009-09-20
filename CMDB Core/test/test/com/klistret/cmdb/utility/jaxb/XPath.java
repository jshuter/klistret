package test.com.klistret.cmdb.utility.jaxb;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.om.Axis;

public class XPath {

	private IndependentContext staticContext;

	@Before
	public void setUp() throws Exception {
		Configuration config = Configuration.newConfiguration();
		staticContext = new IndependentContext(config);
	}

	@Test
	public void execute() throws Throwable {
		String expression = "/pojo:Element/@fromTimeStamp";

		staticContext.declareNamespace("cmdb", "http://www.klistret.com/cmdb");
		staticContext.declareNamespace("pojo",
				"http://www.klistret.com/cmdb/pojo");

		Expression exp = ExpressionTool.make(expression, staticContext, 0, -1,
				1, false);
		Assert.assertNotNull(exp);

		Iterator<Expression> subExpressions = exp.iterateSubExpressions();
		while (subExpressions.hasNext()) {
			Expression child = subExpressions.next();

			if (child instanceof SlashExpression) {
				child = ((SlashExpression) child).getLastStep();
			}

			if (child instanceof AxisExpression) {
				System.out.println(Axis.axisJavaName[((AxisExpression)child).getAxis()]);
			}
		}
	}
}
