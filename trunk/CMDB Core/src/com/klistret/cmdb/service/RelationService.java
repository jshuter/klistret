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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.klistret.cmdb.ci.pojo.Relation;

@Path("/resteasy")
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface RelationService {
	@GET
	@Path("/relation/{id}")
	Relation get(@PathParam("id")
	Long id);

	@GET
	@Path("/relation")
	List<Relation> find(@QueryParam("expressions")
	List<String> expressions, @QueryParam("start")
	@DefaultValue("0")
	int start, @QueryParam("limit")
	@DefaultValue("10")
	int limit);

	/**
	 * Create a relation
	 * 
	 * @param relation
	 * @return Relation
	 */
	@POST
	@Path("/relation")
	Relation create(Relation relation);

	/**
	 * Update an relation
	 * 
	 * @param relation
	 * @return Relation
	 */
	@PUT
	@Path("/relation/{id}")
	Relation update(@PathParam("id")
	Long id, Relation relation);

	/**
	 * Delete an relation
	 * 
	 * @param id
	 */
	@DELETE
	@Path("/relation/{id}")
	void delete(@PathParam("id")
	Long id);
}
