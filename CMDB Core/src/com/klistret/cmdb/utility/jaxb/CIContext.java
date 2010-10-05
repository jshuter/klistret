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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import com.klistret.cmdb.annotations.ci.Element;
import com.klistret.cmdb.annotations.ci.Relation;
import com.klistret.cmdb.annotations.ci.Proxy;
import com.klistret.cmdb.exception.InfrastructureException;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class CIContext {
	/**
	 * Based on
	 * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
	 */
	private volatile static CIContext ciContext;

	private static final Logger logger = LoggerFactory
			.getLogger(CIContext.class);

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
	 * 
	 */
	private Set<BeanMetadata> metadata;

	/**
	 * JABContext
	 */
	private JAXBContext jaxbContext;

	/**
	 * Validation Schema object
	 */
	private Schema schemaGrammers;

	private class SchemaStreamSource extends StreamSource {

		private XSModel xsModel;

		public SchemaStreamSource(String resource) {
			super(Utils.getContextClassLoader().getResource(resource)
					.toString());
		}

		public XSModel getXSModel() {
			if (xsModel == null) {
				xsModel = new XMLSchemaLoader().loadURI(getSystemId());
			}

			return xsModel;
		}
	}

	/**
	 * More information about this resolver at
	 * http://stackoverflow.com/questions/3558333/jaxb-schemafactory-source-order-must-follow-import-order-between-schemas/3830649#3830649
	 */
	private class SimpleResolver implements LSResourceResolver {

		private Set<SchemaStreamSource> schemaStreamSources;

		/**
		 * Constructor accepts a set of SchemaSource objects (which are really
		 * extensions of the Source class)
		 * 
		 * @param streams
		 */
		public SimpleResolver(Set<SchemaStreamSource> schemaStreamSources) {
			this.schemaStreamSources = schemaStreamSources;
		}

		private String getResourceName(String path) {
			int lastIndexOf = path.lastIndexOf("/");

			if (lastIndexOf == -1)
				return path;

			if (lastIndexOf == path.length())
				return null;

			return path.substring(lastIndexOf + 1, path.length());
		}

		@Override
		public LSInput resolveResource(String type, String namespaceURI,
				String publicId, String systemId, String baseURI) {
			DOMImplementationRegistry registry;
			try {
				/**
				 * Still unsure about the different types of implementations
				 * that need to be loaded
				 */
				registry = DOMImplementationRegistry.newInstance();
				DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry
						.getDOMImplementation("LS 3.0");

				LSInput ret = domImplementationLS.createLSInput();

				/**
				 * Spin through the available streams to find keyed by systemId
				 * and namespace a match to resolve
				 */
				for (SchemaStreamSource schemaStreamSource : schemaStreamSources) {
					if (getResourceName(schemaStreamSource.getSystemId())
							.equals(getResourceName(systemId))
							&& schemaStreamSource.getXSModel().getNamespaces()
									.contains(namespaceURI)) {

						logger.debug(
								"Resolved systemid [{}] with namespace [{}]",
								getResourceName(systemId), namespaceURI);

						URL url = new URL(schemaStreamSource.getSystemId());
						URLConnection uc = url.openConnection();

						/**
						 * InputStream must a newly created (ie. not previously
						 * read)
						 */
						ret.setByteStream(uc.getInputStream());
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
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

			logger.error("No stream found for system id [{}]", systemId);
			throw new InfrastructureException(String.format(
					"No stream found for system id [%s]", systemId));
		}

	}

	/**
	 * 
	 * @param javaClass
	 * @return
	 */
	private String findBeanNamespace(Class<?> javaClass) {
		/**
		 * Local element (complex type) schema mapping (By default, this is the
		 * target namespace to which the package containing the class is
		 * mapped.)
		 */
		XmlType xt = javaClass.getAnnotation(XmlType.class);
		if (xt != null && !xt.namespace().equals("##default")) {
			logger.debug(
					"XmlType annotation on class [{}] defines namespace [{}]",
					javaClass.getName(), xt.namespace());
			return xt.namespace();
		}

		/**
		 * A global element of an anonymous type
		 */
		XmlRootElement xre = javaClass.getAnnotation(XmlRootElement.class);
		if (xre != null && !xre.namespace().equals("##default")) {
			logger
					.debug(
							"XmlRootElement annotation on class [{}] defines namespace [{}]",
							javaClass.getName(), xre.namespace());
			return xre.namespace();
		}

		/**
		 * Package level
		 */
		XmlSchema xs = javaClass.getPackage().getAnnotation(XmlSchema.class);
		if (xs != null && !xs.namespace().equals("")) {
			logger
					.debug(
							"XmlSchema annotation on package [{}] defines namespace [{}]",
							javaClass.getPackage().getName(), xs.namespace());
			return xs.namespace();
		}

		return null;
	}

	/**
	 * 
	 * @param javaClass
	 * @return
	 */
	private String findBeanLocalName(Class<?> javaClass) {
		XmlType xt = javaClass.getAnnotation(XmlType.class);
		if (xt != null && !xt.name().equals("##default")) {
			logger.debug(
					"XmlType annotation on class [{}] defines local name [{}]",
					javaClass.getName(), xt.name());
			return xt.name();
		}

		XmlRootElement xre = javaClass.getAnnotation(XmlRootElement.class);
		if (xre != null && !xre.name().equals("##default")) {
			logger
					.debug(
							"XmlRootElement annotation on class [{}] defines local name [{}]",
							javaClass.getName(), xre.name());
			return xre.name();
		}

		return null;
	}

	/**
	 * 
	 * @param localName
	 * @param namespace
	 * @return
	 */
	private XSTypeDefinition getXSTypeDefinition(String localName,
			String namespace) {
		XSTypeDefinition xstd = null;

		for (SchemaStreamSource schemaStreamSource : schemaStreamSources) {
			xstd = schemaStreamSource.getXSModel().getTypeDefinition(localName,
					namespace);
			if (xstd != null)
				break;
		}

		return xstd;
	}

	/**
	 * 
	 * @param xsObject
	 * @param beanMetadata
	 */
	private void makePropertyMetadata(XSObject xsObject,
			BeanMetadata beanMetadata) {
		short objectType = xsObject.getType();

		switch (xsObject.getType()) {
		case XSConstants.PARTICLE:
			makePropertyMetadata(((XSParticle) xsObject).getTerm(),
					beanMetadata);
			break;
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) xsObject).getParticles();
			for (int index = 0; index < particles.getLength(); index++) {
				makePropertyMetadata(particles.item(index), beanMetadata);
			}
			break;
		case XSConstants.ELEMENT_DECLARATION:
			XSElementDeclaration elementDeclaration = ((XSElementDeclaration) xsObject);

			PropertyMetadata elementMetadata = new PropertyMetadata();
			elementMetadata.namespace = elementDeclaration.getNamespace();
			elementMetadata.localName = elementDeclaration.getName();

			elementMetadata.elementType = true;

			beanMetadata.properties.add(elementMetadata);
			break;
		case XSConstants.ATTRIBUTE_USE:
			makePropertyMetadata(((XSAttributeUse) xsObject)
					.getAttrDeclaration(), beanMetadata);
			break;
		case XSConstants.ATTRIBUTE_DECLARATION:
			XSAttributeDeclaration attributeDeclaration = ((XSAttributeDeclaration) xsObject);

			PropertyMetadata attributeMetadata = new PropertyMetadata();
			attributeMetadata.localName = attributeDeclaration.getName();
			attributeMetadata.namespace = attributeDeclaration.getNamespace();

			attributeMetadata.elementType = false;

			beanMetadata.properties.add(attributeMetadata);
			break;
		case XSConstants.WILDCARD:
			break;
		default:
			throw new InfrastructureException(
					String
							.format(
									"Unknown XSObject [type: %s] neither a model group, element declaration or wildcard",
									objectType));
		}
	}

	/**
	 * 
	 * @param javaClass
	 * @return
	 */
	private BeanMetadata makeBeanMetadata(Class<?> javaClass) {
		BeanMetadata beanMetadata = new BeanMetadata();

		beanMetadata.namespace = findBeanNamespace(javaClass);
		beanMetadata.localName = findBeanLocalName(javaClass);

		XSTypeDefinition xstd = getXSTypeDefinition(beanMetadata.localName,
				beanMetadata.namespace);
		if (xstd == null)
			throw new InfrastructureException(
					String
							.format(
									"Schema type definition not found by namespace [%s] and local name [%s]",
									beanMetadata.namespace,
									beanMetadata.localName));

		/**
		 * Construct property metadata (from attributes/elements), XmlType only
		 * gives the property order for elements without regard to attributes
		 */
		if (xstd.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
			/**
			 * Element are wrapped in a tree of XSParticle types (model group,
			 * element declaration and wildcards) and the recursive code drills
			 * down to the element declarations to make properties
			 */
			XSObject elements = ((XSComplexTypeDefinition) xstd).getParticle();
			makePropertyMetadata(elements, beanMetadata);

			/**
			 * Attributes can be directly pulled into a flat XSObject list
			 */
			XSObjectList attributes = ((XSComplexTypeDefinition) xstd)
					.getAttributeUses();
			for (int index = 0; index < attributes.getLength(); index++)
				makePropertyMetadata(attributes.item(index), beanMetadata);
		}

		return beanMetadata;
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

		/**
		 * Create metadata
		 */
		for (Class<?> javaClass : contextPath) {
			makeBeanMetadata(javaClass);
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

	/**
	 * JAXBContext
	 * 
	 * @return
	 */
	public JAXBContext getJAXBContext() {
		return jaxbContext;
	}

	/**
	 * 
	 * @return
	 */
	public Schema getSchema() {
		return schemaGrammers;
	}

	/**
	 * 
	 * @return
	 */
	public Set<BeanMetadata> getBeanMetadata() {
		return metadata;
	}
}
