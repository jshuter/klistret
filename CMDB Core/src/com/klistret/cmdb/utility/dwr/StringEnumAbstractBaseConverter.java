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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.directwebremoting.convert.BaseV20Converter;
import org.directwebremoting.extend.Converter;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.dwrp.SimpleOutboundVariable;

/**
 * 
 * @author Matthew Young
 *
 */
public class StringEnumAbstractBaseConverter extends BaseV20Converter implements
		Converter {

	private final static Logger logger = Logger
			.getLogger(StringEnumAbstractBaseConverter.class.getName());

	@SuppressWarnings("unchecked")
	public Object convertInbound(Class paramType, InboundVariable iv,
			InboundContext inctx) throws MarshallException {
		String value = LocalUtil.decode(iv.getValue());

		logger.fine("convering StrinEnumAbstractBase from value (" + value
				+ ")");

		try {
			Method getter = paramType.getMethod("forString",
					new Class[] { String.class });
			Object bean = getter.invoke(paramType, value);

			if (bean == null) {
				throw new MarshallException(paramType, "unknown enum value ("
						+ value + ")");
			}

			return bean;
		} catch (NoSuchMethodException e) {
			throw new MarshallException(paramType, e);
		} catch (IllegalArgumentException e) {
			throw new MarshallException(paramType, e);
		} catch (IllegalAccessException e) {
			throw new MarshallException(paramType, e);
		} catch (InvocationTargetException e) {
			throw new MarshallException(paramType, e);
		}
	}

	public OutboundVariable convertOutbound(Object object,
			OutboundContext outctx) throws MarshallException {
		return new SimpleOutboundVariable('\'' + object.toString() + '\'',
				outctx, true);
	}

}
