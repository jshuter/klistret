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

import java.util.NoSuchElementException;
import java.util.concurrent.RejectedExecutionException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

/**
 * 
 * @author Matthew Young
 * 
 */
@Provider
public class ApplicationExceptionMapper implements
		ExceptionMapper<ApplicationException> {

	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationExceptionMapper.class);

	@Override
	public Response toResponse(ApplicationException exception) {
		logger
				.debug(
						"Handling ApplicationException [{}] inside a custom resteasy mapper",
						exception.getMessage());

		ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST)
				.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);

		if (exception.contains(NoSuchElementException.class)) {
			rb.status(Response.Status.NOT_FOUND);
		}

		if (exception.contains(IllegalArgumentException.class)) {
			rb.status(Response.Status.BAD_REQUEST);
		}

		if (exception.contains(RejectedExecutionException.class)) {
			rb.status(Response.Status.FORBIDDEN);
		}

		return rb.build();
	}

}
