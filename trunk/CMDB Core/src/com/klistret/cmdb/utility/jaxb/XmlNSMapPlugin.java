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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;

import com.klistret.cmdb.exception.ApplicationException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	private String targetProperties;

	@Override
	public String getOptionName() {
		return "XxmlNSMap";
	}

	@Override
	public String getUsage() {
		return ""
				+ "-XxmlNSMap:  adds resteasy XmlNsMap annotations for element/relation pojo "
				+ "-XxmlNSMap-targetProperties: properties with CI hierarchy";
	}

	public String getTargetProperties() {
		return this.targetProperties;
	}

	public void setTargetProperties(String targetProperties) {
		this.targetProperties = targetProperties;
	}

	private boolean isAssignable(JDefinedClass other, JDefinedClass base) {
		if (base.isAssignableFrom(other))
			return true;

		if (other._extends() != null
				&& other._extends() instanceof JDefinedClass)
			return isAssignable((JDefinedClass) other._extends(), base);

		return false;
	}

	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
		List<TargetProperty> targets = new ArrayList<TargetProperty>();

		for (String path : targetProperties.split(","))
			targets.add(new TargetProperty(model, path.trim()));

		List<String> xmlNSMapCache = new LinkedList<String>();
		for (ClassOutline co : model.getClasses()) {
			for (TargetProperty target : targets) {
				if (isAssignable(co.implClass, target.getBase().implClass)) {

					String key = String.format("%s:%s",
							target.getBase().implClass.fullName(), co
									._package().getMostUsedNamespaceURI());

					if (!xmlNSMapCache.contains(key)) {
						JAnnotationUse xmlNsMapJAnnotation = target
								.getNamespaceMapParam()
								.annotate(XmlNsMap.class);
						xmlNsMapJAnnotation.param("namespace", co._package()
								.getMostUsedNamespaceURI());
						xmlNsMapJAnnotation.param("jsonName", co.implClass
								.getPackage().name());

						xmlNSMapCache.add(key);
					}
				}
			}
		}

		return true;
	}

	private class TargetProperty {
		private ClassOutline container;

		private ClassOutline base;

		private String path;

		private String baseTypeFullName;

		private JAnnotationArrayMember namespaceMapParam;

		TargetProperty(Outline model, String path) {
			this.path = path;

			try {
				String containerFullName = path.substring(0, path
						.lastIndexOf("."));
				String propertyName = path.substring(path.lastIndexOf(".") + 1);

				/**
				 * Determine if the container is schema defined
				 */
				for (ClassOutline co : model.getClasses())
					if (co.implClass.fullName().equals(containerFullName))
						container = co;

				if (container == null)
					throw new ApplicationException(
							String
									.format(
											"Target property [%s] with container [%s] is not schema defined",
											path, containerFullName));

				/**
				 * Does the container have the expected property
				 */
				for (Map.Entry<String, JFieldVar> entry : container.implClass
						.fields().entrySet())
					if (entry.getKey().equals(propertyName))
						baseTypeFullName = entry.getValue().type().fullName();

				if (baseTypeFullName == null)
					throw new ApplicationException(
							String
									.format(
											"Target property [%s] with property [%s] is not defined to the container [%s]",
											path, propertyName,
											containerFullName));

				/**
				 * Determine if the property type is schema defined
				 */
				for (ClassOutline co : model.getClasses())
					if (co.implClass.fullName().equals(baseTypeFullName))
						base = co;

				if (base == null)
					throw new ApplicationException(
							String
									.format(
											"Target property [%s] with property type [%s] is not schema defined]",
											path, baseTypeFullName));

				/**
				 * Setup Mapped structure
				 */
				JAnnotationUse mappedJAnnotation = container.implClass
						.annotate(Mapped.class);
				namespaceMapParam = mappedJAnnotation
						.paramArray("namespaceMap");

				JAnnotationUse xmlNsMapJAnnotation = namespaceMapParam
						.annotate(XmlNsMap.class);
				xmlNsMapJAnnotation.param("namespace", container._package()
						.getMostUsedNamespaceURI());
				xmlNsMapJAnnotation.param("jsonName", container.implClass
						.getPackage().name());

			} catch (IndexOutOfBoundsException e) {
				throw new ApplicationException(e);
			}
		}

		public ClassOutline getContainer() {
			return this.container;
		}

		public ClassOutline getBase() {
			return this.base;
		}

		public String getPath() {
			return this.path;
		}

		public JAnnotationArrayMember getNamespaceMapParam() {
			return this.namespaceMapParam;
		}
	}
}
