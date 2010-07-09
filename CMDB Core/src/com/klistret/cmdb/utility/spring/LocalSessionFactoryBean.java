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

package com.klistret.cmdb.utility.spring;

import java.util.Iterator;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.utility.hibernate.JAXBUserType;

/**
 * Extension to the Spring LocalSessionFactoryBean to send parameters to the
 * JAXBUserType from the Spring configuration (avoids having to use the Spring
 * AOP Configuration or building a home grown singleton).
 * 
 * @author Matthew Young
 * 
 */
public class LocalSessionFactoryBean extends
		org.springframework.orm.hibernate3.LocalSessionFactoryBean {

	private static final Logger logger = LoggerFactory
			.getLogger(LocalSessionFactoryBean.class);

	private String baseTypes;

	private String assignablePackages;

	public void setBaseTypes(String baseTypes) {
		this.baseTypes = baseTypes;
	}

	public void setAssignablePackages(String assignablePackages) {
		this.assignablePackages = assignablePackages;
	}

	@SuppressWarnings("unchecked")
	protected void postProcessMappings(Configuration config)
			throws HibernateException {
		logger
				.debug("Overriding the postProcessMappings method in the Spring LocalSessionFactoryBean");

		/**
		 * http://stackoverflow.com/questions/672063/creating-a-custom-hibernate-usertype-find-out-the-current-entity-table-name
		 */
		for (Iterator classMappingIterator = config.getClassMappings(); classMappingIterator
				.hasNext();) {
			PersistentClass persistentClass = (PersistentClass) classMappingIterator
					.next();

			for (Iterator propertyIterator = persistentClass
					.getPropertyIterator(); propertyIterator.hasNext();) {
				Property property = (Property) propertyIterator.next();

				if (property.getType().getName().equals(
						JAXBUserType.class.getName())) {
					logger
							.debug(
									"Setting ci context properties for Hibernate user type [{}]",
									JAXBUserType.class.getName());

					SimpleValue simpleValue = (SimpleValue) property.getValue();
					Properties parameterMap = new Properties();

					parameterMap.setProperty("baseTypes", baseTypes);
					logger.debug("Base types [{}]", baseTypes);

					parameterMap.setProperty("assignablePackages",
							assignablePackages);
					logger
							.debug("Assignable packages [{}]",
									assignablePackages);

					simpleValue.setTypeParameters(parameterMap);
				}
			}
		}
	}
}
