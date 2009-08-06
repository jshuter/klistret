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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.impl.common.QNameHelper;

import com.klistret.cmdb.exception.ApplicationException;

/**
 * 
 * @author Matthew Young
 * 
 */
public class PropertyExpression implements Expression {
	/**
	 * Regular expression for property location paths
	 */
	private final static String propertyPathExpression = "(\\w+)|(\\w+[.]\\w+)*";

	/**
	 * @see Expression
	 */
	private String variableReference = "this";

	/**
	 * @see Expression
	 */
	private String defaultElementNamespace;

	/**
	 * @see Expression
	 */
	private String defaultFunctionNamespace;

	/**
	 * XmlBeans Java Type for XML documents
	 */
	private SchemaType schemaType;

	/**
	 * Property location path
	 */
	private String propertyLocationPath;

	/**
	 * Ordered sequence of nodes (elements/attributes) mirroring the property
	 * location path
	 */
	private List<Node> nodes = new ArrayList<Node>();

	/**
	 * 
	 * @param classname
	 * @param propertyLocationPath
	 */
	public PropertyExpression(String classname, String propertyLocationPath) {
		this(XmlBeans.getContextTypeLoader().typeForClassname(classname),
				propertyLocationPath);
	}

	/**
	 * 
	 * @param qname
	 * @param propertyLocationPath
	 */
	public PropertyExpression(QName qname, String propertyLocationPath) {
		this(XmlBeans.getContextTypeLoader().findType(qname),
				propertyLocationPath);
	}

	/**
	 * 
	 * @param schemaType
	 * @param propertyLocationPath
	 */
	public PropertyExpression(SchemaType schemaType, String propertyLocationPath) {
		if (!propertyLocationPath.matches(propertyPathExpression))

			throw new ApplicationException(String.format(
					"path [%s] does not match expression [%s]",
					propertyLocationPath, propertyPathExpression));

		this.schemaType = schemaType;
		this.propertyLocationPath = propertyLocationPath;

		transformPropertyLocationPath(schemaType, propertyLocationPath);
	}

	public String getVariableReference() {
		return variableReference;
	}

	public void setVariableReference(String variableReference) {
		this.variableReference = variableReference;
	}

	public String getDefaultElementNamespace() {
		return defaultElementNamespace;
	}

	public void setDefaultElementNamespace(String defaultElementNamespace) {
		this.defaultElementNamespace = defaultElementNamespace;
	}

	public String getDefaultFunctionNamespace() {
		return defaultFunctionNamespace;
	}

	public void setDefaultFunctionNamespace(String defaultFunctionNamespace) {
		this.defaultFunctionNamespace = defaultFunctionNamespace;
	}

	public String getPropertyLocationPath() {
		return propertyLocationPath;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}

	/**
	 * Reuses an existing qname with prefix from the node list or adds a prefix
	 * to a new qname
	 * 
	 * @param qname
	 * @return
	 */
	private QName getQNameWithPrefix(QName qname) {
		/**
		 * empty name-space URIs not handled
		 */
		if (qname.getNamespaceURI() != null
				&& qname.getNamespaceURI().length() == 0)
			return qname;

		/**
		 * namespace URI may already exists in node list and the corresponding
		 * qname be reused otherwise create a new qname with prefix from
		 * QNameHelper
		 */
		for (Node node : nodes)
			if (node.getQName().getNamespaceURI().equals(
					qname.getNamespaceURI()))
				return node.getQName();

		return new QName(qname.getNamespaceURI(), qname.getLocalPart(),
				QNameHelper.suggestPrefix(qname.getNamespaceURI()));
	}

