package com.klistret.cmdb.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FindQuery")
@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://www.klistret.com/cmdb/pojo", jsonName = "com.klistret.cmdb.pojo") })
public class FindQuery {

	@XmlElement
	protected String[] expressions;

	@XmlElement
	protected Integer start;

	@XmlElement
	protected Integer limit;

	public String[] getExpressions() {
		return expressions;
	}

	public void setExpressions(String[] expressions) {
		this.expressions = expressions;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
