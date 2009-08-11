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
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

/**
 * XmlBeans gives a great interface for selection/querying based on XPath/XQuery
 * statements but no way to pull data with property path expression. Odd since
 * the representation of XML is thrown around as POJOs. Maybe a good addition in
 * the future to the XmlBeans project. Basically, this class creates XPath
 * selections or comparative functions on nested properties within a particular
 * SchemaType object. Property based criteria can be translated into XPath
 * querying against XmlObjects or against a XML database (i.e. inside SQL).
 * 
 * @author Matthew Young
 * 
 */
public class PropertyExpression implements Expression {
	private static final Logger logger = LoggerFactory
			.getLogger(PropertyExpression.class);

	/**
	 * Regular expression for property location paths
	 */
	private final static String propertyPathRegularExpression = "(\\w+)|(\\w+[.]\\w+)*";

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
	 * Constructs a property expression (returning a XPath selection and basic
	 * function calls) based on the passed XmlObject java class name and
	 * property path
	 * 
	 * @param classname
	 *            XmlObject (document type only) java class name
	 * @param propertyLocationPath
	 */
	public PropertyExpression(String classname, String propertyLocationPath) {
		this(SchemaTypeHelper.getSchemaType(classname), propertyLocationPath);
	}

	/**
	 * Constructs a property expression (returning a XPath selection and basic
	 * function calls) based on the passed QName and property path
	 * 
	 * @param qname
	 *            (document type only)
	 * @param propertyLocationPath
	 */
	public PropertyExpression(QName qname, String propertyLocationPath) {
		this(SchemaTypeHelper.getSchemaType(qname), propertyLocationPath);
	}

	/**
	 * Constructor first adds the document type's schema property to the node
	 * list then translates each property in the path, in order, into nodes. The
	 * addition of the document type is important since the XML saved into the
	 * database is a complete document not a global element therefore the
	 * generated XPath must start from the root (also more efficient SQL
	 * queries).
	 * 
	 * @param schemaType
	 * @param propertyLocationPath
	 */
	private PropertyExpression(SchemaType schemaType,
			String propertyLocationPath) {
		this.schemaType = schemaType;
		this.propertyLocationPath = propertyLocationPath;

		/**
		 * property path must match regular expression
		 */
		if (!propertyLocationPath.matches(propertyPathRegularExpression)) {
			logger.error("path [{}] does not match expression [{}]",
					propertyLocationPath, propertyPathRegularExpression);
			throw new ApplicationException(String.format(
					"path [%s] does not match expression [%s]",
					propertyLocationPath, propertyPathRegularExpression));
		}

		/**
		 * adds the document type's schema property to the nodes
		 */
		SchemaType documentSchemaType = SchemaTypeHelper.getDocument(schemaType
				.getName());
		if (documentSchemaType != null) {
			SchemaProperty schemaProperty = documentSchemaType
					.getElementProperty(schemaType.getName());
			nodes.add(new Node(schemaProperty,
					getQNameWithPrefix(schemaProperty.getName())));
		}

		/**
		 * translate the property path into nodes (schema property, qname pairs)
		 */
		transformPropertyLocationPath(schemaType, propertyLocationPath);
	}

	/**
	 * Singular variable reference to the context
	 * 
	 * @return String
	 */
	public String getVariableReference() {
		return variableReference;
	}

	/**
	 * Set variable reference
	 */
	public void setVariableReference(String variableReference) {
		this.variableReference = variableReference;
	}

	/**
	 * Default element name-space
	 * 
	 * @return String
	 */
	public String getDefaultElementNamespace() {
		return defaultElementNamespace;
	}

	/**
	 * Set default element name-space
	 */
	public void setDefaultElementNamespace(String defaultElementNamespace) {
		this.defaultElementNamespace = defaultElementNamespace;
	}

	/**
	 * Default function name-space (used for database queries whereby the
	 * database manager usually has a specific name-space for XPath functions)
	 * 
	 * @return String
	 */
	public String getDefaultFunctionNamespace() {
		return defaultFunctionNamespace;
	}

	/**
	 * Set default function name-space
	 */
	public void setDefaultFunctionNamespace(String defaultFunctionNamespace) {
		this.defaultFunctionNamespace = defaultFunctionNamespace;
	}

	/**
	 * Original property location path upon construction
	 * 
	 * @return String
	 */
	public String getPropertyLocationPath() {
		return propertyLocationPath;
	}

	/**
	 * Derived schema type upon construction
	 * 
	 * @return SchemaType
	 */
	public SchemaType getSchemaType() {
		return schemaType;
	}

