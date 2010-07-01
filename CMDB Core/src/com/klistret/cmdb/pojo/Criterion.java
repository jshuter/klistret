package com.klistret.cmdb.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Criterion", propOrder = { "expressions" })
public class Criterion {

	@XmlElement(name = "Expressions", namespace = "http://www.klistret.com/cmdb/ci/persistence", required = true)
	protected List<String> expressions;
	@XmlAttribute(name = "Name")
	protected String name;

	/**
	 * Gets the value of the expressions property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the expressions property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExpressions().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getExpressions() {
		if (expressions == null) {
			expressions = new ArrayList<String>();
		}
		return this.expressions;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Sets the value of the expressions property.
	 * 
	 * @param expressions
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExpressions(List<String> expressions) {
		this.expressions = expressions;
	}
}
