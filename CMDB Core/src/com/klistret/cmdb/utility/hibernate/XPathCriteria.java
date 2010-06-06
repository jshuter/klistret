package com.klistret.cmdb.utility.hibernate;

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

public class XPathCriteria {

	private String[] xpaths;

	private JAXBContextHelper jaxbContextHelper;

	private Session session;

	public XPathCriteria(String[] xpaths, JAXBContextHelper jaxbContextHelper,
			Session session) {
		this.xpaths = xpaths;
		this.jaxbContextHelper = jaxbContextHelper;
		this.session = session;
	}

	public String[] getXPaths() {
		return xpaths;
	}

	public JAXBContextHelper getJAXBContextHelper() {
		return jaxbContextHelper;
	}

	public Session getSession() {
		return session;
	}

	public Criteria getCriteria() {
		PathExpression[] expressions = new PathExpression[xpaths.length];

		/**
		 * Leading step as Hibernate class metadata common to all expressions
		 * which the criteria is based on
		 */
		QName step = getContainer(expressions);
		ClassMetadata hClassMetadata = getClassMetadata(getContainer(expressions));
		if (hClassMetadata == null)
			throw new ApplicationException(String.format(
					"Hibernate class does not exist for qname [%s]", step));

		Criteria criteria = session.createCriteria(hClassMetadata
				.getEntityName());

		/**
		 * piece together criteria from each expression
		 */
		for (PathExpression expression : expressions)
			buildFromExpression(hClassMetadata, criteria, (Step) expression
					.getExpr(0));

		return criteria;
	}

	private QName getContainer(PathExpression[] expressions) {
		QName container = null;

		for (int index = 0; index < xpaths.length; index++) {
			PathExpression expression = new PathExpression(xpaths[index]);
			expressions[index] = expression;

			if (!expression.hasRoot())
				throw new ApplicationException(String.format(
						"XPath [%s] does not have a root expression",
						expression.getXPath()));

			QName qname = expression.getQName(1);
			if (qname == null)
				throw new ApplicationException(String.format(
						"Containg QName not defined for xpath [%s]", expression
								.getXPath()));

			if (container != null && !container.equals(qname))
				throw new ApplicationException(
						String
								.format(
										"Leading QName [%s] not unique across xpath statements [%s]",
										qname, xpaths));

			container = qname;
		}

		return container;
	}

	private ClassMetadata getClassMetadata(QName qname) {
		XMLBean xmlBean = jaxbContextHelper.getXMLBean(qname);
		ClassMetadata hClassMetadata = session.getSessionFactory()
				.getClassMetadata(xmlBean.getName().getLocalPart());

		return hClassMetadata;
	}

	private void buildFromExpression(ClassMetadata hClassMetadata,
			Criteria criteria, Step step) {
		if (step == null)
			return;

		switch (step.getType()) {
		case Step:
			if (step.getDepth() == 1) {
				criteria.add(buildFromPredicate(((StepExpr) step)
						.getPredicate()));

				buildFromExpression(hClassMetadata, criteria, step.getNext());
			} else {
				if (step.getQName() == null)
					throw new ApplicationException(String.format(
							"Step QName not defined for xpath [%s]", step
									.getXPath()));

				Type propertyType = hClassMetadata.getPropertyType(step
						.getQName().getLocalPart());

				if (propertyType.isEntityType()) {
					ClassMetadata nextClassMetadata = getClassMetadata(step
							.getQName());

					Criteria nextCriteria = criteria
							.createCriteria(hClassMetadata.getEntityName());
					nextCriteria.add(buildFromPredicate(((StepExpr) step)
							.getPredicate()));

					buildFromExpression(nextClassMetadata, nextCriteria, step
							.getNext());
				} else {
					criteria.add(Restrictions.sqlRestriction(""));
				}

			}
			break;
		case Irresolute:
			break;
		case Root:
			buildFromExpression(hClassMetadata, criteria, step.getNext());
			break;
		default:
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for step", step));
		}
	}

	private Criterion buildFromPredicate(Expr expr) {
		return null;
	}
}
