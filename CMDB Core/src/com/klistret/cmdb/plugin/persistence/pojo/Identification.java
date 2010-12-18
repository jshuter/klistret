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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Matthew Young
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identification", propOrder = {})
public class Identification {

	@XmlAttribute(name = "Type")
	protected String type;

	@XmlElement(name = "CriterionRule")
	protected List<CriterionRule> criterionRule;

	public String getType() {
		return type;
	}

	public List<CriterionRule> getCriterionRule() {
		if (criterionRule == null)
			criterionRule = new ArrayList<CriterionRule>();

		return criterionRule;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCriterionRule(List<CriterionRule> criterionRule) {
		this.criterionRule = criterionRule;
	}
}
