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

	@Test
	public void getById() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/getById/44");
		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	// @Test
	public void set() throws URISyntaxException, JAXBException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/resteasy/element/set");
		MockHttpResponse response = new MockHttpResponse();

		String responseBodyAsString = "{\"com.klistret.cmdb.pojo.Element\":{\"com.klistret.cmdb.pojo.name\":\"development\",\"com.klistret.cmdb.pojo.type\":{\"com.klistret.cmdb.pojo.id\":1,\"com.klistret.cmdb.pojo.name\":\"com.klistret.cmdb.xmlbeans.element.logical.collection.Environment\",\"com.klistret.cmdb.pojo.fromTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.pojo.createTimeStamp\":\"2009-08-05T11:20:12.471+02:00\",\"com.klistret.cmdb.pojo.updateTimeStamp\":\"2009-08-05T11:20:12.471+02:00\"},\"com.klistret.cmdb.pojo.fromTimeStamp\":\"2009-08-20T15:33:54.993+02:00\",\"com.klistret.cmdb.pojo.createTimeStamp\":\"2009-08-20T15:33:54.993+02:00\",\"com.klistret.cmdb.pojo.updateTimeStamp\":\"2009-09-15T14:53:14.825+02:00\",\"com.klistret.cmdb.element.logical.collection.Environment\":{\"com.klistret.cmdb.Name\":\"not billy\",\"com.klistret.cmdb.Namespace\":\"development\"}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(responseBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}
}
