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

import org.jboss.resteasy.util.HttpResponseCodes;

public class RestEasyService {

	private Dispatcher dispatcher;

	private ConfigurableApplicationContext factory;

	@Before
	public void setUp() throws Exception {
		// embedded server
		dispatcher = MockDispatcherFactory.createDispatcher();

		// load up the processor and build a spring factory
		SpringBeanProcessor processor = new SpringBeanProcessor(dispatcher,
				null, null);
		factory = new ClassPathXmlApplicationContext("Spring.cfg.xml");
		factory.addBeanFactoryPostProcessor(processor);

		// add service to the dispatcher
		SpringResourceFactory noDefaults = new SpringResourceFactory(
				"elementService", factory,
				com.klistret.cmdb.service.ElementService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);
	}

	// @Test
	public void get() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.get("/resteasy/element/81");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	// @Test
	public void delete() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.delete("/resteasy/element/123");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	@Test
	public void put() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.put("/resteasy/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.Element\":{\"com.klistret.cmdb.ci.pojo.id\":123,\"com.klistret.cmdb.ci.pojo.name\":\"Mars\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2010-07-08T16:38:00.478+02:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2010-07-08T16:38:00.478+02:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2010-10-08T23:52:09.963+02:00\",\"com.klistret.cmdb.ci.pojo.type\":{\"com.klistret.cmdb.ci.pojo.id\":1,\"com.klistret.cmdb.ci.pojo.name\":\"{http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/logical\\/collection}Environment\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2009-08-05T11:20:12.471+02:00\"},\"com.klistret.cmdb.ci.pojo.configuration\":{\"@www.w3.org.2001.XMLSchema-instance.type\":\"com.klistret.cmdb.ci.element.logical.collection:Environment\",\"@Watermark\":\"past\",\"com.klistret.cmdb.ci.commons.Name\":\"Mars\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	// @Test
	public void create() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/resteasy/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.Element\":{\"com.klistret.cmdb.ci.pojo.name\":\"Mars\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2010-07-08T16:38:00.478+02:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2010-07-08T16:38:00.478+02:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2010-10-08T23:52:09.963+02:00\",\"com.klistret.cmdb.ci.pojo.type\":{\"com.klistret.cmdb.ci.pojo.id\":1,\"com.klistret.cmdb.ci.pojo.name\":\"{http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/logical\\/collection}Environment\",\"com.klistret.cmdb.ci.pojo.fromTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.createTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.ci.pojo.updateTimeStamp\":\"2009-08-05T11:20:12.471+02:00\"},\"com.klistret.cmdb.ci.pojo.configuration\":{\"@www.w3.org.2001.XMLSchema-instance.type\":\"com.klistret.cmdb.ci.element.logical.collection:Environment\",\"@Watermark\":\"test\",\"com.klistret.cmdb.ci.commons.Name\":\"Mars\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	//@Test
	public void query() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element?expressions="
						+ URLEncoder
								.encode(
										"declare namespace xsi=\"http://www.w3.org/2001/XMLSchema-instance\"; declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; declare namespace commons=\"http://www.klistret.com/cmdb/ci/commons\"; declare namespace col=\"http://www.klistret.com/cmdb/ci/element/logical/collection\"; /pojo:Element[empty(pojo:toTimeStamp) and exists(pojo:fromTimeStamp)]/pojo:type[pojo:name=\"{http://www.klistret.com/cmdb/ci/element/logical/collection}Environment\"]",
										"UTF-8"));

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		System.out.println(response.getContentAsString());
	}

}
