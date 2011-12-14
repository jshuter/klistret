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
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.hibernate.HibernateException;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.jaxb.CIContext;

/**
 * Hibernate User type which marshals/unmarshals XML columns (string data) into
 * POJO. The JAXBContext is constructed every time the type parameters are set
 * and these are pulled from the Spring configuration (properties) into an
 * extension of the Spring LocalSessionFactoryBean. This is done to bypass using
 * the Hibernate mapping documents (ie. no hard coding) plus also for potential
 * Spring refreshing of the session factory if the CI hierarchy should
 * dynamically change.
 * 
 * TODO: Performance testing shows that the Pooling saves about 25% in response
 * times (marshall, unmarshall). Without pooling the constant creation of
 * marshall, unmarshall objects (no hit to GC) is not as stable (response times
 * again). The bigger fish is finding a way to cut the number of nullSafeGet
 * calls (4:1 against the nullSafeSet calls). For every call (CRUD) there are
 * about 4 nullSafeGet calls.
 * 
 */
public class JAXBUserType implements UserType {

	private static final Logger logger = LoggerFactory
			.getLogger(JAXBUserType.class);

	/**
	 * Static SQL type definition (string data)
	 */
	private static final int[] SQL_TYPES = { Types.VARCHAR };

	/**
	 * Unmarshaller pool
	 */
	private GenericObjectPool unmarshallerPool = new GenericObjectPool(
			new UnmarshallerFactory(), 100,
			GenericObjectPool.WHEN_EXHAUSTED_FAIL, 0);

	/**
	 * Marshaller pool
	 */
	private GenericObjectPool marshallerPool = new GenericObjectPool(
			new MarshallerFactory(), 50, GenericObjectPool.WHEN_EXHAUSTED_FAIL,
			0);

	/**
	 * 
	 */
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		if (!(cached instanceof String)) {
			throw new IllegalArgumentException("Cached value must be a String",
					new IllegalArgumentException());
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

	@SuppressWarnings("deprecation")
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		String xmlString = (String) StandardBasicTypes.STRING.nullSafeGet(rs,
				names[0]);

		logger.debug("getting xml data [{}] as Object", xmlString);
		return fromXMLString(xmlString);
	}

	@SuppressWarnings("deprecation")
	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		logger.debug("setting Object [{}] as xml", value);

		String xmlString = toXMLString(value);
		StandardBasicTypes.STRING.nullSafeSet(st, xmlString, index);
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

	/**
	 * Marshals the object into XML
	 * 
	 * @param value
	 * @return
	 */
	public String toXMLString(Object value) {
		StringWriter stringWriter = new StringWriter();
		Marshaller m = null;
		try {
			m = (Marshaller) marshallerPool.borrowObject();

			m.marshal(value, stringWriter);
			String result = stringWriter.toString();
			stringWriter.close();
			logger.debug("marshalled Object class [{}] to xml [{}]", value
					.getClass().getName(), result);

			return result;
		} catch (Exception e) {
			logger.error("Unable to disassemble object: {}", e.getMessage());
			throw new InfrastructureException(String.format(
					"Unable to disassemble object: %s", e.getMessage()));
		} finally {
			try {
				if (null != m) {
					marshallerPool.returnObject(m);
				}
			} catch (Exception e) {
				logger.error("Unable to return marshaller to pool: {}", e);
			}
		}
	}

	/**
	 * Unmarshals the XML into a POJO
	 * 
	 * @param xmlString
	 * @return
	 */
	public Object fromXMLString(String xmlString) {
		logger.debug("unmarshalling xml [{}]", xmlString);
		Unmarshaller um = null;

		StreamSource source = new StreamSource(new StringReader(xmlString));
		try {
			um = (Unmarshaller) unmarshallerPool.borrowObject();

			Object result = um.unmarshal(source);
			logger.debug("unmarshalled xml [{}] to Object class [{}]",
					xmlString, result.getClass().getName());
			return result;
		} catch (Exception e) {
			logger.error("Unable to assemble object: {} [pool active: {}]",
					e.getMessage(), unmarshallerPool.getNumActive());
			throw new InfrastructureException(String.format(
					"Unable to assemble object: %s", e.getMessage()));
		} finally {
			try {
				if (null != um) {
					unmarshallerPool.returnObject(um);
				}
			} catch (Exception e) {
				logger.error("Unable to return unmarshaller to pool: {}", e);
			}
		}
	}

	public class UnmarshallerFactory implements PoolableObjectFactory {

		@Override
		public Object makeObject() throws Exception {
			try {
				JAXBContext jaxbContext = CIContext.getCIContext()
						.getJAXBContext();
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

				unmarshaller.setSchema(CIContext.getCIContext().getSchema());
				unmarshaller.setEventHandler(new ValidationEventHandler() {
					public boolean handleEvent(ValidationEvent event) {
						logger.debug("Validation error: {}", event);
						return false;
					}
				});

				return unmarshaller;
			} catch (JAXBException e) {
				logger.error("Unable to create JAXB ummarshaller: {}", e);
				throw new InfrastructureException(String.format(
						"Unable to create JAXB ummarshaller: %s", e));
			}
		}

		@Override
		public void destroyObject(Object obj) throws Exception {
		}

		@Override
		public boolean validateObject(Object obj) {
			return true;
		}

		@Override
		public void activateObject(Object obj) throws Exception {
		}

		@Override
		public void passivateObject(Object obj) throws Exception {
		}

	}

	public class MarshallerFactory implements PoolableObjectFactory {

		@Override
		public Object makeObject() throws Exception {
			try {
				JAXBContext jaxbContext = CIContext.getCIContext()
						.getJAXBContext();
				Marshaller marshaller = jaxbContext.createMarshaller();

				marshaller.setSchema(CIContext.getCIContext().getSchema());
				marshaller.setEventHandler(new ValidationEventHandler() {
					public boolean handleEvent(ValidationEvent event) {
						logger.debug("Validation error: {}", event);
						return false;
					}
				});

				return marshaller;
			} catch (JAXBException e) {
				logger.error("Unable to create JAXB marshaller: {}", e);
				throw new InfrastructureException(String.format(
						"Unable to create JAXB marshaller: %s", e));
			}
		}

		@Override
		public void destroyObject(Object obj) throws Exception {
		}

		@Override
		public boolean validateObject(Object obj) {
			return true;
		}

		/**
		 * TODO: Add control for type/interface
		 */
		public void activateObject(Object obj) throws Exception {
		}

		/**
		 * TODO: Add control for type/interface otherwise there is strong risk
		 * that somewhere in the code the wrong object is recycled back to the
		 * cache.
		 */
		@Override
		public void passivateObject(Object obj) throws Exception {
		}

	}
}