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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.ReadFromStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.service.RestEasyService.Bubble;
import com.klistret.cmdb.utility.annotations.CustomElement;

@Provider
@Produces("application/x-protobuf")
@Consumes("application/x-protobuf")
public class TestMessageBody implements MessageBodyWriter<Bubble>,
		MessageBodyReader<Bubble> {

	private static final Logger logger = LoggerFactory
			.getLogger(TestMessageBody.class);

	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return type.isAnnotationPresent(CustomElement.class);
	}

	public Bubble readFrom(Class<Bubble> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		String buffer = new String(ReadFromStream.readFromStream(10,
				entityStream));

		logger.debug(buffer);

		Bubble bubble = new Bubble();
		bubble.setPlastic("sparkeling");
		bubble.setWater("wet");

		return bubble;
	}

	public long getSize(Bubble arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return type.isAssignableFrom(Bubble.class);
		// return type.isAnnotationPresent(CustomElement.class);
	}

	public void writeTo(Bubble arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream entityStream)
			throws IOException, WebApplicationException {
		entityStream.write("hello".getBytes());
	}

}
