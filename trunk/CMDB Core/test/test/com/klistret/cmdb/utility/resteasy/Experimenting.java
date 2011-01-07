package test.com.klistret.cmdb.utility.resteasy;

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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.klistret.cmdb.ci.commons.Property;
import com.klistret.cmdb.ci.element.system.Environment;
import com.klistret.cmdb.ci.pojo.Element;

public class Experimenting {

	@Path("test")
	public static class MyResource {
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "Property", propOrder = { "name", "value" })
		public static class MyProperty {
			@XmlElement(name = "Name", required = true)
			protected String name;
			@XmlElement(name = "Value", required = true)
			protected String value;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}
		}

		@XmlType(name = "MyElement", propOrder = { "myProperty" })
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "MyElement", namespace = "http://www.klistret.com/cmdb/ci/commons")
		public static class MyElement {
			@XmlElement(name = "MyProperty", namespace = "http://www.klistret.com/cmdb/ci/commons")
			protected List<MyProperty> myProperty;

			public List<MyProperty> getMyProperty() {
				if (myProperty == null) {
					myProperty = new ArrayList<MyProperty>();
				}
				return this.myProperty;
			}

			public void setMyProperty(List<MyProperty> myProperty) {
				this.myProperty = myProperty;
			}
		}

		@BadgerFish
		@GET
		@Path("element/{id}")
		@Produces(MediaType.APPLICATION_JSON)
		public Element get(@PathParam("id")
		Long id) throws Exception {
			Element element = new Element();
			element.setName("hello");
			element.setFromTimeStamp(new java.util.Date());
			element.setCreateTimeStamp(new java.util.Date());
			element.setUpdateTimeStamp(new java.util.Date());

			Property example = new Property();
			example.setName("example");
			example.setValue("of a property");

			Property another = new Property();
			another.setName("another");
			another.setValue("property to look at");

			Property[] properties = new Property[] { example, another };

			Environment environment = new Environment();
			environment.setProperty(Arrays.asList(properties));
			
			String[] tags = new String[] {"my special tag" , "danny's tag"};
			environment.setTag(Arrays.asList(tags));
			
			environment.setWatermark("production");

			element.setConfiguration(environment);

			return element;
		}

		@BadgerFish
		@POST
		@Path("/element")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Element create(@BadgerFish Element element) {
			List<Property> properties = element.getConfiguration()
					.getProperty();
			System.out.println("Properties size: " + properties.size());

			return element;
		}

		@BadgerFish
		@GET
		@Path("myElement/{id}")
		@Produces(MediaType.APPLICATION_JSON)
		public MyElement getMy(@PathParam("id")
		Long id) {
			MyElement myElement = new MyElement();

			MyProperty example = new MyProperty();
			example.setName("example");
			example.setValue("of a property");

			MyProperty another = new MyProperty();
			another.setName("another");
			another.setValue("just a test");

			MyProperty[] properties = new MyProperty[] { example, another };
			myElement.setMyProperty(Arrays.asList(properties));

			return myElement;
		}

		@BadgerFish
		@POST
		@Path("/myElement")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public MyElement createMy(@BadgerFish MyElement myElement) {
			List<MyProperty> properties = myElement.getMyProperty();
			System.out.println("Properties size: " + properties.size());

			return myElement;
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
	public void get() throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.get("/test/element/44");

		MockHttpResponse response = new MockHttpResponse();

		dispatcher.invoke(request, response);
		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());

		String responseBodyAsString = response.getContentAsString();
		System.out.println(responseBodyAsString);
	}

	//@Test
	public void create() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest request = MockHttpRequest.post("/test/element");
		MockHttpResponse response = new MockHttpResponse();

		String requestBodyAsString = "{\"Element\":{\"@xmlns\":{\"ns10\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/element\\/system\",\"ns2\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/commons\",\"$\":\"http:\\/\\/www.klistret.com\\/cmdb\\/ci\\/pojo\"},\"name\":{\"$\":\"hello\"},\"fromTimeStamp\":{\"$\":\"2011-01-07T09:08:20.413+01:00\"},\"createTimeStamp\":{\"$\":\"2011-01-07T09:08:20.413+01:00\"},\"updateTimeStamp\":{\"$\":\"2011-01-07T09:08:20.413+01:00\"},\"configuration\":{\"@xmlns\":{\"xsi\":\"http:\\/\\/www.w3.org\\/2001\\/XMLSchema-instance\"},\"@xsi:type\":\"ns10:Environment\",\"ns2:Tag\":[{\"$\":\"my special tag\"},{\"$\":\"danny's tag\"}],\"ns2:Property\":[{\"ns2:Name\":{\"$\":\"example\"},\"ns2:Value\":{\"$\":\"of a property\"}},{\"ns2:Name\":{\"$\":\"another\"},\"ns2:Value\":{\"$\":\"property to look at\"}}]}}}";

		request.contentType(MediaType.APPLICATION_JSON);
		request.content(requestBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(request, response);
		System.out.println(String.format(
				"Response code [%s] with payload [%s]", response.getStatus(),
				response.getContentAsString()));

		Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
	}

	//@Test
	public void getAndCreate() throws URISyntaxException,
			UnsupportedEncodingException {
		MockHttpRequest getRequest = MockHttpRequest.get("/test/element/44");
		MockHttpResponse getResponse = new MockHttpResponse();

		dispatcher.invoke(getRequest, getResponse);
		String getResponseBodyAsString = getResponse.getContentAsString();

		System.out.println(String.format(
				"Get Response code [%s] with payload [%s]", getResponse
						.getStatus(), getResponse.getContentAsString()));

		MockHttpRequest postRequest = MockHttpRequest.post("/test/element");
		MockHttpResponse postResponse = new MockHttpResponse();

		postRequest.contentType(MediaType.APPLICATION_JSON);
		postRequest.content(getResponseBodyAsString.getBytes("UTF-8"));

		dispatcher.invoke(postRequest, postResponse);
		System.out.println(String.format(
				"Post Response code [%s] with payload [%s]", postResponse
						.getStatus(), postResponse.getContentAsString()));
	}
}
