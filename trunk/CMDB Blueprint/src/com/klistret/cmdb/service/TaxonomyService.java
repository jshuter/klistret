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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import com.klistret.cmdb.taxonomy.pojo.Element;

/**
 * 
 * @author Matthew Young
 * 
 */
@Path("/resteasy")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface TaxonomyService {

	/**
	 * Get all of the defined taxonomy units
	 * 
	 * @return List
	 */
	@BadgerFish
	@GET
	@Path("/taxonomies")
	public List<String> getTaxonomies();

	/**
	 * Get all of the levels of granularity per taxonomy unit
	 * 
	 * @return List
	 */
	@BadgerFish
	@GET
	@Path("/taxonomy/{taxonomyName}/granularities")
	public List<String> getGranularities(
			@PathParam("taxonomyName") String taxonomyName);

	/**
	 * Get all of the element entities covered within a taxonomy unit for a
	 * given granularity as well extended granularity units
	 * 
	 * @return List
	 */
	@BadgerFish
	@GET
	@Path("/taxonomy/{taxonomyName}/granularity/{granularityName}/elements")
	public List<Element> getElements(
			@PathParam("taxonomyName") String taxonomyName,
			@PathParam("granularityName") String granularityName);

}
