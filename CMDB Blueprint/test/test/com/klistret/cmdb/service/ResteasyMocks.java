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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.klistret.cmdb.utility.resteasy.ListStringMessageBodyWriter;

public class ResteasyMocks {

	/**
	 * Mock dispatcher
	 */
	private Dispatcher dispatcher;

	/**
	 * Spring application context
	 */
	private GenericXmlApplicationContext ctx;

	/**
	 * Sets up the RestEasy Mock framework
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Embedded server
		dispatcher = MockDispatcherFactory.createDispatcher();

		// Load up the processor and build a spring factory
		SpringBeanProcessor processor = new SpringBeanProcessor(dispatcher,
				null, null);

		ctx = new GenericXmlApplicationContext();
		ctx.getEnvironment().setActiveProfiles("development");
		ctx.load("classpath:Test.cfg.xml");
		ctx.refresh();
		ctx.addBeanFactoryPostProcessor(processor);

		// Add service to the dispatcher
		SpringResourceFactory taxonomyService = new SpringResourceFactory(
				"taxonomyService", ctx,
				com.klistret.cmdb.service.TaxonomyService.class);
		SpringResourceFactory identificationService = new SpringResourceFactory(
				"identificationService", ctx,
				com.klistret.cmdb.service.IdentificationService.class);
		dispatcher.getRegistry().addResourceFactory(taxonomyService);
		dispatcher.getRegistry().addResourceFactory(identificationService);

		ResteasyProviderFactory providerFactory = dispatcher
				.getProviderFactory();
		providerFactory.registerProvider(ListStringMessageBodyWriter.class);
	}

	public void getTaxonomies() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/taxonomies")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	public void getGranularities() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/taxonomy/Application/granularities")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	public void getElements() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/taxonomy/Application/granularity/J2EE/elements")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	@Test
	public void getFullCriterion() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.post("/resteasy/identification/fullCriterion")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));
		
		request.contentType(MediaType.APPLICATION_XML);
		request.content("".getBytes("UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}
}
