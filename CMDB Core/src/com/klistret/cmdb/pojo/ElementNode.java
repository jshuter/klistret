package com.klistret.cmdb.pojo;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Based on http://www.w3.org/TR/2000/WD-query-datamodel-20000511 and
 * http://www.w3.org/TR/xpath-datamodel
 * 
 * @author 40042466
 * 
 */
public class ElementNode {

	private QName name;

	private String className;

	private String propertyName;

	private List<NamespaceNode> namespaces = new LinkedList<NamespaceNode>();

	private QName typeName;

	private ElementNode parent;

	private Boolean isAbstract;

	private Boolean isFinal;

	private Boolean isEntity;

	private Boolean isSimpleType;

	private Boolean isXmlRootElement;

	private Boolean isNilled;

	private List<ElementNode> children = new LinkedList<ElementNode>();

	private List<AttributeNode> attributes = new LinkedList<AttributeNode>();

	private List<ElementNode> extending = new LinkedList<ElementNode>();

	private ElementNode extended;

	private List<ElementNode> descendents;

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
	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Element property name (non-language specific)
	 * 
	 * @return
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Set of namespaces contain one namespace node for each distinct namespace
	 * that is declared explicitly on the element
	 * 
	 * @return
	 */
	public List<NamespaceNode> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(List<NamespaceNode> namespaces) {
		this.namespaces = namespaces;
	}

	/**
	 * A Java type can be mapped to multiple XML types, but one of them is
	 * considered "primary" and used when generating a schema.
	 * 
	 * @return
	 */
	public QName getTypeName() {
		return typeName;
	}

	public void setTypeName(QName typeName) {
		this.typeName = typeName;
	}

	/**
	 * A reference to the unique parent of an ElemNode
	 * 
	 * @return
	 */
	public ElementNode getParent() {
		return parent;
	}

	public void setParent(ElementNode parent) {
		this.parent = parent;
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
	 * JPA entity
	 * 
	 * @return
	 */
	public boolean isEntity() {
		return isEntity;
	}

	public void setEntity(boolean isEntity) {
		this.isEntity = isEntity;
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
	 * Children is a list of references to ElemNode, ValueNode, PINode,
	 * CommentNode, and InfoItemNode values except XPath/SQL queries limit to
	 * only ElemNode nodes.
	 * 
	 * @return
	 */
	public List<ElementNode> getChildren() {
		return children;
	}

	public void setChildren(List<ElementNode> children) {
		this.children = children;
	}

	/**
	 * List of attributes to ElemNode
	 * 
	 * @return
	 */
	public List<AttributeNode> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeNode> attributes) {
		this.attributes = attributes;
	}

	/**
	 * List of element extending ElemNode
	 * 
	 * @return
	 */
	public List<ElementNode> getExtending() {
		return extending;
	}

	public void setExtending(List<ElementNode> extending) {
		this.extending = extending;
	}

	/**
	 * List of elemented which ElemNode extends
	 * 
	 * @return
	 */
	public ElementNode getExtended() {
		return extended;
	}

	public void setExtended(ElementNode extended) {
		this.extended = extended;
	}

	public List<ElementNode> getDescendents() {
		return null;
	}

	public String toString() {
		return String
				.format(
						"element node name [%s], xml schema type [%s], class name [%s]",
						name, typeName, className);
	}

	/**
	 * 
	 */
	public boolean equals(Object other) {
		if (other instanceof ElementNode) {
			if (((ElementNode) other).getName() != null && getName() != null
					&& ((ElementNode) other).getName().equals(getName()))
				return true;
		}

		return false;
	}

}
