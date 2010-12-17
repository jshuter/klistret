package com.klistret.cmdb.utility.resteasy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

@Provider
public class ApplicationExceptionMapper implements
		ExceptionMapper<ApplicationException> {

	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationExceptionMapper.class);

	@Override
	public Response toResponse(ApplicationException exception) {
		logger
				.debug("Handling ApplicationException inside a custom resteasy mapper");
		ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST).entity(
				exception.getMessage()).type(MediaType.TEXT_HTML);
		
		return rb.build();
	}

}
