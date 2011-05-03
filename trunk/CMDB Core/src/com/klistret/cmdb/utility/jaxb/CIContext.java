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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.klistret.cmdb.annotations.ci.Bean;
import com.klistret.cmdb.annotations.ci.Element;
import com.klistret.cmdb.annotations.ci.Relation;
import com.klistret.cmdb.annotations.ci.Proxy;
import com.klistret.cmdb.exception.InfrastructureException;

/**
 * Singleton class that locates all of the CI beans (elements, relations, and
 * their proxies) using the Reflections project. During XJC generation each
 * element, relation and proxy is given an specific annotation that Reflections
 * finding by searching through the loaded archives. A validation Schema is
 * created to enable validation of JAXB objects. Bean metadata with property
 * metadata is built up for each element, relation and proxy. Metadata are
 * simple beans describing the CI items with their QName, type, properties and
 * so forth.
 * 
 * @author Matthew Young
 * 
 */
public class CIContext {
	private static final Logger logger = LoggerFactory
			.getLogger(CIContext.class);

	/**
	 * Based on
	 * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
	 */
	private volatile static CIContext ciContext;

	/**
	 * Selects all schema files with suffix cmdb.xsd
	 */
	private static final Pattern schemaNamePattern = Pattern
			.compile(".*cmdb\\.xsd");

	/**
	 * Set of elements
	 */
	private Set<Class<?>> elements;

	/**
	 * Set of relations
	 */
	private Set<Class<?>> relations;

	/**
	 * Set of CI proxies
	 */
	private Set<Class<?>> proxies;

	/**
	 * Set of schema files
	 */
	private Set<String> schemaLocations;

	/**
	 * Set of schema streams
	 */
	private Set<SchemaStreamSource> schemaStreamSources;

	/**
	 * List of classes used to build the JAXBContext
	 */
	private Set<Class<?>> contextPath;

	/**
	 * Set of metadata beans
	 */
	private Set<CIBean> beans;

	/**
	 * JABContext
	 */
	private JAXBContext jaxbContext;

	/**
	 * Validation Schema object
	 */
	private Schema schemaGrammers;


	/**
	 * Singleton constructor
	 */
	private CIContext() {
		/**
		 * Eliminate invalid URLs (tested the Commons Validate suite but the
		 * file schema was not supported)
		 */
		Set<URL> candidates = ClasspathHelper.getUrlsForCurrentClasspath();
		Set<URL> validations = new HashSet<URL>();
		for (URL candidate : candidates) {
			try {
				URLConnection connection = candidate.openConnection();
				connection.connect();

				if (connection instanceof HttpURLConnection) {
					HttpURLConnection httpConnection = (HttpURLConnection) connection;
					int code = httpConnection.getResponseCode();

					if (code == HttpURLConnection.HTTP_OK)
						validations.add(candidate);
					else
						logger
								.warn(
										"URL [{}] connection response code [{}] elimated from valid URLs for Scannoation",
										candidate.toString(), code);
				} else {
					if (connection.getContentLength() > 0)
						validations.add(candidate);
					else
						logger
								.warn(
										"URL [{}] content length [{}] elimated from valid URLs for Scannoation",
										candidate.toString(), connection
												.getContentLength());
				}
			} catch (IOException e) {
				logger
						.warn(
								"URL [{}] connect failed [{}] elimated from valid URLs for Scannoation",
								candidate.toString(), e.getMessage());
			}
		}
		logger.warn("{} URLs elimated from {} candidates", candidates.size()
				- validations.size(), candidates.size());

		/**
		 * Using scannotation model to find classes with particular annotations
		 * (noteworthly if the SubTypesScanner isn't included then the Inherited
		 * annotation is not utilized)
		 */
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(validations).setScanners(new TypeAnnotationsScanner(),
						new SubTypesScanner(), new ResourcesScanner()));

		/**
		 * JAXB context path
		 */
		contextPath = new HashSet<Class<?>>();

