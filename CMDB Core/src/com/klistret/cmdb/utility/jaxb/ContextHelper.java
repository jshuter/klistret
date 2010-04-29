package com.klistret.cmdb.utility.jaxb;

import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.klistret.cmdb.exception.InfrastructureException;

public class ContextHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(ContextHelper.class);

	private String[] assignablePackages;

	private String[] baseTypes;

	private JAXBContext jaxbContext;

	private Map<QName, RuntimeClassInfo> beanMapByQName;

	public ContextHelper(String[] baseTypes, String[] assignablePackages) {
		this.baseTypes = baseTypes;
		this.assignablePackages = assignablePackages;

		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false);

		for (String baseType : baseTypes) {
			logger.debug("assigning type filter for base type [{}]", baseType);
			try {
				provider.addIncludeFilter(new AssignableTypeFilter(Class
						.forName(baseType)));
			} catch (ClassNotFoundException e) {
				logger.debug("base type [{}] class not present to classloader",
						baseType);
			}
		}

		Set<BeanDefinition> beans = new HashSet<BeanDefinition>();

		for (String assignablePackage : assignablePackages) {
			logger.debug(
					"adding beans from package [{}] to a context collection",
					assignablePackage);
			beans.addAll(provider.findCandidateComponents(assignablePackage));
		}

		setContext(beans);
	}

	@SuppressWarnings("unchecked")
	private void setContext(Set<BeanDefinition> beans) {
		Set<Class<?>> contextPath = new HashSet<Class<?>>();

		for (BeanDefinition bean : beans) {
			logger.debug("adding bean [{}] to context path", bean
					.getBeanClassName());
			try {
				contextPath.add(Class.forName(bean.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		try {
			Class<?>[] dummy = contextPath.toArray(new Class[0]);
			jaxbContext = JAXBContext.newInstance(dummy);
		} catch (JAXBException e) {
			throw new InfrastructureException(String.format(
					"Unable instantiate JAXBContext with context path [%s]",
					contextPath), e);
		}

		try {
			RuntimeTypeInfoSet runtimeTypeInfoSet = JAXBModelFactory
					.create(contextPath.toArray(new Class[0]));

			Map<Class, ? extends RuntimeClassInfo> beanMapByClass = runtimeTypeInfoSet
					.beans();
			beanMapByQName = new HashMap();

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

	public Class<?> getClass(QName qname) {
		RuntimeClassInfo runtimeClassInfo = beanMapByQName.get(qname);

		if (runtimeClassInfo == null)
			return null;
		else
			return runtimeClassInfo.getClazz();
	}
}
