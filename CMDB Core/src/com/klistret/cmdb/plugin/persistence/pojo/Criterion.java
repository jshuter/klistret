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

package com.klistret.cmdb.plugin.persistence.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Matthew Young
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Criterion", propOrder = { "expression" })
@XmlRootElement(name = "Criterion")
public class Criterion {

	@XmlAttribute(name = "Name", required = true)
	protected String name;

	@XmlElement(name = "Expression")
	protected List<String> expression;

	public String getName() {
		return name;
	}

	public List<String> getExpression() {
		if (expression == null)
			expression = new ArrayList<String>();

		return expression;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExpression(List<String> expression) {
		this.expression = expression;
	}
}
