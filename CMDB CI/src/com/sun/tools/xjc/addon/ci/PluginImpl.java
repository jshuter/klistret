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
package com.sun.tools.xjc.addon.ci;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.klistret.cmdb.annotations.ci.Bean;
import com.klistret.cmdb.annotations.ci.Element;
import com.klistret.cmdb.annotations.ci.Proxy;
import com.klistret.cmdb.annotations.ci.Relation;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * See
 * http://metro.1045641.n5.nabble.com/XJC-Plugin-Custom-plugin-causes-ClassCastException-td1061066.html
 * 
 * @author Matthew Young
 * 
 */
public class PluginImpl extends Plugin {

	private static final Logger logger = LoggerFactory
			.getLogger(PluginImpl.class);

	/**
	 * Namespace URI to JSON mappings
	 */
	private Map<String, String> namespaceJSONMapping = new HashMap<String, String>();

	/**
	 * Only one root element
	 */
	private ClassOutline element;

	/**
	 * Only one root relation
	 */
	private ClassOutline relation;

	/**
	 * Proxies for element/relation extensions
	 */
	private List<ClassOutline> proxies = new ArrayList<ClassOutline>();

	@Override
	public String getOptionName() {
		return "Xci";
	}

	/**
	 * Necessary method to be an extension binding prefix
	 */
	public List<String> getCustomizationURIs() {
		return Collections.singletonList(Const.NS);
	}

	/**
	 * Identifies appInfo annotations specific to this plugin.
	 */
	public boolean isCustomizationTagName(String nsUri, String localName) {
		return nsUri.equals(Const.NS)
				&& localName.matches("Proxy|Element|Relation");
	}

	/**
	 * Determine if a class extends a base class
	 * 
	 * @param other
	 * @param base
	 * @return
	 */
	private boolean isAssignable(JDefinedClass other, JDefinedClass base) {
		if (base.isAssignableFrom(other))
			return true;

		if (other._extends() != null
				&& other._extends() instanceof JDefinedClass)
			return isAssignable((JDefinedClass) other._extends(), base);

		return false;
	}

	/**
	 * Determine if the class is concrete and extends a element/relation/proxy
	 * 
	 * @param other
	 * @param base
	 * @return
	 */
	private boolean isXmlRootElementCandidate(JDefinedClass other,
			JDefinedClass base) {
		if (isAssignable(other, base) && !other.isAbstract())
			return true;

		return false;
	}

