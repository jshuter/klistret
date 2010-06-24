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

package com.klistret.cmdb.pojo;

import javax.xml.namespace.QName;

/**
 * XML property of an XML bean
 * 
 * @author Matthew Young
 *
 */
public abstract class XMLProperty {

	private String name;

	private QName type;

	public String getName() {
		return name;
	}

	public QName getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(QName type) {
		this.type = type;
	}
}
