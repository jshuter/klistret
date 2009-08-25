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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;

/**
 * 
 * @author Matthew Young
 * 
 */
public class RelationDAOImpl extends BaseImpl implements RelationDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(RelationDAOImpl.class);

	/**
	 * 
	 * @param id
	 * @return Relation
	 */
	public com.klistret.cmdb.xmlbeans.pojo.Relation getById(Long id) {
		try {
			Criteria criteria = getSession().createCriteria("Relation");

			criteria.add(Restrictions.idEq(id));

			logger.debug("getting relation [id: {}] by id", id);
			com.klistret.cmdb.xmlbeans.pojo.Relation relation = (com.klistret.cmdb.xmlbeans.pojo.Relation) criteria
					.uniqueResult();

			if (relation == null) {
				throw new ApplicationException(String.format(
						"relation [id: %s] does not exist", id));
			}

			return relation;
		} catch (HibernateException he) {
			logger
					.error(
							"HibernateException running get by ID [message: {}, cause: {}]",
							he.getMessage(), he.getCause());
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

}
