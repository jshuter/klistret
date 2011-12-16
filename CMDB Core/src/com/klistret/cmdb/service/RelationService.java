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
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import com.klistret.cmdb.ci.pojo.Relation;

@Path("/resteasy")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface RelationService {
	@BadgerFish
	@GET
	@Path("/relation/{id}")
	Relation get(@PathParam("id") Long id);

	@BadgerFish
	@GET
	@Path("/relation")
	List<Relation> find(@QueryParam("expressions") List<String> expressions,
			@QueryParam("start") @DefaultValue("0") int start,
			@QueryParam("limit") @DefaultValue("10") int limit);

	/**
	 * Same as the find method except a row count is returned
	 * 
	 * @param expressions
	 * @return
	 */
	@BadgerFish
	@GET
	@Path("/relation/count")
	Integer count(@QueryParam("expressions") List<String> expressions);

	/**
	 * Find an unique relation by expressions
	 * 
	 * @param expressions
	 * @return
	 */
	@BadgerFish
	@GET
	@Path("/relation/unique")
	Relation unique(@QueryParam("expressions") List<String> expressions);

	/**
	 * Create a relation
	 * 
	 * @param relation
	 * @return Relation
	 */
	@BadgerFish
	@POST
	@Path("/relation")
	Relation create(@BadgerFish Relation relation);

	/**
	 * Update an relation
	 * 
	 * @param relation
	 * @return Relation
	 */
	@BadgerFish
	@PUT
	@Path("/relation")
	Relation update(@BadgerFish Relation relation);

	/**
	 * Delete an relation
	 * 
	 * @param id
	 */
	@BadgerFish
	@DELETE
	@Path("/relation/{id}")
	Relation delete(@BadgerFish @PathParam("id") Long id);

	/**
	 * Preflighed requests
	 * (https://developer.mozilla.org/En/HTTP_access_control) for cross domain
	 * access necessary for local testing
	 * 
	 */
	@OPTIONS
	@Path("/{var:.*}")
	@Produces({ MediaType.TEXT_HTML })
	String preflighted();

	/**
	 * DML-style cascade deletion of an element's relations
	 * 
	 * @param id
	 */
	int cascade(Long id);
	
	/**
	 * DML-style cascade deletion of an element's relations
	 * 
	 * @param id
	 */
	int cascade(Long id, boolean source, boolean destination);
}
