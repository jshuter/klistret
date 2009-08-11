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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.xmlbeans.XmlObject;
import org.directwebremoting.convert.BeanConverter;
import org.directwebremoting.dwrp.ObjectOutboundVariable;
import org.directwebremoting.dwrp.ParseUtil;
import org.directwebremoting.dwrp.ProtocolConstants;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;
import org.directwebremoting.extend.Property;
import org.directwebremoting.extend.TypeHintContext;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Messages;

/**
 * Extract Bean converter for XmlObject to publish them as JSON. Requires that
 * only the interfaces (not the actual implementations) are transformed
 * otherwise extract XmlBeans structures are processed rather than Bean methods
 * (get/set).
 * 
 * Note: Wrote this class when first started working with XmlBeans. Likely, the
 * majority could be cleaned up and rely more on XmlBeans than reflection to
 * access properties or create instances.
 * 
 * @author Matthew Young
 * 
 */
public class XmlBeansConverter extends BeanConverter {
	private final static Logger logger = Logger
			.getLogger(XmlBeansConverter.class.getName());

	private static StringEnumAbstractBaseConverter enumConverter = new StringEnumAbstractBaseConverter();

	@SuppressWarnings("unchecked")
	public Object convertInbound(Class paramType, InboundVariable iv,
			InboundContext inctx) throws MarshallException {

		logger.fine("converting inbound variable ["
				+ (iv == null ? null : iv.getValue()) + "] for parameter ["
				+ (paramType == null ? null : paramType.getName())
				+ "] while instanceType ["
				+ (instanceType == null ? null : instanceType.getName()) + "]");

		/**
		 * actual variable text
		 */
		String value = iv.getValue();

		// If the text is null then the whole bean is null
		if (value.trim().equals(ProtocolConstants.INBOUND_NULL)) {
			return null;
		}

		if (!value.startsWith(ProtocolConstants.INBOUND_MAP_START)) {
			throw new MarshallException(paramType, Messages.getString(
					"BeanConverter.FormatError",
					ProtocolConstants.INBOUND_MAP_START));
		}

		if (!value.endsWith(ProtocolConstants.INBOUND_MAP_END)) {
			throw new MarshallException(paramType, Messages.getString(
					"BeanConverter.FormatError",
					ProtocolConstants.INBOUND_MAP_START));
		}

		/**
		 * variable text minus brackets
		 */
		value = value.substring(1, value.length() - 1);

		try {
			/**
			 * instanceType almost has to exist otherwise it will be impossible
			 * to determine which extending class to a higher schema document is
			 * being processed
			 */
			if (instanceType != null) {
				logger
						.fine("instanceType null which likely means that the javascript parameter was not set");
			}

			Object bean = null;
			Class beanClass = instanceType == null ? paramType : instanceType;

			/**
			 * Access Factory with XMLObjects to create instance and the
			 * instanceType is used rather than the paramType since the
			 * parameter is likely a root schema interface
			 */
			Class[] innerClasses = beanClass.getClasses();
			Class factory = null;
			for (Class aClass : innerClasses) {
				if (aClass.getName().endsWith("Factory")) {
					factory = aClass;
				}
			}

			// exception if Factory inner class not present
			if (factory == null) {
				logger.severe("XmlObject.Factory method not found for Class ["
						+ beanClass.toString() + "]");
				throw new MarshallException(beanClass,
						"XmlObject.Factory method not found");
			}

			// invoke newInstance method with XmlObject.Factory
			Class[] emptyArglist = new Class[0];
			Method newInstance = factory.getMethod("newInstance", emptyArglist);
			bean = newInstance.invoke(null, (Object[]) emptyArglist);

			// add bean to context
			inctx.addConverted(iv, beanClass, bean);
			logger.fine("added bean ["
					+ (bean == null ? null : beanClass.getName())
					+ "] to context");

			/**
			 * find properties from interface (local method below)
			 */
			Map properties = getPropertyMapFromClass(beanClass, false, true);

			/**
			 * loop through the properties passed in
			 */
			Map tokens = extractInboundTokens(beanClass, value);
			for (Iterator it = tokens.entrySet().iterator(); it.hasNext();) {
				// get key (variable property name) and value
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				String val = (String) entry.getValue();
				logger.fine("inbound token (iterated from variable value) ["
						+ key + "] with value [" + val + "]");

				// find in property list
				Property property = (Property) properties.get(key);
				if (property == null) {
					logger
							.warning("Missing java bean property to match javascript property: "
									+ key
									+ ". For causes see debug level logs:");

					logger
							.fine("- The javascript may be refer to a property that does not exist");
					logger.fine("- You may be missing the correct setter: set"
							+ Character.toTitleCase(key.charAt(0))
							+ key.substring(1) + "()");
					logger
							.fine("- The property may be excluded using include or exclude rules.");

					StringBuffer all = new StringBuffer();
					for (Iterator pit = properties.keySet().iterator(); pit
							.hasNext();) {
						all.append(pit.next());
						if (pit.hasNext()) {
							all.append(',');
						}
					}
					logger.fine("fields exist for properties [" + all
							+ "] - continue to next inbound token");
					continue;
				}

				// get type from property
				Class propType = property.getPropertyType();
				logger.fine("property associated to token has type ["
						+ propType.getName() + "]");

				// split value
				String[] split = ParseUtil.splitInbound(val);
				String splitValue = split[LocalUtil.INBOUND_INDEX_VALUE];
				String splitType = split[LocalUtil.INBOUND_INDEX_TYPE];
				logger.fine("split value [" + splitValue + "] with type ["
						+ splitType + "] to create inbound variable");

				// build a nested value from split value and give a hint
				// as to the context of the nested value
				InboundVariable nested = new InboundVariable(iv.getLookup(),
						null, splitType, splitValue);
				logger.fine("value transformed into an inbound variable ["
						+ (nested == null ? null : nested.toString()) + "]");
				TypeHintContext incc = createTypeHintContext(inctx, property);
				logger.fine("hint to type converter ["
						+ (incc == null ? null : incc.getExtraTypeInfo()
								.getName()) + "]");

				// set property with write method inside the bean after
				// recursive
				// call to nested value
				Object output = converterManager.convertInbound(propType,
						nested, inctx, incc);
				property.setValue(bean, output);
			}

			// log xml-fragement (testing)
			XmlObject xmlObject = (XmlObject) bean;
			logger.fine("returning bean [" + bean.getClass().getName()
					+ "] with xml-fragement [" + xmlObject.xmlText() + "]");

			return bean;
		} catch (MarshallException e) {
			logger.severe("marshalling exception: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.severe("marshalling exception with parameter type ["
					+ paramType.getName() + "] and instance type ["
					+ instanceType.getName() + "]: " + e.getMessage());
			throw new MarshallException(paramType, e);
		}
	}

	@SuppressWarnings("unchecked")
	public OutboundVariable convertOutbound(Object data, OutboundContext outctx)
			throws MarshallException {
		logger.fine("converting outbound variable type ["
				+ (data == null ? null : data.getClass().getName()) + "]");

		// Where we collect out converted children
		Map ovs = new TreeMap();

		// We need to do this before collecing the children to save recurrsion
		ObjectOutboundVariable ov = new ObjectOutboundVariable(outctx);
		outctx.put(data, ov);

		try {
			Map properties = getPropertyMapFromObject(data, true, false);
			for (Iterator it = properties.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String name = (String) entry.getKey();
				Property property = (Property) entry.getValue();

				Object value = property.getValue(data);
				logger.fine("value ["
						+ (value == null ? null : value.toString())
						+ "] for property [" + name + "]");
				OutboundVariable nested = getConverterManager()
						.convertOutbound(value, outctx);

				ovs.put(name, nested);
			}
		} catch (MarshallException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new MarshallException(data.getClass(), ex);
		}

		ov.init(ovs, getJavascript());

		return ov;
	}

	@SuppressWarnings("unchecked")
	public Map getPropertyMapFromClass(Class paramType, boolean readRequired,
			boolean writeRequired) throws MarshallException {

		try {
			Map properties = new HashMap();

			/**
			 * confirm the the object being converted is XmlObject
			 */
			if (!XmlObject.class.isAssignableFrom(paramType)) {
				throw new MarshallException(paramType, "class ("
						+ paramType.getName()
						+ ") not assignable from XmlObject");
			}

			/**
			 * get interfaces to pull only XML element/attribute properties
			 * rather than implementation specifics
			 */
			Class beanInterface = null;
			if (paramType.isInterface()) {
				beanInterface = paramType;
			} else {
				beanInterface = paramType.getInterfaces()[0];
			}
			Class superInterface = (Class) beanInterface.getGenericInterfaces()[0];

			/**
			 * loop through heirarchy of interfaces (extended beans)
			 */
			while (XmlObject.class.isAssignableFrom(superInterface)) {
				BeanInfo info = Introspector.getBeanInfo(beanInterface);
				PropertyDescriptor[] descriptors = info
						.getPropertyDescriptors();

				for (int i = 0; i < descriptors.length; i++) {
					PropertyDescriptor descriptor = descriptors[i];
					String name = descriptor.getName();
					String type = descriptor.getPropertyType().getName();

					// register Enum types
					if (type.matches(".*\\$Enum")) {
						getConverterManager().addConverter(type, enumConverter);
					}

					// We don't marshall getClass()
					if (name.equals("class")) {
						continue;
					}

					// Access rules mean we might not want to do this one
					if (!isAllowedByIncludeExcludeRules(name)) {
						continue;
					}

					if (readRequired && descriptor.getReadMethod() == null) {
						continue;
					}

					if (writeRequired && descriptor.getWriteMethod() == null) {
						continue;
					}

					properties.put(name, new PropertyDescriptorProperty(
							descriptor));
				}

				beanInterface = (Class) beanInterface.getGenericInterfaces()[0];
				superInterface = (Class) beanInterface.getGenericInterfaces()[0];
			}

			return properties;
		} catch (IntrospectionException ex) {
			throw new MarshallException(paramType, ex);
		}

	}
}
