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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Persistence rules are fairly simple. To enforce uniqueness criterion housing
 * XPath statements (expressions) identify persisted objects as unique
 * (criterion are atomic). Each criterion has a name. The rule elements connect
 * CI classes (not the POJO transport layer) to criterion and within a class the
 * criterion may be ordered (descending). Criterion may be associated to
 * abstract/parent CIs that are extended by concrete classes. Even these are
 * selected for concrete classes but ordered by ancestry. Exclusions for
 * abstract/parent rules eliminate criterion for concrete classes.
 * 
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersistenceRules", propOrder = { "criterion", "rule" })
@XmlRootElement(name = "PersistenceRules", namespace = "http://www.klistret.com/cmdb/aspects/persistence")
public class PersistenceRules {

	@XmlElement(name = "Criterion", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
	protected List<Criterion> criterion;

	@XmlElement(name = "Rule", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
	protected List<Rule> rule;

	public List<Criterion> getCriterion() {
		if (criterion == null) {
			criterion = new ArrayList<Criterion>();
		}
		return this.criterion;
	}

	public void setCriterion(List<Criterion> criterion) {
		this.criterion = criterion;
	}

	public List<Rule> getRule() {
		if (rule == null) {
			rule = new ArrayList<Rule>();
		}
		return this.rule;
	}

	public void setRule(List<Rule> rule) {
		this.rule = rule;
	}
}
