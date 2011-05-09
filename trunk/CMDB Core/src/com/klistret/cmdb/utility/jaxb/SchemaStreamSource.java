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

import javax.xml.transform.stream.StreamSource;

import org.reflections.util.Utils;

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSModel;

/**
 * Adds a constructor accepting strings to the StreamSource class and helper
 * method to get the XSModel
 * 
 * @author Matthew Young
 * 
 */
public class SchemaStreamSource extends StreamSource {

	private XSModel xsModel;

	public SchemaStreamSource(String resource) {
		super(Utils.getContextClassLoader().getResource(resource).toString());
	}

	public XSModel getXSModel() {
		if (xsModel == null) {
			xsModel = new XMLSchemaLoader().loadURI(getSystemId());
		}

		return xsModel;
	}
}
