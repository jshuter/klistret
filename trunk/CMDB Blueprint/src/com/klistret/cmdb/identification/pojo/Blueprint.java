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
package com.klistret.cmdb.identification.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Blueprint", propOrder = { "criterion", "identification" })
@XmlRootElement(name = "Blueprint")
public class Blueprint {

	@XmlElement(name = "Criterion")
	protected List<Criterion> criterion;
	
	@XmlElement(name = "Identification")
	protected List<Identification> identification;

	public List<Criterion> getCriterion() {
		if (criterion == null)
			criterion = new ArrayList<Criterion>();

		return criterion;
	}

	public void setCriterion(List<Criterion> criterion) {
		this.criterion = criterion;
	}

	public List<Identification> getIdentification() {
		if (identification == null)
			identification = new ArrayList<Identification>();

		return identification;
	}

	public void setIdentification(List<Identification> identification) {
		this.identification = identification;
	}
}
