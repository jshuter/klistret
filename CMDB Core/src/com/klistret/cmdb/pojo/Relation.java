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

public class Relation {

	private Long id;

	private RelationType type;

	private Element source;

	private Element destination;

	private Date fromTimeStamp;

	private Date toTimeStamp;

	private String createId;

	private Date createTimeStamp;

	private Date updateTimeStamp;

	private com.klistret.cmdb.xmlbeans.Relation configuration;

	public Relation() {

	}

	public Long getId() {

		return this.id;

	}

	public void setId(Long id) {

		this.id = id;

	}

	public RelationType getType() {

		return this.type;

	}

	public void setType(RelationType type) {

		this.type = type;

	}

	public Element getSource() {

		return this.source;

	}

	public void setSource(Element source) {

		this.source = source;

	}

	public Element getDestination() {

		return this.destination;

	}

	public void setDestination(Element destination) {

		this.destination = destination;

	}

	public Date getFromTimeStamp() {

		return this.fromTimeStamp;

	}

	public void setFromTimeStamp(Date fromTimeStamp) {

		this.fromTimeStamp = fromTimeStamp;

	}

	public Date getToTimeStamp() {

		return this.toTimeStamp;

	}

	public void setToTimeStamp(Date toTimeStamp) {

		this.toTimeStamp = toTimeStamp;

	}

	public String getCreateId() {

		return this.createId;

	}

	public void setCreateId(String createId) {

		this.createId = createId;

	}

	public Date getCreateTimeStamp() {

		return this.createTimeStamp;

	}

	public void setCreateTimeStamp(Date createTimeStamp) {

		this.createTimeStamp = createTimeStamp;

	}

	public Date getUpdateTimeStamp() {

		return this.updateTimeStamp;

	}

	public void setUpdateTimeStamp(Date updateTimeStamp) {

		this.updateTimeStamp = updateTimeStamp;

	}

	public com.klistret.cmdb.xmlbeans.Relation getConfiguration() {

		return this.configuration;

	}

	public void setConfiguration(com.klistret.cmdb.xmlbeans.Relation configuration) {

		this.configuration = configuration;

	}

	public String toString() {

		if (this.id != null) {

			return "id: " + this.id.toString() + ", type: "

			+ this.type.getName();

		} else {

			return null;

		}

	}

	public boolean equals(Object other) {

		if (this == other) {

			return true;

		}

		if (this.id == null) {

			return false;

		}

		if (!(other instanceof Relation)) {

			return false;

		}

		final Relation that = (Relation) other;

		return this.id.equals(that.getId());

	}

	public int hashCode() {

		return this.id == null ? System.identityHashCode(this) : id.hashCode();

	}
}
