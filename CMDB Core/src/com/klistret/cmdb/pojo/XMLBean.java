package com.klistret.cmdb.pojo;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Based on http://www.w3.org/TR/2000/WD-query-datamodel-20000511 and
 * http://www.w3.org/TR/xpath-datamodel
 * 
 */
public class XMLBean {

	private QName name;

	private Class<?> clazz;

	private QName type;

	private Boolean isAbstract;

	private Boolean isFinal;

	private Boolean isSimpleType;

	private Boolean isXmlRootElement;

	private Boolean isNilled;

	private List<XMLProperty> properties = new LinkedList<XMLProperty>();

	private List<QName> extending = new LinkedList<QName>();

	private QName extended;

	/**
	 * Element qualified name value
	 * 
	 * @return
	 */
	public QName getName() {
		return this.name;
	}

	public void setName(QName name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public Class<?> getClazz() {
		return this.clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * A Java type can be mapped to multiple XML types, but one of them is
	 * considered "primary" and used when generating a schema.
	 * 
	 * @return
	 */
	public QName getType() {
		return type;
	}

	public void setType(QName type) {
		this.type = type;
	}

	/**
	 * Abstract or concrete ElemNode
	 * 
	 * @return
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * Either primitive (e.g., string, boolean, float, double, ID, IDREF) or
	 * derived (e.g., language, NMTOKEN, long, etc., or user defined)
	 * 
	 * @return
	 */
	public boolean isSimpleType() {
		return isSimpleType;
	}

	public void setSimpleType(boolean isSimpleType) {
		this.isSimpleType = isSimpleType;
	}

	/**
	 * Earmarked XML root element (annotation)
	 * 
	 * @return
	 */
	public boolean isXmlRootElement() {
		return isXmlRootElement;
	}

	public void setXmlRootElement(boolean isXmlRootElement) {
		this.isXmlRootElement = isXmlRootElement;
	}

	/**
	 * If the nilled property is true, then the children property must not
	 * contain Element Nodes or Text Nodes
	 * 
	 * @return
	 */
	public boolean isNilled() {
		return isNilled;
	}

	public void setNilled(boolean isNilled) {
		this.isNilled = isNilled;
	}

	/**
	 * List of attributes to ElemNode
	 * 
	 * @return
	 */
	public List<XMLProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<XMLProperty> properties) {
		this.properties = properties;
	}

	/**
	 * List of element extending ElemNode
	 * 
	 * @return
	 */
	public List<QName> getExtending() {
		return extending;
	}

	public void setExtending(List<QName> extending) {
		this.extending = extending;
	}

	/**
	 * List of elemented which ElemNode extends
	 * 
	 * @return
	 */
	public QName getExtended() {
		return extended;
	}

	public void setExtended(QName extended) {
		this.extended = extended;
	}

	public List<XMLBean> getDescendents() {
		return null;
	}

	public String toString() {
		return String
				.format(
						"element node name [%s], class name [%s], extends [%s], extending %d",
						name, clazz.getName(), extended, extending.size());
	}

	/**
	 * 
	 */
	public boolean equals(Object other) {
		if (other instanceof XMLBean) {
			if (((XMLBean) other).getName() != null && getName() != null
					&& ((XMLBean) other).getName().equals(getName()))
				return true;
		}

		return false;
	}

}
