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

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;

@Configurable(dependencyCheck=true)
public class JAXBUserType implements UserType {
	private final static String[] assignablePackages = { "com/klistret/cmdb" };

	private final static String[] baseTypes = { "com.klistret.cmdb.Base" };

	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	private JAXBContext jaxbContext;

	private static final Logger logger = LoggerFactory
			.getLogger(JAXBUserType.class);

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Autowired
	public void setJAXBContextHelper(JAXBContextHelper jaxbContextHelper) {
		logger.debug("injected the jaxb context helper");
		jaxbContext = jaxbContextHelper.getJAXBContext();
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		if (!(cached instanceof String)) {
			throw new IllegalArgumentException("Cached value must be a String");
		}
		logger.debug("assembling string [{}]", (String) cached);
		return fromXMLString((String) cached);
	}

	public Object deepCopy(Object value) throws HibernateException {
		logger.debug("deep copy Object class [{}]", value.getClass().getName());
		return fromXMLString(toXMLString(value));
	}

	public Serializable disassemble(Object value) throws HibernateException {
		logger.debug("disassembling Object class [{}]", value.getClass()
				.getName());
		return toXMLString(value);
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

	public int hashCode(Object x) throws HibernateException {
		if (x == null) {
			throw new IllegalArgumentException(
					" Parameter for hashCode must not be null");
		}
		return x.hashCode();
	}

	public boolean isMutable() {
		return true;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		String xmlString = (String) Hibernate.TEXT.nullSafeGet(rs, names[0]);

		logger.debug("getting xml data [{}] as Object", xmlString);
		return fromXMLString(xmlString);
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		logger.debug("setting Object [{}] as xml", value);

		String xmlString = toXMLString(value);
		Hibernate.TEXT.nullSafeSet(st, xmlString, index);
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return deepCopy(original);
	}

	public Class<Object> returnedClass() {
		return Object.class;
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	protected JAXBContext getJAXBContext() {
		if (jaxbContext == null)
			jaxbContext = new JAXBContextHelper(baseTypes, assignablePackages)
					.getJAXBContext();

		return jaxbContext;
	}

	protected Unmarshaller getUnmarshaller() {
		if (unmarshaller == null) {
			JAXBContext jc = getJAXBContext();
			try {
				unmarshaller = jc.createUnmarshaller();
			} catch (JAXBException e) {
				IllegalArgumentException ex = new IllegalArgumentException(
						"Cannot instantiate unmarshaller");
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		}
		return unmarshaller;
	}

	protected Marshaller getMarshaller() {
		if (marshaller == null) {
			JAXBContext jc = getJAXBContext();
			try {
				marshaller = jc.createMarshaller();
			} catch (JAXBException e) {
				IllegalArgumentException ex = new IllegalArgumentException(
						"Cannot instantiate marshaller");
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		}
		return marshaller;
	}

	@SuppressWarnings("unchecked")
	protected String toXMLString(Object value) {
		StringWriter stringWriter = new StringWriter();
		try {
			getMarshaller().marshal(value, stringWriter);
			String result = stringWriter.toString();
			stringWriter.close();
			logger.debug("marshalled Object class [{}] to xml [{}]", value
					.getClass().getName(), result);

			return result;
		} catch (Exception e) {
			throw new InfrastructureException(String.format(
					"Unable to disassemble object: %s", e.getMessage()));
		}
	}

	protected Object fromXMLString(String xmlString) {
		logger.debug("unmarshalling xml [{}]", xmlString);

		Unmarshaller unmarshaller = getUnmarshaller();
		StreamSource source = new StreamSource(new StringReader(xmlString));
		try {
			Object result = unmarshaller.unmarshal(source);
			logger.debug("unmarshalled xml [{}] to Object class [{}]",
					xmlString, result.getClass().getName());
			return result;
		} catch (JAXBException e) {
			throw new InfrastructureException(String.format(
					"Unable to assemble object: %s", e.getMessage()));
		}
	}
}
