package test.com.klistret.cmdb.utility.resteasy;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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

import com.klistret.cmdb.pojo.QueryRequest;


public class Experimenting {

	@Path("atom")
	public static class MyResource {
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "test")
		public static class Test {

			@XmlElement(required = true)
			protected List<String> expressions;

			protected int start;

			protected int limit;

			public List<String> getExpressions() {
				return expressions;
			}

			public int getStart() {
				return start;
			}

			public int getLimit() {
				return limit;
			}
		}

		@GET
		@Path("getById/{id}")
		@Produces(MediaType.APPLICATION_JSON)
		public Test getTest(@PathParam("id")
		Long id) throws Exception {
			String[] whatever = { "hello", "yeah" };

			Test test = new Test();
			test.expressions = Arrays.asList(whatever);

			return test;
		}

		@POST
		@Path("find")
		@Consumes( { MediaType.APPLICATION_JSON })
		public void find(Test test) {
			for (String expression : test.getExpressions()) {
				System.out.println(String.format(
						"expresion [%s], start [%d], limit [%d]", expression,
						test.getStart(), test.getLimit()));
			}
		}
		
		@POST
		@Path("finding")
		@Consumes( { MediaType.APPLICATION_JSON })
		public void finding(QueryRequest queryRequest) {
			for (String expression : queryRequest.getExpressions()) {
				System.out.println(String.format(
						"expresion [%s], start [%d], limit [%d]", expression,
						queryRequest.getStart(), queryRequest.getLimit()));
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

	//@Test
	public void simpleGet() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.get("/atom/getById/44");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	//@Test
	public void find() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/atom/find");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"test\":{\"start\":0,\"limit\":50,\"expressions\":[\"hello\",\"yeah\"]}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());
	}
	
	@Test
	public void finding() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/atom/finding");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"QueryRequest\":{\"expressions\":[\"hello\",\"yeah\"]}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response.getStatus());
	}
}
