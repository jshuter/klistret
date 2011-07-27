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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Lists of strings are not automatically handled by the Resteasy/JAXB framework
 * so this MessageBodyWriter simply puts the strings into a single string
 * delimitated by commas. Another solution would have been to return JAXBString
 * objects in a list by the called service but this is too intrusive.
 * 
 * @author Matthew Young
 * 
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class ListStringMessageBodyWriter implements
		MessageBodyWriter<List<String>> {

	/**
	 * The passed type has to be a List with one typed argument which is a
	 * String otherwise the method returns false and disables this message body
	 * writer.
	 * 
	 * @return boolean
	 */
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		if (!List.class.isAssignableFrom(type))
			return false;

		if (!(genericType instanceof ParameterizedType))
			return false;

		Type[] typeArguments = ((ParameterizedType) genericType)
				.getActualTypeArguments();
		if (typeArguments.length != 1)
			return false;

		Class<?> listClass = (Class<?>) typeArguments[0];
		if (!String.class.isAssignableFrom(listClass))
			return false;

		return true;
	}

	/**
	 * Get the size of the string being returned
	 * 
	 * @return long
	 */
	public long getSize(List<String> t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		if (t == null)
			return 0;

		long results = 0;
		for (String value : t)
			results = results == 0 ? value.length() : results + value.length()
					+ 2;

		return results;
	}

	/**
	 * Writes the List of Strings as a single string delimitated by commas.
	 */
	public void writeTo(List<String> t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		if (t == null)
			entityStream.write("".getBytes());

		String results = null;
		for (String value : t)
			results = results == null ? value : String.format("%s, %s",
					results, value);

		entityStream
				.write(results == null ? "".getBytes() : results.getBytes());
	}

}
