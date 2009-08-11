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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

/**
 * XmlBeans offers a ton of ways to get at schema information but information
 * about base types, extending types, the root document type, and the document
 * wrapper for global schema elements was lacking.
 * 
 * @author Matthew Young
 * 
 */
public class SchemaTypeHelper {
	private static final Logger logger = LoggerFactory
			.getLogger(SchemaTypeHelper.class);

	/**
	 * Transform full java class path to XmlObject Schema Type
	 * 
	 * @param classname
	 *            Full Java class path
	 * @return SchemaType
	 */
	public static SchemaType getSchemaType(String classname) {
		SchemaType schemaType = XmlBeans.getContextTypeLoader()
				.typeForClassname(classname);

		if (schemaType == null) {
			logger
					.error(
							"SchemaType by classname [{}] not found using ContextTypeLoader",
							classname);
			throw new ApplicationException(
					String
							.format(
									"SchemaType by classname [%s] not found using ContextTypeLoader",
									classname));
		}

		return schemaType;
	}

	/**
	 * Transform qname to XmlObject Schema Type
	 * 
	 * @param qname
	 * @return SchemaType
	 */
	public static SchemaType getSchemaType(QName qname) {
		SchemaType schemaType = XmlBeans.getContextTypeLoader().findType(qname);

		if (schemaType == null) {
			logger
					.error(
							"SchemaType by QName [{}] not found using ContextTypeLoader",
							qname.toString());
			throw new ApplicationException(
					String
							.format(
									"SchemaType by QName [%s] not found using ContextTypeLoader",
									qname.toString()));
		}

		return schemaType;
	}

	/**
	 * Get base types (ancestor types extended by the passed java class
	 * representing a XmlObject schema type) in order of extension
	 * 
	 * @param classname
	 * @return SchemaType[]
	 */
	public static SchemaType[] getBaseSchemaTypes(String classname) {

		return getBaseSchemaTypes(getSchemaType(classname));
	}

	/**
	 * Get base types (ancestor types extended by the passed QName representing
	 * a XmlObject schema type) in order of extension
	 * 
	 * @param qname
	 * @return SchemaType[]
	 */
	public static SchemaType[] getBaseSchemaTypes(QName qname) {
		return getBaseSchemaTypes(getSchemaType(qname));
	}

	/**
	 * Loops through the base type defined in the SchemaType until the super
	 * type is null (excluding all built in types to XmlBeans along the way)
	 * adding each to an array list.
	 * 
	 * @param schemaType
	 *            Document types only
	 * @return SchemaType[]
	 */
	private static SchemaType[] getBaseSchemaTypes(SchemaType schemaType) {
		if (schemaType.getTypeSystem().findDocumentType(schemaType.getName()) == null) {
			String name = schemaType.getName() == null ? schemaType
					.getFullJavaName() : schemaType.getName().toString();

			logger.error("SchemaType [{}] is not a document type", name);
			throw new ApplicationException(String.format(
					"SchemaType [%s] is not a document type", name));
		}

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

	/**
	 * Get extending types (by the passed java class representing a XmlObject
	 * schema type) in order of extension with the option of filtering out
	 * abstractions
	 * 
	 * @param classname
	 * @param filterAbstracts
	 * @return SchemaType[]
	 */
	public static SchemaType[] getExtendSchemaTypes(String classname,
			boolean filterAbstracts) {
		return getExtendSchemaTypes(getSchemaType(classname), filterAbstracts);
	}

	/**
	 * Get extending types (by the passed java class representing a XmlObject
	 * schema type) in order of extension
	 * 
	 * @param classname
	 * @return SchemaType[]
	 */
	public static SchemaType[] getExtendSchemaTypes(String classname) {
		return getExtendSchemaTypes(getSchemaType(classname), false);
	}

	/**
	 * Get extending types (by the passed QName representing a XmlObject schema
	 * type) in order of extension with the option of filtering out abstractions
	 * 
	 * @param qname
	 * @param filterAbstracts
	 * @return SchemaType[]
	 */
	public static SchemaType[] getExtendSchemaTypes(QName qname,
			boolean filterAbstracts) {
		return getExtendSchemaTypes(getSchemaType(qname), filterAbstracts);
	}

	/**
	 * Get extending types (by the passed QName representing a XmlObject schema
	 * type) in order of extension
	 * 
	 * @param qname
	 * @return SchemaType[]
	 */
	public static SchemaType[] getExtendSchemaTypes(QName qname) {
		return getExtendSchemaTypes(getSchemaType(qname), false);
	}

	/**
	 * Loops through all of the document types registered to the XmlBeans type
	 * system looking to see if the current schema type is a subclass (positive
	 * hits cause the method to act recursively)
	 * 
	 * @param schemaType
	 *            Document types only
	 * @param filterAbstracts
	 * @return
	 */
	private static SchemaType[] getExtendSchemaTypes(SchemaType schemaType,
			boolean filterAbstracts) {
		if (schemaType.getTypeSystem().findDocumentType(schemaType.getName()) == null) {
			String name = schemaType.getName() == null ? schemaType
					.getFullJavaName() : schemaType.getName().toString();

			logger.error("SchemaType [{}] is not a document type", name);
			throw new ApplicationException(String.format(
					"SchemaType [%s] is not a document type", name));
		}

		List<SchemaType> extendingSchemaTypes = new ArrayList<SchemaType>();

		/**
		 * loop through all registered document types with the current type
		 * system
		 */
		SchemaTypeSystem schemaTypeSystem = schemaType.getTypeSystem();
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
				SchemaGlobalElement sge = XmlBeans.getContextTypeLoader()
						.findElement(elementSchemaType.getName());

				if (!(filterAbstracts && sge.isAbstract())) {
					extendingSchemaTypes.add(elementSchemaType);
				}

				extendingSchemaTypes.addAll(Arrays.asList(getExtendSchemaTypes(
						elementSchemaType, filterAbstracts)));
			}

		}

