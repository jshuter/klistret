package test.com.klistret.cmdb.utility.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;

import org.junit.Before;
import org.junit.Test;

public class Experimenting {
	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void xquery() {
		String xquery = String
				.format(
						"declare namespace persistence=\'http://www.klistret.com/cmdb/ci/persistence\'; declare variable $this external; for $qnames at $qnameIndex in (%s) "
								+ "for $rule in $this/persistence:PersistenceRules/persistence:Rule[not(persistence:Exclusions = \'"
								+ "%s\')] "
								+ "for $criterion in $this/persistence:PersistenceRules/persistence:Criterion "
								+ "where $rule/persistence:QName = $qnames "
								+ "and $rule/persistence:Criterion = $criterion/@Name "
								+ "order by $qnameIndex, $rule/@Order empty greatest "
								+ "return $criterion",
						"com.klistret.cmdb.ci.logical.collection.Environment",
						"");

		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();
		DocumentBuilder db = processor.newDocumentBuilder();

		try {
			XQueryExecutable xqexec = xqc.compile(xquery);
			XQueryEvaluator xqeval = xqexec.load();
		} catch (SaxonApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
