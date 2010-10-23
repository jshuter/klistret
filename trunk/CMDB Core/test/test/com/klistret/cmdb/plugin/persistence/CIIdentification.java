package test.com.klistret.cmdb.plugin.persistence;

import java.util.Date;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.reflections.util.Utils;

import com.klistret.cmdb.ci.element.logical.collection.Environment;
import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementType;

public class CIIdentification {

	private com.klistret.cmdb.plugin.persistence.aspect.CIIdentification helper;

	private Element element;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {

		helper = new com.klistret.cmdb.plugin.persistence.aspect.CIIdentification(
				Utils.getContextClassLoader().getResource(
						"persistence.rules.xml"));

		ElementType elementType = new ElementType();
		elementType.setId(new Long(1));
		elementType
				.setName("{http://www.klistret.com/cmdb/ci/element/logical/collection}Environment");
		elementType.setCreateTimeStamp(new Date());

		Environment environment = new Environment();
		environment.setName("Saturnus");
		environment.setNamespace("whatever");

		element = new Element();
		element.setName("Saturnus");
		element.setType(elementType);
		element.setConfiguration(environment);
	}

	// @Test
	public void parse() {
		QName qname = new QName(
				"http://www.klistret.com/cmdb/ci/element/logical/collection",
				"Environment");

		helper.getCriteria(qname);
	}

	@Test
	public void criterion() {
		helper
				.getCriterion(element);
	}
}
