package com.klistret.cmdb.pojo;

import javax.xml.namespace.QName;

public class ElementMetadata {

	private QName qname;

	private Class<?> elementClass;

	public QName getQName() {
		return this.qname;
	}

	public Class<?> getElementClass() {
		return this.elementClass;
	}

	public boolean isAbstract() {
		return false;
	}

	public boolean isEntity() {
		return false;
	}

	public ElementMetadata[] getElements() {
		return null;
	}

	public String[] getAttributes() {
		return null;
	}

	public ElementMetadata[] getExtending() {
		return null;
	}

	public ElementMetadata[] getExtended() {
		return null;
	}
}
