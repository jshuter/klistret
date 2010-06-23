package test.com.klistret.cmdb.service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

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

	//@Test
	public void getById() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/get/81");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	//@Test
	public void set() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/resteasy/element/set");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.Element\":{\"com.klistret.cmdb.pojo.id\":44,\"com.klistret.cmdb.pojo.name\":\"development\",\"com.klistret.cmdb.pojo.type\":{\"com.klistret.cmdb.pojo.id\":1,\"com.klistret.cmdb.pojo.name\":\"com.klistret.cmdb.element.logical.collection.Environment\",\"com.klistret.cmdb.pojo.fromTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.pojo.createTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.pojo.updateTimeStamp\":\"2009-08-05T11:20:12.471+02:00\"},\"com.klistret.cmdb.pojo.fromTimeStamp\":\"2009-08-20T15:33:54.993+02:00\",\"com.klistret.cmdb.pojo.createTimeStamp\":\"2009-08-20T15:33:54.993+02:00\",\"com.klistret.cmdb.pojo.updateTimeStamp\":\"2009-09-15T15:34:19.769+02:00\",\"com.klistret.cmdb.pojo.configuration\":{\"@www.w3.org.2001.XMLSchema-instance.type\":\"com.klistret.cmdb.element.logical.collection:Environment\",\"com.klistret.cmdb.Name\":\"something\",\"com.klistret.cmdb.Namespace\":\"test\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}
	
	@Test
	public void find() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/resteasy/element/find");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"QueryRequest\":{\"start\":0,\"limit\":100,\"expressions\":[\"declare mapping pojo:configuration=col:Environment; declare namespace pojo=\\\"http://www.klistret.com/cmdb/ci/pojo\\\"; declare namespace commons=\\\"http://www.klistret.com/cmdb/ci/commons\\\"; declare namespace col=\\\"http://www.klistret.com/cmdb/ci/element/logical/collection\\\"; /pojo:Element[matches(@name,\\\"Saturnus\\\")]\"]}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
		
		System.out.println(response.getContentAsString());
	}
}
