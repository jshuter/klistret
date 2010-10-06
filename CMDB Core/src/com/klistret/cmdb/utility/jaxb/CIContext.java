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
import javax.xml.namespace.QName;
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

import com.klistret.cmdb.annotations.ci.Bean;
import com.klistret.cmdb.annotations.ci.Element;
import com.klistret.cmdb.annotations.ci.Relation;
import com.klistret.cmdb.annotations.ci.Proxy;
import com.klistret.cmdb.exception.InfrastructureException;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition;
import com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
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
	private Set<BeanMetadata> beans;

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
	 * Is the class a CMDB element
	 * 
	 * @param javaClass
	 * @return
	 */
	private boolean isElement(Class<?> javaClass) {
		for (Class<?> other : elements)
			if (other.getName().equals(javaClass.getName()))
				return true;

		return false;
	}

	/**
	 * Is the class a CMDB relation
	 * 
	 * @param javaClass
	 * @return
	 */
	private boolean isRelation(Class<?> javaClass) {
		for (Class<?> other : relations)
			if (other.getName().equals(javaClass.getName()))
				return true;

		return false;
	}

	/**
	 * Is the class a CMDB proxy
	 * 
	 * @param javaClass
	 * @return
	 */
	private boolean isProxy(Class<?> javaClass) {
		for (Class<?> other : proxies)
			if (other.getName().equals(javaClass.getName()))
				return true;

		return false;
	}

	/**
	 * Based on the XSObject passed the different types of element/attribute
	 * models are traversed to finally create a Java property and add it to the
	 * passed bean definition.
	 * 
	 * @param xsObject
	 * @param beanMetadata
	 */
	private void makePropertyMetadata(XSObject xsObject,
			BeanMetadata beanMetadata) {
		short objectType = xsObject.getType();

		switch (xsObject.getType()) {
		/**
		 * Schema particle is the entry-point for element definitions with a
		 * schema term that is either a model group, element declaration or
		 * wildcard.
		 */
		case XSConstants.PARTICLE:
			makePropertyMetadata(((XSParticle) xsObject).getTerm(),
					beanMetadata);
			break;
		/**
		 * Model groups are element groupings like sequences or choice
		 * constructs. With extensions multiple groups can co-exist.
		 */
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) xsObject).getParticles();
			for (int pIndex = 0; pIndex < particles.getLength(); pIndex++) {
				makePropertyMetadata(particles.item(pIndex), beanMetadata);
			}
			break;
		/**
		 * Singular element declaration which is either a simple or complex
		 * type. The QName keys for identification within the context of the
		 * bean and the type are the essentials.
		 */
		case XSConstants.ELEMENT_DECLARATION:
			XSElementDeclaration elementDeclaration = ((XSElementDeclaration) xsObject);

			// QName keys
			PropertyMetadata elementMetadata = new PropertyMetadata();
			elementMetadata.namespace = elementDeclaration.getNamespace();
			elementMetadata.localName = elementDeclaration.getName();

			/**
			 * Element type (simple/complex) as QName keys with simple elements
			 * associated to their primitives (like strings/int so forth).
			 */
			XSTypeDefinition elementTypeDefinition = elementDeclaration
					.getTypeDefinition();
			if (elementTypeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
				elementMetadata.typeCategory = PropertyMetadata.TypeCategory.ComplexElement;
				elementMetadata.typeNamespace = elementTypeDefinition
						.getNamespace();
				elementMetadata.typeLocalName = elementTypeDefinition.getName();
			}
			if (elementTypeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
				elementMetadata.typeCategory = PropertyMetadata.TypeCategory.SimpleElement;

				XSTypeDefinition primitiveElementTypeDefinition = ((XSSimpleTypeDefinition) elementTypeDefinition)
						.getPrimitiveType();
				elementMetadata.typeNamespace = primitiveElementTypeDefinition
						.getNamespace();
				elementMetadata.typeLocalName = primitiveElementTypeDefinition
						.getName();
			}

			elementMetadata.nillable = elementDeclaration.getNillable();

			beanMetadata.properties.add(elementMetadata);
			break;
		/**
		 * Groups of attributes
		 */
		case XSConstants.ATTRIBUTE_GROUP:
			XSObjectList attributes = ((XSAttributeGroupDefinition) xsObject)
					.getAttributeUses();
			for (int aIndex = 0; aIndex < attributes.getLength(); aIndex++) {
				makePropertyMetadata(attributes.item(aIndex), beanMetadata);
			}
			break;
		/**
		 * Usage information surrounding an attribute
		 */
		case XSConstants.ATTRIBUTE_USE:
			XSAttributeUse attributeUse = (XSAttributeUse) xsObject;

			makePropertyMetadata((attributeUse).getAttrDeclaration(),
					beanMetadata);

			beanMetadata.properties.get(beanMetadata.properties.size() - 1).required = attributeUse
					.getRequired();
			break;
		/**
		 * Singular attibute declaration which is processed just like simple
		 * elements.
		 */
		case XSConstants.ATTRIBUTE_DECLARATION:
			XSAttributeDeclaration attributeDeclaration = ((XSAttributeDeclaration) xsObject);

			PropertyMetadata attributeMetadata = new PropertyMetadata();
			attributeMetadata.localName = attributeDeclaration.getName();
			attributeMetadata.namespace = attributeDeclaration.getNamespace();

			attributeMetadata.typeCategory = PropertyMetadata.TypeCategory.Attribute;

			XSSimpleTypeDefinition attributeTypeDefinition = attributeDeclaration
					.getTypeDefinition();

			XSTypeDefinition primativeAttributeTypeDefinition = attributeTypeDefinition
					.getPrimitiveType();
			attributeMetadata.namespace = primativeAttributeTypeDefinition
					.getNamespace();
			attributeMetadata.localName = primativeAttributeTypeDefinition
					.getName();

			beanMetadata.properties.add(attributeMetadata);
			break;
		/**
		 * Wildcards are not handled at the moment
		 */
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
	 * Beans are any class generated from schema grammers
	 * 
	 * @param javaClass
	 * @return
	 */
	private void makeBeanMetadata(Class<?> javaClass) {
		BeanMetadata beanMetadata = new BeanMetadata();
		beanMetadata.javaClass = javaClass;

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
		 * Complex types
		 */
		if (xstd.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
			beanMetadata.typeCategory = BeanMetadata.TypeCategory.Complex;
			beanMetadata.abstraction = ((XSComplexTypeDefinition) xstd)
					.getAbstract();

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

		/**
		 * Simple types
		 */
		if (xstd.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE)
			throw new InfrastructureException(String.format(
					"Bean [%s] can not be a simple element", javaClass
							.getName()));

		/**
		 * CMDB category (Element, Proxy, Relation)
		 */
		if (isElement(javaClass))
			beanMetadata.cmdbCategory = BeanMetadata.CMDBCategory.Element;
		if (isRelation(javaClass))
			beanMetadata.cmdbCategory = BeanMetadata.CMDBCategory.Relation;
		if (isProxy(javaClass))
			beanMetadata.cmdbCategory = BeanMetadata.CMDBCategory.Proxy;

		/**
		 * Base
		 */
		XSTypeDefinition baseTypeDefinition = xstd.getBaseType();
		beanMetadata.baseLocalName = baseTypeDefinition.getName();
		beanMetadata.baseNamespace = baseTypeDefinition.getNamespace();

		logger
				.debug(
						"Created bean metadata [class: {}, local name: {}, namespace: {}]",
						new Object[] { beanMetadata.javaClass.getName(),
								beanMetadata.localName, beanMetadata.namespace });
		beans.add(beanMetadata);
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
		 * Create metadata for all generated classes
		 */
		Set<Class<?>> beanClasses = reflections
				.getTypesAnnotatedWith(Bean.class);
		beans = new HashSet<BeanMetadata>(beanClasses.size());
		for (Class<?> javaClass : beanClasses)
			makeBeanMetadata(javaClass);
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
	public Set<BeanMetadata> getBeans() {
		return beans;
	}

	/**
	 * 
	 * @param qname
	 * @return
	 */
	public BeanMetadata getBean(QName qname) {
		for (BeanMetadata bean : beans)
			if (bean.getLocalName().equals(qname.getLocalPart())
					&& bean.getNamespace().equals(qname.getNamespaceURI()))
				return bean;

		return null;
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	public BeanMetadata getBean(String className) {
		for (BeanMetadata bean : beans)
			if (bean.getJavaClass().getName().equals(className))
				return bean;

		return null;
	}
}
