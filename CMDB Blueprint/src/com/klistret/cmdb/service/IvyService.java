/**
 ** This file is part of Klistret. Klistret is free software: you can
 ** redistribute it and/or modify it under the terms of the GNU General
 ** Public License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.

 ** Klistret is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 ** General Public License for more details. You should have received a
 ** copy of the GNU General Public License along with Klistret. If not,
 ** see <http://www.gnu.org/licenses/>
 */
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

/**
 * 
 * @author Matthew Young
 *
 */
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
