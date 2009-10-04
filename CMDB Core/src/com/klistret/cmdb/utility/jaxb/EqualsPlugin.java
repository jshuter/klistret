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
import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class EqualsPlugin extends Plugin {

	private static final Logger logger = LoggerFactory
			.getLogger(EqualsPlugin.class);

	@Override
	public String getOptionName() {
		return "Xequals";
	}

	@Override
	public String getUsage() {
		return "  -Xequals: adds an equals method for Hibernate Pojos ";
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
			throws SAXException {
		for (ClassOutline co : model.getClasses()) {
			logger.debug("processing ClassOutline [name: {}, package: {}]",
					co.implClass.name(), co.implClass.getPackage().name());

			if (co.implClass.getPackage().name().equals(
					"com.klistret.cmdb.pojo")) {
				JMethod equalsMethod = co.implClass.method(JMod.PUBLIC, model
						.getCodeModel().BOOLEAN, "equals");

				equalsMethod.param(Object.class, "other");

				String equalsCode = "if (this == other) {\n" + "return true;\n"
						+ "}\n" + "if (this.id == null) {\n"
						+ "return false;\n" + "}\n" + "if (!(other instanceof "
						+ co.implClass.name() + ")) {\n" + "return false;\n"
						+ "}\n" + "final " + co.implClass.name() + " that = ("
						+ co.implClass.name() + ") other;\n"
						+ "return this.id.equals(that.getId());\n";

				JBlock equalsBody = equalsMethod.body();
				equalsBody.directStatement(equalsCode);
			}
		}

		return true;
	}

}
