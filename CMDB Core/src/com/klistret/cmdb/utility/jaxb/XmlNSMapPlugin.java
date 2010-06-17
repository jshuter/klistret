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

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationArrayMember;

import java.util.Iterator;

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

	private String targets;

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

	public String getTargets() {
		return this.targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {

		for (ClassOutline co : model.getClasses()) {
			for (String target : targets.split(",")) {
				if (co.implClass.fullName().equals(target.trim())) {
					JAnnotationUse mappedJAnnotation = co.implClass
							.annotate(Mapped.class);
					JAnnotationArrayMember namespaceMapParam = mappedJAnnotation
							.paramArray("namespaceMap");

					Iterator<? extends PackageOutline> packageOutlineIterator = model
							.getAllPackageContexts().iterator();

					while (packageOutlineIterator.hasNext()) {
						PackageOutline packageOutline = packageOutlineIterator
								.next();

						JAnnotationUse xmlNsMapJAnnotation = namespaceMapParam
								.annotate(XmlNsMap.class);
						xmlNsMapJAnnotation.param("namespace", packageOutline
								.getMostUsedNamespaceURI());
						xmlNsMapJAnnotation.param("jsonName", packageOutline
								._package().name());

						logger.debug("Added namespace [{}]/json [{}] mapping",
								packageOutline.getMostUsedNamespaceURI(),
								packageOutline._package().name());
					}

					JAnnotationUse xmlNsMapJAnnotation = namespaceMapParam
							.annotate(XmlNsMap.class);
					xmlNsMapJAnnotation.param("namespace",
							"http://www.w3.org/2001/XMLSchema-instance");
					xmlNsMapJAnnotation.param("jsonName",
							"www.w3.org.2001.XMLSchema-instance");
				}
			}
		}

		return true;
	}
}
