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

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import com.klistret.cmdb.ci.pojo.ElementType;

/**
 * 
 * @author Matthew Young
 *
 */
@Path("/resteasy")
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface ElementTypeService {

	@BadgerFish
	@GET
	@Path("/elementType/{name}")
	ElementType get(@PathParam("name")
	String name);

	@BadgerFish
	@GET
	@Path("/elementType")
	List<ElementType> find(@QueryParam("name")
	String name);
	
	/**
	 * 
	 * @param elementType
	 * @return
	 */
	ElementType create(ElementType elementType);
	
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
