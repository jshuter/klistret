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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.klistret.cmdb.exception.InfrastructureException;

public class ContextHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(ContextHelper.class);

	private Class<?>[] baseTypes;

	private Package[] basePackages;

	private JAXBContext jaxbContext;

	private Map<QName, ? extends RuntimeClassInfo> runtimeTypeInfoSet;

	public ContextHelper(Package[] basePackages, Class<?>[] baseTypes) {
		this.basePackages = basePackages;
		this.baseTypes = baseTypes;

		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false);

		for (Class<?> baseType : baseTypes)
			provider.addIncludeFilter(new AssignableTypeFilter(baseType));

		Set<BeanDefinition> beans = new HashSet<BeanDefinition>();

		for (Package basePackage : basePackages)
			beans.addAll(provider
					.findCandidateComponents(basePackage.getName()));

		setContext(beans);
	}

	@SuppressWarnings("unchecked")
	private void setContext(Set<BeanDefinition> beans) {
		String contextPath = null;
		
		Class<?>

		for (BeanDefinition bean : beans) {
			String packageName = bean.getBeanClassName().substring(0,
					bean.getBeanClassName().lastIndexOf("."));
			contextPath = contextPath == null ? packageName : String.format(
					"%s:%s", contextPath, packageName);
			
			Class.forName(bean.getBeanClassName());
		}

		try {
			logger.debug("Instatiate JAXBContext with context path [{}]",
					contextPath);
			jaxbContext = JAXBContext.newInstance(contextPath);
		} catch (JAXBException e) {
			throw new InfrastructureException(String.format(
					"Unable instantiate JAXBContext with context path [%s]",
					contextPath), e);
		}
		
		try {
			RuntimeTypeInfoSet runtimeTypeInfoSet = JAXBModelFactory
					.create(contextPath);

			Map<Class, ? extends RuntimeClassInfo> beanMapByClass = runtimeTypeInfoSet
					.beans();
		} catch (IllegalAnnotationsException e) {
			throw new InfrastructureException(
					String
							.format("Unable instantiate JAXBModelFactory with context path"),
					e);
		}
	}

	public Package[] getBasePackages() {
		return this.basePackages;
	}

	public Class<?>[] getBaseType() {
		return this.baseTypes;
	}

	public JAXBContext getJAXBContext() {
		return this.jaxbContext;
	}
}
