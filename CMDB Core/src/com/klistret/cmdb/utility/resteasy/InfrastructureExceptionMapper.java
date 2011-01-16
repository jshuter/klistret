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

package com.klistret.cmdb.utility.resteasy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;

@Provider
public class InfrastructureExceptionMapper implements
		ExceptionMapper<InfrastructureException> {

	private static final Logger logger = LoggerFactory
			.getLogger(InfrastructureExceptionMapper.class);

	@Override
	public Response toResponse(InfrastructureException exception) {
		logger
				.debug("Handling InfrastructureException inside a custom resteasy mapper");

		ResponseBuilder rb = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
				exception.getMessage()).type(MediaType.TEXT_PLAIN);

		if (exception.contains(org.hibernate.TransactionException.class)) {
			rb.status(Response.Status.SERVICE_UNAVAILABLE);
		}

		return rb.build();
	}

}
