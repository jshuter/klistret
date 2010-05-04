package com.klistret.cmdb.utility.hibernate;

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.jvnet.jaxb.reflection.model.runtime.RuntimeClassInfo;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
import com.klistret.cmdb.utility.saxon.PathExpression;

public class XPathCriteria {

	private String[] xpaths;

	private JAXBContextHelper jaxbContextHelper;

	private SessionFactory sessionFactory;

	private QName containingQName;

	public XPathCriteria(String[] xpaths, JAXBContextHelper jaxbContextHelper,
			SessionFactory sessionFactory) {
		this.xpaths = xpaths;
		this.jaxbContextHelper = jaxbContextHelper;
		this.sessionFactory = sessionFactory;
	}

	public String[] getXPaths() {
		return this.xpaths;
	}

	public JAXBContextHelper getJAXBContextHelper() {
		return this.jaxbContextHelper;
	}

	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	public Criteria getCriteria(Session session) {
		PathExpression[] pathExpressions = explain(xpaths);

		ClassMetadata hClassMetadata = getClassMetadata(containingQName);
		Criteria criteria = session.createCriteria(hClassMetadata
				.getEntityName());

		for (PathExpression pathExpression : pathExpressions) {

		}

		return criteria;
	}

	private PathExpression[] explain(String[] xpaths) {
		PathExpression[] pathExpressions = new PathExpression[xpaths.length];

		for (int index = 0; index < xpaths.length; index++) {
			PathExpression pathExpression = new PathExpression(xpaths[index]);
			pathExpressions[index] = pathExpression;

			if (!pathExpression.hasRoot())
				throw new ApplicationException(String.format(
						"XPath [%s] does not have a root expression",
						pathExpression.getXPath()));

			QName qname = pathExpression.getQName(1);
			if (qname == null)
				throw new ApplicationException(String.format(
						"Containg QName not defined for xpath [%s]",
						pathExpression.getXPath()));

			if (containingQName != null && !containingQName.equals(qname))
				throw new ApplicationException(
						String
								.format(
										"Leading QName [%s] not unique across xpath statements [%s]",
										qname, xpaths));

			containingQName = qname;
		}

		return pathExpressions;
	}

	private ClassMetadata getClassMetadata(QName qname) {
		RuntimeClassInfo runtimeClassInfo = jaxbContextHelper
				.getRuntimeClassInfo(qname);

		ClassMetadata hClassMetadata = sessionFactory
				.getClassMetadata(runtimeClassInfo.getClazz());

		if (hClassMetadata == null)
			throw new ApplicationException(
					String
							.format(
									"QName [%s] has not corresponding entity defined to Hibernate",
									containingQName));

		return hClassMetadata;
	}
}
