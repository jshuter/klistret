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
import javax.xml.bind.annotation.XmlType;

/**
 * Simple mapping of criterion to a class. Order is within the criterion for the
 * class. Exclusions are against extending classes so a rule is not returned for
 * the excluded class.
 * 
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rule", propOrder = { "exclusions", "classname", "criterion" })
public class Rule {

	@XmlElement(name = "Exclusions", namespace = "http://www.klistret.com/cmdb/aspects/persistence")
	protected List<String> exclusions;

	@XmlElement(name = "Classname", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
	protected String classname;

	@XmlElement(name = "Criterion", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
	protected String criterion;

	@XmlAttribute(name = "Order")
	protected Integer order;

	public List<String> getExclusions() {
		if (exclusions == null) {
			exclusions = new ArrayList<String>();
		}
		return this.exclusions;
	}

	public void setExclusions(List<String> exclusions) {
		this.exclusions = exclusions;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getCriterion() {
		return criterion;
	}

	public void setCriterion(String value) {
		this.criterion = value;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer value) {
		this.order = value;
	}

}
