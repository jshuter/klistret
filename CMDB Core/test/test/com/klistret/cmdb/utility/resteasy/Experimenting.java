package test.com.klistret.cmdb.utility.resteasy;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Experimenting {

	@Path("atom")
	public static class MyResource {
		@XmlRootElement(name = "test")
		public static class Test {

			@XmlElement
			private String[] expressions;

			public String[] getExpressions() {
				return expressions;
			}
		}

		@GET
		@Path("getById/{id}")
		@Produces(MediaType.APPLICATION_JSON)
		public Test getTest(@PathParam("id")
		Long id) throws Exception {
			String[] whatever = { "hello", "yeah" };

			Test test = new Test();
			test.expressions = whatever;

			return test;
		}

		@POST
		@Path("find")
		@Consumes( { MediaType.APPLICATION_JSON })
		public void find(Test test) {
			for (String expression : test.getExpressions()) {
				System.out.println(expression);
			}
		}
	}

	private Dispatcher dispatcher;

	@Before
	public void setUp() throws Exception {
		// embedded server
		dispatcher = MockDispatcherFactory.createDispatcher();
		dispatcher.getRegistry().addPerRequestResource(MyResource.class);

	}

	@Test
	public void simpleGet() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.get("/atom/getById/44");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	@Test
	public void find() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/atom/find");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"test\":{\"expressions\":[\"declare namespace pojo=\\\"http://www.klistret.com/cmdb/pojo\\\"; declare namespace cmdb=\\\"http://www.klistret.com/cmdb\\\"; declare namespace col=\\\"http://www.klistret.com/cmdb/element/logical/collection\\\"; /pojo:Element[matches(@name,\\\"dev\\\")]/pojo:configuration/cmdb:Namespace[. = \\\"development\\\"]\",\"yeah\"]}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}
}
