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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * The Resteasy framework does't provide a out-of-box way to roll Integer so
 * this message body writer just transforms the Integer into a string value. If
 * the Integer is null then "-1" is returned.
 * 
 * @author Matthew Young
 * 
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class IntegerMessageBodyWriter implements MessageBodyWriter<Integer> {

	/**
	 * Only Integer classes are allowed.
	 * 
	 */
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		if (!Integer.class.isAssignableFrom(type))
			return false;

		return true;
	}

	/**
	 * Gets the length/size of the string value of the Integer
	 */
	public long getSize(Integer t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return t == null ? 0 : String.valueOf(t).trim().length();
	}

	/**
	 * Writes the string value of the Integer to the output stream.
	 */
	public void writeTo(Integer t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		entityStream.write(t == null ? "-1".getBytes() : t.toString()
				.getBytes());
	}

}
