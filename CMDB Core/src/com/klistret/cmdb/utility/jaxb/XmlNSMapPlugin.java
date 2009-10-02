package com.klistret.cmdb.utility.jaxb;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.xml.sax.ErrorHandler;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlSchema;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;

/**
 * Page
 * http://wiki.glassfish.java.net/Wiki.jsp?page=Jaxb2CommonsUsingAndDeveloping
 * give more information about the extended plugin class
 * 
 * @author Matthew Young
 * 
 */
public class XmlNSMapPlugin extends AbstractParameterizablePlugin {

	private static final Logger logger = LoggerFactory
			.getLogger(XmlNSMapPlugin.class);

	private static ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
			false);

	private File archive;

	@Override
	public String getOptionName() {
		return "XxmlNSMap";
	}

	@Override
	public String getUsage() {
		return ""
				+ "-XxmlNSMap:  adds resteasy XmlNsMap annotations for element/relation pojo "
				+ "-XxmlNSMap-archive: archive containing CI hierarchy";
	}

	public void setArchive(File archive) {
		ClassLoader providerCL = provider.getResourceLoader().getClassLoader();
		ClassLoader currentCL = this.getClass().getClassLoader();
		try {
			Method m = currentCL.getClass().getDeclaredMethod("addPathFile",
					new Class[] { File.class });

			logger
					.debug(
							"Executing inside Ant, adding {} to classpath for plugin and the Spring resource loader",
							archive.getAbsolutePath());

			m.setAccessible(true);

			m.invoke(currentCL, new Object[] { archive });
			m.invoke(providerCL, new Object[] { archive });

		} catch (Exception e) {
			logger.error("Exception adding jar to classpath: {}", e
					.getMessage());
		}

		this.archive = archive;
	}

	public File getArchive() {
		return this.archive;
	}

	private void setXmlNSMap(String className, JDefinedClass implClass,
			String implNamespace) {
		JAnnotationUse mappedJAnnotation = implClass.annotate(Mapped.class);
		JAnnotationArrayMember namespaceMapParam = mappedJAnnotation
				.paramArray("namespaceMap");

		try {
			JAnnotationUse xmlNsMapJAnnotation = namespaceMapParam
					.annotate(XmlNsMap.class);
			xmlNsMapJAnnotation.param("namespace", implNamespace);
			xmlNsMapJAnnotation
					.param("jsonName", implClass.getPackage().name());

			logger.debug("defining namespace map for classname: {}", className);
			Class<?> baseClass = Class.forName(className);

			provider.addIncludeFilter(new AssignableTypeFilter(baseClass));

			logger.debug("Scanning off of package name: {}", baseClass
					.getPackage().getName());
			Set<BeanDefinition> beans = provider
					.findCandidateComponents(baseClass.getPackage().getName());
			logger.debug("Spring scanner found {} candidate components", beans
					.size());

			/**
			 * Add JSON Namespace mapping for subclasses of className pulling
			 * information from the JAXB generated XmlSchema annotation.
			 */
			for (BeanDefinition bean : beans) {
				Class<?> beanClass = Class.forName(bean.getBeanClassName());

				for (Annotation packageAnnotation : beanClass.getPackage()
						.getAnnotations()) {
					if (packageAnnotation instanceof XmlSchema) {
						logger
								.debug(
										"adding namespace json mapping for bean [name: {}]",
										bean.getBeanClassName());

						xmlNsMapJAnnotation = namespaceMapParam
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

			if (co.implClass.fullName()
					.equals("com.klistret.cmdb.pojo.Element"))
				setXmlNSMap("com.klistret.cmdb.Element", co.implClass, co
						._package().getMostUsedNamespaceURI());

			if (co.implClass.fullName().equals(
					"com.klistret.cmdb.pojo.Relation"))
				setXmlNSMap("com.klistret.cmdb.Relation", co.implClass, co
						._package().getMostUsedNamespaceURI());
		}

		return true;
	}
}
