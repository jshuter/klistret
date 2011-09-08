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
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpResponseCodes;

import com.klistret.cmdb.utility.resteasy.ApplicationExceptionMapper;
import com.klistret.cmdb.utility.resteasy.AccessControlInterceptor;
import com.klistret.cmdb.utility.resteasy.InfrastructureExceptionMapper;
import com.klistret.cmdb.utility.resteasy.IntegerMessageBodyWriter;

/**
 * Element services tested through the RestEasy mock servlet
 * 
 * @author Matthew Young
 * 
 */
public class RestEasyService {

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
		ctx.load("classpath:Spring.cfg.xml");
		ctx.refresh();
		ctx.addBeanFactoryPostProcessor(processor);

		// Add service to the dispatcher
		SpringResourceFactory elementService = new SpringResourceFactory(
				"elementService", ctx,
				com.klistret.cmdb.service.ElementService.class);
		SpringResourceFactory relationService = new SpringResourceFactory(
				"relationService", ctx,
				com.klistret.cmdb.service.RelationService.class);
		SpringResourceFactory elementTypeService = new SpringResourceFactory(
				"elementTypeService", ctx,
				com.klistret.cmdb.service.ElementTypeService.class);
		SpringResourceFactory relationTypeService = new SpringResourceFactory(
				"relationTypeService", ctx,
				com.klistret.cmdb.service.RelationTypeService.class);
		dispatcher.getRegistry().addResourceFactory(elementService);
		dispatcher.getRegistry().addResourceFactory(relationService);
		dispatcher.getRegistry().addResourceFactory(elementTypeService);
		dispatcher.getRegistry().addResourceFactory(relationTypeService);

		// Necessary providers
		ResteasyProviderFactory providerFactory = dispatcher
				.getProviderFactory();
		providerFactory.registerProvider(ApplicationExceptionMapper.class);
		providerFactory.registerProvider(InfrastructureExceptionMapper.class);
		providerFactory.registerProvider(AccessControlInterceptor.class);
		providerFactory.registerProvider(IntegerMessageBodyWriter.class);

		// Language mapper
		Map<String, String> lm = new HashMap<String, String>();
		lm.put("ISO-8859-1", "UTF-8");
		dispatcher.setLanguageMappings(lm);
	}

	/**
	 * Get an element by id
	 * 
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */

	public void getElement() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/78941")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Delete an element with invalid id
	 * 
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */

	public void deleteElementExpect404() throws URISyntaxException,
			JAXBException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.delete("/resteasy/element/0");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_NOT_FOUND,
				response.getStatus());
	}

	/**
	 * Get a known element then resend as PUT
	 * 
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */

	public void putElement() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest getRequest = MockHttpRequest
				.get("/resteasy/element/78941")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));
		MockHttpResponse getResponse = new MockHttpResponse();
		dispatcher.invoke(getRequest, getResponse);

		MockHttpRequest putRequest = MockHttpRequest.put("/resteasy/element");
		MockHttpResponse putResponse = new MockHttpResponse();

		String requestBodyAsString = getResponse.getContentAsString();

		putRequest.contentType(MediaType.APPLICATION_XML);
		putRequest.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(putRequest, putResponse);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]",
				putResponse.getStatus(), putResponse.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, putResponse.getStatus());
	}

	/**
	 * Get a known element then resend as POST which is expected to fail
	 * 
	 * @throws URISyntaxException
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */

	public void createElementExpect403() throws URISyntaxException,
			JAXBException, UnsupportedEncodingException {
		MockHttpRequest getRequest = MockHttpRequest
				.get("/resteasy/element/78941")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));
		MockHttpResponse getResponse = new MockHttpResponse();
		dispatcher.invoke(getRequest, getResponse);

		MockHttpRequest postRequest = MockHttpRequest.post("/resteasy/element");
		MockHttpResponse postResponse = new MockHttpResponse();

		String requestBodyAsString = getResponse.getContentAsString();

		postRequest.contentType(MediaType.APPLICATION_XML);
		postRequest.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(postRequest, postResponse);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]",
				postResponse.getStatus(), postResponse.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_FORBIDDEN,
				postResponse.getStatus());
	}

	/**
	 * Valid query after software elements
	 * 
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */

	public void findElement() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Module = (\"KUI\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component/software}ApplicationSoftware\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Availability = (\"Nov2010R\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Valid query after software elements
	 * 
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */

	public void uniqueElement() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/unique?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Module = (\"KUI\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component/software}ApplicationSoftware\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Availability = (\"Nov2010R\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Count elements
	 * 
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void countElement() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/count?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Module = (\"KUI\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component/software}ApplicationSoftware\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Availability = (\"Nov2010R\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Find elements expected to return nothing due to a misspelling of an
	 * element name
	 * 
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */

	public void findElementExpect200() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Modul = (\"KUI\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Find elements expected to fail (400) due to bad comparison operator
	 * 
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */

	public void findElementExpect400() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace sw=\"http://www.klistret.com/cmdb/ci/element/component/software\"; /pojo:Element/pojo:configuration[sw:Module == (\"KUI\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_BAD_REQUEST,
				response.getStatus());
	}

	
	public void findRelations() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/relation?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation/pojo:source[pojo:id eq 8788]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK,
				response.getStatus());
	}
}
