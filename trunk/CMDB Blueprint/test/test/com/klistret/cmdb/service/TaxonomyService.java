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
package test.com.klistret.cmdb.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.klistret.cmdb.taxonomy.pojo.Element;

public class TaxonomyService {

	private com.klistret.cmdb.service.TaxonomyService taxonomyService;

	@Before
	public void setUp() throws Exception {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
		ctx.getEnvironment().setActiveProfiles("development");
		ctx.load("classpath:Test.cfg.xml");
		ctx.refresh();

		taxonomyService = ctx
				.getBean(com.klistret.cmdb.service.TaxonomyService.class);
	}

	@Test
	public void getTaxonomies() {
		List<String> results = taxonomyService.getTaxonomies();
		for (String name : results)
			System.out.println(name);
	}

	@Test
	public void getGranularities() {
		List<String> results = taxonomyService.getGranularities("Application");
		for (String name : results)
			System.out.println(name);
	}

	@Test
	public void getElements() {
		List<Element> results = taxonomyService.getElements("Application",
				"J2EE");
		for (Element element : results)
			System.out.println(String.format(
					"Element type [%s] with %d relations, %d properties",
					element.getType(), element.getRelation().size(), element
							.getProperty().size()));

	}
}
