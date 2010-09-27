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

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.QueryResponse;
import com.klistret.cmdb.ci.pojo.QueryRequest;
import com.klistret.cmdb.dao.ElementDAO;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;

public class ElementServiceImpl implements ElementService {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementServiceImpl.class);

	private ElementDAO elementDAO;

	public void setElementDAO(ElementDAO elementDAO) {
		this.elementDAO = elementDAO;
	}

	public List<Element> findByExpressions(String[] expressions, int start,
			int limit) {
		return elementDAO.findByExpressions(expressions, start, limit);
	}

	public QueryResponse findByExpressions(QueryRequest queryRequest) {
		QueryResponse queryResponse = new QueryResponse();

		try {
			List<Element> elements = findByExpressions(queryRequest
					.getExpressions().toArray(new String[0]), queryRequest
					.getStart(), queryRequest.getLimit());
			queryResponse.setElements(elements);
			queryResponse.setCount(elements.size());
			queryResponse.setSuccessful(true);
		} catch (ApplicationException e) {
			logger.error("Error executing query: {}", e);
			queryResponse.setCount(0);
			queryResponse.setSuccessful(false);
			queryResponse.setMessage(e.getMessage());
		} catch (InfrastructureException e) {
			logger.error("Error executing query: {}", e);
			queryResponse.setCount(0);
			queryResponse.setSuccessful(false);
			queryResponse.setMessage(e.getMessage());
		}

		return queryResponse;
	}

	public Element getById(Long id) {
		return elementDAO.getById(id);
	}

	public Element set(Element element) {
		return elementDAO.set(element);
	}
}
