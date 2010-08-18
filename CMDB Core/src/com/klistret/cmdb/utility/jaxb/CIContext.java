package com.klistret.cmdb.utility.jaxb;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
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

	private AnnotationDB annotationDB;

	private Set<String> elementNames;

	private Set<String> relationNames;

	private JAXBContext jaxbContext;

	private RuntimeTypeInfoSet runtimeTypeInfoSet;

	/**
	 * Singleton constructor
	 */
	private CIContext() {
		/**
		 * Using scannotation model to find classes with particular annotations
		 */
		URL[] urls = ClasspathUrlFinder.findClassPaths();
		annotationDB = new AnnotationDB();

		try {
			annotationDB.scanArchives(urls);
		} catch (IOException e) {
			logger
					.error(
							"Unable to scan classpath to build an annotation database: {}",
							e.getMessage());
			throw new InfrastructureException(
					"Unable to scan classpath to build an annotation database",
					e);
		}

		/**
		 * JAXB context path
		 */
		Set<Class<?>> contextPath = new HashSet<Class<?>>();

		/**
		 * Find all of the CI elements
		 */
		elementNames = annotationDB.getAnnotationIndex().get(
				Element.class.getName());
		for (String name : elementNames) {
			try {
				contextPath.add(Class.forName(name));
				logger.debug("Adding element {} to JAXB context path", name);
			} catch (ClassNotFoundException e) {
				logger
						.error(
								"Element {} class could not be loaded by Class.forName",
								name);
			}
		}

		/**
		 * Find all of the CI relations
		 */
		relationNames = annotationDB.getAnnotationIndex().get(
				Relation.class.getName());
		for (String name : relationNames) {
			try {
				contextPath.add(Class.forName(name));
				logger.debug("Adding relation {} to JAXB context path", name);
			} catch (ClassNotFoundException e) {
				logger
						.error(
								"Relation {} class could not be loaded by Class.forName",
								name);
			}
		}

		/**
		 * Construct the JAXContext
		 */
		try {
			jaxbContext = JAXBContext.newInstance(contextPath
					.toArray(new Class[0]));
		} catch (JAXBException e) {
			logger.error("Unable to create JAXBContext: {}", e.getMessage());
			throw new InfrastructureException("Unable to create JAXBContext", e);
		}

		try {
			runtimeTypeInfoSet = JAXBModelFactory.create(contextPath
					.toArray(new Class[0]));
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
