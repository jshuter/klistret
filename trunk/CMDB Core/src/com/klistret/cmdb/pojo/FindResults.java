package com.klistret.cmdb.pojo;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FindResults")
@Mapped(namespaceMap = {
    @XmlNsMap(namespace = "http://www.klistret.com/cmdb/pojo", jsonName = "com.klistret.cmdb.pojo")
})
public class FindResults {

	@XmlElement
	private boolean successful;

	@XmlElement
	private Integer count;

	@XmlElement
	private Collection<?> payload;

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

	public Collection<?> getPayload() {
		return payload;
	}

	public void setPayload(Collection<?> payload) {
		this.payload = payload;
	}
}
