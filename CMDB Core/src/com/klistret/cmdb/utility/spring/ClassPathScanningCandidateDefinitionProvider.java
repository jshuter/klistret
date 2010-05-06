package com.klistret.cmdb.utility.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

public class ClassPathScanningCandidateDefinitionProvider extends
		ClassPathScanningCandidateComponentProvider {

	public ClassPathScanningCandidateDefinitionProvider(
			boolean useDefaultFilters) {
		super(useDefaultFilters);
	}

	protected boolean isCandidateComponent(
			AnnotatedBeanDefinition beanDefinition) {
		return (beanDefinition.getMetadata().isIndependent());
	}
}