		return extendingSchemaTypes.toArray(new SchemaType[0]);
	}

	/**
	 * Get root document type (by the passed java class representing a XmlObject
	 * schema type) which global elements like Elements or Relations.
	 * 
	 * @param classname
	 * @return SchemaType
	 */
	public static SchemaType getRootDocumentType(String classname) {
		return getRootDocumentType(getSchemaType(classname));
	}

	/**
	 * Get root document type (by the passed QName representing a XmlObject
	 * schema type) which global elements like Elements or Relations.
	 * 
	 * @param qname
	 * @return SchemaType
	 */
	public static SchemaType getRootDocumentType(QName qname) {
		return getRootDocumentType(getSchemaType(qname));
	}

	/**
	 * Loops through the base schema types in reverse looking for a global
	 * element with the same QName (exception is if the passed schema type is a
	 * root document type)
	 * 
	 * @param schemaType
	 * @return SchemaType
	 */
	private static SchemaType getRootDocumentType(SchemaType schemaType) {
		SchemaType[] baseSchemaTypes = getBaseSchemaTypes(schemaType);

		for (int index = baseSchemaTypes.length - 1; index >= 0; index--) {
			SchemaGlobalElement sge = XmlBeans.getContextTypeLoader()
					.findElement(baseSchemaTypes[index].getName());
			if (sge != null)
				return baseSchemaTypes[index];
		}

		if (XmlBeans.getContextTypeLoader().findElement(schemaType.getName()) != null)
			return schemaType;

		return null;
	}

	/**
	 * Get document class (contains no QName and can not be loaded through the
	 * XmlBeans type system)
	 * 
	 * @param classname
	 * @return SchemaType
	 */
	public static SchemaType getDocument(String classname) {
		return getDocument(getSchemaType(classname));
	}

	/**
	 * Get document class (contains no QName and can not be loaded through the
	 * XmlBeans type system)
	 * 
	 * @param qname
	 * @return SchemaType
	 */
	public static SchemaType getDocument(QName qname) {
		return getDocument(getSchemaType(qname));
	}

	/**
	 * Document classes can not be loaded from the XmlBeans type system nor
	 * found via the SchemaType class but the type system does contain a list of
	 * document types (not to be confused with global schema elements) which has
	 * references to their document elements that can be matched to the passed
	 * schema type.
	 * 
	 * @param schemaType
	 * @return SchemaType
	 */
	private static SchemaType getDocument(SchemaType schemaType) {
		if (schemaType.getTypeSystem().findDocumentType(schemaType.getName()) == null) {
			String name = schemaType.getName() == null ? schemaType
					.getFullJavaName() : schemaType.getName().toString();

			logger.error("SchemaType [{}] is not a document type", name);
			throw new ApplicationException(String.format(
					"SchemaType [%s] is not a document type", name));
		}

		SchemaType[] documentTypes = schemaType.getTypeSystem().documentTypes();
		for (SchemaType documentType : documentTypes) {
			XmlObject xmlObject = XmlBeans.getContextTypeLoader().newInstance(
					documentType, null);
			if (schemaType.getName().equals(
					xmlObject.schemaType().getDocumentElementName()))
				return xmlObject.schemaType();
		}

		return null;
	}
}
