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
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;

import com.klistret.cmdb.exception.ApplicationException;

public class PropertyExpression implements Expression {
	private final static String propertyPathExpression = "(\\w+)|(\\w+[.]\\w+)*";

	private String context = "this";

	private String defaultElementPrefix;

	private String defaultFunctionPrefix;

	private SchemaType schemaType;

	private String path;

	private ArrayList<Node> nodes = new ArrayList<Node>(5);

	private Hashtable<String, QName> namespaces = new Hashtable<String, QName>();

	public PropertyExpression(QName qname, String path) {
		this(XmlBeans.getContextTypeLoader().findType(qname), path);
	}

	public PropertyExpression(SchemaType schemaType, String path) {
		if (!path.matches(propertyPathExpression))

			throw new ApplicationException(String.format(
					"path [%s] does not match expression [%s]", path,
					propertyPathExpression));

		this.schemaType = schemaType;
		this.path = path;

		resolve(schemaType, path);
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDefaultElementPrefix() {
		return defaultElementPrefix;
	}

	public void setDefaultElementPrefix(String defaultElementPrefix) {
		this.defaultElementPrefix = defaultElementPrefix;
	}

	public String getDefaultFunctionPrefix() {
		return defaultFunctionPrefix;
	}

	public void setDefaultFunctionPrefix(String defaultFunctionPrefix) {
		this.defaultFunctionPrefix = defaultFunctionPrefix;
	}

	public String getPath() {
		return path;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}

	public Node getSelectedNode() {
		return nodes.get(nodes.size() - 1);
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<String> getDeclarations() {
		ArrayList<String> declarations = new ArrayList<String>(namespaces
				.size());

		for (Map.Entry<String, QName> map : namespaces.entrySet()) {
			QName qname = map.getValue();

			declarations.add(String.format("declare namespace %s=\'%s\';",
					qname.getPrefix(), qname.getNamespaceURI()));
		}

		return declarations;
	}

	public String getDeclareClause() {
		StringBuilder buffer = new StringBuilder();

		// add default element declaration
		if (defaultElementPrefix != null)
			buffer.append(String.format(
					"declare default element namespace \'%s\';",
					defaultElementPrefix));

		// add default function declaration
		if (defaultFunctionPrefix != null)
			buffer.append(String.format(
					"declare default function namespace \'%s\';",
					defaultFunctionPrefix));

		// add declarations (doesn't harm to add the last nodes declare)
		for (String declaration : getDeclarations()) {
			buffer.append(declaration);
		}

		return buffer.toString();
	}

	public String getParentXPath() {
		StringBuilder buffer = new StringBuilder();

		// add context
		if (context != null)
			buffer.append("$").append(context);
		
		// add nodes (except for last)
		for (Node node : nodes.subList(0, nodes.size() - 1)) {
			if (node.getSchemaProperty().isAttribute())
				buffer.append(getAttribute(node));
			else
				buffer.append(getElement(node));
		}

		return buffer.toString();
	}

	public String getXPath() {
		StringBuilder buffer = new StringBuilder(getParentXPath());

		// add selected node
		Node node = getSelectedNode();
		if (node.getSchemaProperty().isAttribute())
			buffer.append(getAttribute(node));
		else
			buffer.append(getElement(node));

		return buffer.toString();
	}

	public String toString() {
		StringBuilder buffer = new StringBuilder();

		// add declare clause
		buffer.append(getDeclareClause());

		// add xpath
		buffer.append(getXPath());

		return buffer.toString();
	}

	private String getAttribute(Node node) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("/@");

		if (!node.getQName().getPrefix().isEmpty())
			buffer.append(node.getQName().getPrefix()).append(":");

		return buffer.append(node.getQName().getLocalPart()).toString();
	}

	private String getElement(Node node) {
		StringBuilder buffer = new StringBuilder();

		return buffer.append("/").append(node.getQName().getPrefix()).append(
				":").append(node.getQName().getLocalPart()).toString();
	}

	private QName addNamespace(QName qname) {
		// control empty namespaces
		if (qname.getNamespaceURI().isEmpty())
			return qname;

		// split URI to capture last logical node as prefix
		String[] nodes = qname.getNamespaceURI().split("/");
		String prefix = nodes[nodes.length - 1];

		QName other = new QName(qname.getNamespaceURI(), qname.getLocalPart(),
				prefix);

		// add other qname when not in namespaces
		if (!namespaces.containsKey(qname.getNamespaceURI()))
			namespaces.put(qname.getNamespaceURI(), other);

		return other;
	}

	private void resolve(SchemaType schemaType, String path) {
		// next schemaType
		SchemaProperty nextProperty = null;

		// split properties between first and remainder
		String[] pathSplit = path.split("[.]", 2);
		String property = pathSplit[0];

		// path is remainder otherwise null
		if (pathSplit.length == 2) {
			path = pathSplit[1];
		} else {
			path = null;
		}

		// search after element/attribute properties by name
		SchemaProperty[] properties = schemaType.getProperties();
		for (SchemaProperty schemaProperty : properties) {
			if (property.equals(schemaProperty.getJavaPropertyName())) {
				nextProperty = schemaProperty;
			}
		}

		// error if property not found
		if (nextProperty == null)
			throw new ApplicationException(
					String
							.format(
									"property [%s] neither a child element or attribute of class [%s]",
									property, schemaType.getFullJavaName()));

		// add the schema property into the node containers
		nodes.add(new Node(nextProperty, addNamespace(nextProperty.getName())));

		// recursive if path is not empty
		if (path != null)
			resolve(nextProperty.getType(), path);
	}
}
