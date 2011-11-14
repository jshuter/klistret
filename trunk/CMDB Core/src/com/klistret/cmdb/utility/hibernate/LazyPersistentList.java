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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;
import org.hibernate.util.CollectionHelper;

/**
 * During gets both for Elements/relations only the following methods are used:
 * setOwner, setSnapshot, getValue and unsetSession. When updating Hibernate
 * pulls down a snapshot of the Element first then replaces data. So the
 * CollectionType extension replaces the elements in the replaceElements method.
 * That class just returns the original object. There after this class checks
 * isDirty (always false), wasInitialized (hard coded to false but as true the
 * effect is the same with extra calls to isDirectlyAccessible plus
 * equalsSnapshot). Both constructors are called, the passed list in the second
 * constructor is empty. Finally clearDirty, needsRecreate (empty array),
 * setSnapshot, toArray, and warInitialed are run prior to Element update. None
 * of the List methods are called.
 * 
 */
@SuppressWarnings("rawtypes")
public class LazyPersistentList implements PersistentCollection, List {

	private transient SessionImplementor session;

	protected List<?> list;

	private Object owner;

	private Serializable key;

	private String role;

	private Serializable storedSnapshot;


	public LazyPersistentList() {
	}

	public LazyPersistentList(SessionImplementor session) {
		this.session = session;
	}

	public LazyPersistentList(SessionImplementor session, List<?> list) {
		this(session);
		this.list = new ArrayList<Object>();
	}

	public Object getValue() {
		return new ArrayList<Object>();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Object getOwner() {
		return owner;
	}

	@Override
	public void setOwner(Object entity) {
		this.owner = entity;
	}

	@Override
	public boolean empty() {
		return list.isEmpty();
	}

	@Override
	public void setSnapshot(Serializable key, String role, Serializable snapshot) {
		this.key = key;
		this.role = role;
		this.storedSnapshot = snapshot;
	}

	@Override
	public void postAction() {
	}

	@Override
	public void beginRead() {
	}

	@Override
	public boolean endRead() {
		return true;
	}

	@Override
	public boolean afterInitialize() {
		return false;
	}

	@Override
	public boolean isDirectlyAccessible() {
		return true;
	}

	@Override
	public boolean unsetSession(SessionImplementor currentSession) {
		if (currentSession == this.session) {
			this.session = null;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean setCurrentSession(SessionImplementor session)
			throws HibernateException {
		return false;
	}

	@Override
	public void initializeFromCache(CollectionPersister persister,
			Serializable disassembled, Object owner) throws HibernateException {
	}

	@Override
	public Iterator entries(CollectionPersister persister) {
		return list.iterator();
	}

	@Override
	public Object readFrom(ResultSet rs, CollectionPersister role,
			CollectionAliases descriptor, Object owner)
			throws HibernateException, SQLException {
		return null;
	}

	@Override
	public Object getIdentifier(Object entry, int i) {
		return null;
	}

	@Override
	public Object getIndex(Object entry, int i, CollectionPersister persister) {
		return null;
	}

	@Override
	public Object getElement(Object entry) {
		return null;
	}

	@Override
	public Object getSnapshotElement(Object entry, int i) {
		return null;
	}

	@Override
	public void beforeInitialize(CollectionPersister persister,
			int anticipatedSize) {
	}

	@Override
	public boolean equalsSnapshot(CollectionPersister persister)
			throws HibernateException {
		return true;
	}

	@Override
	public boolean isSnapshotEmpty(Serializable snapshot) {
		return true;
	}

	@Override
	public Serializable disassemble(CollectionPersister persister)
			throws HibernateException {
		return new Serializable[0];
	}

	@Override
	public boolean needsRecreate(CollectionPersister persister) {
		return false;
	}

	@Override
	public Serializable getSnapshot(CollectionPersister persister)
			throws HibernateException {
		return new ArrayList<Object>();
	}

	@Override
	public void forceInitialization() throws HibernateException {
	}

	@Override
	public boolean entryExists(Object entry, int i) {
		return false;
	}

	@Override
	public boolean needsInserting(Object entry, int i, Type elemType)
			throws HibernateException {
		return false;
	}

	@Override
	public boolean needsUpdating(Object entry, int i, Type elemType)
			throws HibernateException {
		return false;
	}

	@Override
	public boolean isRowUpdatePossible() {
		return false;
	}

	@Override
	public Iterator getDeletes(CollectionPersister persister,
			boolean indexIsFormula) throws HibernateException {
		return new ArrayList<Object>().iterator();
	}

	@Override
	public boolean isWrapper(Object collection) {
		return list == collection;
	}

	@Override
	public boolean wasInitialized() {
		return true;
	}

	@Override
	public boolean hasQueuedOperations() {
		return false;
	}

	@Override
	public Iterator queuedAdditionIterator() {
		return new ArrayList<Object>().iterator();
	}

	@Override
	public Collection getQueuedOrphans(String entityName) {
		return CollectionHelper.EMPTY_COLLECTION;
	}

	@Override
	public Serializable getKey() {
		return this.key;
	}

	@Override
	public String getRole() {
		return this.role;
	}

	@Override
	public boolean isUnreferenced() {
		return role == null;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void clearDirty() {
	}

	@Override
	public Serializable getStoredSnapshot() {
		return this.storedSnapshot;
	}

	@Override
	public void dirty() {
	}

	@Override
	public void preInsert(CollectionPersister persister)
			throws HibernateException {
	}

	@Override
	public void afterRowInsert(CollectionPersister persister, Object entry,
			int i) throws HibernateException {
	}

	@Override
	public Collection getOrphans(Serializable snapshot, String entityName)
			throws HibernateException {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public Iterator iterator() {
		return new ArrayList<Object>().iterator();
	}

	@Override
	public Object[] toArray() {
		return new Object[] {};
	}

	@Override
	public Object[] toArray(Object[] a) {
		return new Object[] {};
	}

	@Override
	public boolean add(Object e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection c) {
		return false;
	}

	@Override
	public boolean addAll(Collection c) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection c) {
		return false;
	}

	@Override
	public void clear() {
	}

	@Override
	public Object get(int index) {
		return null;
	}

	@Override
	public Object set(int index, Object element) {
		return null;
	}

	@Override
	public void add(int index, Object element) {
	}

	@Override
	public Object remove(int index) {
		return null;
	}

	@Override
	public int indexOf(Object o) {
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		return -1;
	}

	@Override
	public ListIterator listIterator() {
		return null;
	}

	@Override
	public ListIterator listIterator(int index) {
		return null;
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return null;
	}
}
