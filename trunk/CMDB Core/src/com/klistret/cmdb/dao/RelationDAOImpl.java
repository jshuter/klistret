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

import java.util.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;

public class RelationDAOImpl extends BaseImpl implements RelationDAO {

	private final static Logger logger = Logger.getLogger(RelationDAOImpl.class
			.getName());

	public com.klistret.cmdb.pojo.Relation getById(Long id) {
		try {
			Criteria criteria = getSession().createCriteria(
					com.klistret.cmdb.pojo.Relation.class);

			criteria.add(Restrictions.idEq(id));

			logger.fine("getting relation [id:" + id + "] by id");
			com.klistret.cmdb.pojo.Relation relation = (com.klistret.cmdb.pojo.Relation) criteria
					.uniqueResult();

			if (relation == null) {
				throw new ApplicationException("relation [id: " + id
						+ "] does not exist");
			}

			return relation;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

}
