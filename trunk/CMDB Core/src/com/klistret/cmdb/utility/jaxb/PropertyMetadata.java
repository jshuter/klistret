package com.klistret.cmdb.utility.jaxb;

import javax.xml.namespace.QName;

public class PropertyMetadata {
	public enum TypeCategory {
		SimpleElement, ComplexElement, Attribute
	};

	protected QName name;

	protected TypeCategory typeCategory;

	protected Boolean required;

	protected Boolean nillable;

	protected QName type;
	
	protected Integer maxOccurs;
	
	protected Integer minOccurs;
	
	protected Boolean maxOccursUnbounded;
	
	protected String annotation;

	public QName getName() {
		return name;
	}

	public TypeCategory getTypeCategory() {
		return typeCategory;
	}

	public Boolean isRequired() {
		return required;
	}

	public Boolean isNillable() {
		return nillable;
	}

	public QName getType() {
		return type;
	}
	
	public String getAnnotation() {
		return annotation;
	}
}