	@Override
	public String getUsage() {
		return "  -Xci      :  inject specified CI directives as annotations into the generated code";
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
			throws SAXException {

		/**
		 * Identify schemas
		 */
		InputSource[] grammers = opt.getGrammars();
		for (InputSource grammer : grammers)
			logger.debug("Grammer (schema) {}", grammer.getSystemId());

		/**
		 * First pass to associate namespaces with packages for JSON mappings
		 * plus store proxy, element, and relation classes.
		 */
		for (ClassOutline co : model.getClasses()) {
			/**
			 * Associate namespace to package name
			 */
			QName key = co.target.getTypeName();
			if (!namespaceJSONMapping.containsKey(key.getNamespaceURI())) {
				logger.debug("Mapping namescape [{}] to package [{}]", key
						.getNamespaceURI(), co.implClass.getPackage().name());
				namespaceJSONMapping.put(key.getNamespaceURI(), co.target
						.getOwnerPackage().name());
			}

			/**
			 * Proxy information (multiple)
			 */
			CPluginCustomization ciProxy = co.target.getCustomizations().find(
					Const.NS, "Proxy");
			if (ciProxy != null) {
				if (co.implClass.isAbstract()) {
					logger
							.error(
									"Proxy annotation applicable only for concrete classes [target class: {}]",
									co.implClass.name());
					throw new SAXException(
							String
									.format(
											"Proxy annotation applicable only for concrete classes [target class: %s]",
											co.implClass.name()));
				}

				logger.debug("Annotating class [{}] with Proxy", co.target
						.getName());
				co.implClass.annotate(Proxy.class);

				proxies.add(co);
				ciProxy.markAsAcknowledged();
				continue;
			}

			/**
			 * Element information (unique)
			 */
			CPluginCustomization ciElement = co.target.getCustomizations()
					.find(Const.NS, "Element");
			if (ciElement != null) {
				if (element != null) {
					logger
							.error(
									"Multiple Element annotations [current element class: {}, this class: {}]",
									element.implClass.name(), co.implClass
											.name());
					throw new SAXException(
							String
									.format(
											"Multiple Element annotations [current element class: %s, this class: %s]",
											element.implClass.name(),
											co.implClass.name()));
				}

				if (!co.implClass.isAbstract()) {
					logger
							.error(
									"Element annotation applicable only for abstract classes [target class: {}]",
									co.implClass.name());
					throw new SAXException(
							String
									.format(
											"Element annotation applicable only for abstract classes [target class: %s]",
											co.implClass.name()));
				}

				logger.debug("Annotating class [{}] with Element", co.implClass
						.name());
				co.implClass.annotate(Element.class);

				element = co;
				ciElement.markAsAcknowledged();
				continue;
			}

			/**
			 * Relation information (unique)
			 */
			CPluginCustomization ciRelation = co.target.getCustomizations()
					.find(Const.NS, "Relation");
			if (ciRelation != null) {
				if (relation != null) {
					logger
							.error(
									"Multiple Relation annotations [current relation class: {}, this class: {}]",
									relation.implClass.name(), co.implClass
											.name());
					throw new SAXException(
							String
									.format(
											"Multiple Relation annotations [current relation class: %s, this class: %s]",
											relation.implClass.name(),
											co.implClass.name()));
				}

				if (!co.implClass.isAbstract()) {
					logger
							.error(
									"Relation annotation applicable only for abstract classes [target class: {}]",
									co.implClass.name());
					throw new SAXException(
							String
									.format(
											"Relation annotation applicable only for abstract classes [target class: %s]",
											co.implClass.name()));
				}

				logger.debug("Annotating class [{}] with Relation",
						co.implClass.name());
				co.implClass.annotate(Relation.class);

				relation = co;
				ciRelation.markAsAcknowledged();
				continue;
			}
		}

		if (element == null) {
			logger.error("Element root is not defined by CI extension");
			throw new SAXException(String
					.format("Element root is not defined by CI extension"));
		}

		if (relation == null) {
			logger.error("Relation root is not defined by CI extension");
			throw new SAXException(String
					.format("Relation root is not defined by CI extension"));
		}

		/**
		 * Add JSON XmlNSMap annotations (necessary for Jettison/Jackson) to the
		 * proxy classes
		 */
		for (ClassOutline co : proxies) {
			/**
			 * Construct the Mapped annotation (RestEasy/Jettison requirement)
			 */
			JAnnotationUse mapped = co.implClass.annotate(Mapped.class);
			JAnnotationArrayMember namespaceMap = mapped
					.paramArray("namespaceMap");

			/**
			 * Add every package as a potential namespace
			 */
			for (Map.Entry<String, String> entry : namespaceJSONMapping
					.entrySet()) {
				JAnnotationUse xmlNsMapJAnnotation = namespaceMap
						.annotate(XmlNsMap.class);
				xmlNsMapJAnnotation.param("namespace", entry.getKey());
				xmlNsMapJAnnotation.param("jsonName", entry.getValue());
			}

			/**
			 * Add general XML schema mappings
			 */
			JAnnotationUse xmlNsMapJAnnotation = namespaceMap
					.annotate(XmlNsMap.class);
			xmlNsMapJAnnotation.param("namespace",
					"http://www.w3.org/2001/XMLSchema-instance");
			xmlNsMapJAnnotation.param("jsonName",
					"www.w3.org.2001.XMLSchema-instance");

			/**
			 * XmlRootElement
			 */
			JAnnotationUse xmlRootElement = co.implClass
					.annotate(XmlRootElement.class);
			xmlRootElement.param("name", co.implClass.name());
		}

		/**
		 * Add XmlRootElement annotations to concrete element/relation classes
		 * then for every bean add the Bean annotation to denote classes as
		 * generated for CMDB usage.
		 */
		for (ClassOutline co : model.getClasses()) {
			if ((isXmlRootElementCandidate(co.implClass, element.implClass) || isXmlRootElementCandidate(
					co.implClass, relation.implClass))) {
				JAnnotationUse xmlRootElement = co.implClass
						.annotate(XmlRootElement.class);
				xmlRootElement.param("name", co.implClass.name());
			}

			co.implClass.annotate(Bean.class);
		}

		return true;
	}
}
