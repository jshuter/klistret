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

import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeClassInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimePropertyInfo;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;
import org.jvnet.jaxb.reflection.util.QNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.pojo.ElementNode;
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
	private QNameMap<ElementNode> elementNodes;

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
	private ElementNode getElementNode(RuntimeClassInfo runtimeClassInfo) {
		ElementNode elementNode = new ElementNode();

		elementNode.setName(runtimeClassInfo.getElementName());
		elementNode.setClassName(runtimeClassInfo.getName());
		elementNode.setTypeName(runtimeClassInfo.getTypeName());

		/**
		 * Unknown right now what to do with namespaces
		 */

		elementNode.setAbstract(runtimeClassInfo.isAbstract());
		elementNode.setFinal(runtimeClassInfo.isFinal());
		elementNode.setSimpleType(runtimeClassInfo.isSimpleType());

		RuntimeClassInfo baseClass = runtimeClassInfo.getBaseClass();

		for (RuntimePropertyInfo runtimePropertyInfo : runtimeClassInfo
				.getProperties()) {
			runtimePropertyInfo.getRawType();
		}

		return elementNode;
	}

	@SuppressWarnings("unchecked")
	private Set<RuntimeClassInfo> findBeans(
			RuntimeTypeInfoSet runtimeTypeInfoSet, RuntimeClassInfo baseType) {
		Set<RuntimeClassInfo> beans = new HashSet<RuntimeClassInfo>();

		for (Map.Entry<Class, ? extends RuntimeClassInfo> entry : runtimeTypeInfoSet
				.beans().entrySet()) {
			RuntimeClassInfo runtimeClassInfo = entry.getValue();

			if (runtimeClassInfo.getBaseClass().equals(baseType))
				beans.add(runtimeClassInfo);
		}

		return beans;
	}

	private void tranlateBeans(RuntimeTypeInfoSet runtimeTypeInfoSet,
			RuntimeClassInfo baseType) {
		Set<RuntimeClassInfo> beans = findBeans(runtimeTypeInfoSet, baseType);

		for (RuntimeClassInfo bean : beans) {
			ElementNode elementNode = getElementNode(bean);

			Set<RuntimeClassInfo> extentions = findBeans(runtimeTypeInfoSet,
					bean);
			for (RuntimeClassInfo extending : extentions) {
				tranlateBeans(runtimeTypeInfoSet, bean);
				
			}

			elementNodes.put(elementNode.getName(), elementNode);
		}
	}

	/**
	 * 
	 * @return
	 */
	public QNameMap<ElementNode> getElementNodes() {
		if (elementNodes == null) {
			try {
				elementNodes = new QNameMap<ElementNode>();

				RuntimeTypeInfoSet runtimeTypeInfoSet = JAXBModelFactory
						.create(contextPath.toArray(new Class[0]));

				/**
				 * Start with the roots of hierarchy and recursively work
				 * downward
				 */
				tranlateBeans(runtimeTypeInfoSet, null);
			} catch (IllegalAnnotationsException e) {
				throw new InfrastructureException(
						String
								.format(
										"Unable instantiate JAXBModelFactory with context path [%s]",
										contextPath), e);
			}
		}

		return elementNodes;
	}
}
