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
package com.klistret.cmdb.taxonomy.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Element", propOrder = { "relation", "property" })
@XmlRootElement(name = "Element")
public class Element {

	@XmlAttribute(name = "Type")
	protected String type;

	@XmlElement(name = "Relation")
	protected List<Relation> relation;

	@XmlElement(name = "Property")
	protected List<Property> property;

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setRelation(List<Relation> relation) {
		this.relation = relation;
	}

	public List<Relation> getRelation() {
		if (this.relation == null)
			this.relation = new ArrayList<Relation>();

		return this.relation;
	}

	public void setProperty(List<Property> property) {
		this.property = property;
	}

	public List<Property> getProperty() {
		if (this.property == null)
			this.property = new ArrayList<Property>();

		return this.property;
	}
}
