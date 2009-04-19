package com.klistret.cmdb.pojo;

public class PropertyCriterion {

	public enum Comparisons {
		Equal, NotEqual, LessThan, LessThanOrEqualTo, GreaterThan, GreaterThanOrEqualTo, Matches, Contains, StartsWith, EndsWith
	};

	private String path;

	private String value;

	private Comparisons comparison;

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
}
