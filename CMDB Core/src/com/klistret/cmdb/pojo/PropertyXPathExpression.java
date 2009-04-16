package com.klistret.cmdb.pojo;

import javax.xml.namespace.QName;

public class PropertyXPathExpression {

	public enum Comparisons {
		Equal, NotEqual, LessThan, LessThanOrEqualTo, GreaterThan, GreaterThanOrEqualTo, Matches, Contains, StartsWith, EndsWith
	};

	private QName qname;

	private String path;

	private String value;

	private Comparisons comparison;

	public QName getQName() {
		return qname;
	}

	public void setQName(QName qname) {
		this.qname = qname;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Comparisons getComparison() {
		return comparison;
	}

	public void setComparison(Comparisons comparison) {
		this.comparison = comparison;
	}

	public boolean isFunction() {
		if (comparison == Comparisons.Matches
				|| comparison == Comparisons.Contains
				|| comparison == Comparisons.StartsWith
				|| comparison == Comparisons.EndsWith) {
			return true;
		}
		return false;
	}

	public boolean isOperator() {
		return !(isFunction());
	}
}
