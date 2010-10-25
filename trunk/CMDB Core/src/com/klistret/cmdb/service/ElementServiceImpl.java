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
import com.klistret.cmdb.ci.pojo.QueryResponse;
import com.klistret.cmdb.dao.ElementDAO;
import com.klistret.cmdb.exception.ApplicationException;

public class ElementServiceImpl implements ElementService {

	private ElementDAO elementDAO;

	public void setElementDAO(ElementDAO elementDAO) {
		this.elementDAO = elementDAO;
	}

	public Element get(Long id) {
		return elementDAO.get(id);
	}

	public List<Element> find(List<String> expressions, int start, int limit) {
		if (start < 0)
			throw new ApplicationException(String.format(
					"Start parameter [%d] less than zero", start));

		if (limit < 0 || limit > 100)
			throw new ApplicationException(String.format(
					"Limit parameter [%d] less than zero or greater than 100",
					limit));

		return elementDAO.find(expressions, start, limit);
	}

	public QueryResponse query(List<String> expressions, int start, int limit) {
		List<Element> elements = find(expressions, start, limit);

		QueryResponse qr = new QueryResponse();
		qr.setSuccessful(true);
		qr.setCount(elements.size());
		qr.setElements(elements);

		return qr;
	}

	public Element create(Element element) {
		if (element.getId() != null)
			throw new ApplicationException(String.format(
					"Create against a persistent element [%s]", element));

		return elementDAO.set(element);
	}

	public Element update(Element element) {
		if (element.getId() == null)
			throw new ApplicationException(String.format(
					"Update against a non-persistent element [%s]", element));

		return elementDAO.set(element);
	}

	public void delete(Long id) {
		elementDAO.delete(id);
	}

}
