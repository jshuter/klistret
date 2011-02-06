package com.klistret.cmdb.utility.resteasy;


import javax.ws.rs.WebApplicationException;

import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

public class EncodingInterceptor implements PreProcessInterceptor {

	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
			throws Failure, WebApplicationException {

		return null;
	}

}
