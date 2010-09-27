package com.klistret.cmdb.utility.jaxb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jvnet.jaxb.reflection.JAXBModelFactory;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeTypeInfoSet;
import org.jvnet.jaxb.reflection.runtime.IllegalAnnotationsException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

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

	private static final Pattern schemaNamePattern = Pattern
			.compile(".*cmdb\\.xsd");

	private Set<Class<?>> elements;

	private Set<Class<?>> relations;

	private Set<String> schemaLocations;

	private JAXBContext jaxbContext;

	private Schema schemaGrammers;

	private RuntimeTypeInfoSet runtimeTypeInfoSet;

	private class SimpleResolver implements LSResourceResolver {

		private Set<Source> streams;

		public SimpleResolver(Set<Source> streams) {
			this.streams = streams;
		}

		@Override
		public LSInput resolveResource(String type, String namespaceURI,
				String publicId, String systemId, String baseURI) {
			DOMImplementationRegistry registry;
			try {

				registry = DOMImplementationRegistry.newInstance();
				DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry
						.getDOMImplementation("LS 3.0");

				LSInput ret = domImplementationLS.createLSInput();

				for (Source source : streams) {
					SchemaSource schema = (SchemaSource) source;
					if (schema.getResourceName().equals(
							schema.getResourceName(systemId))
							& schema.getTargetNamespace().equals(namespaceURI)) {
						logger.debug(
								"Resolved systemid [{}] with namespace [{}]",
								schema.getResourceName(systemId), namespaceURI);

						ret.setByteStream(new FileInputStream(schema
								.getSystemId()));
						ret.setSystemId(systemId);
						return ret;
					}
				}

			} catch (ClassCastException e) {
				logger.error(e.getMessage());
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
			} catch (InstantiationException e) {
				logger.error(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage());
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage());
			}

			return null;
		}

	}

	/**
	 * Singleton constructor
	 */
	private CIContext() {
		/**
		 * Using scannotation model to find classes with particular annotations
		 * (noteworthly if the SubTypesScanner isn't included then the Inherited
		 * annotation is not utilized)
		 */
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.getUrlsForCurrentClasspath())
				.setScanners(new TypeAnnotationsScanner(),
						new SubTypesScanner(), new ResourcesScanner()));

		/**
		 * JAXB context path
		 */
		Set<Class<?>> contextPath = new HashSet<Class<?>>();

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
		 * Locate potential CMDB schema files
		 */
		Set<Source> streams = new HashSet<Source>();

		schemaLocations = reflections.getResources(schemaNamePattern);
		for (String location : schemaLocations) {
			if (streams.add(new SchemaSource(location)))
				logger.debug("Adding schema {} to binding schema object",
						location);
		}

		/**
		 * Construct binding Schema
		 */
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		factory.setResourceResolver(new SimpleResolver(streams));
		try {
			schemaGrammers = factory.newSchema(streams
					.toArray(new SchemaSource[0]));
		} catch (SAXException e) {
			logger.error(
					"Generating binding schema from streamed XSD sources: {}",
					e);
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

	public Schema getSchemaGrammers() {
		return schemaGrammers;
	}
}
