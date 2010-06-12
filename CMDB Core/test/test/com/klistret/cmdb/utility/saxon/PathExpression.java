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
import com.klistret.cmdb.utility.saxon.StepExpr;
import com.klistret.cmdb.utility.saxon.Step;

public class PathExpression {
	static final String validXPathWithoutDeclares = "/a:google/a:without/b:microsoft";

	static final String validXPathWithoutDeclaresOrRoot = "a:google/a:without/b:microsoft";

	static final String validXPathStepsOnly = "a:google[@id = 'hello']/a:without[exists(@namespace)]/b:microsoft[matches(@name,'yes')]";

	static final String invalidXPathStepsOnly = "a:google[@id = 'hello']/a:without[exists(@namespace)]/b:microsoft[contains(@name,'yes')]";

	com.klistret.cmdb.utility.saxon.PathExpression pathExpression;

	@Before
	public void setUp() throws Exception {

	}

	// @Test
	/**
	 * Valid namespace declarations
	 */
	public void validNamespace() {
		String xpath = String
				.format(
						"declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNotNull(pathExpression);
	}

	// @Test
	/**
	 * First namespace declaration is missing a "e" in the namespace token
	 */
	public void invalidNamespace1() {
		String xpath = String
				.format(
						"declare namespac a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			// expected
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNull(pathExpression);
	}

	// @Test
	/**
	 * Missing namespace declaration for prefix a
	 */
	public void invalidNamespace2() {
		String xpath = String.format(
				"declare namespace b=\"http://www.google.com/b\"; %s",
				validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			// expected
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNull(pathExpression);
	}

	// @Test
	/**
	 * Missing semicolon between prefix a and prefix b
	 */
	public void syntaxError() {
		String xpath = String
				.format(
						"declare namespace a=\"http://www.google.com/a\" declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			// expected
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertNull(pathExpression);
	}

	// @Test
	/**
	 * Validate that a root exists
	 */
	public void typeValidation1() {
		String xpath = String
				.format(
						"declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclares);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertTrue(pathExpression.hasRoot());
	}

	// @Test
	/**
	 * Validate that a root does not exist
	 */
	public void typeValidation2() {
		String xpath = String
				.format(
						"declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathWithoutDeclaresOrRoot);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertFalse(pathExpression.hasRoot());
	}

	@Test
	/**
	 * Validate that only step expressions are present
	 */
	public void typeValidation3() {
		String xpath = String
				.format(
						"declare mapping a:configuration=b:Environment; declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						validXPathStepsOnly);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
			System.out.println(String.format("%s/%s/%s", "yes", "no", null));
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		for (Expr expr : pathExpression.getRelativePath()) {
			System.out.println(expr);
			assertEquals(StepExpr.class, expr.getClass());
		}
	}

	// @Test
	/**
	 * Validate that the relative path does not have homogeneous step
	 * expressions
	 */
	public void typeValidation4() {
		String xpath = String
				.format(
						"declare namespace a=\"http://www.google.com/a\"; declare namespace b=\"http://www.google.com/b\"; %s",
						invalidXPathStepsOnly);

		try {
			pathExpression = new com.klistret.cmdb.utility.saxon.PathExpression(
					xpath);
		} catch (ApplicationException e) {
			fail(String.format("Application expression caught [%s]", e));
		} catch (InfrastructureException e) {
			fail(String.format("Intfrastructure expression caught [%s]", e));
		}

		assertEquals(IrresoluteExpr.class, pathExpression.getExpr(2).getClass());
	}
}
