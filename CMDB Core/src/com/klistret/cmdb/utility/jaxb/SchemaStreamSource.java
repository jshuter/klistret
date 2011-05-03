package com.klistret.cmdb.utility.jaxb;

import javax.xml.transform.stream.StreamSource;

import org.reflections.util.Utils;

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSModel;

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
