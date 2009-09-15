package com.klistret.cmdb.utility.resteasy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.MessageBodyReaderContext;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@ServerInterceptor
public class JSONNamespaceMappingInterceptor implements
		MessageBodyReaderInterceptor, AcceptedByMethod {

	private static final Logger logger = LoggerFactory
			.getLogger(JSONNamespaceMappingInterceptor.class);

	public Object read(MessageBodyReaderContext context) throws IOException,
			WebApplicationException {
		InputStream is = context.getInputStream();
		StringBuilder sb = new StringBuilder();

		if (is != null) {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			String data = null;
			while ((data = reader.readLine()) != null) {
				sb.append(data + "\n");
			}
		}

		logger.debug("reading [data: {}]...", sb.toString());
		return context.proceed();
	}

	@SuppressWarnings("unchecked")
	public boolean accept(Class declaring, Method method) {
		logger.debug("accepting...");
		return true;
	}

}
