/**
 ** This file is part of Klistret. Klistret is free software: you can
 ** redistribute it and/or modify it under the terms of the GNU General
 ** Public License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.

 ** Klistret is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 ** General Public License for more details. You should have received a
 ** copy of the GNU General Public License along with Klistret. If not,
 ** see <http://www.gnu.org/licenses/>
 */

package test.com.klistret.cmdb.utility.saxon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.IrresoluteExpr;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

public class PathExpression {
	static final String validXPathWithoutDeclares = "/a:google/a:without/b:microsoft";

	static final String validXPathWithoutDeclaresOrRoot = "/a:google/a:without[/b:this/b:mine = 2 and empty(b:here)]/@microsoft";

	static final String validXPathStepsOnly = "a:google[@id = (2,'hello',4)]/a:without[exists(@namespace)]/b:microsoft[matches(@name,'yes')]";

	static final String invalidXPathStepsOnly = "a:google[@id = 'hello']/a:without[exists(@namespace)]/b:microsoft[contains(@name,'yes')]";

	static final String complexXPath = "/a:google[@id eq 'hello' and a:without/b:mine eq 'yes']";

	com.klistret.cmdb.utility.saxon.PathExpression pathExpression;

	@Before
	public void setUp() throws Exception {

	}

	/**
	 * Valid namespace declarations
	 */
	public void validNamespace() {
		String xpath = String
				.format("declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			assertNotNull(String.format(
					"Application exception expected [xpath: %s]", xpath), e);
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNotNull(String.format(
				"Expression should not be valid [xpath: %s]", xpath),
				pathExpression);
	}

	/**
	 * First namespace declaration is missing a "e" in the namespace token
	 */
	public void invalidNamespace1() {
		String xpath = String
				.format("declare namespac a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			assertNotNull(String.format(
					"Application exception expected [xpath: %s]", xpath), e);
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure exception caught [%s]", e));
		}

		assertNull(String.format("Expression should not be valid [xpath: %s]",
				xpath), pathExpression);
	}

	/**
	 * Missing namespace declaration for prefix "a"
	 */
	public void invalidNamespace2() {
		String xpath = String.format(
				"declare namespace b=\"http://www.google.com/b\"; %s",
				validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			assertNotNull(String.format(
					"Application exception expected [xpath: %s]", xpath), e);
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNull(String.format("Expression should not be valid [xpath: %s]",
				xpath), pathExpression);
	}

	/**
	 * Missing semicolon between prefix "a" and prefix "b"
	 */
	public void syntaxError() {
		String xpath = String
				.format("declare namespace a=\"http://www.google.com/a\" declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			assertNotNull(String.format(
					"Application exception expected [xpath: %s]", xpath), e);
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNull(String.format("Expression should not be valid [xpath: %s]",
				xpath), pathExpression);
	}

	/**
	 * Validate that a root exists
	 */
	public void typeValidation1() {
		String xpath = String
				.format("declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertTrue(String.format("Path expression [%s] has root step", xpath),
				pathExpression.getRelativePath().hasRoot());
	}

	/**
	 * Validate that a root does not exist
	 */
	@Test
	public void typeValidation2() {
		String xpath = String
				.format("declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclaresOrRoot);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);

			for (Expr expr : pathExpression.getRelativePath().getSteps())
				System.out.println(String.format("xpath [%s], type [%s]",
						expr.getXPath(), expr.getType().name()));

			System.out.println(pathExpression.getRawXPath(1, 1));
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertFalse(String.format(
				"Path expression [xpath: %s] does not have root step", xpath),
				pathExpression.getRelativePath().hasRoot());
	}

	/**
	 * Validate that only step expressions are present and that mappings
	 * declarations are accepted.
	 */
	public void typeValidation3() {
		String xpath = String
				.format("declare mapping a:configuration=b:Environment; declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathStepsOnly);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		for (Expr expr : pathExpression.getRelativePath().getSteps()) {
			System.out.println(expr);
			assertEquals(StepExpr.class, expr.getClass());
		}
	}

	/**
	 * Validate that the relative path does not have homogeneous step
	 * expressions
	 */
	public void typeValidation4() {
		String xpath = String
				.format("declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						invalidXPathStepsOnly);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertEquals(IrresoluteExpr.class, pathExpression.getRelativePath()
				.getExpr(2).getClass());
	}

	public void xsiType() {
		String xpath = String
				.format("declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\";   %s",
						"/a:google/a:whatever[@Name=\"yes\"][@Type=\"no\"]/b:microsoft[.=\"block\"]");

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);

			for (Expr expr : pathExpression.getRelativePath().getSteps()) {
				System.out.println(expr);
			}

			System.out.println(pathExpression.getRelativePath().getExpr(3));

			System.out.println(pathExpression.getRelativePath().getXPath(3));
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}
	}

	public void getValues() {
		String xpath = String
				.format("declare mapping a:configuration=b:Environment; declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathStepsOnly);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);

			Step firstStep = (Step) pathExpression.getRelativePath()
					.getFirstExpr();
			String descendingRawXPath = pathExpression.getRawXPath(firstStep
					.getDepth() + 1, pathExpression.getRelativePath()
					.getDepth() - 1);

			System.out.println(descendingRawXPath);

			String[] values = pathExpression.getRelativePath().getValues(0);
			for (String value : values)
				System.out.println(value);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}
	}

	public void complexExpression() {
		String xpath = String
				.format("declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						complexXPath);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);

			for (Expr expr : pathExpression.getRelativePath().getSteps()) {
				System.out.println(expr);
			}
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}
	}

	public void split() {
		String slashDelimiter = "/(?=([^']*'[^']*')*(?![^']*'))(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))(?=([^\\[]*\\[.*\\]))";

		String[] steps = "/a:google/b:big[c:men/c:another[2=2]]/d:hello/d:microsoft"
				.split(slashDelimiter);
		for (String step : steps)
			System.out.println(step);
	}

	public void matching() {
		String expression = "/a:google//b:big[c:men/c:another[2='2']][c:another]/d:microsoft";

		String[] steps = com.klistret.cmdb.utility.saxon.PathExpression
				.split(expression);
		for (String step : steps)
			System.out.println(step);
	}
}
