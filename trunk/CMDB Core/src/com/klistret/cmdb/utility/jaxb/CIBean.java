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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
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
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "Bean")
@XmlRootElement(name = "Bean")
public class CIBean {

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
	 * Base CIBean (extended)
	 */
	protected CIBean base;

	/**
	 * Base schema type (extended)
	 */
	protected QName baseType;

	/**
	 * Abstract conditional
	 */
	protected Boolean abstraction;

	/**
	 * Annotations on the schema type
	 */
	protected List<String> annotations = new ArrayList<String>();

	/**
	 * Lexical string list if Enumeration
	 */
	protected List<String> enumerations = new ArrayList<String>();

	/**
	 * Properties
	 */
	protected List<CIProperty> properties = new ArrayList<CIProperty>();

	public Class<?> getJavaClass() {
		return javaClass;
	}

	@XmlElement(name = "JavaName")
	public String getJavaName() {
		return javaClass.getName();
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

	public TypeCategory getTypeCategory() {
		return typeCategory;
	}

	@XmlElement(name = "TypeCategoryName")
	public String getTypeCategoryName() {
		switch (typeCategory) {
		case Complex:
			return "Complex";
		case Simple:
			return "Simple";
		}

		return null;
	}

	public CIBean getBase() {
		return base;
	}

	public QName getBaseType() {
		return baseType;
	}

	@XmlElement(name = "BaseLocalPart")
	public String getBaseTypeLocalPart() {
		return baseType.getLocalPart();
	}

	@XmlElement(name = "BaseNamespaceURI")
	public String getBaseTypeNamespaceURI() {
		return baseType.getNamespaceURI();
	}

	@XmlElement(name = "Abstract")
	public Boolean isAbstraction() {
		return abstraction;
	}

	@XmlElement(name = "Annotation")
	public List<String> getAnnotations() {
		return annotations;
	}

	@XmlElement(name = "Property")
	public List<CIProperty> getProperties() {
		return properties;
	}

	public CIProperty getPropertyByName(QName name) {
		for (CIProperty property : properties)
			if (property.name.equals(name))
				return property;

		return null;
	}

	public CIProperty getPropertyByType(QName type) {
		for (CIProperty property : properties)
			if (property.type.equals(type))
				return property;

		return null;
	}

	public boolean hasPropertyByName(QName name) {
		for (CIProperty property : properties)
			if (property.name.equals(name))
				return true;

		return false;
	}

	public boolean hasPropertyByType(QName type) {
		for (CIProperty property : properties)
			if (property.type.equals(type))
				return true;

		return false;
	}
	
	public boolean isAncestor(QName baseType) {
		if (this.baseType != null && this.baseType.equals(baseType))
			return true;
		
		if (this.base != null)
			return this.base.isAncestor(baseType);
		
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

		if (!(other instanceof CIBean))
			return false;

		final CIBean that = (CIBean) other;

		return this.javaClass.getName().equals(that.javaClass.getName());
	}

	/**
	 * Override toString method
	 */
	public String toString() {
		return String.format("class: %s, type: %s", javaClass.getName(), type);
	}
}
