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
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Granularity", propOrder = { "element" })
public class Granularity {
	@XmlAttribute(name = "Name", required = true)
	protected String name;

	@XmlAttribute(name = "Extension")
	protected String extension;

	@XmlElement(name = "Element", required = true)
	protected List<Element> element;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return this.extension;
	}

	public void setElement(List<Element> element) {
		this.element = element;
	}

	public List<Element> getElement() {
		if (this.element == null) 
			this.element = new ArrayList<Element>();
		
		return this.element;
	}
}
