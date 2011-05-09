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
package com.klistret.cmdb.utility.jaxb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.klistret.cmdb.exception.InfrastructureException;

/**
 * More information about this resolver at
 * http://stackoverflow.com/questions/3558333/jaxb-schemafactory-source-order-must-follow-import-order-between-schemas/3830649#3830649
 */
public class SimpleResolver implements LSResourceResolver {
	private static final Logger logger = LoggerFactory
			.getLogger(SimpleResolver.class);

	private Set<SchemaStreamSource> schemaStreamSources;

	/**
	 * Constructor accepts a set of SchemaSource objects (which are really
	 * extensions of the Source class)
	 * 
	 * @param streams
	 */
	public SimpleResolver(Set<SchemaStreamSource> schemaStreamSources) {
		this.schemaStreamSources = schemaStreamSources;
	}

	/**
	 * Resource name
	 * 
	 * @param path
	 * @return
	 */
	private String getResourceName(String path) {
		int lastIndexOf = path.lastIndexOf("/");

		if (lastIndexOf == -1)
			return path;

		if (lastIndexOf == path.length())
			return null;

		return path.substring(lastIndexOf + 1, path.length());
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI,
			String publicId, String systemId, String baseURI) {
		DOMImplementationRegistry registry;
		try {
			/**
			 * Still unsure about the different types of implementations that
			 * need to be loaded
			 */
			registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry
					.getDOMImplementation("LS 3.0");

			LSInput ret = domImplementationLS.createLSInput();

			/**
			 * Spin through the available streams to find keyed by systemId and
			 * namespace a match to resolve
			 */
			for (SchemaStreamSource schemaStreamSource : schemaStreamSources) {
				if (getResourceName(schemaStreamSource.getSystemId()).equals(
						getResourceName(systemId))
						&& schemaStreamSource.getXSModel().getNamespaces()
								.contains(namespaceURI)) {

					logger.debug("Resolved systemid [{}] with namespace [{}]",
							getResourceName(systemId), namespaceURI);

					URL url = new URL(schemaStreamSource.getSystemId());
					URLConnection uc = url.openConnection();

					/**
					 * InputStream must a newly created (ie. not previously
					 * read)
					 */
					ret.setByteStream(uc.getInputStream());
					ret.setSystemId(systemId);
					return ret;
				}
			}

		} catch (ClassCastException e) {
			logger.error(e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		} catch (InstantiationException e) {
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		logger.error("No stream found for system id [{}]", systemId);
		throw new InfrastructureException(String.format(
				"No stream found for system id [%s]", systemId));
	}

}
