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

package com.klistret.cmdb.utility.dwr;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.directwebremoting.extend.MarshallException;

/**
 * Pretty sure wrote this class for debugging purposes. Messy code with DWR that
 * probably won't live on with the integration of EasyRest.
 * 
 * @author Matthew Young
 * 
 */
public class PropertyDescriptorProperty extends
		org.directwebremoting.impl.PropertyDescriptorProperty {

	private final static Logger logger = Logger
			.getLogger(PropertyDescriptorProperty.class.getName());

	public PropertyDescriptorProperty(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.directwebremoting.extend.Property#setValue(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void setValue(Object bean, Object value) throws MarshallException {
		try {
			logger.finest("setting value ["
					+ (value == null ? null : value.toString())
					+ "] for bean ["
					+ (bean == null ? null : bean.getClass().getName()) + "]");
			logger.finest("setter method ["
					+ descriptor.getWriteMethod().getName() + "]");
			logger.finest("getter method ["
					+ descriptor.getReadMethod().getName() + "]");

			Object arguments[] = { value };
			Method writeMethod = descriptor.getWriteMethod();

			Class<?>[] parameterTypes = writeMethod.getParameterTypes();
			for (Class<?> type : parameterTypes) {
				logger
						.finest("write method parameter [" + type.getName()
								+ "]");
				logger.finest("is array? " + type.isArray());

				if (type.isArray() && value == null) {
					arguments[0] = (Object[]) null;
				}
			}

			// invoke write call
			writeMethod.invoke(bean, arguments);
		} catch (InvocationTargetException ex) {
			logger.severe("invocation message: " + ex.getMessage());
			throw new MarshallException(bean.getClass(), ex
					.getTargetException());
		} catch (Exception ex) {
			logger.severe("general exception message: " + ex.getMessage());
			throw new MarshallException(bean.getClass(), ex);
		}
	}
}
