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

package test.com.klistret.cmdb.rules;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;

import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.pojo.Criterion;
import com.klistret.cmdb.pojo.PersistenceRules;
import com.klistret.cmdb.pojo.Rule;

public class Persistence {

	private PersistenceRules persistenceRules;

	@Before
	public void setUp() throws Exception {
		String selection = "declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element[matches(@name,\"Saturnus\")]/pojo:configuration/commons:Name[. = \"Saturnus\"]";

		persistenceRules = new PersistenceRules();

		Criterion cName = new Criterion();
		cName.setName("Name");
		cName
				.getExpressions()
				.add(
						"declare mapping pojo:configuration=col:Environment; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element[matches(@name,\"Saturnus\")]/pojo:configuration/commons:Name[. = \"Saturnus\"]");

		persistenceRules.getCriterion().add(cName);

		Rule rEnvironment = new Rule();
		rEnvironment.setCriterion(cName.getName());
		rEnvironment
				.setQName("com.klistret.cmdb.ci.element.logical.collection.Environment");

		persistenceRules.getRule().add(rEnvironment);
	}

	@Test
	public void xquery() {
		String xquery = String
				.format("declare default element namespace \'http://www.klistret.com/cmdb/ci/persistence\'; "
						+ "for $rule in /PersistenceRules/Rule "
						+ "for $criterion in /PersistenceRules/Criterion "
						+ "where $rule/QName = 'com.klistret.cmdb.ci.element.logical.collection.Environment' "
						+ "and $rule/Criterion = $criterion/@Name "
						+ "return $criterion");

		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();

		try {
			XQueryExecutable xqexec = xqc.compile(xquery);
			XQueryEvaluator xqeval = xqexec.load();

			JAXBContext jc = JAXBContext
					.newInstance(com.klistret.cmdb.pojo.PersistenceRules.class);
			JAXBSource source = new JAXBSource(jc, persistenceRules);

			xqeval.setSource(source);
			XdmValue results = xqeval.evaluate();

			results.size();
		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void dummy() {
		try {
			JAXBContext jc = JAXBContext
					.newInstance(com.klistret.cmdb.pojo.PersistenceRules.class);
			Marshaller m = jc.createMarshaller();

			m.marshal(persistenceRules, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
