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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaTypeHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(SchemaTypeHelper.class);

	/**
	 * 
	 * @param classname
	 * @return
	 */
	public static SchemaType[] getBaseSchemaTypes(String classname) {
		SchemaType schemaType = XmlBeans.getContextTypeLoader()
				.typeForClassname(classname);

		if (schemaType == null) {
			logger
					.debug(
							"SchemaType by classname [{}] not found using ContextTypeLoader",
							classname);
			return null;
		}

		return getBaseSchemaTypes(schemaType);
	}

	/**
	 * 
	 * @param qname
	 * @return
	 */
	public static SchemaType[] getBaseSchemaTypes(QName qname) {
		// return SchemaType from the context type loader
		SchemaType schemaType = XmlBeans.getContextTypeLoader().findType(qname);

		if (schemaType == null) {
			logger
					.debug(
							"SchemaType by QName [{}] not found using ContextTypeLoader",
							qname.toString());
			return null;
		}

		return getBaseSchemaTypes(schemaType);
	}

	/**
	 * 
	 * @param schemaType
	 * @return
	 */
	private static SchemaType[] getBaseSchemaTypes(SchemaType schemaType) {
		// base type cache
		ArrayList<SchemaType> baseSchemaTypes = new ArrayList<SchemaType>();

		// iterate through super base types excluding XmlBeans built-in types
		SchemaType superSchemaType = schemaType.getBaseType();
		while (superSchemaType != null && !superSchemaType.isBuiltinType()) {
			baseSchemaTypes.add(superSchemaType);
			superSchemaType = superSchemaType.getBaseType();
		}

		return (SchemaType[]) baseSchemaTypes.toArray(new SchemaType[0]);
	}

	public static SchemaType[] getExtendSchemaTypes(SchemaType schemaType,
			boolean filterAbstracts) {
		List<SchemaType> extendingSchemaTypes = new ArrayList<SchemaType>();

		SchemaTypeSystem schemaTypeSystem = schemaType.getTypeSystem();

		/**
		 * loop through all registered document types with the current type
		 * system
		 */
		for (SchemaType documentType : schemaTypeSystem.documentTypes()) {
			/**
			 * collect only documents with root elements
			 */
			QName elementQName = documentType.getDocumentElementName();
			if (elementQName == null)
				continue;

			SchemaType elementSchemaType = XmlBeans.getContextTypeLoader()
					.findType(elementQName);

			/**
			 * if the element has the passed schema type as a base type then
			 * cache (depending on the filter argument) and recursively search
			 */
			if (schemaType.getName().equals(
					elementSchemaType.getBaseType().getName())) {
				if (!filterAbstracts && !elementSchemaType.isAbstract()) {
					extendingSchemaTypes.add(elementSchemaType);
				}
				extendingSchemaTypes
						.addAll(getExtendSchemaTypes(elementSchemaType));
			}

		}

		return null;
	}

	public static SchemaType[] getExtendSchemaTypes(SchemaType schemaType) {
		return null;
	}
}