	/**
	 * Processes a property location one step at a time saving XmlBean
	 * constructs to the node list with the path's remainder recursively handled
	 * 
	 * @param schemaType
	 * @param propertyLocationPath
	 */
	private void transformPropertyLocationPath(SchemaType schemaType,
			String propertyLocationPath) {
		/**
		 * split properties between current and remainder to get at the first
		 * property
		 */
		String[] split = propertyLocationPath.split("[.]", 2);
		String property = split[0];

		/**
		 * reset propertyLocationPath as remainder if exists otherwise null
		 */
		if (split.length == 2) {
			propertyLocationPath = split[1];
		} else {
			propertyLocationPath = null;
		}

		/**
		 * locate current property from the set of properties in the schemaType
		 */
		SchemaProperty schemaProperty = null;
		for (SchemaProperty other : schemaType.getProperties()) {
			if (property.equals(other.getJavaPropertyName())) {
				schemaProperty = other;
			}
		}

		/**
		 * error if current property is null otherwise add to nodes list
		 */
		if (schemaProperty == null)
			throw new ApplicationException(
					String
							.format(
									"property [%s] neither an element nor attribute of class [%s]",
									property, schemaType.getFullJavaName()));
		nodes.add(new Node(schemaProperty, getQNameWithPrefix(schemaProperty
				.getName())));

		/**
		 * recursive call with the schemaType of the current property and path
		 * remainder if not null
		 */
		if (propertyLocationPath != null)
			transformPropertyLocationPath(schemaProperty.getType(),
					propertyLocationPath);
	}

	/**
	 * Prolog consists of a declaration that defines the processing environment
	 * for an XPath expression. A declaration in the prolog is followed by a
	 * semicolon (;). The prolog is an optional part of the XPath expression.
	 * 
	 * @return Prolog
	 */
	private String getProlog() {
		StringBuilder prolog = new StringBuilder();

		// append default element declaration
		if (defaultElementNamespace != null)
			prolog.append(String.format(
					"declare default element namespace \"%s\";",
					defaultElementNamespace));

		// append default function declaration
		if (defaultFunctionNamespace != null)
			prolog.append(String.format(
					"declare default function namespace \"%s\";",
					defaultFunctionNamespace));

		// append declaration derived from the node list without duplicates
		for (Node node : nodes) {
			if (prolog.indexOf(node.getQName().getPrefix()) == -1)
				prolog.append(String.format("declare namespace %s=\"%s\";",
						node.getQName().getPrefix(), node.getQName()
								.getNamespaceURI()));
		}

		return prolog.toString();
	}

	private String getAttribute(Node node) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("/@");

		if (!(node.getQName().getPrefix() != null && node.getQName()
				.getPrefix().length() == 0))
			buffer.append(node.getQName().getPrefix()).append(":");

