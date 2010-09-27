package com.klistret.cmdb.utility.jaxb;

import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import org.reflections.util.Utils;

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSModel;

public class SchemaSource extends StreamSource {

	private String resource;

	private URL url;

	private XSModel xsModel;

	public SchemaSource(String resource) {
		super(Utils.getContextClassLoader().getResource(resource).toString());

		this.resource = resource;
		this.url = Utils.getContextClassLoader().getResource(resource);
		this.xsModel = new XMLSchemaLoader().loadURI(url.toString());
	}

	public String getResource() {
		return resource;
	}
	
	public String getResourceName() {
		return getResourceName(resource);
	}

	public String getResourceName(String path) {
		int lastIndexOf = path.lastIndexOf("/");

		if (lastIndexOf == -1)
			return path;

		if (lastIndexOf == path.length())
			return null;

		return path.substring(lastIndexOf + 1, path.length());
	}

	public XSModel getXSModel() {
		return xsModel;
	}

	public String getTargetNamespace() {
		if (xsModel.getNamespaces() != null)
			return xsModel.getNamespaces().item(0);

		return null;
	}
}
