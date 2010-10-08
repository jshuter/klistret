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
package com.klistret.cmdb.utility.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * 
 * @author Matthew Young
 * 
 */
public class BeanMetadata {

	public enum TypeCategory {
		Complex, Simple
	};

	public enum CMDBCategory {
		Proxy, Element, Relation
	};

	protected Class<?> javaClass;

	protected QName type;

	protected TypeCategory typeCategory;

	protected CMDBCategory cmdbCategory;

	protected QName base;

	protected Boolean abstraction;

	protected List<PropertyMetadata> properties = new ArrayList<PropertyMetadata>();

	public Class<?> getJavaClass() {
		return javaClass;
	}

	public QName getType() {
		return type;
	}

	public TypeCategory getTypeCategory() {
		return typeCategory;
	}

	public CMDBCategory getCMDBCategory() {
		return cmdbCategory;
	}

	public QName getBase() {
		return base;
	}

	public Boolean isAbstraction() {
		return abstraction;
	}

	public List<PropertyMetadata> getProperties() {
		return properties;
	}

	public boolean hasPropertyByName(QName name) {
		for (PropertyMetadata property : properties)
			if (property.name.equals(name))
				return true;

		return false;
	}

	public boolean hasPropertyByType(QName type) {
		for (PropertyMetadata property : properties)
			if (property.type.equals(type))
				return true;

		return false;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (this.javaClass == null)
			return false;

		if (!(other instanceof BeanMetadata))
			return false;

		final BeanMetadata that = (BeanMetadata) other;

		return this.javaClass.getName().equals(that.javaClass.getName());
	}

	public String toString() {
		return String.format("class: %s, localName: %s, namespace: %s",
				javaClass.getName(), type.getLocalPart(), type
						.getNamespaceURI());
	}
}
