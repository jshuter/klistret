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

package com.klistret.cmdb.utility.xmlbeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.klistret.cmdb.exception.ApplicationException;

public class SchemaTypeHelper {

	private Hashtable<QName, SchemaType[]> cache = new Hashtable<QName, SchemaType[]>();

	private static int extendingDepth = 2;

	public SchemaTypeHelper() {

	}

	public SchemaType[] getExtendingSchemaTypes(QName qname) {
		SchemaType[] results = cache.get(qname);

		if (results == null) {
			// allocate a result space
			ArrayList<SchemaType> extensions = new ArrayList<SchemaType>(
					extendingDepth);

			// get parent schemaType
			SchemaType parent = XmlBeans.getContextTypeLoader().findType(qname);

			if (parent == null)
				throw new ApplicationException(
						String
								.format(
										"qname [%s] does not exist in the xmlbeans type loader",
										qname.toString()));

			SchemaTypeSystem sTypeSystem = parent.getTypeSystem();
			for (SchemaType documentType : sTypeSystem.documentTypes()) {
				// find document's root element and that element's qname
				QName candidateQName = documentType.getDocumentElementName();
				if (candidateQName == null)
					continue;

				// load the root element's schemaType
				SchemaType candidate = XmlBeans.getContextTypeLoader()
						.findType(candidateQName);

				// if the element's schemaType equals the parent then
				// add the element's schemaType to the results
				if (parent.getName().equals(candidate.getBaseType().getName()))
					extensions.add(candidate);
			}

			// add extensions to the cache
			results = (SchemaType[]) extensions.toArray(new SchemaType[0]);
			cache.put(qname, results);
		}

		return results;
	}

	public SchemaType[] getDescendingSchemaTypes(QName qname) {
		// to-do (logical to determine a list size)
		ArrayList<SchemaType> descendents = new ArrayList<SchemaType>();

		// get direct extending schemaType
		SchemaType[] extensions = getExtendingSchemaTypes(qname);
		descendents.addAll(Arrays.asList(extensions));

		// iterate through extensions' extending schemaType
		// recursively calling this method
		for (SchemaType extension : extensions) {
			SchemaType[] children = getDescendingSchemaTypes(extension
					.getName());
			descendents.addAll(Arrays.asList(children));
		}

		return (SchemaType[]) descendents.toArray(new SchemaType[0]);
	}

}
