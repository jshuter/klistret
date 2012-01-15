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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

/**
 * Returns an extended persistent collection that disables persistence. This
 * class is jacked into the XML configuration for Elements.
 * 
 * @author Matthew Young
 * 
 */
public class LazyCollectionType implements UserCollectionType {

	@Override
	public PersistentCollection instantiate(SessionImplementor session,
			CollectionPersister persister) throws HibernateException {
		return new LazyPersistentList(session);
	}

	@Override
	public PersistentCollection wrap(SessionImplementor session,
			Object collection) {
		return new LazyPersistentList(session, (List<?>) collection);
	}

	/**
	 * Left returning null to intentionally create an error
	 */
	@Override
	public Iterator<?> getElementsIterator(Object collection) {
		return ((List<?>) collection).iterator();
	}

	/**
	 * Returns false since the collection is never to be used
	 */
	@Override
	public boolean contains(Object collection, Object entity) {
		return false;
	}

	/**
	 * Left returning null to intentionally create an error
	 */
	@Override
	public Object indexOf(Object collection, Object entity) {
		return null;
	}

	/**
	 * Left returning null to intentionally create an error
	 */
	@Override
	public Object replaceElements(Object original, Object target,
			CollectionPersister persister, Object owner,
			@SuppressWarnings("rawtypes") Map copyCache,
			SessionImplementor session) throws HibernateException {
		return original;
	}

	/**
	 * Returns an empty Array list of objects
	 */
	@Override
	public Object instantiate(int anticipatedSize) {
		return new ArrayList<Object>();
	}

}
