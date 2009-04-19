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

package test.com.klistret.cmdb.utility.xmlbeans;

import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.utility.xmlbeans.XPathFunctions;

//import com.klistret.cmdb.utility.xmlbeans.XPathOperators;

public class PropertyExpression {

	private SchemaType schemaType;

	@Before
	public void setUp() throws Exception {
		com.klistret.cmdb.xmlbeans.IdentificationDocument document = com.klistret.cmdb.xmlbeans.IdentificationDocument.Factory
				.newInstance();

		this.schemaType = document.addNewIdentification().schemaType();
	}

	@Test
	public void transform() {
		com.klistret.cmdb.utility.xmlbeans.PropertyExpression expression = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				schemaType, "Binding.Class1");

		System.out.println(String.format("%s %s",
				expression.getDeclareClause(), XPathFunctions.matches(
						expression, "me")));
		assertNotNull(expression);
	}

	@Test
	public void transformQName() {
		QName qname = new QName("http://www.klistret.com/cmdb",
				"Identification");

		com.klistret.cmdb.utility.xmlbeans.PropertyExpression expression = new com.klistret.cmdb.utility.xmlbeans.PropertyExpression(
				qname, "Binding.Class1");

		System.out.println(String.format("%s %s",
				expression.getDeclareClause(), XPathFunctions.matches(
						expression, "me")));
		assertNotNull(expression);
	}
}
