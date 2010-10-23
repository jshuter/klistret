package com.klistret.cmdb.plugin.persistence.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CriterionRule", propOrder = {})
public class CriterionRule {

	@XmlAttribute(name = "Name", required = true)
	protected String name;

	@XmlAttribute(name = "Order")
	protected Integer order;

	public String getName() {
		return name;
	}

	public Integer getOrder() {
		return order;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
}
