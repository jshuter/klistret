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
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.dao.ElementDAO;
import com.klistret.cmdb.exception.ApplicationException;

/**
 * Element service implementation
 * 
 * @author Matthew Young
 * 
 */
public class ElementServiceImpl implements ElementService {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(ElementServiceImpl.class);

	/**
	 * Element DAO
	 */
	private ElementDAO elementDAO;

	/**
	 * Dependency injection
	 * 
	 * @param elementDAO
	 */
	public void setElementDAO(ElementDAO elementDAO) {
		this.elementDAO = elementDAO;
	}

	/**
	 * Get an element
	 * 
	 * @param id
	 * 
	 * @return Element
	 */
	public Element get(Long id) {
		return elementDAO.get(id);
	}

	/**
	 * Find elements by criteria (XPath). Results are restricted to 100.
	 * 
	 * @param expressions
	 * @param start
	 * @param limit
	 * 
	 * @return List<Element>
	 */
	public List<Element> find(List<String> expressions, int start, int limit) {
		if (start < 0)
			throw new ApplicationException(String.format(
					"Start parameter [%d] less than zero", start),
					new IllegalArgumentException());

		if (limit < 0 || limit > 100)
			throw new ApplicationException(String.format(
					"Limit parameter [%d] less than zero or greater than 100",
					limit), new IllegalArgumentException());

		return elementDAO.find(expressions, start, limit);
	}

	/**
	 * Create an element if the id property is null
	 * 
	 * @param element
	 * 
	 * @return Element
	 */
	public Element create(Element element) {
		if (element.getId() != null)
			throw new ApplicationException(String.format(
					"Create disallowed against persistent element [%s]",
					element), new RejectedExecutionException());

		return elementDAO.set(element);
	}

	/**
	 * Update an element
	 * 
	 * @param element
	 * 
	 * @return Element
	 */
	public Element update(Element element) {
		if (element.getId() == null)
			throw new ApplicationException(String.format(
					"Update against a non-persistent element [%s]", element),
					new RejectedExecutionException());

		return elementDAO.set(element);
	}

	/**
	 * Delete an element (soft-delete)
	 * 
	 * @param id
	 */
	public void delete(Long id) {
		elementDAO.delete(id);
	}

	/**
	 * Dummy method for pre-flighted calls
	 */
	public String preflighted() {
		logger.debug("Entered preflighted method (ie handled HTTP OPTIONS");

		return "HTTP OPTIONS accepted";
	}
}
