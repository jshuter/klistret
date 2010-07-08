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

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.aspects.persistence.Criterion;
import com.klistret.cmdb.aspects.persistence.PersistenceRules;
import com.klistret.cmdb.aspects.persistence.Rule;
import com.klistret.cmdb.ci.element.logical.collection.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.utility.jaxb.CIContextHelper;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class Identification {

	private PersistenceRules persistenceRules;

	private CIContextHelper ciContextHelper;

	private Element element;

	@Before
	public void setUp() throws Exception {
		/**
		 * persistence rules
		 */
		persistenceRules = new PersistenceRules();

		Criterion cName = new Criterion();
		cName.setName("Name");
		cName
				.getExpressions()
				.add(
						"declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element/pojo:configuration/commons:Name");

		persistenceRules.getCriterion().add(cName);

		Criterion cNamespace = new Criterion();
		cNamespace.setName("Namespace");
		cNamespace
				.getExpressions()
				.add(
						"declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element[pojo:name = \"Saturnus\"]/pojo:configuration/commons:Namespace[. = \"whatever\"]");

		persistenceRules.getCriterion().add(cNamespace);

		Rule rEnvironmentName = new Rule();
		rEnvironmentName.setCriterion(cName.getName());
		rEnvironmentName
				.setClassname("com.klistret.cmdb.ci.element.logical.collection.Environment");
		rEnvironmentName.setOrder(2);

		persistenceRules.getRule().add(rEnvironmentName);

		Rule rEnvironmentNamespace = new Rule();
		rEnvironmentNamespace.setCriterion(cNamespace.getName());
		rEnvironmentNamespace
				.setClassname("com.klistret.cmdb.ci.element.logical.Collection");
		rEnvironmentNamespace.setOrder(1);

		persistenceRules.getRule().add(rEnvironmentNamespace);

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

	// @Test
	public void execute() {
		QName qname = new QName(
				"http://www.klistret.com/cmdb/ci/element/logical/collection",
				"Environment");

		com.klistret.cmdb.aspects.persistence.Identification identification = new com.klistret.cmdb.aspects.persistence.Identification();
		identification.setCiContextHelper(ciContextHelper);
		identification.setPersistenceRules(persistenceRules);

		List<PathExpression[]> criteria = identification
				.getCriteriaByQName(qname);

		String[] criterion = identification.getCriterionByObject(criteria,
				element);
		if (criterion != null) {
			for (String xpath : criterion)
				System.out.println(xpath);
		}
	}

	@Test
	public void unmarshaller() {
		StringWriter stringWriter = new StringWriter();

		try {
			JAXBContext jc = JAXBContext
					.newInstance(
							com.klistret.cmdb.aspects.persistence.PersistenceRules.class,
							com.klistret.cmdb.aspects.persistence.Criterion.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(persistenceRules, stringWriter);

			System.out.println(String.format("Element [%s]", stringWriter));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
