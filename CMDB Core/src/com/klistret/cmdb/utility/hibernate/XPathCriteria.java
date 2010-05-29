package com.klistret.cmdb.utility.hibernate;

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.StepExpr;

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
		/**
		 * validate xpath expressions
		 */
		PathExpression[] expressions = explain(xpaths);

		/**
		 * construct hibernate criteria based on the root step
		 */
		XMLBean xmlBean = jaxbContextHelper.getXMLBeans().get(containingQName);
		ClassMetadata hClassMetadata = sessionFactory.getClassMetadata(xmlBean
				.getClazz());

		if (hClassMetadata == null)
			throw new ApplicationException();

		Criteria criteria = session.createCriteria(hClassMetadata
				.getEntityName());

		/**
		 * piece together criteria from each expression
		 */
		for (PathExpression expression : expressions)
			transform(criteria, expression);

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

	private void transform(Criteria critera, PathExpression expression) {
		// ignore root
		for (int index = 1; index < expression.getRelativePath().size(); index++) {
			Expr expr = expression.getRelativePath().get(index);

			// ignore containing step except for the predicate
			if (index == 1 && expr.getType().equals(Expr.Type.Step)
					&& ((StepExpr) expr).hasPredicate())
				expr = ((StepExpr) expr).getPredicate();

			switch (expr.getType()) {
			case Step:
				break;
			case Comparison:
				break;
			case Irresolute:
				break;
			}
		}
	}
}
