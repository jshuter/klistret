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

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import com.klistret.cmdb.ci.pojo.Element;

/**
 * Element service provides the basic CRUD methods plus a find which uses XPath
 * expressions to build criteria. Support for cross-domain scripting is built in
 * to speed up testing and can be disabled in the Web context in production.
 * 
 * @author Matthew Young
 */
@Path("/resteasy")
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface ElementService {

	/**
	 * Get an element by unique id
	 */
	@BadgerFish
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
	@BadgerFish
	@GET
	@Path("/element")
	List<Element> find(@QueryParam("expressions")
	List<String> expressions, @QueryParam("start")
	@DefaultValue("0")
	int start, @QueryParam("limit")
	@DefaultValue("25")
	int limit);

	/**
	 * Same as the find method except a row count is returned
	 * 
	 * @param expressions
	 * @return
	 */
	@BadgerFish
	@GET
	@Path("/element/count")
	Integer count(@QueryParam("expressions")
	List<String> expressions);

	/**
	 * Creating an element is subject to AOP checks
	 * 
	 * @param element
	 * @return Element
	 */
	@BadgerFish
	@POST
	@Path("/element")
	Element create(@BadgerFish
	Element element);

	/**
	 * Updating an element is subject to AOP checks
	 * 
	 * @param element
	 * @return Element
	 */
	@BadgerFish
	@PUT
	@Path("/element")
	Element update(@BadgerFish
	Element element);

	/**
	 * Delete an element (soft-delete)
	 * 
	 * @param id
	 */
	@BadgerFish
	@DELETE
	@Path("/element/{id}")
	Element delete(@PathParam("id")
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
