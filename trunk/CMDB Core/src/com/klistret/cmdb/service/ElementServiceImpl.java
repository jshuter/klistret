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

import java.util.Collection;

import com.klistret.cmdb.pojo.Element;
import com.klistret.cmdb.pojo.FindQuery;
import com.klistret.cmdb.pojo.FindResults;
import com.klistret.cmdb.dao.ElementDAO;
import com.klistret.cmdb.utility.annotations.Timer;

public class ElementServiceImpl implements ElementService {

	private ElementDAO elementDAO;

	public void setElementDAO(ElementDAO elementDAO) {
		this.elementDAO = elementDAO;
	}

	public Collection<Element> findByExpressions(String[] expressions,
			Integer start, Integer limit) {
		return elementDAO.findByExpressions(expressions, start, limit);
	}

	public FindResults findByExpressions(FindQuery findQuery) {
		FindResults findResults = new FindResults();

		Collection<Element> payload = findByExpressions(findQuery
				.getExpressions(), findQuery.getStart(), findQuery.getLimit());

		findResults.setPayload(payload);
		findResults.setCount(payload.size());
		findResults.setSuccessful(true);

		return findResults;
	}

	public Element getById(Long id) {
		return elementDAO.getById(id);
	}

	@Timer
	public Element set(Element element) {
		return elementDAO.set(element);
	}
}
