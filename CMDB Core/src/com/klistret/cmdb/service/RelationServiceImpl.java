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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.dao.RelationDAO;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.ci.pojo.QueryRequest;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.ci.pojo.QueryResponse;

public class RelationServiceImpl implements RelationService {
	private static final Logger logger = LoggerFactory
			.getLogger(RelationServiceImpl.class);

	private RelationDAO relationDAO;

	public void setRelationDAO(RelationDAO relationDAO) {
		this.relationDAO = relationDAO;
	}

	public Relation getById(Long id) {
		return relationDAO.getById(id);
	}

	public List<Relation> findByExpressions(List<String> expressions, int start,
			int limit) {
		return relationDAO.findByExpressions(expressions, start, limit);
	}

	public QueryResponse findByExpressions(QueryRequest queryRequest) {
		QueryResponse queryResponse = new QueryResponse();

		try {
			List<Relation> relations = findByExpressions(queryRequest
					.getExpressions().toArray(new String[0]), queryRequest
					.getStart(), queryRequest.getLimit());
			queryResponse.setRelations(relations);
			queryResponse.setCount(relations.size());
			queryResponse.setSuccessful(true);
		} catch (ApplicationException e) {
			logger.error("Error executing query: {}", e);
			queryResponse.setCount(0);
			queryResponse.setSuccessful(false);
		} catch (InfrastructureException e) {
			logger.error("Error executing query: {}", e);
			queryResponse.setCount(0);
			queryResponse.setSuccessful(false);
		}

		return queryResponse;
	}

	public Relation set(Relation relation) {
		return relationDAO.set(relation);
	}

}
