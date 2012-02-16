package test.com.klistret.cmdb.service;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:Spring.cfg.xml" })
@ActiveProfiles("development")
public class DummyService {

	@Autowired
	protected com.klistret.cmdb.service.ElementService elementService;

	/**
	 * Error
	 */
	@Test
	public void error() {
		Integer response = elementService
				.count(Arrays
						.asList(new String[] {
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"][pojo:name = (\"UOT\",\"TFP\")]",
								"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:destinationRelations[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]/pojo:source[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]/pojo:configuration[element:Environment = (\"Uranus\")]" }));

		assertNotNull(response);
	}
}
