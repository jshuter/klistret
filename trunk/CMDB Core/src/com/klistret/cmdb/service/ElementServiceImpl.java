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
import com.klistret.cmdb.dao.ElementDAO;

public class ElementServiceImpl implements ElementService {

	private static final Logger logger = LoggerFactory
			.getLogger(ElementServiceImpl.class);

	private ElementDAO elementDAO;

	public void setElementDAO(ElementDAO elementDAO) {
		this.elementDAO = elementDAO;
	}

	public Element get(Long id) {
		return elementDAO.getById(id);
	}

	public List<Element> findByExpressions(List<String> expressions) {
		return elementDAO.findByExpressions(expressions, 0, 10);
	}

	public List<Element> findByExpressions(List<String> expressions, int start,
			int limit) {
		return elementDAO.findByExpressions(expressions, start, limit);
	}

	public Element create(Element element) {
		return elementDAO.set(element);
	}

	public Element update(Long id, Element element) {
		return elementDAO.set(element);
	}

	public void delete(Long id) {
	}

}
