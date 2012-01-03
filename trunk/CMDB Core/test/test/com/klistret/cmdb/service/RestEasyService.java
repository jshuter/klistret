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
				.get("/resteasy/element?start=0&limit=100&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/process/change}SoftwareInstallation\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace process=\"http://www.klistret.com/cmdb/ci/element/process\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:configuration[process:State = (\"Planned\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	@Test
	public void aggregate() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/aggregate?projection="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; max(/pojo:Element/pojo:configuration/component:Version)",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\" or pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element[pojo:name eq \"INF\"]/pojo:configuration[matches(component:Version,\"0068\")]",
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
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"inf\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Availability = (\"Mar2008R\")]",
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

	public void countElement() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/count?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[matches(pojo:name,\"inf\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Availability = (\"Mar2008R\", \"Jun2008R\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	public void countElement2() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/count?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Software\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:name = (\"KUI\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace element=\"http://www.klistret.com/cmdb/ci/element\"; /pojo:Element/pojo:destinationRelations[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Composition\"]/pojo:source[empty(pojo:toTimeStamp)][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/system}Application\"]/pojo:configuration[element:Environment = (\"Produktion\",\"Uranus\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
	}

	public void countElement3() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/count?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/process/change}SoftwareInstallation\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace change=\"http://www.klistret.com/cmdb/ci/element/process/change\"; /pojo:Element/pojo:configuration/change:Environment[commons:Name = (\"Test\")]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace process=\"http://www.klistret.com/cmdb/ci/element/process\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:configuration[process:State = (\"Planned\")]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
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

	public void findRelation() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/relation/unique?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[empty(pojo:toTimeStamp)]/pojo:source[pojo:id eq 448055]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation[pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/relation}Dependency\"]",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Relation/pojo:destination[pojo:id eq 468925][pojo:type/pojo:name eq \"{http://www.klistret.com/cmdb/ci/element/component}Publication\"]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	public void getRelation() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/relation/737397");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	public void putElement2() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest putRequest = MockHttpRequest.put("/resteasy/element");
		MockHttpResponse putResponse = new MockHttpResponse();

		String requestBodyAsString = "{\"Element\":{\"@xmlns\":{\"$\":\"http://www.klistret.com/cmdb/ci/pojo\",\"ns10\":\"http://www.klistret.com/cmdb/ci/element\",\"ns11\":\"http://www.klistret.com/cmdb/ci/element/system\",\"ns12\":\"http://www.klistret.com/cmdb/ci/element/system/computerSystem\",\"ns13\":\"http://www.klistret.com/cmdb/ci/relation\",\"ns2\":\"http://www.klistret.com/cmdb/ci/commons\",\"ns3\":\"http://www.klistret.com/cmdb/ci/element/context\",\"ns4\":\"http://www.klistret.com/cmdb/ci/element/context/lifecycle\",\"ns5\":\"http://www.klistret.com/cmdb/ci/element/process\",\"ns6\":\"http://www.klistret.com/cmdb/ci/element/process/change\",\"ns7\":\"http://www.klistret.com/cmdb/ci/element/service\",\"ns8\":\"http://www.klistret.com/cmdb/ci/element/component\",\"ns9\":\"http://www.klistret.com/cmdb/ci/element/component/software\"},\"createTimeStamp\":{\"$\":\"2011-10-12T10:29:29+02:00\"},\"fromTimeStamp\":{\"$\":\"2011-10-12T10:29:29+02:00\"},\"id\":{\"$\":\"414385\"},\"name\":{\"$\":\"INF WLSSERVER\"},\"type\":{\"createTimeStamp\":{\"$\":\"2011-09-22T10:48:41.469+02:00\"},\"fromTimeStamp\":{\"$\":\"2011-09-22T10:48:41.469+02:00\"},\"id\":{\"$\":\"87\"},\"name\":{\"$\":\"{http://www.klistret.com/cmdb/ci/element/process/change}SoftwareInstallation\"},\"updateTimeStamp\":{\"$\":\"2011-09-22T10:48:41.639+02:00\"}},\"updateTimeStamp\":{\"$\":\"2011-10-13T10:00:09.099+02:00\"},\"configuration\":{\"@xmlns\":{\"xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"},\"@xsi:type\":\"ns6:SoftwareInstallation\",\"ns2:Name\":{\"$\":\"INF WLSSERVER\"},\"ns2:Tag\":[{\"$\":\"Script\"}],\"ns5:State\":\"Completed\",\"ns6:Environment\":{\"ns2:Id\":{\"$\":\"303330\"},\"ns2:Name\":{\"$\":\"Test\"},\"ns2:QName\":{\"$\":\"{http://www.klistret.com/cmdb/ci/element/context}Environment\"}},\"ns6:Software\":{\"ns2:Id\":{\"$\":\"321889\"},\"ns2:Name\":{\"$\":\"INF WLSSERVER\"},\"ns2:QName\":{\"$\":\"{http://www.klistret.com/cmdb/ci/element/component}Software\"}},\"ns6:Type\":{\"$\":\"WLSSERVER\"},\"ns6:Label\":{\"$\":\"INF_0068_A01_UTSKRIFT_WLSSERVER.110412.tar\"},\"ns6:Version\":{\"$\":\"0068_A01\"}}}}";

		putRequest.contentType(MediaType.APPLICATION_JSON);
		putRequest.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(putRequest, putResponse);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]",
				putResponse.getStatus(), putResponse.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, putResponse.getStatus());
	}
}
