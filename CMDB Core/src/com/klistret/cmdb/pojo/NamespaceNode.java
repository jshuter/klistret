package com.klistret.cmdb.pojo;

/**
 * A namespace node may contain: a non-null prefix and a non-null uri; a null
 * prefix and a non-null uri; or a null prefix and a null uri. A namespace node
 * may not contain a non-null prefix and a null uri
 * 
 * @author 40042466
 * 
 */
public class NamespaceNode {

	private String prefix;

	private String uri;

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getURI() {
		return this.uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}
}
