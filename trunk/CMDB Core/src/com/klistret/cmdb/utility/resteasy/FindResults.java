package com.klistret.cmdb.utility.resteasy;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

import com.klistret.cmdb.ci.pojo.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FindResults")
@Mapped(namespaceMap = {
		@XmlNsMap(namespace = "http://www.klistret.com/cmdb/pojo", jsonName = "com.klistret.cmdb.pojo"),
		@XmlNsMap(namespace = "http://www.w3.org/2001/XMLSchema-instance", jsonName = "www.w3.org.2001.XMLSchema-instance") })
public class FindResults {

	@XmlElement
	private boolean successful;

	@XmlElement
	private Integer count;

	@XmlElement
	private Collection<Element> payload;

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Collection<Element> getPayload() {
		return payload;
	}

	public void setPayload(Collection<Element> payload) {
		this.payload = payload;
	}
}
