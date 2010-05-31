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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.core.BuiltinLeafInfo;
import org.jvnet.jaxb.reflection.model.core.PropertyKind;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeAttributePropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeClassInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeElementPropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimePropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;
import org.jvnet.jaxb.reflection.util.QNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.pojo.XMLAttributeProperty;
import com.klistret.cmdb.pojo.XMLElementProperty;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.spring.ClassPathScanningCandidateDefinitionProvider;

public class JAXBContextHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(JAXBContextHelper.class);

	/**
	 * Packages potentially containing beans assignable from the set of base
	 * types
	 */
	private String[] assignablePackages;

	/**
	 * Base types are roots of XML hierarchy
	 */
	private String[] baseTypes;

	/**
	 * Context path comprised of classes found by the Spring Bean scanner
	 */
	private Set<Class<?>> contextPath = new HashSet<Class<?>>();

	/**
	 * JAXB context (expensive to build)
	 */
	private JAXBContext jaxbContext;

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
	public JAXBContextHelper(String[] baseTypes, String[] assignablePackages) {
		this.baseTypes = baseTypes;
		this.assignablePackages = assignablePackages;

		ClassPathScanningCandidateDefinitionProvider provider = new ClassPathScanningCandidateDefinitionProvider(
				false);

		for (String baseType : baseTypes) {
			baseType = baseType.trim();
			logger.debug("assigning type filter for base type [{}]", baseType);
			try {
				provider.addIncludeFilter(new AssignableTypeFilter(Class
						.forName(baseType)));
			} catch (ClassNotFoundException e) {
				logger.debug("base type [{}] class not present to classloader",
						baseType);
			}
		}

		/**
		 * Set is used since the number of identifiable beans is unknown and
		 * user specific
		 */
		Set<BeanDefinition> candidateBeans = new HashSet<BeanDefinition>();
		for (String assignablePackage : assignablePackages) {
			logger.debug(
					"adding beans from package [{}] to a context collection",
					assignablePackage);
			candidateBeans.addAll(provider
					.findCandidateComponents(assignablePackage));
		}

		/**
		 * Add all beans into the context path (which essentially build on the
		 * package name of the class so all class in the package are captured)
		 */
		for (BeanDefinition beanDefinition : candidateBeans) {
			logger.debug("adding bean [{}] to context path", beanDefinition
					.getBeanClassName());
			try {
				contextPath.add(Class
						.forName(beanDefinition.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				throw new InfrastructureException(String.format(
						"Class unloadable [%s] into the context path",
						beanDefinition.getBeanClassName()), e);
			}
		}

		/**
		 * Define XML beans
		 */
		try {
			xmlBeans = new QNameMap<XMLBean>();

			RuntimeTypeInfoSet runtimeTypeInfoSet = JAXBModelFactory
					.create(contextPath.toArray(new Class[0]));

			translateBeans(runtimeTypeInfoSet, xmlBeans);
		} catch (IllegalAnnotationsException e) {
			throw new InfrastructureException(
					String
							.format(
									"Unable instantiate JAXBModelFactory with context path [%s]",
									contextPath), e);
		}
	}

	/**
	 * 
	 * @return
	 */
	public String[] getAssignablePackages() {
		return this.assignablePackages;
	}

	/**
	 * 
	 * @return
	 */
	public String[] getBaseTypes() {
		return this.baseTypes;
	}

	/**
	 * 
	 * @return
	 */
	public JAXBContext getJAXBContext() {
		if (jaxbContext == null) {
			try {
				jaxbContext = JAXBContext.newInstance(contextPath
						.toArray(new Class[0]));
			} catch (JAXBException e) {
				throw new InfrastructureException(
						String
								.format(
										"Unable instantiate JAXBContext with context path [%s]",
										contextPath), e);
			}
		}

		return jaxbContext;
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
		xmlBean.setName(beanInfo.getTypeName());
		xmlBean.setClazz(beanInfo.getClazz());

		if (beanInfo.getType() instanceof BuiltinLeafInfo)
			xmlBean.setType(((BuiltinLeafInfo<?, ?>) beanInfo.getType())
					.getTypeName());

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
								entry.getValue().getName()))
					entry.getValue().getExtending().add(
							other.getValue().getName());
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
			if (entry.getValue().getName().equals(qname))
				return entry.getValue();
		}

		return null;
	}
}
