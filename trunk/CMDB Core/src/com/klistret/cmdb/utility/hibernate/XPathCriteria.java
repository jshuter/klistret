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

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
import com.klistret.cmdb.utility.saxon.AndExpr;
import com.klistret.cmdb.utility.saxon.ComparisonExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.LiteralExpr;
import com.klistret.cmdb.utility.saxon.OrExpr;
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
			buildFromExpression(criteria, (Step) expression.getExpr(0));

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
				.getClassMetadata(xmlBean.getType().getLocalPart());

		return hClassMetadata;
	}

	private void buildFromExpression(Criteria criteria, Step step) {
		if (step == null)
			return;

		switch (step.getType()) {
		case Step:
			if (step.getDepth() == 1) {

				criteria.add(buildFromPredicate(((StepExpr) step)
						.getPredicate()));

				buildFromExpression(criteria, step.getNext());
			} else {
				Step parent = (Step) step.getPathExpression().getRelativePath()
						.get(step.getDepth() - 1);

				ClassMetadata hClassMetadata = getClassMetadata(parent
						.getQName());
				String propertyName = jaxbContextHelper.suggestPropertyName(
						parent.getQName(), step.getQName());
				Type propertyType = hClassMetadata
						.getPropertyType(propertyName);

				if (propertyType.isEntityType()) {
					Criteria nextCriteria = criteria
							.createCriteria(hClassMetadata.getEntityName());
					nextCriteria.add(buildFromPredicate(((StepExpr) step)
							.getPredicate()));

					buildFromExpression(nextCriteria, step.getNext());
				} else {
					criteria.add(new XPathRestriction(propertyName, step));
				}

			}
			break;
		case Irresolute:
			break;
		case Root:
			buildFromExpression(criteria, step.getNext());
			break;
		default:
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for step", step));
		}
	}

	private Criterion buildFromPredicate(Expr expr) {
		switch (expr.getType()) {
		case Or:
			if (((OrExpr) expr).getOperands().size() != 2)
				throw new ApplicationException(String.format(
						"OrExpr expression [%s] expects 2 operands", expr));

			return Restrictions.or(buildFromPredicate(((OrExpr) expr)
					.getOperands().get(0)), buildFromPredicate(((OrExpr) expr)
					.getOperands().get(1)));
		case And:
			if (((AndExpr) expr).getOperands().size() != 2)
				throw new ApplicationException(String.format(
						"AndExpr expression [%s] expects 2 operands", expr));

			return Restrictions.and(buildFromPredicate(((AndExpr) expr)
					.getOperands().get(0)), buildFromPredicate(((AndExpr) expr)
					.getOperands().get(1)));
		case Comparison:
			if (((ComparisonExpr) expr).getOperands().size() != 2)
				throw new ApplicationException(String.format(
						"ComparisonExpr expression [%s] expects 2 operands",
						expr));

			Expr right = ((ComparisonExpr) expr).getOperands().get(0);
			if (!(right instanceof StepExpr))
				throw new ApplicationException(
						String
								.format(
										"Right operand in comparison expression [%s] must be a step",
										expr));
			if (!(((StepExpr) right).getPrimaryNodeKind()
					.equals(StepExpr.PrimaryNodeKind.Attribute)))
				throw new ApplicationException(
						String
								.format(
										"Right operand in comparison expression [%s] must be an attribute node",
										expr));

			Expr left = ((ComparisonExpr) expr).getOperands().get(1);
			if (!(left instanceof LiteralExpr))
				throw new ApplicationException(
						String
								.format(
										"Left operand in comparison expression [%s] must be a liberal",
										expr));

			switch (((ComparisonExpr) expr).getOperator()) {
			case ValueEquals:
				return Restrictions.eq(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case GeneralEquals:
				if (((LiteralExpr) left).isAtomic())
					return Restrictions.eq(((StepExpr) right).getQName()
							.getLocalPart(), ((LiteralExpr) left).getValue());

				return Restrictions
						.in(((StepExpr) right).getQName().getLocalPart(),
								((LiteralExpr) left).getValueAsArray());
			case Matches:
				return Restrictions.ilike(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left)
						.getValueAsString(), MatchMode.ANYWHERE);
			default:
				throw new ApplicationException(
						String
								.format(
										"Unexpected comparison operator [%s] handling predicates",
										((ComparisonExpr) expr).getOperator()));
			}
		default:
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for predicate", expr));
		}
	}
}
