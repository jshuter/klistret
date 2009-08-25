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

package com.klistret.cmdb.utility.hibernate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.namespace.QName;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

@SuppressWarnings("serial")
public class XmlBeansProxyInterceptor extends EmptyInterceptor {
	private static final Logger logger = LoggerFactory
			.getLogger(XmlBeansProxyInterceptor.class);

	public String getEntityName(Object object) {
		if (Proxy.isProxyClass(object.getClass())
				&& object instanceof XmlObject)
			return ((XmlObject) object).schemaType().getShortJavaName();
		return super.getEntityName(object);
	}

	public Object instantiate(String entityName, EntityMode entityMode,
			Serializable id) {
		SchemaType schemaType = XmlBeans.getContextTypeLoader().findType(
				new QName("http://www.klistret.com/cmdb/pojo", entityName));
		if (schemaType != null) {
			XmlObject xmlObject = XmlBeans.getContextTypeLoader().newInstance(
					schemaType, null);
			try {
				Method setIdMethod = xmlObject.getClass().getMethod("setId",
						long.class);
				Object[] arguments = { id };
				setIdMethod.invoke(xmlObject, arguments);
			} catch (SecurityException e) {
				logger.error(
						"unable to get method setId for schema type [{}]: {}",
						schemaType.getFullJavaName(), e.getMessage());
			} catch (NoSuchMethodException e) {
				logger.error(
						"unable to get method setId for schema type [{}]: {}",
						schemaType.getFullJavaName(), e.getMessage());
			} catch (IllegalArgumentException e) {
				logger
						.error(
								"illegel argument in method setId for schema type [{}]: {}",
								schemaType.getFullJavaName(), e.getMessage());
			} catch (IllegalAccessException e) {
				logger
						.error(
								"illegel access in method setId for schema type [{}]: {}",
								schemaType.getFullJavaName(), e.getMessage());
			} catch (InvocationTargetException e) {
				logger
						.error(
								"invocation error in method setId for schema type [{}]: {}",
								schemaType.getFullJavaName(), e.getMessage());
			}
			return xmlObject;
		}
		return super.instantiate(entityName, entityMode, id);
	}

}
