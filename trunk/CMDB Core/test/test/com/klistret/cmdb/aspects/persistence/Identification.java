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

package test.com.klistret.cmdb.aspects.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.ci.element.logical.collection.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.utility.jaxb.CIContextHelper;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class Identification {

	private CIContextHelper ciContextHelper;

	private Element element;

	@Before
	public void setUp() throws Exception {
		/**
		 * context helper
		 */
		String[] baseTypes = { "com.klistret.cmdb.ci.commons.Base",
				"com.klistret.cmdb.ci.pojo.Element" };
		String[] assignablePackages = { "com/klistret/cmdb/ci" };

		ciContextHelper = new CIContextHelper(baseTypes, assignablePackages);

		/**
		 * element
		 */
		ElementType elementType = new ElementType();
		elementType.setId(new Long(1));
		elementType
				.setName("com.klistret.cmdb.ci.element.logical.collection.Environment");
		elementType.setCreateTimeStamp(new Date());

		Environment environment = new Environment();
		environment.setName("Saturnus");
		environment.setNamespace("whatever");

		element = new Element();
		element.setId(new Long(1));
		element.setName("Saturnus");
		element.setType(elementType);
		element.setConfiguration(environment);
	}

	@Test
	public void execute() throws MalformedURLException {
		QName qname = new QName(
				"http://www.klistret.com/cmdb/ci/element/logical/collection",
				"Environment");

		com.klistret.cmdb.aspects.persistence.Identification identification = new com.klistret.cmdb.aspects.persistence.Identification();
		identification.setCiContextHelper(ciContextHelper);
		identification.setPersistenceRules(new URL("classpath:persistence.rules.xml"));

		List<PathExpression[]> criteria = identification
				.getCriteriaByQName(qname);

		String[] criterion = identification.getCriterionByObject(criteria,
				element);
		if (criterion != null) {
			for (String xpath : criterion)
				System.out.println(xpath);
		}
	}
}
