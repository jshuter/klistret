package test.com.klistret.cmdb.service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpResponseCodes;

import com.klistret.cmdb.utility.resteasy.ApplicationExceptionMapper;
import com.klistret.cmdb.utility.resteasy.InfrastructureExceptionMapper;

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
	private ConfigurableApplicationContext factory;

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
		factory = new ClassPathXmlApplicationContext("Spring.cfg.xml");
		factory.addBeanFactoryPostProcessor(processor);

		// Add service to the dispatcher
		SpringResourceFactory noDefaults = new SpringResourceFactory(
				"elementService", factory,
				com.klistret.cmdb.service.ElementService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

		// Necessary providers
		ResteasyProviderFactory providerFactory = dispatcher
				.getProviderFactory();
		providerFactory.registerProvider(ApplicationExceptionMapper.class);
		providerFactory.registerProvider(InfrastructureExceptionMapper.class);
	}

	//@Test
	public void get() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.get("/resteasy/element/186");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
		
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	//@Test
	public void delete() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.delete("/resteasy/element/123");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
		
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	//@Test
	public void put() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.put("/resteasy/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.Element\":{\"com.klistret.cmdb.ci.pojo.name\":\"Mars\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2010-12-28T10:37:20.923+01:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2010-12-28T10:37:20.923+01:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2010-12-28T10:37:26.408+01:00\",\"com.klistret.cmdb.ci.pojo.type\":{\"com.klistret.cmdb.ci.pojo.id\":1,\"com.klistret.cmdb.ci.pojo.name\":\"{http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/system}Environment\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2009-08-05T11:20:12.471+02:00\"},\"com.klistret.cmdb.ci.pojo.configuration\":{\"@www.w3.org.2001.XMLSchema-instance.type\":\"com.klistret.cmdb.ci.element.system:Environment\",\"@Watermark\":\"production\",\"com.klistret.cmdb.ci.commons.Name\":\"Mars\",\"com.klistret.cmdb.ci.element.State\":\"active\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
		
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	@Test
	public void create() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/resteasy/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.Element\":{\"com.klistret.cmdb.ci.pojo.name\":\"Mars\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2010-12-28T10:37:20.923+01:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2010-12-28T10:37:20.923+01:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2010-12-28T10:37:26.408+01:00\",\"com.klistret.cmdb.ci.pojo.type\":{\"com.klistret.cmdb.ci.pojo.id\":1,\"com.klistret.cmdb.ci.pojo.name\":\"{http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/system}Environment\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2009-08-05T11:20:12.471+02:00\"},\"com.klistret.cmdb.ci.pojo.configuration\":{\"@www.w3.org.2001.XMLSchema-instance.type\":\"com.klistret.cmdb.ci.element.system:Environment\",\"@Watermark\":\"production\",\"com.klistret.cmdb.ci.commons.Name\":\"Mars\",\"com.klistret.cmdb.ci.element.State\":\"active\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
		
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	// @Test
	public void query() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?expressions="
						+ URLEncoder
								.encode(
										"declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:fromTimeStamp gt \"2010-07-08T16:38:00.478+02:00\" cast as xs:dateTime]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));
		
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

}
