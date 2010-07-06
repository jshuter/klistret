package com.klistret.cmdb.utility.spring;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

public class LocalSessionFactoryBean extends
		org.springframework.orm.hibernate3.LocalSessionFactoryBean {

	@SuppressWarnings("unchecked")
	protected void postProcessConfiguration(Configuration config)
			throws HibernateException {
		/**
		 * http://stackoverflow.com/questions/672063/creating-a-custom-hibernate-usertype-find-out-the-current-entity-table-name
		 */

	}
}
