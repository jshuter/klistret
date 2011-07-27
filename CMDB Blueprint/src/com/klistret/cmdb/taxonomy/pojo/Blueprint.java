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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Blueprint", propOrder = { "taxonomy" })
@XmlRootElement(name = "Blueprint")
public class Blueprint {

	@XmlElement(name = "Taxonomy", required = true, nillable = false)
	protected List<Taxonomy> taxonomy;

	public void setTaxonomy(List<Taxonomy> taxonomy) {
		this.taxonomy = taxonomy;
	}

	public List<Taxonomy> getTaxonomy() {
		return this.taxonomy;
	}
}
