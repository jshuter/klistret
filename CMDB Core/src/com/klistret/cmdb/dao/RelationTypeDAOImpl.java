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

import com.klistret.cmdb.exception.InfrastructureException;

/**
 * 
 * @author Matthew Young
 * 
 */
public class RelationTypeDAOImpl extends BaseImpl implements RelationTypeDAO {

	private final static Logger logger = Logger
			.getLogger(RelationTypeDAOImpl.class.getName());

	/**
	 * Get relation type by composite ID (name)
	 * 
	 * @param name
	 *            relation type
	 * @return com.klistret.cmdb.pojo.RelationType
	 * @throws InfrastructureException
	 *             when Hibernate criteria does not return a unique result
	 */
	public com.klistret.cmdb.pojo.RelationType getByCompositeId(String name) {
		logger.fine("getting relation type by composite id [" + name + "]");

		Criteria criteria = getSession().createCriteria(
				com.klistret.cmdb.pojo.RelationType.class);

		criteria.add(Restrictions.isNull("toTimeStamp"));
		criteria.add(Restrictions.eq("name", name));

		try {
			com.klistret.cmdb.pojo.RelationType relationType = (com.klistret.cmdb.pojo.RelationType) criteria
					.uniqueResult();

			if (relationType != null) {
				logger.fine("found relation type [" + relationType.toString()
						+ "]");
			}

			return relationType;
		} catch (HibernateException he) {
			throw new InfrastructureException(he.getMessage(), he.getCause());
		}
	}

}