		/**
		 * Find all of the CI elements
		 */
		elements = reflections.getTypesAnnotatedWith(Element.class);
		for (Class<?> element : elements) {
			if (contextPath.add(element))
				logger.debug("Adding element {} to JAXB context path", element
						.getName());
		}

		/**
		 * Find all of the CI relations
		 */
		relations = reflections.getTypesAnnotatedWith(Relation.class);
		for (Class<?> relation : relations) {
			if (contextPath.add(relation))
				logger.debug("Adding relation {} to JAXB context path",
						relation.getName());
		}

		/**
		 * Find all of the CI proxies
		 */
		proxies = reflections.getTypesAnnotatedWith(Proxy.class);
		for (Class<?> proxy : proxies) {
			if (contextPath.add(proxy))
				logger.debug("Adding proxy {} to JAXB context path", proxy
						.getName());
		}

		/**
		 * Locate potential CMDB schema files
		 */
		schemaStreamSources = new HashSet<SchemaStreamSource>();

		schemaLocations = reflections.getResources(schemaNamePattern);
		for (String location : schemaLocations) {
			if (schemaStreamSources.add(new SchemaStreamSource(location)))
				logger.debug("Adding schema {} to binding schema object",
						location);
		}

		/**
		 * Construct binding Schema
		 */
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		factory.setResourceResolver(new SimpleResolver(schemaStreamSources));
		try {
			schemaGrammers = factory.newSchema(schemaStreamSources
					.toArray(new SchemaStreamSource[0]));
		} catch (SAXException e) {
			throw new InfrastructureException(
					String
							.format("Generating binding schema from streamed XSD sources"),
					e);
		}

		/**
		 * Construct the JAXContext
		 */
		try {
			jaxbContext = JAXBContext.newInstance(contextPath
					.toArray(new Class[0]));
			logger.debug("Created JAXB context");
		} catch (JAXBException e) {
			throw new InfrastructureException("Unable to create JAXBContext", e);
		}

		/**
		 * Create metadata for all generated classes
		 */
		Set<Class<?>> beanClasses = reflections
				.getTypesAnnotatedWith(Bean.class);
		CIModel ciModel = new CIModel(schemaStreamSources
				.toArray(new SchemaStreamSource[0]), beanClasses
				.toArray(new Class<?>[0]));
		beans = ciModel.getCIBeans();
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

	/**
	 * JAXBContext
	 * 
	 * @return
	 */
	public JAXBContext getJAXBContext() {
		return jaxbContext;
	}

	/**
	 * Get the Schema validation
	 * 
	 * @return
	 */
	public Schema getSchema() {
		return schemaGrammers;
	}

	/**
	 * Get the set of bean metadata
	 * 
	 * @return
	 */
	public Set<CIBean> getBeans() {
		return beans;
	}

	/**
	 * Get a particular bean metadata by type
	 * 
	 * @param qname
	 * @return
	 */
	public CIBean getBean(QName type) {
		for (CIBean bean : beans)
			if (bean.type.equals(type))
				return bean;

		return null;
	}

	/**
	 * Get a particular bean metadata by type
	 * 
	 * @param namespaceURI
	 * @param localPart
	 * @return
	 */
	public CIBean getBean(String namespaceURI, String localPart) {
		for (CIBean bean : beans)
			if (bean.type.getLocalPart().equals(localPart)
					&& bean.type.getNamespaceURI().equals(namespaceURI))
				return bean;

		return null;
	}

	/**
	 * Get a particular bean metadata by class name
	 * 
	 * @param className
	 * @return
	 */
	public CIBean getBean(String className) {
		for (CIBean bean : beans)
			if (bean.getJavaClass().getName().equals(className))
				return bean;

		return null;
	}

	/**
	 * Is the type associated with a bean
	 * 
	 * @param type
	 * @return
	 */
	public boolean isBean(QName type) {
		for (CIBean bean : beans)
			if (bean.type.equals(type))
				return true;

		return false;
	}
}
