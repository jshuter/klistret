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
 * Bean metadata is a schema element either of complex or simple type. The type
 * (QName) locates the bean inside the schema definitions and the Java class
 * name locates the bean in the Java code (i.e. bridging Java/Schema). Outside
 * of that critical relationship it is important to know if the bean is abstract
 * and what properties are available.
 * 
 * @author Matthew Young
 * 
 */
public class BeanMetadata {

	/**
	 * Types are reduced to either complex or simple types
	 * 
	 * @author Matthew Young
	 * 
	 */
	public enum TypeCategory {
		Complex, Simple
	};

	/**
	 * Category of CMDB either an element, relation or proxy.
	 * 
	 * @author Matthew Young
	 * 
	 */
	public enum CMDBCategory {
		Proxy, Element, Relation
	};

	/**
	 * Underlying java class representing the schema type
	 */
	protected Class<?> javaClass;

	/**
	 * Schema type
	 */
	protected QName type;

	/**
	 * Type category
	 */
	protected TypeCategory typeCategory;

	/**
	 * CMDB category
	 */
	protected CMDBCategory cmdbCategory;

	/**
	 * Base schema type (extended)
	 */
	protected QName base;

	/**
	 * Abstract conditional
	 */
	protected Boolean abstraction;

	/**
	 * Annotations on the schema type
	 */
	protected List<String> annotations = new ArrayList<String>();

	/**
	 * Properties
	 */
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

	public List<String> getAnnotations() {
		return annotations;
	}

	public List<PropertyMetadata> getProperties() {
		return properties;
	}

	public PropertyMetadata getPropertyByName(QName name) {
		for (PropertyMetadata property : properties)
			if (property.name.equals(name))
				return property;

		return null;
	}

	public PropertyMetadata getPropertyByType(QName type) {
		for (PropertyMetadata property : properties)
			if (property.type.equals(type))
				return property;

		return null;
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

	/**
	 * If the Java class names are identical then the objects are equal
	 */
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
		return String
				.format("class: %s, type localName: %s, type namespace: %s",
						javaClass.getName(), type.getLocalPart(), type
								.getNamespaceURI());
	}
}
