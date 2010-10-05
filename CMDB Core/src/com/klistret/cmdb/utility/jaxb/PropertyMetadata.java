package com.klistret.cmdb.utility.jaxb;

public class PropertyMetadata {
	public enum TypeCategory { Element , Attribute };

	protected String namespace;

	protected String localName;

	protected TypeCategory typeCategory;
	
	protected Boolean referencing;
	
	protected Boolean required;

	public String getNamespace() {
		return namespace;
	}

	public String getLocalName() {
		return localName;
	}

	public TypeCategory getTypeCategory() {
		return typeCategory;
	}
	
	public Boolean isReferencing() {
		return referencing;
	}
	
	public Boolean isRequired() {
		return required;
	}
}
