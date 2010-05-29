package com.klistret.cmdb.pojo;

public class XMLElementProperty extends XMLProperty {

	private Boolean valueList;

	private Boolean required;

	public Boolean isValueList() {
		return valueList;
	}

	public void setValueList(Boolean valueList) {
		this.valueList = valueList;
	}

	public Boolean isRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
}
