package test.com.klistret.cmdb.utility.resteasy;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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

import com.klistret.cmdb.ci.pojo.QueryRequest;

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
				if (expressions == null) {
					expressions = new ArrayList<String>();
				}
				return this.expressions;
			}

			public void setExpressions(List<String> expressions) {
				this.expressions = expressions;
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
		@Path("simplePost")
		@Consumes( { MediaType.APPLICATION_JSON })
		public void find(Test test) {
			if (test.getExpressions() == null)
				return;

			for (String expression : test.getExpressions()) {
				System.out.println(String.format(
						"expresion [%s], start [%d], limit [%d]", expression,
						test.getStart(), test.getLimit()));
			}
		}

		@POST
		@Path("weirdPost")
		@Consumes( { MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML })
		public void finding(QueryRequest queryRequest) {
			if (queryRequest.getExpressions() == null)
				return;

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

	// @Test
	public void simpleGet() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.get("/atom/getById/44");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	@Test
	public void simplePost() throws URISyntaxException, UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/atom/simplePost");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"test\":{\"start\":0,\"limit\":50,\"expressions\":[\"hello\",\"yeah\"]}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response
				.getStatus());
	}

	@Test
	public void findingJSON() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/atom/weirdPost");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"com.klistret.cmdb.ci.pojo.QueryRequest\":{\"com.klistret.cmdb.ci.pojo.start\":0,\"com.klistret.cmdb.ci.pojo.limit\":10,\"com.klistret.cmdb.ci.pojo.expressions\":[\"hello\",\"yeah\"]}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		System.out.println(response.getContentAsString());
	}
	
	@Test
	public void findingXML() throws URISyntaxException,
	UnsupportedEncodingException, JAXBException {
		String[] expressions = {"hello", "whatever"};
		
		JAXBContext jaxbContext = JAXBContext.newInstance(QueryRequest.class);
		
		QueryRequest query = new QueryRequest();
		query.setExpressions(Arrays.asList(expressions));
		query.setLimit(10);
		query.setStart(1);
		
		StringWriter sw = new StringWriter();
		
		Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(query, sw);
        
        System.out.println(sw.toString());
		
		MockHttpRequest request = MockHttpRequest.post("/atom/weirdPost");
		MockHttpResponse response = new MockHttpResponse();
		
		request.contentType(MediaType.APPLICATION_XML);
		request.content(sw.toString().getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_NO_CONTENT, response
				.getStatus());
	}

}
