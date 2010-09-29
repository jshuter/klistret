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

package com.klistret.cmdb.utility.jaxb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.jvnet.jaxb.reflection.model.core.BuiltinLeafInfo;
import org.jvnet.jaxb.reflection.model.core.PropertyKind;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeAttributePropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeClassInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeElementPropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimePropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.util.QNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.pojo.XMLAttributeProperty;
import com.klistret.cmdb.pojo.XMLElementProperty;
import com.klistret.cmdb.pojo.XMLProperty;
import com.klistret.cmdb.pojo.XMLBean;

/**
 * 
 * Utilizes the Spring class scanning module to filter on the supplied base
 * types against the assignable packages (thereby limiting the search to a
 * reasonable load) to get a list of CIs as POJOs (including the POJO transport
 * objects). A hierarchy of these CIs is then built up using the JAXB2
 * reflection project.
 * 
 * @author Matthew Young
 * 
 */
public class CIContextHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(CIContextHelper.class);

	/**
	 * Map keyed off qname rather than class names since the primary method for
	 * searching is XPath
	 */
	private QNameMap<XMLBean> xmlBeans;

	/**
	 * Relies on Spring class path scanning module to filter out beans based on
	 * assignment (ie. extending) in the set of packages.
	 * 
	 * @param baseTypes
	 * @param assignablePackages
	 */
	public CIContextHelper() {
		RuntimeTypeInfoSet runtimeTypeInfoSet = CIContext.getCIContext()
				.getRuntimeTypeInfoSet();

		logger.debug("Translating runtime type info into XML beans");
		xmlBeans = new QNameMap<XMLBean>();
		translateBeans(runtimeTypeInfoSet, xmlBeans);
	}

	/**
	 * 
	 * @return
	 */
	public JAXBContext getJAXBContext() {
		return CIContext.getCIContext().getJAXBContext();
	}
	
	public Schema getSchemaGrammers() {
		return CIContext.getCIContext().getSchemaGrammers();
	}

	/**
	 * 
	 * @param runtimeClassInfo
	 * @return
	 */
	private XMLBean getXMLBean(RuntimeClassInfo beanInfo) {
		XMLBean xmlBean = new XMLBean();

		/**
		 * General information (no parent for beans)
		 */
		xmlBean.setType(beanInfo.getTypeName());
		xmlBean.setClazz(beanInfo.getClazz());

		/**
		 * Specific conditions
		 */
		xmlBean.setAbstract(beanInfo.isAbstract());
		xmlBean.setFinal(beanInfo.isFinal());
		xmlBean.setSimpleType(beanInfo.isSimpleType());

		/**
		 * xml root element
		 */
		if (((Class<?>) beanInfo.getClazz())
				.getAnnotation(XmlRootElement.class) != null)
			xmlBean.setXmlRootElement(true);

		/**
		 * Extended base type
		 */
		xmlBean.setExtended(beanInfo.getBaseClass() == null ? null : beanInfo
				.getBaseClass().getTypeName());

		/**
		 * Children/Attributes
		 */
		for (RuntimePropertyInfo runtimePropertyInfo : beanInfo.getProperties()) {
			if (runtimePropertyInfo.kind().equals(PropertyKind.ATTRIBUTE)
					&& runtimePropertyInfo instanceof RuntimeAttributePropertyInfo) {
				RuntimeAttributePropertyInfo attributeInfo = (RuntimeAttributePropertyInfo) runtimePropertyInfo;

				XMLAttributeProperty property = new XMLAttributeProperty();
				property.setName(attributeInfo.getName());
				property.setRequired(attributeInfo.isRequired());

				for (RuntimeTypeInfo typeInfo : attributeInfo.ref()) {
					if (typeInfo instanceof BuiltinLeafInfo)
						property.setType(((BuiltinLeafInfo<?, ?>) typeInfo)
								.getTypeName());
				}

				xmlBean.getProperties().add(property);
			}

			if (runtimePropertyInfo.kind().equals(PropertyKind.ELEMENT)
					&& runtimePropertyInfo instanceof RuntimeElementPropertyInfo) {
				RuntimeElementPropertyInfo elementInfo = (RuntimeElementPropertyInfo) runtimePropertyInfo;

				XMLElementProperty property = new XMLElementProperty();
				property.setName(elementInfo.getName());
				property.setRequired(elementInfo.isRequired());
				property.setValueList(elementInfo.isValueList());

				for (RuntimeTypeInfo typeInfo : elementInfo.ref()) {
					if (typeInfo instanceof BuiltinLeafInfo)
						property.setType(((BuiltinLeafInfo<?, ?>) typeInfo)
								.getTypeName());

					if (typeInfo instanceof RuntimeClassInfo)
						property.setType(((RuntimeClassInfo) typeInfo)
								.getTypeName());
				}

				xmlBean.getProperties().add(property);
			}
		}

		return xmlBean;
	}

	@SuppressWarnings("unchecked")
	private void translateBeans(RuntimeTypeInfoSet runtimeTypeInfoSet,
			QNameMap<XMLBean> xmlBeans) {

		/**
		 * construct nodes with property information from JAXB beans
		 */
		for (Map.Entry<Class, ? extends RuntimeClassInfo> entry : runtimeTypeInfoSet
				.beans().entrySet()) {
			RuntimeClassInfo bean = entry.getValue();

			if (bean.getTypeName() != null)
				xmlBeans.put(bean.getTypeName(), getXMLBean(bean));
		}

		/**
		 * add extending information
		 */
		for (QNameMap.Entry<XMLBean> entry : xmlBeans.entrySet()) {

			for (QNameMap.Entry<XMLBean> other : xmlBeans.entrySet()) {
				if (other.getValue().getExtended() != null
						&& other.getValue().getExtended().equals(
								entry.getValue().getType()))
					entry.getValue().getExtending().add(
							other.getValue().getType());
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public QNameMap<XMLBean> getXMLBeans() {
		return xmlBeans;
	}

	/**
	 * 
	 * @param qname
	 * @return
	 */
	public XMLBean getXMLBean(QName qname) {
		for (QNameMap.Entry<XMLBean> entry : xmlBeans.entrySet()) {
			if (entry.getValue().getType().equals(qname))
				return entry.getValue();
		}

		return null;
	}

	/**
	 * 
	 * @param classname
	 * @return
	 */
	public XMLBean getXMLBean(String classname) {
		for (QNameMap.Entry<XMLBean> entry : xmlBeans.entrySet()) {
			if (entry.getValue().getClazz().getName().equals(classname))
				return entry.getValue();
		}

		return null;
	}

	/**
	 * 
	 * @param xmlBean
	 * @return
	 */
	public List<XMLBean> getAncestors(XMLBean xmlBean) {
		List<XMLBean> ancestors = new ArrayList<XMLBean>();

		while (xmlBean.getExtended() != null) {
			xmlBean = getXMLBean(xmlBean.getExtended());
			ancestors.add(xmlBean);
		}

		return ancestors;
	}

	/**
	 * 
	 * @param propertyOwner
	 * @param propertyType
	 * @return
	 */
	public String suggestPropertyName(QName propertyOwner, QName propertyType) {
		XMLBean xmlBeanOwner = getXMLBean(propertyOwner);
		if (xmlBeanOwner == null)
			throw new ApplicationException(String.format(
					"Property owner [%s] has no corresponding xmlbean",
					propertyOwner));

		XMLBean xmlBeanType = getXMLBean(propertyType);
		if (xmlBeanType == null)
			throw new ApplicationException(String.format(
					"Property type [%s] has no corresponding xmlbean",
					propertyOwner));
		for (XMLProperty xmlProperty : xmlBeanOwner.getProperties()) {
			if (xmlProperty instanceof XMLElementProperty) {
				XMLBean type = xmlBeanType;

				while (type != null) {
					if (type.getType().equals(xmlProperty.getType()))
						return xmlProperty.getName();

					type = type.getExtended() == null ? null : getXMLBean(type
							.getExtended());
				}
			}
		}

		return null;
	}
}
