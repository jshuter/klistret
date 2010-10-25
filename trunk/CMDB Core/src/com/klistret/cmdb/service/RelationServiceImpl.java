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

package com.klistret.cmdb.service;

import java.util.List;

import com.klistret.cmdb.dao.RelationDAO;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.ci.pojo.Relation;

public class RelationServiceImpl implements RelationService {

	private RelationDAO relationDAO;

	public void setRelationDAO(RelationDAO relationDAO) {
		this.relationDAO = relationDAO;
	}

	public Relation get(Long id) {
		return relationDAO.get(id);
	}

	public List<Relation> find(List<String> expressions, int start, int limit) {
		if (start < 0)
			throw new ApplicationException(String.format(
					"Start parameter [%d] less than zero", start));

		if (limit < 0 || limit > 100)
			throw new ApplicationException(String.format(
					"Limit parameter [%d] less than zero or greater than 100",
					limit));

		return relationDAO.find(expressions, start, limit);
	}

	public Relation create(Relation relation) {
		if (relation.getId() != null)
			throw new ApplicationException(String.format(
					"Create against a persistent relation [%s]", relation));

		return relationDAO.set(relation);
	}

	public Relation update(Long id, Relation relation) {
		return relationDAO.set(relation);
	}

	public void delete(Long id) {
		relationDAO.delete(id);
	}

}
