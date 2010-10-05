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

	protected Class<?> javaClass;

	protected String namespace;

	protected String localName;

	protected TypeCategory typeCategory;

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

	public Boolean isAbstraction() {
		return abstraction;
	}

	public List<PropertyMetadata> getProperties() {
		return properties;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (this.javaClass == null)
			return false;

		if (!(other instanceof BeanMetadata))
			return false;

		final BeanMetadata that = (BeanMetadata) other;

		return this.javaClass.equals(that.getJavaClass());
	}
}
