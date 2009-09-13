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

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

import com.klistret.cmdb.pojo.Element;

@Path("/resteasy")
public interface ElementService {

	@GET
	@Path("/element/getById/{id}")
	@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Mapped(namespaceMap = {
			@XmlNsMap(namespace = "http://www.klistret.com/cmdb", jsonName = "com.klistret.cmdb"),
			@XmlNsMap(namespace = "http://www.klistret.com/cmdb/pojo", jsonName = "com.klistret.cmdb.pojo"),
			@XmlNsMap(namespace = "http://www.klistret.com/cmdb/element/logical/collection", jsonName = "com.klistret.cmdb.element.logical.collection") })
	Element getById(@PathParam("id") Long id);

	Collection<Element> findByCriteria(
			com.klistret.cmdb.pojo.PropertyCriteria criteria);

	Integer countByCriteria(com.klistret.cmdb.pojo.PropertyCriteria criteria);

	Element set(Element element);
}