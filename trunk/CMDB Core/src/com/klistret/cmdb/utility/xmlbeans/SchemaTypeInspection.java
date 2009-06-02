package com.klistret.cmdb.utility.xmlbeans;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.klistret.cmdb.exception.ApplicationException;

public class SchemaTypeInspection {

	private Hashtable<QName, SchemaType[]> schemaTypeTree = new Hashtable<QName, SchemaType[]>();

	public SchemaTypeInspection(QName qname) {
		SchemaType schemaType = XmlBeans.getContextTypeLoader().findType(qname);

		if (schemaType == null)
			throw new ApplicationException(String.format(
					"qname [%s] does not exist in the xmlbeans type loader",
					qname.toString()));

		cacheExtendingTypes(schemaType);
	}

	public SchemaTypeInspection(String namespaceURI, String localPart) {
		this(new QName(namespaceURI, localPart));
	}

	private void cacheExtendingTypes(SchemaType schemaType) {
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

	private SchemaType[] purgeAbstract(SchemaType[] schemaTypes,
			boolean isAbstract) {
		ArrayList<SchemaType> results = new ArrayList<SchemaType>();
		for (SchemaType schemaType : schemaTypes) {
			if (isAbstract == XmlBeans.getContextTypeLoader().findElement(
					schemaType.getName()).isAbstract()) {
				results.add(schemaType);
			}
		}

		return (SchemaType[]) results.toArray(new SchemaType[0]);
	}

	public SchemaType[] getTypes() {
		ArrayList<SchemaType> results = new ArrayList<SchemaType>();

		for (Map.Entry<QName, SchemaType[]> map : schemaTypeTree.entrySet())
			results.add(XmlBeans.getContextTypeLoader().findType(map.getKey()));

		return (SchemaType[]) results.toArray(new SchemaType[0]);
	}

	public SchemaType[] getTypes(boolean isAbstract) {
		return purgeAbstract(getTypes(), isAbstract);
	}

	public SchemaType[] getExtendingTypes(QName qname) {
		return schemaTypeTree.get(qname);
	}

	public SchemaType[] getExtendingTypes(QName qname, boolean isAbstract) {
		return purgeAbstract(getExtendingTypes(qname), isAbstract);
	}
}
