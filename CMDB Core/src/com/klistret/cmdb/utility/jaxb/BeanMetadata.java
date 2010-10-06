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

	protected String namespace;

	protected String localName;

	protected TypeCategory typeCategory;

	protected CMDBCategory cmdbCategory;

	protected String baseNamespace;

	protected String baseLocalName;

	protected Boolean abstraction;

	protected List<PropertyMetadata> properties = new ArrayList<PropertyMetadata>();

	public Class<?> getJavaClass() {
		return javaClass;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getLocalName() {
		return localName;
	}

	public TypeCategory getTypeCategory() {
		return typeCategory;
	}

	public CMDBCategory getCMDBCategory() {
		return cmdbCategory;
	}

	public String getBaseNamespace() {
		return baseNamespace;
	}

	public String getBaseLocalName() {
		return baseLocalName;
	}

	public Boolean isAbstraction() {
		return abstraction;
	}

	public List<PropertyMetadata> getProperties() {
		return properties;
	}
}
