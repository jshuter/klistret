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

import javax.xml.bind.annotation.XmlRootElement;

import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

public class XmlRootPlugin extends Plugin {

	@Override
	public String getOptionName() {
		return "Xxmlroot";
	}

	@Override
	public String getUsage() {
		return "  -Xxmlroot      :  inject XmlRootElement annonation for all classes in the Outline model ";
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
		for (ClassOutline co : model.getClasses()) {

			// inject the specified code fragment into the implementation class.
			JAnnotationUse xmlRootElementAnnotation = co.implClass
					.annotate(XmlRootElement.class);
			xmlRootElementAnnotation.param("name", co.implClass.name());

			JAnnotationUse mappedAnnotation = co.implClass
					.annotate(Mapped.class);

			JAnnotationArrayMember namespaceMap = mappedAnnotation
					.paramArray("namespaceMap");
			JAnnotationUse xmlNsMapAnnotation = namespaceMap
					.annotate(XmlNsMap.class);

			xmlNsMapAnnotation.param("namespace", co.target.getTypeName()
					.getNamespaceURI());
			xmlNsMapAnnotation.param("jsonName", co.implClass.getPackage()
					.name());
		}

		return true;
	}

}
