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

package com.klistret.cmdb.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Query carrier for RestEasy using XPath expressions. Ideally this class should
 * be XJC generated but for some unknown reason the expressions property could
 * be "bridged" property by the RestEasy framework.
 * 
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryRequest")
public class QueryRequest {

	@XmlElement(required = true)
	protected List<String> expressions;

	protected int start;

	protected int limit;

	public int getStart() {
		return start;
	}

	public void setStart(int value) {
		this.start = value;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int value) {
		this.limit = value;
	}

	public List<String> getExpressions() {
		if (expressions == null) {
			expressions = new ArrayList<String>();
		}
		return this.expressions;
	}

	public void setExpressions(List<String> expressions) {
		this.expressions = expressions;
	}
}
