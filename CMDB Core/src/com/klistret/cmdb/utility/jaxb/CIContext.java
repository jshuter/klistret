package com.klistret.cmdb.utility.jaxb;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.annotations.ci.Element;
import com.klistret.cmdb.annotations.ci.Relation;
import com.klistret.cmdb.exception.InfrastructureException;

public class CIContext {
	/**
	 * Based on
	 * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
	 */
	private volatile static CIContext ciContext;

	private static final Logger logger = LoggerFactory
			.getLogger(CIContext.class);

	private Set<Class<?>> elements;

	private Set<Class<?>> relations;

	private JAXBContext jaxbContext;

	private RuntimeTypeInfoSet runtimeTypeInfoSet;

	/**
	 * Singleton constructor
	 */
	private CIContext() {
		/**
		 * Using scannotation model to find classes with particular annotations
		 */
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.getUrlsForCurrentClasspath())
				.setScanners(new TypeAnnotationsScanner()));

		/**
		 * JAXB context path
		 */
		Set<Class<?>> contextPath = new HashSet<Class<?>>();

		/**
		 * Find all of the CI elements
		 */
		elements = reflections.getTypesAnnotatedWith(Element.class);
		for (Class<?> element : elements) {
			contextPath.add(element);
			logger.debug("Adding element {} to JAXB context path", element
					.getName());
		}

		/**
		 * Find all of the CI relations
		 */
		relations = reflections.getTypesAnnotatedWith(Relation.class);
		for (Class<?> relation : relations) {
			contextPath.add(relation);
			logger.debug("Adding relation {} to JAXB context path", relation
					.getName());
		}

		/**
		 * Construct the JAXContext
		 */
		try {
			jaxbContext = JAXBContext.newInstance(contextPath
					.toArray(new Class[0]));
			logger.debug("Created JAXB context");
		} catch (JAXBException e) {
			logger.error("Unable to create JAXBContext: {}", e.getMessage());
			throw new InfrastructureException("Unable to create JAXBContext", e);
		}

		try {
			runtimeTypeInfoSet = JAXBModelFactory.create(contextPath
					.toArray(new Class[0]));
			logger.debug("Created JAXB runtime type info set");
		} catch (IllegalAnnotationsException e) {
			logger.error("Unable to create JAXB reflection info set: {}", e
					.getMessage());
			throw new InfrastructureException(
					"Unable to create JAXB reflection info set", e);
		}
	}

	/**
	 * Forces a singleton instance of this object
	 * 
	 * @return
	 */
	public static CIContext getCIContext() {
		if (ciContext == null) {
			synchronized (CIContext.class) {
				if (ciContext == null)
					ciContext = new CIContext();
			}
		}
		return ciContext;
	}

	public JAXBContext getJAXBContext() {
		return jaxbContext;
	}

	public RuntimeTypeInfoSet getRuntimeTypeInfoSet() {
		return runtimeTypeInfoSet;
	}
}
