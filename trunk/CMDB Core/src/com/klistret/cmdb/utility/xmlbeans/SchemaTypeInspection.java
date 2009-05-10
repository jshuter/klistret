package com.klistret.cmdb.utility.xmlbeans;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.klistret.cmdb.exception.ApplicationException;

public class SchemaTypeInspection {

	private static Hashtable<QName, SchemaType[]> schemaTypeTree = new Hashtable<QName, SchemaType[]>();

	public static QName baseQName = new QName("http://www.klistret.com/cmdb",
			"Base");

	static {
		// get base schema type (abstract root complex type)
		SchemaType baseSchemaType = XmlBeans.getContextTypeLoader().findType(
				baseQName);

		if (baseSchemaType == null)
			throw new ApplicationException(String.format(
					"qname [%s] does not exist in the xmlbeans type loader",
					baseQName.toString()));

		cacheExtendingTypes(baseSchemaType);
	}

	private static void cacheExtendingTypes(SchemaType schemaType) {
		ArrayList<SchemaType> extendingTypes = new ArrayList<SchemaType>();

		// get schema type system
		SchemaTypeSystem schemaTypeSystem = schemaType.getTypeSystem();
		for (SchemaType documentType : schemaTypeSystem.documentTypes()) {
			// find passed type's root element and qname
			QName candidateQName = documentType.getDocumentElementName();
			if (candidateQName == null)
				continue;

			// load the root element schema type
			SchemaType candidate = XmlBeans.getContextTypeLoader().findType(
					candidateQName);

			// if the candidate's base (parent) schemaType equals the passed
			// then add the candidate's schemaType to the results plus
			// recursively execute this method again
			if (schemaType.getName().equals(candidate.getBaseType().getName())) {
				extendingTypes.add(candidate);
				cacheExtendingTypes(candidate);
			}

		}

		// add extending types to the cache
		SchemaType[] results = (SchemaType[]) extendingTypes
				.toArray(new SchemaType[0]);
		schemaTypeTree.put(schemaType.getName(), results);
	}

	public static SchemaType[] getExtendingTypes(QName qname) {
		return schemaTypeTree.get(qname);
	}

	public static SchemaType[] getBaseExtendingTypes() {
		return getExtendingTypes(baseQName);
	}

	public static SchemaType[] getAncestorTypes() {
		return null;
	}

	public static SchemaType[] getDecendentTypes() {
		return null;
	}
}
