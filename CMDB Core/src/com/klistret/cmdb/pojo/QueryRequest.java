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

	/**
	 * Gets the value of the start property.
	 * 
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the value of the start property.
	 * 
	 */
	public void setStart(int value) {
		this.start = value;
	}

	/**
	 * Gets the value of the limit property.
	 * 
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the value of the limit property.
	 * 
	 */
	public void setLimit(int value) {
		this.limit = value;
	}

	/**
	 * Gets the value of the expressions property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the expressions property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExpressions().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getExpressions() {
		if (expressions == null) {
			expressions = new ArrayList<String>();
		}
		return this.expressions;
	}

	/**
	 * Sets the value of the expressions property.
	 * 
	 * @param expressions
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExpressions(List<String> expressions) {
		this.expressions = expressions;
	}
}
