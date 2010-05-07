package com.klistret.cmdb.pojo;

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

	private NamespaceNode[] namespaces;

	private QName typeName;

	private ElementNode parent;

	private boolean isAbstract = false;

	private boolean isFinal = false;

	private boolean isEntity = false;

	private boolean isSimpleType = false;

	private boolean isNilled = false;

	private ElementNode[] children;

	private AttributeNode[] attributes;

	private ElementNode[] extending;

	private ElementNode extended;

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
	 * Set of namespaces contain one namespace node for each distinct namespace
	 * that is declared explicitly on the element
	 * 
	 * @return
	 */
	public NamespaceNode[] getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(NamespaceNode[] namespaces) {
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
	public ElementNode[] getChildren() {
		return children;
	}

	public void setChildren(ElementNode[] children) {
		this.children = children;
	}

	/**
	 * List of attributes to ElemNode
	 * 
	 * @return
	 */
	public AttributeNode[] getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeNode[] attributes) {
		this.attributes = attributes;
	}

	/**
	 * List of element extending ElemNode
	 * 
	 * @return
	 */
	public ElementNode[] getExtending() {
		return extending;
	}

	public void setExtending(ElementNode[] extending) {
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
}
