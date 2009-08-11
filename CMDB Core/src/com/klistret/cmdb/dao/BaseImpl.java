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

package com.klistret.cmdb.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every DAO needs a SessionFactory making this class perfect as a super class
 * for the rest of the classes in this package.
 * 
 * @author Matthew Young
 * 
 */
public class BaseImpl {
	private static final Logger logger = LoggerFactory
			.getLogger(BaseImpl.class);

	private SessionFactory sessionFactory;

	/**
	 * Get Hibernate Session
	 * 
	 * @return org.hibernate.Session
	 */
	protected Session getSession() {
		if (this.sessionFactory == null) {
			logger
					.error("Session Factory has not been set on DAO before usage");
			throw new IllegalStateException(
					"Session Factory has not been set on DAO before usage");
		}
		return this.sessionFactory.getCurrentSession();
	}

	/**
	 * Spring injection of Hibernate session factory (which may be in or outside
	 * a j2ee container)
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
