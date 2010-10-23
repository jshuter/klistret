package com.klistret.cmdb.plugin.persistence.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Criterion", propOrder = { "expression" })
@XmlRootElement(name = "Criterion")
public class Criterion {

	@XmlAttribute(name = "Name", required = true)
	protected String name;

	@XmlElement(name = "Expression")
	protected List<String> expression;

	public String getName() {
		return name;
	}

	public List<String> getExpression() {
		if (expression == null)
			expression = new ArrayList<String>();

		return expression;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExpression(List<String> expression) {
		this.expression = expression;
	}
}
