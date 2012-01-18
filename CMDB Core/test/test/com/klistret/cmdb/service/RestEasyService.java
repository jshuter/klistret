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
import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpResponseCodes;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.resteasy.ApplicationExceptionMapper;

/**
 * Element services tested through the RestEasy mock servlet
 * 
 * @author Matthew Young
 * 
 */
public class RestEasyService {

	private static Dispatcher dispatcher;

	private static GenericXmlApplicationContext ctx;

	private static final String elementId = "701120";

	private static final String organization = "com.test";

	private static final String version = "0.1";

	private static final String name = "demo";

	private static final String url = "http://vsgtmklistret.sfa.se:50003/CMDB";

	/**
	 * Without a Before static initialize then the setup is run for every test
	 * case which leads to multiple registrations of MBeans.
	 */
	@BeforeClass
	public static void runBeforeClass() {
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

		/** Add services to the dispatcher */
		SpringResourceFactory elementService = new SpringResourceFactory(
				"elementService", ctx,
				com.klistret.cmdb.service.ElementService.class);

		dispatcher.getRegistry().addResourceFactory(elementService);

		/** Add providers */
		ResteasyProviderFactory providerFactory = dispatcher
				.getProviderFactory();
		providerFactory.registerProvider(ApplicationExceptionMapper.class);
	}

	/**
	 * Get element
	 */
	@Test
	public void get() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/" + elementId)
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Delete element (expects 404 requiring the ApplicationExceptionMapper)
	 */
	@Test
	public void delete404() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.delete("/resteasy/element/0");

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_NOT_FOUND,
				response.getStatus());
	}

	/**
	 * Put (get an element then update)
	 */
	@Test
	public void put() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/" + elementId)
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		request = MockHttpRequest
				.put("/resteasy/element")
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }));

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(response.getContentAsString().getBytes("UTF-8"));
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Post (expects 403)
	 */
	@Test
	public void create403() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/" + elementId)
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));
		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		request = MockHttpRequest.post("/resteasy/element");

		request.contentType(MediaType.APPLICATION_XML);
		request.content(response.getContentAsString().getBytes("UTF-8"));
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_FORBIDDEN,
				response.getStatus());
	}

	/**
	 * Find (software)
	 */
	@Test
	public void find() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?start=0&limit=10&expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder.encode(
								String.format(
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
										organization, version), "UTF-8"));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Find (404 due to an known XPath syntax error)
	 */
	@Test
	public void find404() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?start=0&limit=10&expressions="
						+ URLEncoder
								.encode("eclare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_BAD_REQUEST,
				response.getStatus());
	}

	/**
	 * Aggregate (software version)
	 */
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
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder.encode(
								String.format(
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and commons:Name ge \"%s\"]",
										organization, name), "UTF-8"));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Unique
	 */
	@Test
	public void unique() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/unique?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder.encode(
								String.format(
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element[pojo:name eq \"%s\"]/pojo:configuration[component:Organization = (\"%s\") and component:Version ge \"%s\"]",
										name, organization, version), "UTF-8"));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	/**
	 * Count
	 */
	@Test
	public void count() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/count?expressions="
						+ URLEncoder
								.encode("declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]/pojo:type[pojo:name eq '{http://www.klistret.com/cmdb/ci/element/component}Software']",
										"UTF-8")
						+ "&expressions="
						+ URLEncoder.encode(
								String.format(
										"declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace component=\"http://www.klistret.com/cmdb/ci/element/component\"; /pojo:Element/pojo:configuration[component:Organization = (\"%s\") and commons:Name ge \"%s\"]",
										organization, name), "UTF-8"));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}


	public void client() throws Exception {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/" + elementId)
				.accept(Arrays
						.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));

		MockHttpResponse response = new MockHttpResponse();
		dispatcher.invoke(request, response);

		Element element = null;
		if (response.getStatus() == 200) {
			StreamSource source = new StreamSource(new StringReader(
					response.getContentAsString()));

			Unmarshaller um = CIContext.getCIContext().getJAXBContext()
					.createUnmarshaller();
			element = (Element) um.unmarshal(source);
		}

		ClientRequest crequest = new ClientRequest(url + "/resteasy/element");
		crequest.accept("application/xml");
		crequest.body(MediaType.APPLICATION_XML, element);

		ClientResponse<String> cresponse = crequest.put(String.class);

		Assert.assertEquals(200, cresponse.getStatus());
	}
}
