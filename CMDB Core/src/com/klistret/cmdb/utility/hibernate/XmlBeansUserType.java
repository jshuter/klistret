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

package com.klistret.cmdb.utility.hibernate;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.klistret.cmdb.exception.InfrastructureException;

public class XmlBeansUserType implements UserType {

	private final static Logger logger = Logger
			.getLogger(XmlBeansUserType.class.getName());

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	public int[] sqlTypes() {
		return SQL_TYPES;
	}
	
	public Class<XmlObject> returnedClass() {
		return XmlObject.class;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		} else if (x == null || y == null) {
			return false;
		} else {
			return x.equals(y);
		}
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
			throws HibernateException, SQLException {
		String configuration = resultSet.getString(names[0]);

		if (configuration == null) {
			logger.fine("configuration column is null");
			return null;
		} else {
			logger.fine("parsing XML to XMLBean");
			try {
				XmlObject document = XmlObject.Factory.parse(configuration);
				SchemaType documentSchemaType = document.schemaType();

				logger.fine("XML string of type ["
						+ documentSchemaType.getJavaClass().toString()
						+ "], isDocument ["
						+ documentSchemaType.isDocumentType()
						+ "], document element qname ["
						+ documentSchemaType.getDocumentElementName()
								.toString() + "]");

				XmlObject[] xmlObjectArray = document
						.selectChildren(documentSchemaType
								.getDocumentElementName());

				if (xmlObjectArray.length != 1) {
					throw new InfrastructureException(
							"document elements not singelton");
				}

				XmlObject xmlObject = xmlObjectArray[0];
				logger.fine("returning XmlObject [" + xmlObject.xmlText()
						+ "]");
				return xmlObject;

			} catch (XmlException xmle) {
				throw new InfrastructureException(xmle);
			} catch (NullPointerException npe) {
				throw new InfrastructureException(npe);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null) {
			logger.fine("saving null to XML management");
			statement.setString(index, null);
		} else {
			StringWriter writer = new StringWriter();

			XmlObject xmlObject = (XmlObject) value;
			SchemaType xmlObjectSchemaType = xmlObject.schemaType();

			try {
				Class<?>[] emptyParameters = new Class[0];

				String documentClassName = xmlObjectSchemaType
						.getFullJavaName()
						+ "Document$Factory";
				Class<?> factory = Class.forName(documentClassName);

				Method newInstance = factory.getMethod("newInstance",
						emptyParameters);
				XmlObject document = (XmlObject) newInstance.invoke(null,
						(Object[]) emptyParameters);

				SchemaType documentSchemaType = document.schemaType();
				Method setDocumentElement = documentSchemaType.getJavaClass()
						.getMethod(
								"set" + xmlObjectSchemaType.getShortJavaName(),
								xmlObjectSchemaType.getJavaClass());
				Object[] arguments = { xmlObject };
				setDocumentElement.invoke(document, arguments);

				if (!documentSchemaType.isDocumentType()) {
					throw new InfrastructureException(documentSchemaType
							.getFullJavaName()
							+ " is not registered as a Document");
				}

				logger.fine("saving XMLBean ["
						+ documentSchemaType.getFullJavaName() + "] to XML");
				logger.fine("contents [" + document.xmlText() + "]");
				document.save(writer);
			} catch (ClassNotFoundException e) {
				throw new InfrastructureException(e);
			} catch (SecurityException e) {
				throw new InfrastructureException(e);
			} catch (NoSuchMethodException e) {
				throw new InfrastructureException(e);
			} catch (IllegalArgumentException e) {
				throw new InfrastructureException(e);
			} catch (IllegalAccessException e) {
				throw new InfrastructureException(e);
			} catch (InvocationTargetException e) {
				throw new InfrastructureException(e);
			} catch (IOException ie) {
				throw new InfrastructureException(ie);
			}

			statement.setString(index, writer.toString());
		}

	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public boolean isMutable() {
		return false;
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

}
