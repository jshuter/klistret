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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@ServerInterceptor
public class AccessControlInterceptor implements MessageBodyWriterInterceptor {
	private static final Logger logger = LoggerFactory
			.getLogger(AccessControlInterceptor.class);

	private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

	private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	/**
	 * http://www.webdavsystem.com/ajaxfilebrowser/programming/cross_domain
	 */
	@Override
	public void write(MessageBodyWriterContext context) throws IOException,
			WebApplicationException {

		String crossdomain = System.getProperty("resteasy.crossdomain");

		if (crossdomain != null
				&& crossdomain.toLowerCase().trim().matches("on|true|yes")) {
			logger.warn("Activating cross domain scripting");

			context.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			context.getHeaders().add(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			context.getHeaders()
					.add(ACCESS_CONTROL_ALLOW_METHODS,
							"PROPFIND, PROPPATCH, COPY, MOVE, DELETE, MKCOL, LOCK, UNLOCK, PUT, GETLIB, VERSION-CONTROL, CHECKIN, CHECKOUT, UNCHECKOUT, REPORT, UPDATE, CANCELUPLOAD, HEAD, OPTIONS, GET, POST");
			context.getHeaders()
					.add(ACCESS_CONTROL_ALLOW_HEADERS,
							"Overwrite, Destination, Content-Type, Depth, User-Agent, X-File-Size, X-Requested-With, If-Modified-Since, X-File-Name, Cache-Control");
			context.getHeaders().add(ACCESS_CONTROL_MAX_AGE, "1728000");
		}

		context.proceed();
	}

}
