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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpResponseCodes;

import com.klistret.cmdb.utility.resteasy.ApplicationExceptionMapper;
import com.klistret.cmdb.utility.resteasy.EncodingInterceptor;
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
		SpringResourceFactory elementService = new SpringResourceFactory(
				"elementService", factory,
				com.klistret.cmdb.service.ElementService.class);
		SpringResourceFactory relationService = new SpringResourceFactory(
				"relationService", factory,
				com.klistret.cmdb.service.RelationService.class);
		SpringResourceFactory relationTypeService = new SpringResourceFactory(
				"relationTypeService", factory,
				com.klistret.cmdb.service.RelationTypeService.class);
		dispatcher.getRegistry().addResourceFactory(elementService);
		dispatcher.getRegistry().addResourceFactory(relationService);
		dispatcher.getRegistry().addResourceFactory(relationTypeService);

		// Necessary providers
		ResteasyProviderFactory providerFactory = dispatcher
				.getProviderFactory();
		providerFactory.registerProvider(ApplicationExceptionMapper.class);
		providerFactory.registerProvider(InfrastructureExceptionMapper.class);
		providerFactory.registerProvider(EncodingInterceptor.class);
		
		// Language mapper
		Map<String,String> lm = new HashMap<String,String>();
		lm.put("ISO-8859-1", "UTF-8");
		dispatcher.setLanguageMappings(lm);
	}

	// @Test
	public void get() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/242")
				.accept(
						Arrays
								.asList(new MediaType[] { MediaType.APPLICATION_XML_TYPE }));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	// @Test
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

	// @Test
	public void put() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.put("/resteasy/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.Element\":{\"com.klistret.cmdb.ci.pojo.id\":202,\"com.klistret.cmdb.ci.pojo.name\":\"Mars\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2010-12-28T10:37:20.923+01:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2010-12-28T10:37:20.923+01:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2010-12-28T10:37:26.408+01:00\",\"com.klistret.cmdb.ci.pojo.type\":{\"com.klistret.cmdb.ci.pojo.id\":18,\"com.klistret.cmdb.ci.pojo.name\":\"{http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/system}Environment\",},\"com.klistret.cmdb.ci.pojo.configuration\":{\"@www.w3.org.2001.XMLSchema-instance.type\":\"com.klistret.cmdb.ci.element.system:Environment\",\"@Watermark\":\"production\",\"com.klistret.cmdb.ci.commons.Name\":\"Mars\",\"com.klistret.cmdb.ci.element.State\":\"active\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	// @Test
	public void create() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/resteasy/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"Element\":{\"@xmlns\":{\"ns9\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\",\"ns10\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/context\",\"ns2\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/commons\",\"$\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/pojo\"},\"name\":{\"$\":\"Försäkringskassn\"},\"type\":{\"id\":{\"$\":\"10\"},\"name\":{\"$\":\"{http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/context}Environment\"}},\"fromTimeStamp\":{\"$\":\"2011-01-07T09:08:20.413+01:00\"},\"createTimeStamp\":{\"$\":\"2011-01-07T09:08:20.413+01:00\"},\"updateTimeStamp\":{\"$\":\"2011-01-07T09:08:20.413+01:00\"},\"configuration\":{\"@xmlns\":{\"xsi\":\"http:\\/\\/www.w3.org\\/2001\\/XMLSchema-instance\"},\"@xsi:type\":\"ns10:Environment\",\"ns2:Name\":{\"$\":\"Försäkringskassan\"},\"ns2:Tag\":[{\"$\":\"my special tag\"},{\"$\":\"danny's tag\"}],\"ns2:Property\":[{\"ns2:Name\":{\"$\":\"example\"},\"ns2:Value\":{\"$\":\"of a property\"}},{\"ns2:Name\":{\"$\":\"another\"},\"ns2:Value\":{\"$\":\"property to look at\"}}],\"ns9:State\":{\"$\":\"Online\"}}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	@Test
	public void query() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?expressions="
						+ URLEncoder
								.encode(
										"declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:sourceRelations[pojo:id eq 63]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	// @Test
	public void find() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/relationType?name="
						+ URLEncoder.encode("%", "UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}
}
