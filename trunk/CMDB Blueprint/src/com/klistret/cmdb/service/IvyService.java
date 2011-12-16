package com.klistret.cmdb.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import com.klistret.cmdb.ivy.pojo.IvyModule;

@Path("/resteasy")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface IvyService {

	@BadgerFish
	@PUT
	@Path("/ivy")
	public void register(IvyModule moduleDescriptor);

	@BadgerFish
	@GET
	@Path("/ivy")
	public IvyModule get(@QueryParam("organization") String organization,
			@QueryParam("module") String module,
			@QueryParam("revision") String revision);

	@BadgerFish
	@GET
	@Path("/ivy/transient")
	public IvyModule getTransient(
			@QueryParam("organization") String organization,
			@QueryParam("module") String module,
			@QueryParam("revision") String revision);

	@BadgerFish
	@GET
	@Path("/ivy/explicit")
	public IvyModule getExplicit(
			@QueryParam("organization") String organization,
			@QueryParam("module") String module,
			@QueryParam("revision") String revision);
}
