package com.klistret.cmdb.utility.jaxb;

public class PropertyMetadata {
	public enum TypeCategory {
		SimpleElement, ComplexElement, Attribute
	};

	protected String namespace;

	protected String localName;

	protected TypeCategory typeCategory;

	protected Boolean required;

	protected Boolean nillable;

	protected String typeNamespace;

	protected String typeLocalName;

	public String getNamespace() {
		return namespace;
	}

	public String getLocalName() {
		return localName;
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

	public String getTypeNamespace() {
		return typeNamespace;
	}

	public String getTypeLocalName() {
		return typeLocalName;
	}
}
