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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.xml.sax.ErrorHandler;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * 
 * 
 */
public class XmlElementPlugin extends AbstractParameterizablePlugin {

	private String baseClasses;

	private List<JDefinedClass> baseJDefinedClasses;

	@Override
	public String getOptionName() {
		return "XxmlElement";
	}

	@Override
	public String getUsage() {
		return "  -XxmlElement :  inject either the XmlRootElement or XmlElement annonation depending on class assignment"
				+ "-XxmlElement-baseClasses: classes recursively assignable from base classes get the XmlRootElement annotation";
	}

	public String getBaseClasses() {
		return this.baseClasses;
	}

	public void setBaseClasses(String baseClasses) {
		this.baseClasses = baseClasses;
	}

	private boolean isAssignable(JDefinedClass other) {
		for (JDefinedClass base : baseJDefinedClasses) {
			if (base.isAssignableFrom(other))
				return true;
		}

		if (other._extends() != null
				&& other._extends() instanceof JDefinedClass)
			return isAssignable((JDefinedClass) other._extends());

		return false;
	}

	private boolean isXmlRootElementCandidate(JDefinedClass other) {
		if (isAssignable(other) && !other.isAbstract())
			return true;

		return false;
	}

	private boolean isXmlElementCandidate(JDefinedClass other) {
		if (!isAssignable(other))
			return true;

		return false;
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {

		/**
		 * Find defined base classes
		 */
		baseJDefinedClasses = new ArrayList<JDefinedClass>();
		for (ClassOutline co : model.getClasses()) {
			for (String baseClass : baseClasses.split(",")) {
				if (co.implClass.fullName().equals(baseClass.trim()))
					baseJDefinedClasses.add(co.implClass);
			}
		}

		/**
		 * Determine if the class is a candidate for XmlRootElement (something
		 * to serialize all the way) or a simple XmlElement
		 */
		for (ClassOutline co : model.getClasses()) {

			if (isXmlRootElementCandidate(co.implClass)) {
				JAnnotationUse xmlRootElementAnnotation = co.implClass
						.annotate(XmlRootElement.class);

				xmlRootElementAnnotation.param("name", co.implClass.name());
				xmlRootElementAnnotation.param("namespace", co._package()
						.getMostUsedNamespaceURI());
			}

			if (isXmlElementCandidate(co.implClass)) {
				JAnnotationUse xmlElementAnnotation = co.implClass
						.annotate(XmlElement.class);

				xmlElementAnnotation.param("name", co.implClass.name());
				xmlElementAnnotation.param("namespace", co._package()
						.getMostUsedNamespaceURI());
			}
		}

		return true;
	}

}