		return buffer.append(node.getQName().getLocalPart()).toString();
	}

	private String getElement(Node node) {
		StringBuilder buffer = new StringBuilder();

		return buffer.append("/").append(node.getQName().getPrefix()).append(
				":").append(node.getQName().getLocalPart()).toString();
	}

	private String getExpression(int length) {
		if (length > nodes.size())
			throw new ApplicationException(String.format(
					"node size [%d] when accessing length [%d]", nodes.size(),
					length));

		if (length < 0)
			throw new ApplicationException(String.format(
					"accessing with negative length [%d]", length));

		StringBuilder expression = new StringBuilder();
		expression.append(String.format("$%s", getVariableReference()));

		/**
		 * iterate through the node list up to passed length
		 */
		for (Node node : nodes.subList(0, length)) {
			if (node.getSchemaProperty().isAttribute())
				expression.append(getAttribute(node));
			else
				expression.append(getElement(node));
		}

		return expression.toString();
	}

	/**
	 * Complete xpath representation of property location path
	 * 
	 * @return XPath expression
	 */
	private String getExpression() {
		return getExpression(nodes.size());
	}

	/**
	 * Concatenation of prolog and expression clauses
	 * 
	 * @return XPath
	 */
	public String toString() {
		StringBuilder xpath = new StringBuilder();

		xpath.append(getProlog());

		xpath.append(getExpression());

		return xpath.toString();
	}

	/**
	 * Applies function to xpath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	private String compareFn(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

		StringBuilder xpath = new StringBuilder();

		xpath.append(getProlog());

		xpath.append(getExpression(nodes.size() - 1));

		if (node.getSchemaProperty().isAttribute()) {
			if (node.getQName().getPrefix() != null
					&& node.getQName().getPrefix().length() == 0)
				xpath.append(String.format("[%s(@%s,\"%s\")]", operator, node
						.getQName().getLocalPart(), value));
			else
				xpath.append(String.format("[%s(@%s:%s,\"%s\")]", operator,
						node.getQName().getPrefix(), node.getQName()
								.getLocalPart(), value));
		} else
			xpath.append(String.format("[%s(%s:%s,\"%s\")]", operator, node
					.getQName().getPrefix(), node.getQName().getLocalPart(),
					value));

		return xpath.toString();
	}

	/**
	 * Applies text function to xpath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	private String compareFnText(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

		if (node.getSchemaProperty().getJavaTypeCode() != SchemaProperty.JAVA_STRING)
			throw new ApplicationException(
					String
							.format(
									"java type code [%s] not valid for function [%s] with qname [%s]",
									node.getSchemaProperty().getJavaTypeCode(),
									operator, node.getQName().toString()));

		return compareFn(operator, value);
	}

	/**
	 * Applies operation to xpath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	private String compareOp(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

		StringBuilder xpath = new StringBuilder();

		xpath.append(getProlog());

		xpath.append(getExpression(nodes.size() - 1));

		if (node.getSchemaProperty().isAttribute()) {
			if (node.getQName().getPrefix() != null
					&& node.getQName().getPrefix().length() == 0)
				xpath.append(String.format("[@%s %s %s]", node.getQName()
						.getLocalPart(), operator, value));
			else
				xpath.append(String.format("[@%s:%s %s %s]", node.getQName()
						.getPrefix(), node.getQName().getLocalPart(), operator,
						value));
		} else
			xpath.append(String.format("/%s:%s[. %s %s]", node.getQName()
					.getPrefix(), node.getQName().getLocalPart(), operator,
					value));

		return xpath.toString();
	}

	/**
	 * Applies text/numeric operation to xpath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	private String compareOpNumericOrText(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

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

		return compareOp(operator, value);
	}

	/**
	 * Applies numeric operation to xpath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	public String compareOpNumeric(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

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

		return compareOp(operator, value);
	}

	public String matches(String value) {
		return compareFnText("fn:matches", value);
	}

	public String contains(String value) {
		return compareFnText("fn:contains", value);
	}

	public String startsWith(String value) {
		return compareFnText("fn:starts-with", value);
	}

	public String endsWith(String value) {
		return compareFnText("fn:ends-with", value);
	}

	public String equal(String value) {
		return compareOpNumericOrText("=", value);
	}

	public String notEqual(String value) {
		return compareOpNumericOrText("!=", value);
	}

	public String lessThan(String value) {
		return compareOpNumeric("<", value);
	}

	public String lessThanOrEqual(String value) {
		return compareOpNumeric("<=", value);
	}

	public String greaterThan(String value) {
		return compareOpNumeric(">", value);
	}

	public String greaterThanOrEqual(String value) {
		return compareOpNumeric(">=", value);
	}

	/**
	 * 
	 * @author Matthew Young
	 * 
	 */
	private class Node {
		private SchemaProperty schemaProperty;

		private QName qname;

		/**
		 * 
		 * @param schemaProperty
		 *            represents a summary of the the elements with a given name
		 *            or the attribute with a given name
		 * @param qname
		 *            with non-duplicate prefix
		 */
		public Node(SchemaProperty schemaProperty, QName qname) {
			this.schemaProperty = schemaProperty;
			this.qname = qname;
		}

		public QName getQName() {
			return this.qname;
		}

		public SchemaProperty getSchemaProperty() {
			return this.schemaProperty;
		}
	}
}
