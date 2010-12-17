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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.QueryResponse;

@Path("/resteasy")
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface ElementService {

	/**
	 * Get an element by unique id
	 */
	@GET
	@Path("/element/{id}")
	Element get(@PathParam("id")
	Long id);

	/**
	 * Get elements that fulfill the XPath expressions (criterion) at a start
	 * position up to the specified limit (i.e. maximum result size)
	 * 
	 * @param expressions
	 * @param start
	 * @param limit
	 * @return Elements
	 */
	List<Element> find(List<String> expressions, int start, int limit);

	/**
	 * Wrapper service for Ajax clients
	 * 
	 * @param expressions
	 * @param start
	 * @param limit
	 * @return
	 */
	@GET
	@Path("/element")
	QueryResponse query(@QueryParam("expressions")
	List<String> expressions, @QueryParam("start")
	@DefaultValue("0")
	int start, @QueryParam("limit")
	@DefaultValue("10")
	int limit);

	/**
	 * Create an element
	 * 
	 * @param element
	 * @return Element
	 */
	@POST
	@Path("/element")
	Element create(Element element);

	/**
	 * Update an element
	 * 
	 * @param element
	 * @return Element
	 */
	@PUT
	@Path("/element")
	Element update(Element element);

	/**
	 * Delete an element
	 * 
	 * @param id
	 */
	@DELETE
	@Path("/element/{id}")
	void delete(@PathParam("id")
	Long id);

	/**
	 * Preflighed requests
	 * (https://developer.mozilla.org/En/HTTP_access_control) for cross domain
	 * access necessary for local testing
	 * 
	 */
	@OPTIONS
	@Path("/{var:.*}")
	@Produces( { MediaType.TEXT_HTML })
	String preflighted();
}
