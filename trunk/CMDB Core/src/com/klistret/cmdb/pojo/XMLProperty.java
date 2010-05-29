package com.klistret.cmdb.pojo;

import javax.xml.namespace.QName;

public abstract class XMLProperty {

	private String name;

	private QName type;

	public String getName() {
		return name;
	}

	public QName getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(QName type) {
		this.type = type;
	}
}
