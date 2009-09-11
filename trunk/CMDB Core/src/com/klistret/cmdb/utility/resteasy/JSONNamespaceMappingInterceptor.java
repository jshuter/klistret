package com.klistret.cmdb.utility.resteasy;

import java.io.IOException;
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
		logger.debug("made it");
		return context.proceed();
	}

	@SuppressWarnings("unchecked")
	public boolean accept(Class declaring, Method method) {
		logger.debug("made it");
		return true;
	}

}
