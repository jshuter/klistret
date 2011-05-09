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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.jaxb.SchemaStreamSource;
import com.sun.org.apache.xerces.internal.xs.StringList;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition;
import com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class CIModel {
	private static final Logger logger = LoggerFactory.getLogger(CIModel.class);

	/**
	 * Stream sources
	 */
	private SchemaStreamSource[] scheamStreamSources;

	/**
	 * JAXB Classes
	 */
	private Class<?>[] jaxbClasses;

	/**
	 * Set of CIBean
	 */
	private Set<CIBean> beans = new HashSet<CIBean>();

	/**
	 * W3C Schema XSD
	 */
	static final String NS_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";

	/**
	 * Constructor that automatically starts adding CI beans
	 * 
	 * @param streamSources
	 * @param jaxbClasses
	 */
	public CIModel(SchemaStreamSource[] streamSources, Class<?>[] jaxbClasses) {
		this.scheamStreamSources = streamSources;
		this.jaxbClasses = jaxbClasses;

		for (Class<?> jaxbClass : jaxbClasses)
			addCIBean(jaxbClass);

	}

	/**
	 * Get XML StreamSource (schema documents)
	 * 
	 * @return
	 */
	public StreamSource[] getStreamSources() {
		return scheamStreamSources;
	}

	/**
	 * Get CI JAXB classes
	 * 
	 * @return
	 */
	public Class<?>[] getJAXBClasses() {
		return jaxbClasses;
	}

	/**
	 * Get CI Beans
	 * 
	 * @return
	 */
	public Set<CIBean> getCIBeans() {
		return beans;
	}

	/**
	 * A JAXB class is going to a complex type with either element or attribute
	 * properties plus an extending reference.
	 * 
	 * @param jaxbClass
	 */
	private void addCIBean(Class<?> jaxbClass) {
		logger.debug("Adding CIBean based on JAXB class [{}]", jaxbClass
				.getName());

		CIBean bean = new CIBean();
		bean.javaClass = jaxbClass;

		/**
		 * Bean type
		 */
		String namespace = getNamespace(jaxbClass);
		String localName = getLocalName(jaxbClass);
		if (NS_SCHEMA_XSD.equals(namespace) && "anyType".equals(localName))
			throw new ApplicationException(String.format(
					"JAXB Class has W3C namespace and local name anyTime",
					namespace, localName));

		bean.type = new QName(namespace, localName);

		/**
		 * Underlying Bean type (by QName)
		 */
		XSTypeDefinition xstd = getXSTypeDefinition(namespace, localName);
		if (xstd == null)
			throw new ApplicationException(
					String
							.format(
									"Schema type definition not found by namespace [%s] and local name [%s]",
									namespace, localName));

		/**
		 * Handle complex and simple types only
		 */
		switch (xstd.getTypeCategory()) {
		case XSTypeDefinition.COMPLEX_TYPE:
			doType((XSComplexTypeDefinition) xstd, bean);
			break;
		case XSTypeDefinition.SIMPLE_TYPE:
			doType((XSSimpleTypeDefinition) xstd, bean);
			break;
		default:
			throw new ApplicationException(
					String
							.format("CI Beans may only be either complex or simple types"));
		}

		/**
		 * Base (extending) reference
		 */
		XSTypeDefinition xstdBase = xstd.getBaseType();
		bean.base = new QName(xstdBase.getNamespace(), xstdBase.getName());
		
		/**
		 * Add bean 
		 */
		beans.add(bean);
	}

	/**
	 * Handle complex type definitions
	 * 
	 * @param xsctd
	 * @param bean
	 */
	private void doType(XSComplexTypeDefinition xsctd, CIBean bean) {
		bean.typeCategory = CIBean.TypeCategory.Complex;

		short contentType = xsctd.getContentType();
		switch (contentType) {
		case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
			doContentTypeElement(xsctd, bean);
			break;
		default:
			throw new ApplicationException(
					String
							.format(
									"Complex type definitions [%s] other than Element not yet supported for CIBean",
									xsctd));
		}
	}

	/**
	 * Handling content type element
	 * 
	 * @param xsctd
	 * @param bean
	 */
	private void doContentTypeElement(XSComplexTypeDefinition xsctd, CIBean bean) {
		short derivationMethod = xsctd.getDerivationMethod();

		switch (derivationMethod) {
		case XSConstants.DERIVATION_EXTENSION:
			doPropertyAttributes(xsctd, bean);
			doPropertyElements(xsctd, bean);
			break;
		case XSConstants.DERIVATION_RESTRICTION:
			doPropertyAttributes(xsctd, bean);
			doPropertyElements(xsctd, bean);
			break;
		default:
			throw new ApplicationException(
					String
							.format(
									"Complex type definitions [%s] with derivations other than Extension/Restriction not yet supported for CIBean",
									xsctd));
		}
	}

	/**
	 * Marshall attributes into properties
	 * 
	 * @param xsctd
	 * @param bean
	 */
	private void doPropertyAttributes(XSComplexTypeDefinition xsctd, CIBean bean) {
		XSObjectList attributes = ((XSComplexTypeDefinition) xsctd)
				.getAttributeUses();
		for (int index = 0; index < attributes.getLength(); index++)
			doProperty(attributes.item(index), bean);

	}

	/**
	 * Marshall elements into properties
	 * 
	 * @param xsctd
	 * @param bean
	 */
	private void doPropertyElements(XSComplexTypeDefinition xsctd, CIBean bean) {
		doProperty(xsctd.getParticle(), bean);
	}

	/**
	 * Based on the XSObject passed the different types of element/attribute
	 * models are traversed to finally create a Java property and add it to the
	 * passed bean definition.
	 * 
	 * @param xsObject
	 * @param bean
	 */
	private void doProperty(XSObject xsObject, CIBean bean) {
		short objectType = xsObject.getType();

		switch (xsObject.getType()) {
		/**
		 * Schema particle is the entry-point for element definitions with a
		 * schema term that is either a model group, element declaration or
		 * wildcard.
		 */
		case XSConstants.PARTICLE:
			doProperty(((XSParticle) xsObject).getTerm(), bean);
			break;
		/**
		 * Model groups are element groupings like sequences or choice
		 * constructs. The grouping is a list of particles where the min/max
		 * constraints are located. What is left here to-do is capture
		 * sequence/choice groupings.
		 */
		case XSConstants.MODEL_GROUP:
			XSObjectList particles = ((XSModelGroup) xsObject).getParticles();
			for (int pIndex = 0; pIndex < particles.getLength(); pIndex++) {
				XSParticle elementParticle = (XSParticle) particles
						.item(pIndex);
				doProperty(elementParticle, bean);

				CIProperty last = bean.properties
						.get(bean.properties.size() - 1);
				last.maxOccurs = elementParticle.getMaxOccurs();
				last.minOccurs = elementParticle.getMinOccurs();
				last.maxOccursUnbounded = elementParticle
						.getMaxOccursUnbounded();
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
			CIProperty elementProperty = new CIProperty();
			elementProperty.name = new QName(elementDeclaration.getNamespace(),
					elementDeclaration.getName());

			/**
			 * Element type (simple/complex) as QName keys with simple elements
			 * associated to their primitives (like strings/int so forth).
			 */
			XSTypeDefinition elementTypeDefinition = elementDeclaration
					.getTypeDefinition();

			switch (elementTypeDefinition.getTypeCategory()) {
			case XSTypeDefinition.COMPLEX_TYPE:
				elementProperty.typeCategory = CIProperty.TypeCategory.ComplexElement;

				elementProperty.type = new QName(elementTypeDefinition
						.getNamespace(), elementTypeDefinition.getName());
				break;
			case XSTypeDefinition.SIMPLE_TYPE:
				elementProperty.typeCategory = CIProperty.TypeCategory.SimpleElement;

				XSTypeDefinition primitiveElementTypeDefinition = ((XSSimpleTypeDefinition) elementTypeDefinition)
						.getPrimitiveType();

				elementProperty.type = new QName(primitiveElementTypeDefinition
						.getNamespace(), primitiveElementTypeDefinition
						.getName());
				break;
			default:
				throw new ApplicationException(
						String
								.format("CI Properties represented by elements may only be either complex or simple types"));
			}

			elementProperty.nillable = elementDeclaration.getNillable();

			elementProperty.annotation = elementDeclaration.getAnnotation() == null ? null
					: elementDeclaration.getAnnotation().getAnnotationString();

			bean.properties.add(elementProperty);
			logger.debug("Adding CIProperty [{}]", elementProperty);
			break;
		/**
		 * Groups of attributes
		 */
		case XSConstants.ATTRIBUTE_GROUP:
			XSObjectList attributes = ((XSAttributeGroupDefinition) xsObject)
					.getAttributeUses();
			for (int aIndex = 0; aIndex < attributes.getLength(); aIndex++) {
				doProperty(attributes.item(aIndex), bean);
			}
			break;
		/**
		 * Usage information surrounding an attribute
		 */
		case XSConstants.ATTRIBUTE_USE:
			XSAttributeUse attributeUse = (XSAttributeUse) xsObject;

			doProperty((attributeUse).getAttrDeclaration(), bean);

			CIProperty last = bean.properties.get(bean.properties.size() - 1);
			last.required = attributeUse.getRequired();

			break;
		/**
		 * Singular attibute declaration which is processed just like simple
		 * elements.
		 */
		case XSConstants.ATTRIBUTE_DECLARATION:
			XSAttributeDeclaration attributeDeclaration = ((XSAttributeDeclaration) xsObject);

			CIProperty attributeProperty = new CIProperty();
			attributeProperty.name = new QName(attributeDeclaration
					.getNamespace(), attributeDeclaration.getName());

			attributeProperty.typeCategory = CIProperty.TypeCategory.Attribute;

			XSSimpleTypeDefinition attributeTypeDefinition = attributeDeclaration
					.getTypeDefinition();

			XSTypeDefinition primativeAttributeTypeDefinition = attributeTypeDefinition
					.getPrimitiveType();
			attributeProperty.type = new QName(primativeAttributeTypeDefinition
					.getNamespace(), primativeAttributeTypeDefinition.getName());

			attributeProperty.annotation = attributeDeclaration.getAnnotation() == null ? null
					: attributeDeclaration.getAnnotation()
							.getAnnotationString();

			bean.properties.add(attributeProperty);
			logger.debug("Adding CIProperty [{}]", attributeProperty);
			break;
		/**
		 * Wildcards are not handled at the moment
		 */
		case XSConstants.WILDCARD:
			break;
		default:
			throw new ApplicationException(
					String
							.format(
									"Unknown XSObject [type: %s] neither a model group, element declaration or wildcard",
									objectType));
		}

	}

	/**
	 * Handle simple type definitions (enumerations only)
	 * 
	 * @param xsstd
	 * @param bean
	 */
	private void doType(XSSimpleTypeDefinition xsstd, CIBean bean) {
		bean.typeCategory = CIBean.TypeCategory.Simple;

		short variety = xsstd.getVariety();
		switch (variety) {
		case XSSimpleTypeDefinition.VARIETY_ATOMIC:
			/**
			 * Enumerated list, interested only in type
			 */
			StringList stringList = xsstd.getLexicalEnumeration();
			if (stringList != null) {
				for (int index = 0; index < stringList.getLength(); index++)
					bean.enumerations.add(stringList.item(index));
			}
			break;
		default:
			throw new ApplicationException(
					String
							.format(
									"CIBean [%s] simple type definition other than enumerations are not supported",
									bean.getJavaClass().getName()));
		}
	}

	/**
	 * Get the type definition as defined by the Schema validation based on the
	 * QName. The definition represents a complex or simple type.
	 * 
	 * @param localName
	 * @param namespace
	 * @return
	 */
	private XSTypeDefinition getXSTypeDefinition(String namespace,
			String localName) {
		XSTypeDefinition xstd = null;

		for (SchemaStreamSource schemaStreamSource : scheamStreamSources) {
			xstd = schemaStreamSource.getXSModel().getTypeDefinition(localName,
					namespace);
			if (xstd != null)
				break;
		}

		return xstd;
	}

	/**
	 * Find bean namespace by looking first at the class annotation XmlType,
	 * thereafter the class annotation XmlRootElement and finally the package
	 * annotation XmlSchema. The resulting value together with the local part of
	 * the bean makes up the QName describing the bean's type.
	 * 
	 * @param jaxbClass
	 * @return
	 */
	private String getNamespace(Class<?> jaxbClass) {
		/**
		 * Local element (complex type) schema mapping (By default, this is the
		 * target namespace to which the package containing the class is
		 * mapped.)
		 */
		XmlType xt = jaxbClass.getAnnotation(XmlType.class);
		if (xt != null && !xt.namespace().equals("##default")) {
			logger.debug(
					"XmlType annotation on class [{}] defines namespace [{}]",
					jaxbClass.getName(), xt.namespace());
			return xt.namespace();
		}

		/**
		 * A global element of an anonymous type
		 */
		XmlRootElement xre = jaxbClass.getAnnotation(XmlRootElement.class);
		if (xre != null && !xre.namespace().equals("##default")) {
			logger
					.debug(
							"XmlRootElement annotation on class [{}] defines namespace [{}]",
							jaxbClass.getName(), xre.namespace());
			return xre.namespace();
		}

		/**
		 * Package level
		 */
		XmlSchema xs = jaxbClass.getPackage().getAnnotation(XmlSchema.class);
		if (xs != null && !xs.namespace().equals("")) {
			logger
					.debug(
							"XmlSchema annotation on package [{}] defines namespace [{}]",
							jaxbClass.getPackage().getName(), xs.namespace());
			return xs.namespace();
		}

		return null;
	}

	/**
	 * Find the local name first in the class annotation XmlType and thereafter
	 * the class annotation XmlRootElement. Together with the namespace the
	 * local part makes up the QName denoting the bean's type.
	 * 
	 * @param javaClass
	 * @return
	 */
	private String getLocalName(Class<?> jaxbClass) {
		XmlType xt = jaxbClass.getAnnotation(XmlType.class);
		if (xt != null && !xt.name().equals("##default")) {
			logger.debug(
					"XmlType annotation on class [{}] defines local name [{}]",
					jaxbClass.getName(), xt.name());
			return xt.name();
		}

		XmlRootElement xre = jaxbClass.getAnnotation(XmlRootElement.class);
		if (xre != null && !xre.name().equals("##default")) {
			logger
					.debug(
							"XmlRootElement annotation on class [{}] defines local name [{}]",
							jaxbClass.getName(), xre.name());
			return xre.name();
		}

		return null;
	}
}
