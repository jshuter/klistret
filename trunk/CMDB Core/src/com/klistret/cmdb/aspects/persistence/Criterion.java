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

package com.klistret.cmdb.aspects.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Criterion has a unique (currently no constraint as available with XSD
 * http://msdn.microsoft.com/en-us/library/ms256146.aspx) name over a group of
 * XPath expressions (which do not need to utilize the mapping declaration).
 * 
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Criterion", propOrder = { "expressions" })
@XmlRootElement(name = "Criterion", namespace = "http://www.klistret.com/cmdb/aspects/persistence")
public class Criterion {

	@XmlElement(name = "Expressions", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
	protected List<String> expressions;

	@XmlAttribute(name = "Name", required = true)
	protected String name;

	public List<String> getExpressions() {
		if (expressions == null) {
			expressions = new ArrayList<String>();
		}
		return this.expressions;
	}

	public void setExpressions(List<String> expressions) {
		this.expressions = expressions;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

}
