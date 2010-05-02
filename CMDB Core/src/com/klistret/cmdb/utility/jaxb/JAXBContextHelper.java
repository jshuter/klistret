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
import javax.xml.namespace.QName;

import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeClassInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;
import org.jvnet.jaxb.reflection.util.QNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.klistret.cmdb.exception.InfrastructureException;

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
	 * JAXB context (expensive to build)
	 */
	private JAXBContext jaxbContext;

	/**
	 * Map keyed off qname rather than class names since the primary method for
	 * searching is XPath
	 */
	private QNameMap<RuntimeClassInfo> beanMapByQName;

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

		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
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
		Set<BeanDefinition> beans = new HashSet<BeanDefinition>();

		for (String assignablePackage : assignablePackages) {
			logger.debug(
					"adding beans from package [{}] to a context collection",
					assignablePackage);
			beans.addAll(provider.findCandidateComponents(assignablePackage));
		}

		setContext(beans);
	}

	/**
	 * Defines a JAXBContext as well a reflection context
	 * 
	 * @param beans
	 */
	@SuppressWarnings("unchecked")
	private void setContext(Set<BeanDefinition> beans) {
		Set<Class<?>> contextPath = new HashSet<Class<?>>();

		/**
		 * Add all beans into the context path (which essentially build on the
		 * package name of the class so all class in the package are captured)
		 */
		for (BeanDefinition bean : beans) {
			logger.debug("adding bean [{}] to context path", bean
					.getBeanClassName());
			try {
				contextPath.add(Class.forName(bean.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				throw new InfrastructureException(String.format(
						"Class unloadable [%s] into the context path", bean
								.getBeanClassName()), e);
			}
		}

		/**
		 * Generate a JAXBContext
		 */
		try {
			jaxbContext = JAXBContext.newInstance(contextPath
					.toArray(new Class[0]));
		} catch (JAXBException e) {
			throw new InfrastructureException(String.format(
					"Unable instantiate JAXBContext with context path [%s]",
					contextPath), e);
		}

		/**
		 * Reflection module to have runtime class information by qname
		 */
		try {
			RuntimeTypeInfoSet runtimeTypeInfoSet = JAXBModelFactory
					.create(contextPath.toArray(new Class[0]));

			Map<Class, ? extends RuntimeClassInfo> beanMapByClass = runtimeTypeInfoSet
					.beans();
			beanMapByQName = new QNameMap();

			for (Map.Entry<Class, ? extends RuntimeClassInfo> beanMapByClassEntry : beanMapByClass
					.entrySet()) {
				RuntimeClassInfo runtimeClassInfo = beanMapByClassEntry
						.getValue();

				logger.debug("adding bean [{}] to map by qname [{}] ",
						runtimeClassInfo.getClazz().getName(), runtimeClassInfo
								.getElementName());
				beanMapByQName.put(runtimeClassInfo.getElementName(),
						runtimeClassInfo);
			}
		} catch (IllegalAnnotationsException e) {
			throw new InfrastructureException(
					String
							.format(
									"Unable instantiate JAXBModelFactory with context path [%s]",
									contextPath), e);
		}
	}

	public String[] getAssignablePackages() {
		return this.assignablePackages;
	}

	public String[] getBaseTypes() {
		return this.baseTypes;
	}

	public JAXBContext getJAXBContext() {
		return this.jaxbContext;
	}

	public RuntimeClassInfo getRuntimeClassInfo(QName qname) {
		return this.beanMapByQName.get(qname);
	}
}
