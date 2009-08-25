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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Element {
	private Long id;

	private ElementType type;

	private String name;

	private Date fromTimeStamp;

	private Date toTimeStamp;

	private String createId;

	private Date createTimeStamp;

	private Date updateTimeStamp;

	private com.klistret.cmdb.xmlbeans.Element configuration;

	private Set<Relation> sourceRelations = new HashSet<Relation>();

	private Set<Relation> destinationRelations = new HashSet<Relation>();

	public Long getId() {

		return id;

	}

	public void setId(Long id) {

		this.id = id;

	}

	public ElementType getType() {

		return type;

	}

	public void setType(ElementType type) {

		this.type = type;

	}

	public String getName() {

		return name;

	}

	public void setName(String name) {

		this.name = name;

	}

	public Date getFromTimeStamp() {

		return fromTimeStamp;

	}

	public void setFromTimeStamp(Date fromTimeStamp) {

		this.fromTimeStamp = fromTimeStamp;

	}

	public Date getToTimeStamp() {

		return toTimeStamp;

	}

	public void setToTimeStamp(Date toTimeStamp) {

		this.toTimeStamp = toTimeStamp;

	}

	public String getCreateId() {

		return createId;

	}

	public void setCreateId(String createId) {

		this.createId = createId;

	}

	public Date getCreateTimeStamp() {

		return createTimeStamp;

	}

	public void setCreateTimeStamp(Date createTimeStamp) {

		this.createTimeStamp = createTimeStamp;

	}

	public Date getUpdateTimeStamp() {

		return updateTimeStamp;

	}

	public void setUpdateTimeStamp(Date updateTimeStamp) {

		this.updateTimeStamp = updateTimeStamp;

	}

	public com.klistret.cmdb.xmlbeans.Element getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(
			com.klistret.cmdb.xmlbeans.Element configuration) {
		this.configuration = configuration;
	}

	public Set<Relation> getSourceRelations() {

		return sourceRelations;

	}

	public void setSourceRelations(Set<Relation> sourceRelations) {

		this.sourceRelations = sourceRelations;

	}

	public Set<Relation> getDestinationRelations() {

		return destinationRelations;

	}

	public void setDestinationRelations(Set<Relation> destinationRelations) {

		this.destinationRelations = destinationRelations;

	}

	public boolean equals(Object other) {

		if (this == other) {

			return true;

		}

		if (this.id == null) {

			return false;

		}

		if (!(other instanceof Element)) {

			return false;

		}

		final Element that = (Element) other;

		return this.id.equals(that.getId());

	}

	public int hashCode() {

		return this.id == null ? System.identityHashCode(this) : id.hashCode();

	}

	public String toString() {

		if (this.id != null) {

			return "id: " + this.id.toString() + ", name: " + this.name

			+ ", type: " + this.type.getName();

		} else {

			return null;

		}

	}
}
