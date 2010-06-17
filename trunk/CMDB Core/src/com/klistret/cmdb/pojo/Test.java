package com.klistret.cmdb.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "test")
public class Test {

	@XmlElement
	private String[] expressions;

	public String[] getExpressions() {
		return expressions;
	}
}
