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
				exception.getMessage()).type(MediaType.TEXT_HTML);

		if (exception.contains(org.hibernate.TransactionException.class)) {
			rb = Response.status(Response.Status.SERVICE_UNAVAILABLE);
		}

		return rb.build();
	}

}
