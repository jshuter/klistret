package com.klistret.cmdb.utility.xjc.ci;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class PluginImpl extends Plugin {

	private Map<String, String> namespaceJSONMapping = new HashMap<String, String>();

	private Map<QName, Object> appInfoMapping = new HashMap<QName, Object>();

	private Unmarshaller unmarshaller;

	@Override
	public String getOptionName() {
		return "Xci";
	}

	public List<String> getCustomizationURIs() {
		return Collections.singletonList(Const.NS);
	}

	public boolean isCustomizationTagName(String nsUri, String localName) {
		return nsUri.equals(Const.NS);
	}

	@Override
	public String getUsage() {
		return "  -Xci      :  inject specified CI directives as annotations into the generated code";
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
			throws SAXException {
		/**
		 * JAXB context to unmarshall appinfo directives
		 */
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Relation.class,
					Element.class, Proxy.class);
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * First pass through to build up a namespace/json mapping bank and
		 * unmarshall appinfo per ClassOutline object
		 */
		for (ClassOutline co : model.getClasses()) {
			/**
			 * Associate namespace (from the classes QName) to the package to
			 * make an easy JSON mapping
			 */
			QName key = co.target.getTypeName();
			if (!namespaceJSONMapping.containsKey(key.getNamespaceURI()))
				namespaceJSONMapping.put(key.getNamespaceURI(), co.target
						.getOwnerPackage().name());

			CPluginCustomization ciProxy = co.target.getCustomizations().find(
					Const.NS, Const.LN_Proxy);
			if (ciProxy != null) {
				try {
					Proxy proxy = (Proxy) unmarshaller
							.unmarshal(ciProxy.element);
					if (!appInfoMapping.containsKey(key))
						appInfoMapping.put(key, proxy);
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ciProxy.markAsAcknowledged();
			}

			CPluginCustomization ciElement = co.target.getCustomizations()
					.find(Const.NS, Const.LN_Element);
			if (ciElement != null) {
				try {
					Element element = (Element) unmarshaller
							.unmarshal(ciElement.element);
					if (!appInfoMapping.containsKey(key))
						appInfoMapping.put(key, element);
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ciElement.markAsAcknowledged();
			}

			CPluginCustomization ciRelation = co.target.getCustomizations()
					.find(Const.NS, Const.LN_Relation);
			if (ciRelation != null) {
				try {
					Relation relation = (Relation) unmarshaller
							.unmarshal(ciRelation.element);
					if (!appInfoMapping.containsKey(key))
						appInfoMapping.put(key, relation);
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ciRelation.markAsAcknowledged();
			}
		}

		/**
		 * Second pass adds necessary annotations
		 */
		for (ClassOutline co : model.getClasses()) {
			QName key = co.target.getTypeName();

			Object appInfo = appInfoMapping.get(key);
			if (appInfo == null)
				continue;

			if (appInfo instanceof Proxy) {
				/**
				 * Construct the Mapped annotation (RestEasy/Jettison
				 * requirement)
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
				 * Add CI proxy annotation
				 */
				co.implClass
						.annotate(com.klistret.cmdb.annotations.ci.Proxy.class);

				/**
				 * Add XmlRootElement to allow unraveling
				 */
				if (!co.implClass.isAbstract()) {
					JAnnotationUse xmlRootElement = co.implClass
							.annotate(XmlRootElement.class);
					xmlRootElement.param("name", co.implClass.name());
				}
			}

			if (appInfo instanceof Element) {
				/**
				 * Add CI element annotation
				 */
				co.implClass
						.annotate(com.klistret.cmdb.annotations.ci.Element.class);
				
				/**
				 * Add XmlRootElement to allow unraveling
				 */
				if (!co.implClass.isAbstract()) {
					JAnnotationUse xmlRootElement = co.implClass
							.annotate(XmlRootElement.class);
					xmlRootElement.param("name", co.implClass.name());
				}
			}

			if (appInfo instanceof Relation) {
				/**
				 * Add CI element annotation
				 */
				co.implClass
						.annotate(com.klistret.cmdb.annotations.ci.Relation.class);
				
				/**
				 * Add XmlRootElement to allow unraveling
				 */
				if (!co.implClass.isAbstract()) {
					JAnnotationUse xmlRootElement = co.implClass
							.annotate(XmlRootElement.class);
					xmlRootElement.param("name", co.implClass.name());
				}
			}
		}

		return true;
	}

}
