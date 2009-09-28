package com.klistret.cmdb.utility.jaxb;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.xml.sax.ErrorHandler;

import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;

import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlSchema;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

/**
 * Page
 * http://wiki.glassfish.java.net/Wiki.jsp?page=Jaxb2CommonsUsingAndDeveloping
 * give more information about the extended plugin class
 * 
 * @author Matthew Young
 * 
 */
public class XmlNSMapPlugin extends Plugin {

	private static final Logger logger = LoggerFactory
			.getLogger(XmlNSMapPlugin.class);

	@Override
	public String getOptionName() {
		return "XxmlNSMap";
	}

	@Override
	public String getUsage() {
		return ""
				+ "-XxmlNSMap:  adds resteasy XmlNsMap annotations for element/relation pojo ";
	}

	private void setXmlNSMap(String className, JDefinedClass implClass) {
		JAnnotationUse mappedJAnnotation = implClass.annotate(Mapped.class);
		JAnnotationArrayMember namespaceMapParam = mappedJAnnotation
				.paramArray("namespaceMap");

		try {
			logger.debug("defining namespace map for classname: {}", className);
			ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
					false);

			Class<?> baseClass = Class.forName(className);

			provider.addIncludeFilter(new AssignableTypeFilter(baseClass));

			logger.debug("Scanning off of package name: {}", baseClass
					.getPackage().getName());
			Set<BeanDefinition> beans = provider
					.findCandidateComponents(baseClass.getPackage().getName());
			logger.debug("Spring scanner found {} candidate components", beans
					.size());

			for (BeanDefinition bean : beans) {
				Class<?> beanClass = Class.forName(bean.getBeanClassName());

				logger.debug(
						"adding namespace json mapping for bean [name: {}]",
						bean.getBeanClassName());

				for (Annotation packageAnnotation : beanClass.getPackage()
						.getAnnotations()) {
					if (packageAnnotation instanceof XmlSchema) {
						JAnnotationUse xmlNsMapJAnnotation = namespaceMapParam
								.annotate(XmlNsMap.class);
						xmlNsMapJAnnotation.param("namespace",
								((XmlSchema) packageAnnotation).namespace());
						xmlNsMapJAnnotation.param("jsonName", beanClass
								.getPackage().getName());
					}
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException: {}", e.getMessage());
		} catch (BeanDefinitionStoreException e) {
			logger.error("BeanDefinitionStoreException: {}", e.getMessage());
		}
	}

	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {

		for (ClassOutline co : model.getClasses()) {
			logger.debug("processing ClassOutline [name: {}, package: {}]",
					co.implClass.name(), co.implClass.getPackage().name());

			if (co.implClass.name().equals("Element")
					&& co.implClass.getPackage().name().equals(
							"com.klistret.cmdb.pojo"))
				setXmlNSMap("com.klistret.cmdb.Element", co.implClass);

			if (co.implClass.name().equals("Relation")
					&& co.implClass.getPackage().name().equals(
							"com.klistret.cmdb.pojo"))
				setXmlNSMap("com.klistret.cmdb.Relation", co.implClass);
		}

		return true;
	}
}
