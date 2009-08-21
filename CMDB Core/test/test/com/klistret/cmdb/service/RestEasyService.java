package test.com.klistret.cmdb.service;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.klistret.cmdb.service.RestEasyService.Bubble;
import com.klistret.cmdb.utility.resteasy.TestMessageBody;

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
		factory = new ClassPathXmlApplicationContext("RestEasySpring.xml");
		factory.addBeanFactoryPostProcessor(processor);

		// 
		SpringResourceFactory noDefaults = new SpringResourceFactory(
				"restEasyService", factory,
				com.klistret.cmdb.service.RestEasyService.class);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

		// provider has to be registered
		dispatcher.getProviderFactory().addMessageBodyWriter(
				TestMessageBody.class);
	}

	@Test
	public void dummy() throws URISyntaxException {
		MessageBodyWriter writer = dispatcher.getProviderFactory()
				.getMessageBodyWriter(Bubble.class, null, null,
						new MediaType("application", "x-protobuf"));

		MockHttpRequest request = MockHttpRequest.get("/resteasy/getBubble");
		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(response.getContentAsString());
	}

	@Test
	public void another() throws JsonGenerationException, JsonMappingException,
			IOException {
		com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument document = com.klistret.cmdb.xmlbeans.element.logical.collection.EnvironmentDocument.Factory
				.newInstance();
		com.klistret.cmdb.xmlbeans.element.logical.collection.Environment environment = document
				.addNewEnvironment();
		environment.setName("whatever");
		environment.setNamespace("development");

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(System.out, environment);
	}
}
