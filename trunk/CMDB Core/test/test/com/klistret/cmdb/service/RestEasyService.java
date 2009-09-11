package test.com.klistret.cmdb.service;

import java.net.URISyntaxException;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.interception.InterceptorRegistry;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.klistret.cmdb.utility.resteasy.JSONNamespaceMappingInterceptor;

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

		// add intercepter to dispatcher
		InterceptorRegistry<MessageBodyReaderInterceptor> interceptorRegistry = dispatcher
				.getProviderFactory()
				.getServerMessageBodyReaderInterceptorRegistry();
		interceptorRegistry.register(new JSONNamespaceMappingInterceptor());
		Object[] array = interceptorRegistry.bind(null, null);
		for (Object obj : array)
			System.out.println(obj.getClass().getName());
	}

	@Test
	public void getById() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest
				.get("/resteasy/element/getById/44");
		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		System.out.println(response.getContentAsString());
	}
}
