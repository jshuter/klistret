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

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterContext;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;

@Provider
@ServerInterceptor
public class AccessControlInterceptor implements MessageBodyWriterInterceptor {

	private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	@Override
	public void write(MessageBodyWriterContext context) throws IOException,
			WebApplicationException {
		context.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		context.getHeaders().add(ACCESS_CONTROL_ALLOW_METHODS,
				"POST, GET, OPTIONS, PUT, DELETE");
		context.getHeaders().add(ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with");
		context.getHeaders().add(ACCESS_CONTROL_MAX_AGE, "1728000");

		context.proceed();

	}

}
