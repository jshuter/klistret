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

public class XPathOperators {

	private static String compare(Expression expression, String value,
			String operator) {
		boolean attribute = expression.getSelectedNode().getSchemaProperty()
				.isAttribute();

		QName qname = expression.getSelectedNode().getQName();

		if (attribute) {
			if (qname.getPrefix().isEmpty())
				return String.format("%s[@%s %s %s]", expression
						.getParentXPath(), qname.getLocalPart(), operator,
						value);
			else
				return String.format("%s[@%s:%s %s %s]", expression
						.getParentXPath(), qname.getPrefix(), qname
						.getLocalPart(), operator, value);
		} else {
			return String.format("%s/%s:%s[. %s %s]", expression
					.getParentXPath(), qname.getPrefix(), qname.getLocalPart(),
					operator, value);
		}
	}

	private static String compareNumericOrText(Expression expression,
			String value, String operator) {
		Node node = expression.getSelectedNode();

		switch (node.getSchemaProperty().getJavaTypeCode()) {
		case SchemaProperty.JAVA_INT:
			// do not wrap integer
			break;
		case SchemaProperty.JAVA_BIG_INTEGER:
			// do not wrap integer
			break;
		case SchemaProperty.JAVA_STRING:
			value = "\"" + value + "\"";
			break;
		default:
			throw new ApplicationException(
					String
							.format(
									"java type code [%s] not valid for operator [%s] with qname [%s]",
									node.getSchemaProperty().getJavaTypeCode(),
									operator, node.getQName().toString()));
		}

		return compare(expression, value, operator);
	}

	private static String compareNumeric(Expression expression, String value,
			String operator) {
		Node node = expression.getSelectedNode();

		switch (node.getSchemaProperty().getJavaTypeCode()) {
		case SchemaProperty.JAVA_INT:
			// do not wrap integer
			break;
		case SchemaProperty.JAVA_BIG_INTEGER:
			// do not wrap integer
			break;
		default:
			throw new ApplicationException(
					String
							.format(
									"java type code [%s] not valid for operator [%s] with qname [%s]",
									node.getSchemaProperty().getJavaTypeCode(),
									operator, node.getQName().toString()));
		}

		return compare(expression, value, operator);
	}

	public static String equal(Expression expression, String value) {
		return compareNumericOrText(expression, value, "=");
	}

	public static String notEqual(Expression expression, String value) {
		return compareNumericOrText(expression, value, "!=");
	}

	public static String lessThan(Expression expression, String value) {
		return compareNumeric(expression, value, "<");
	}

	public static String lessThanOrEqualTo(Expression expression, String value) {
		return compareNumeric(expression, value, "<=");
	}

	public static String greaterThan(Expression expression, String value) {
		return compareNumeric(expression, value, ">");
	}

	public static String greaterThanOrEqualTo(Expression expression,
			String value) {
		return compareNumeric(expression, value, ">=");
	}
}