	/**
	 * Reuses an existing QName with prefix from the node list or adds a prefix
	 * to a new QName
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
		 * name-space URI may already exists in node list and the corresponding
		 * QName be reused otherwise create a new QName with prefix from
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
		if (schemaProperty == null) {
			logger
					.error(
							"property [{}] neither an element nor attribute of class [{}]",
							property, schemaType.getFullJavaName());
			throw new ApplicationException(
					String
							.format(
									"property [%s] neither an element nor attribute of class [%s]",
									property, schemaType.getFullJavaName()));
		}
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
			if (prolog.indexOf(String.format("declare namespace %s=\"%s\";",
					node.getQName().getPrefix(), node.getQName()
							.getNamespaceURI())) == -1)
				prolog.append(String.format("declare namespace %s=\"%s\";",
						node.getQName().getPrefix(), node.getQName()
								.getNamespaceURI()));
		}

		return prolog.toString();
	}

	/**
	 * Construct attribute (string) based on passed Node
	 * 
	 * @param node
	 * @return String
	 */
	private String getAttribute(Node node) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("/@");

		if (!(node.getQName().getPrefix() != null && node.getQName()
				.getPrefix().length() == 0))
			buffer.append(node.getQName().getPrefix()).append(":");

		return buffer.append(node.getQName().getLocalPart()).toString();
	}

	/**
	 * Construction element (string) passed on passed Node
	 * 
	 * @param node
	 * @return String
	 */
	private String getElement(Node node) {
		StringBuilder buffer = new StringBuilder();

		return buffer.append("/").append(node.getQName().getPrefix()).append(
				":").append(node.getQName().getLocalPart()).toString();
	}

	private String getExpression(int start, int end) {
		if (end > nodes.size()) {
			logger.error("node size [{}] when accessing end [{}]",
					nodes.size(), end);
			throw new ApplicationException(String
					.format("node size [%d] when accessing end [%d]", nodes
							.size(), end));
		}

		if (end < 0) {
			logger.error("accessing with negative end [{}]", end);
			throw new ApplicationException(String.format(
					"accessing with negative end [%d]", end));
		}

		StringBuilder expression = new StringBuilder();
		expression.append(String.format("$%s", getVariableReference()));

		/**
		 * iterate through the node list up to passed length
		 */
		for (Node node : nodes.subList(start, end)) {
			if (node.getSchemaProperty().isAttribute())
				expression.append(getAttribute(node));
			else
				expression.append(getElement(node));
		}

		return expression.toString();
	}

	/**
	 * Concatenation of prolog and expression clauses
	 * 
	 * @param documentContext
	 *            toggle inclusion of the document global element in the XPath
	 * @return XPath
	 */
	public String toString(boolean documentContext) {
		StringBuilder xpath = new StringBuilder();

		xpath.append(getProlog());

		xpath.append(documentContext ? getExpression(0, nodes.size())
				: getExpression(1, nodes.size()));

		return xpath.toString();
	}

	/**
	 * Concatenation of prolog and expression clauses
	 * 
	 * @return String
	 */
	public String toString() {
		return toString(true);
	}

	/**
	 * Applies comparative function to XPath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	private String compareFn(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

		StringBuilder xpath = new StringBuilder();

		xpath.append(getProlog());

		xpath.append(getExpression(0, nodes.size() - 1));

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
	 * Applies comparative text function to XPath
	 * 
	 * @param operator
	 * @param value
	 * @return XPath
	 */
	private String compareFnText(String operator, String value) {
		Node node = nodes.get(nodes.size() - 1);

		if (node.getSchemaProperty().getJavaTypeCode() != SchemaProperty.JAVA_STRING) {
			Object[] argArray = { node.getSchemaProperty().getJavaTypeCode(),
					operator, node.getQName().toString() };
			logger
					.error(
							"java type code [{}] not valid for function [{}] with qname [{}]",
							argArray);
			throw new ApplicationException(
					String
							.format(
									"java type code [%s] not valid for function [%s] with qname [%s]",
									node.getSchemaProperty().getJavaTypeCode(),
									operator, node.getQName().toString()));
		}

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

		xpath.append(getExpression(0, nodes.size() - 1));

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
	 * Applies comparative text/numeric operation to XPath
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
			Object[] argArray = { node.getSchemaProperty().getJavaTypeCode(),
					operator, node.getQName().toString() };
			logger
					.error(
							"java type code [{}] not valid for operator [{}] with qname [{}]",
							argArray);
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
	 * Applies comparative numeric operation to XPath
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
			Object[] argArray = { node.getSchemaProperty().getJavaTypeCode(),
					operator, node.getQName().toString() };
			logger
					.error(
							"java type code [{}] not valid for operator [{}] with qname [{}]",
							argArray);
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
