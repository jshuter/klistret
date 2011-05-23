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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * Properties have an owning bean that gives the property a name localized to
 * the bean. Parallel to that localized name is the actual type locating the
 * element or attribute to a schema definition.
 * 
 * @author Matthew Young
 * 
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "Property")
@XmlRootElement(name = "Property")
public class CIProperty {
	/**
	 * Types are restricted to simple or complex element and attributes.
	 * 
	 * @author Matthew Young
	 * 
	 */
	public enum TypeCategory {
		SimpleElement, ComplexElement, Attribute
	};

	/**
	 * Localized name to the owning bean
	 */
	protected QName name;

	/**
	 * Type category
	 */
	protected TypeCategory typeCategory;

	/**
	 * Is the property required by the bean
	 */
	protected Boolean required;

	/**
	 * Can the property be nill for the bean
	 */
	protected Boolean nillable;

	/**
	 * Schema type
	 */
	protected QName type;

	/**
	 * Maximum occurs for the bean
	 */
	protected Integer maxOccurs;

	/**
	 * Minimum occurs for the bean
	 */
	protected Integer minOccurs;

	/**
	 * If the property can occur unlimited times within the bean
	 */
	protected Boolean maxOccursUnbounded;

	/**
	 * Annotation defined in the schema
	 */
	protected String annotation;

	public QName getName() {
		return name;
	}
	
	@XmlElement(name = "NameLocalPart")
	public String getNameLocalPart() {
		return name.getLocalPart();
	}
	
	@XmlElement(name = "NameNamespaceURI")
	public String getNameNamespaceURI() {
		return name.getNamespaceURI();
	}

	public TypeCategory getTypeCategory() {
		return typeCategory;
	}
	
	@XmlElement(name = "TypeCategoryName")
	public String getTypeCategoryName() {
		switch (typeCategory) {
		case ComplexElement:
			return "ComplexElement";
		case SimpleElement:
			return "SimpleElement";
		case Attribute:
			return "Attribute";
		}
		
		return null;
	}

	@XmlElement(name = "Required")
	public Boolean isRequired() {
		return required;
	}

	@XmlElement(name = "Nillable")
	public Boolean isNillable() {
		return nillable;
	}

	public QName getType() {
		return type;
	}
	
	@XmlElement(name = "TypeLocalPart")
	public String getTypeLocalPart() {
		return type.getLocalPart();
	}

	@XmlElement(name = "TypeNamespaceURI")
	public String getTypeNamespaceURI() {
		return type.getNamespaceURI();
	}

	@XmlElement(name = "MaxOccurs")
	public Integer getMaxOccurs() {
		return maxOccurs;
	}

	@XmlElement(name = "MinOccurs")
	public Integer getMinOccurs() {
		return minOccurs;
	}

	@XmlElement(name = "Unbounded")
	public Boolean isUnbounded() {
		return maxOccursUnbounded;
	}

	@XmlElement(name = "Annotation")
	public String getAnnotation() {
		return annotation;
	}

	public String toString() {
		return String.format("name: %s, type: %s", name, type);
	}
}
