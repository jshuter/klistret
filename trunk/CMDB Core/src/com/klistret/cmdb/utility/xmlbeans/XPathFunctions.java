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

package com.klistret.cmdb.utility.xmlbeans;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaProperty;

import com.klistret.cmdb.exception.ApplicationException;

public class XPathFunctions {

	private static String compare(Expression expression, String value,
			String operator) {
		boolean attribute = expression.getSelectedNode().getSchemaProperty()
				.isAttribute();

		QName qname = expression.getSelectedNode().getQName();

		if (attribute)
			if (qname.getPrefix().isEmpty())
				return String.format("%s[%s(@%s,\'%s\')]", expression
						.getParentXPath(), operator, qname.getLocalPart(),
						value);
			else
				return String.format("%s[%s(@%s:%s,\'%s\')]", expression
						.getParentXPath(), operator, qname.getPrefix(), qname
						.getLocalPart(), value);
		else
			return String.format("%s[%s(%s:%s,\'%s\')]", expression
					.getParentXPath(), operator, qname.getPrefix(), qname
					.getLocalPart(), value);
	}

	private static String compareText(Expression expression, String value,
			String operator) {
		Node node = expression.getSelectedNode();

		if (node.getSchemaProperty().getJavaTypeCode() != SchemaProperty.JAVA_STRING)
			throw new ApplicationException(
					String
							.format(
									"java type code [%s] not valid for function [%s] with qname [%s]",
									node.getSchemaProperty().getJavaTypeCode(),
									operator, node.getQName().toString()));

		return compare(expression, value, operator);
	}

	public static String matches(Expression expression, String value) {
		return compareText(expression, value, "matches");
	}

	public static String contains(Expression expression, String value) {
		return compareText(expression, value, "contains");
	}

	public static String startsWith(Expression expression, String value) {
		return compareText(expression, value, "starts-with");
	}

	public static String endsWith(Expression expression, String value) {
		return compareText(expression, value, "ends-with");
	}
}
