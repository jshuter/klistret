package com.klistret.cmdb.plugin.persistence.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Persistence", propOrder = { "criterion", "identification" })
@XmlRootElement(name = "Persistence")
public class Persistence {

	@XmlElement(name = "Criterion")
	protected List<Criterion> criterion;

	@XmlElement(name = "Identification")
	protected List<Identification> identification;

	public List<Criterion> getCriterion() {
		if (criterion == null)
			criterion = new ArrayList<Criterion>();

		return criterion;
	}

	public void setCriterion(List<Criterion> criterion) {
		this.criterion = criterion;
	}

	public List<Identification> getIdentification() {
		if (identification == null)
			identification = new ArrayList<Identification>();

		return identification;
	}

	public void setIdentification(List<Identification> identification) {
		this.identification = identification;
	}
}
