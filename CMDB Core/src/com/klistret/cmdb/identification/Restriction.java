/**
 ** This file is part of Klistret. Klistret is free software: you can
 ** redistribute it and/or modify it under the terms of the GNU General
 ** Public License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.

 ** Klistret is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 ** General Public License for more details. You should have received a
 ** copy of the GNU General Public License along with Klistret. If not,
 ** see <http://www.gnu.org/licenses/>
 */

package com.klistret.cmdb.identification;

public class Restriction {

	private String qname;

	private String propertyLocationPath;

	private String equals;

	private String notEquals;

	private String lessThan;

	private String greaterThan;

	private String lessThanOrEqualTo;

	private String greaterThanOrEqualTo;

	public String getQName() {
		return this.qname;
	}

	public void setQName(String qname) {
		this.qname = qname;
	}

	public String getPropertyLocationPath() {
		return this.propertyLocationPath;
	}

	public void setPropertyLocationPath(String propertyLocationPath) {
		this.propertyLocationPath = propertyLocationPath;
	}

	public String getEquals() {
		return this.equals;
	}

	public void setEquals(String equals) {
		this.equals = equals;
	}

	public String getNotEquals() {
		return this.notEquals;
	}

	public void setNotEquals(String notEquals) {
		this.notEquals = notEquals;
	}

	public void setLessThan(String lessThan) {
		this.lessThan = lessThan;
	}

	public String getLessThan() {
		return lessThan;
	}

	public void setGreaterThan(String greaterThan) {
		this.greaterThan = greaterThan;
	}

	public String getGreaterThan() {
		return greaterThan;
	}

	public void setLessThanOrEqualTo(String lessThanOrEqualTo) {
		this.lessThanOrEqualTo = lessThanOrEqualTo;
	}

	public String getLessThanOrEqualTo() {
		return lessThanOrEqualTo;
	}

	public void setGreaterThanOrEqualTo(String greaterThanOrEqualTo) {
		this.greaterThanOrEqualTo = greaterThanOrEqualTo;
	}

	public String getGreaterThanOrEqualTo() {
		return greaterThanOrEqualTo;
	}
}
