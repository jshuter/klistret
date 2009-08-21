package com.klistret.cmdb.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import com.klistret.cmdb.utility.annotations.CustomElement;

@Path("/resteasy")
public interface RestEasyService {

	@GET
	@Path("/getHello")
	@Produces( { MediaType.TEXT_PLAIN })
	public String getHello();

	@GET
	@Path("/getBubble")
	@Produces("application/x-protobuf")
	public Bubble getBubble();

	@CustomElement
	public class Bubble {
		private String water;

		private String plastic;

		public String getWater() {
			return water;
		}

		public void setWater(String water) {
			this.water = water;
		}

		public String getPlastic() {
			return plastic;
		}

		public void setPlastic(String plastic) {
			this.plastic = plastic;
		}
	}
}
