package com.klistret.cmdb.plugin.persistence.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identification", propOrder = {})
public class Identification {

	@XmlAttribute(name = "Type")
	protected String type;

	@XmlElement(name = "CriterionRule")
	protected List<CriterionRule> criterionRule;

	public String getType() {
		return type;
	}

	public List<CriterionRule> getCriterionRule() {
		if (criterionRule == null)
			criterionRule = new ArrayList<CriterionRule>();

		return criterionRule;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCriterionRule(List<CriterionRule> criterionRule) {
		this.criterionRule = criterionRule;
	}
}
