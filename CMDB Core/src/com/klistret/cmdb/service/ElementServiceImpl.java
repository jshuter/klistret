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

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.ElementQueryResponse;
import com.klistret.cmdb.pojo.QueryRequest;
import com.klistret.cmdb.dao.ElementDAO;
import com.klistret.cmdb.utility.annotations.Timer;

public class ElementServiceImpl implements ElementService {

	private ElementDAO elementDAO;

	public void setElementDAO(ElementDAO elementDAO) {
		this.elementDAO = elementDAO;
	}

	public List<Element> findByExpressions(String[] expressions,
			Integer start, Integer limit) {
		return elementDAO.findByExpressions(expressions, start, limit);
	}

	public ElementQueryResponse findByExpressions(QueryRequest queryRequest) {
		ElementQueryResponse queryResponse = new ElementQueryResponse();

		List<Element> payload = findByExpressions(queryRequest
				.getExpressions().toArray(new String[0]), queryRequest
				.getStart(), queryRequest.getLimit());

		queryResponse.setPayload(payload);
		queryResponse.setCount(payload.size());
		queryResponse.setSuccessful(true);

		return queryResponse;
	}

	public Element getById(Long id) {
		return elementDAO.getById(id);
	}

	@Timer
	public Element set(Element element) {
		return elementDAO.set(element);
	}
}
