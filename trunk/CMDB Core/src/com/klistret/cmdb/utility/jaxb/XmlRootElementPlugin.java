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

import com.sun.codemodel.JAnnotationUse;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * 
 * @author Matthew Young
 * 
 */
public class XmlRootElementPlugin extends Plugin {

	@Override
	public String getOptionName() {
		return "XxmlRootElement";
	}

	@Override
	public String getUsage() {
		return "  -XxmlRootElement :  inject XmlRootElement (name = class name) annonation for all classes in the Outline model ";
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
		for (ClassOutline co : model.getClasses()) {

			/**
			 * XmlRootElement has to have the name set to the class name
			 * otherwise the unmarshalling of Objects is given a lower case
			 * element name for the root element
			 */
			JAnnotationUse xmlRootElementAnnotation = co.implClass
					.annotate(XmlRootElement.class);
			xmlRootElementAnnotation.param("name", co.implClass.name());
		}

		return true;
	}

}
