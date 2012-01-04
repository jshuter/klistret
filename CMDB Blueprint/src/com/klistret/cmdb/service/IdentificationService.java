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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;
import javax.ws.rs.POST;

import com.klistret.cmdb.ci.pojo.Element;

/**
 * Interface for the identification of elements
 * 
 * @author Matthew Young
 * 
 */
@Path("/resteasy")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface IdentificationService {

	@BadgerFish
	@POST
	@Path("/identification")
	public Integer identified(@BadgerFish Element element);

	@BadgerFish
	@POST
	@Path("/identification/fullCriterion")
	public List<String> getFullCriterion(@BadgerFish Element element);

	@BadgerFish
	@POST
	@Path("/identification/criterion")
	public List<String> getCriterion(@BadgerFish Element element);
}
