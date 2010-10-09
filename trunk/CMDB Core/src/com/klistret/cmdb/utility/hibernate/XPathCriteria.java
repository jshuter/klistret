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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.jaxb.BeanMetadata;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.saxon.AndExpr;
import com.klistret.cmdb.utility.saxon.ComparisonExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.LiteralExpr;
import com.klistret.cmdb.utility.saxon.OrExpr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

public class XPathCriteria {

	private static final Logger logger = LoggerFactory
			.getLogger(XPathCriteria.class);

	/**
	 * XPaths inclusive prologs
	 */
	private String[] xpaths;

	/**
	 * Hibernate session
	 */
	private Session session;

	/**
	 * Hibernate criteria
	 */
	private Criteria criteria;

	/**
	 * 
	 */
	private BeanMetadata contextBean;

	/**
	 * 
	 */
	private CIContext ciContext = CIContext.getCIContext();

	/**
	 * 
	 * @param xpaths
	 * @param session
	 */
	public XPathCriteria(String[] xpaths, Session session) {
		this.xpaths = xpaths;
		this.session = session;

		makeCriteria();
	}

	/**
	 * Get XPaths sent to the constructor
	 * 
	 * @return XPaths (string array)
	 */
	public String[] getXPaths() {
		return xpaths;
	}

	/**
	 * Get Hibernate session sent to the constructor
	 * 
	 * @return Hibernate session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * Get Hibernate criteria based on xpath expressions
	 * 
	 * @return Hibernate criteria
	 */
	public Criteria getCriteria() {
		return criteria;
	}

	/**
	 * 
	 */
	private void makeCriteria() {
		for (String xpath : xpaths) {
			/**
			 * Simplify the xpath into an expression with a root then steps
			 * either resolute or non-resolute.
			 */
			PathExpression expression = new PathExpression(xpath);

			/**
			 * Every expression has to be absolute (ie. starts with a slash)
			 */
			if (!expression.hasRoot()) {
				logger.error("XPath [{}] does not have a root expression",
						expression.getXPath());
				throw new ApplicationException(String.format(
						"XPath [%s] does not have a root expression",
						expression.getXPath()));
			}

			/**
			 * An initial step beyond the starting slash has to exist to define
			 * a Hibernate criteria (based on either a class or entity name).
			 */
			if (!(expression.getDepth() > 1)
					&& expression.getExpr(1).getType() == Expr.Type.Step) {
				logger.error("XPath [{}] must have at least one Step",
						expression.getXPath());
				throw new ApplicationException(String.format(
						"XPath [%s] must have at least one Step", expression
								.getXPath()));
			}

			/**
			 * Initial step must be an element with valid qname with bean
			 * metadata
			 */
			StepExpr contextStepExpr = (StepExpr) expression.getExpr(1);
			if (contextStepExpr.getPrimaryNodeKind() != StepExpr.PrimaryNodeKind.Element
					|| contextStepExpr.getQName() == null) {
				logger
						.error(
								"Context step [{}] must be an element with valid qname",
								expression.getXPath());
				throw new ApplicationException(
						String
								.format(
										"Context step [%s] must be an element with valid qname",
										expression.getXPath()));
			}

			/**
			 * Create criteria based on context bean (initial)
			 */
			BeanMetadata bean = ciContext.getBean(contextStepExpr.getQName());
			if (criteria == null && contextBean == null) {
				contextBean = bean;
				criteria = session.createCriteria(contextBean.getJavaClass());
			}

			if (!contextBean.equals(bean)) {
				logger
						.error(
								"Leading Bean [{}] not unique across xpath statements [{}]",
								bean, xpaths);
				throw new ApplicationException(
						String
								.format(
										"Leading Bean [%s] not unique across xpath statements [%s]",
										bean, xpaths));
			}

			/**
			 * Handle predicate then recursively translate if next step exists
			 */
			if (contextStepExpr.hasPredicate())
				criteria.add(translatePredicate(contextBean, contextStepExpr
						.getPredicate()));
			if (contextStepExpr.getNext() != null)
				translateStep(criteria, contextStepExpr.getNext());
		}
	}

	/**
	 * 
	 * @param criteria
	 * @param step
	 */
	private void translateStep(Criteria criteria, Step step) {
		switch (step.getType()) {
		case Step:
			logger.debug("Translating step [{}] at depth [{}]", step, step
					.getDepth());

			/**
			 * Gather parent information
			 */
			Step parent = (Step) step.getPathExpression().getRelativePath()
					.get(step.getDepth() - 1);

			BeanMetadata pBean = ciContext.getBean(parent.getQName());
			ClassMetadata pHClassMetadata = session.getSessionFactory()
					.getClassMetadata(pBean.getJavaClass());

			/**
			 * Step must be a defined property ot the parent Hibernate entity
			 */
			Type propertyType = pHClassMetadata.getPropertyType(step.getQName()
					.getLocalPart());
			if (propertyType == null) {
				logger
						.error(
								"Step [{}] is not defined as a property to the parent Hibernate entity [{}]",
								step, pHClassMetadata.getEntityName());
				throw new ApplicationException(
						String
								.format(
										"Step [%s] is not defined as a property to the parent Hibernate entity [%s]",
										step, pHClassMetadata.getEntityName()));
			}

			/**
			 * If the step is an entity (ie. complex type) then create a
			 * sub-criteria and handle the predicate otherwise the property is
			 * deemed as a XML column
			 */
			if (propertyType.isEntityType()) {
				/**
				 * Does step have corresponding bean Metadata?
				 */
				BeanMetadata sBean = ciContext.getBean(step.getQName());
				if (sBean == null) {
					logger.error(
							"Step [{}] has no corresponding bean metadata",
							step);
					throw new ApplicationException(String.format(
							"Step [%s] has no corresponding bean metadata",
							step));
				}

				Criteria nextCriteria = criteria.createCriteria(propertyType
						.getName());

				if (((StepExpr) step).hasPredicate()) {
					nextCriteria.add(translatePredicate(sBean,
							((StepExpr) step).getPredicate()));
				}

				if (step.getNext() != null)
					translateStep(nextCriteria, step.getNext());
			}

			if (propertyType.getName().equals(JAXBUserType.class.getName())) {
				logger.debug("Property [{}] is a JAXBUserType type", step
						.getQName().getLocalPart());

				criteria.add(new XPathRestriction(step.getQName()
						.getLocalPart(), step));

			}

			break;
		case Irresolute:
			break;
		/**
		 * Relative paths only contain root, steps or non-resolute
		 */
		default:
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for step", step));
		}
	}

	/**
	 * Predicates are only comparisons which may be compounded inside or/and
	 * expressions. Each comparison expects that the right most or first operand
	 * is a property (Hibernate as well registered by QName to the Bean
	 * metadata).
	 * 
	 * @param bean
	 * @param expr
	 * @return
	 */
	private Criterion translatePredicate(BeanMetadata bean, Expr expr) {
		ClassMetadata hClassMetadata = session.getSessionFactory()
				.getClassMetadata(bean.getJavaClass());

		switch (expr.getType()) {
		case Or:
			/**
			 * Recursively breaks down the OrExpr by translating on the first
			 * and second operands
			 */
			if (((OrExpr) expr).getOperands().size() != 2) {
				logger.error("OrExpr expression [{}] expects 2 operands", expr);
				throw new ApplicationException(String.format(
						"OrExpr expression [%s] expects 2 operands", expr));
			}

			return Restrictions.or(translatePredicate(bean, ((OrExpr) expr)
					.getOperands().get(0)), translatePredicate(bean,
					((OrExpr) expr).getOperands().get(1)));

		case And:
			/**
			 * Recursively breaks down the AndExpr by translating on the first
			 * and second operands
			 */
			if (((AndExpr) expr).getOperands().size() != 2) {
				logger
						.error("AndExpr expression [{}] expects 2 operands",
								expr);
				throw new ApplicationException(String.format(
						"AndExpr expression [%s] expects 2 operands", expr));
			}

			return Restrictions.and(translatePredicate(bean, ((AndExpr) expr)
					.getOperands().get(0)), translatePredicate(bean,
					((AndExpr) expr).getOperands().get(1)));
		case Comparison:
			/**
			 * Always at least one operand (step without predicates)
			 */
			StepExpr right = (StepExpr) ((ComparisonExpr) expr).getOperands()
					.get(0);

			/**
			 * Right step must correspond to a Hibernate property
			 */
			Type propertyType = hClassMetadata.getPropertyType(right.getQName()
					.getLocalPart());
			if (propertyType == null) {
				logger
						.error(
								"Local part [{}] is not a property of the Hibernate entity [{}]",
								right.getQName().getLocalPart(), hClassMetadata
										.getEntityName());
				throw new ApplicationException(
						String
								.format(
										"Local part [%s] is not a property of the Hibernate entity [%s]",
										right.getQName().getLocalPart(),
										hClassMetadata.getEntityName()));
			}

			/**
			 * Right step must exist in the bean metadata
			 */
			if (!bean.hasPropertyByName(right.getQName())) {
				logger.error(
						"Local part [{}] is not a property of the Bean [{}]",
						right.getQName().getLocalPart(), bean);
				throw new ApplicationException(String.format(
						"Local part [%s] is not a property of the Bean [%s]",
						right.getQName().getLocalPart(), bean));
			}

			/**
			 * Value/General comparisons have only 2 operands whereas function
			 * calls vary
			 */
			Expr left = ((ComparisonExpr) expr).isFunctional() ? null
					: ((ComparisonExpr) expr).getOperands().get(1);

			switch (((ComparisonExpr) expr).getOperator()) {
			case ValueEquals:
				return Restrictions.eq(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case ValueGreaterThan:
				return Restrictions.gt(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case ValueGreaterThanOrEquals:
				return Restrictions.ge(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case ValueLessThan:
				return Restrictions.lt(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case ValueLessThanOrEquals:
				return Restrictions.le(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case ValueNotEquals:
				return Restrictions.ne(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left).getValue());
			case GeneralEquals:
				/**
				 * If atomic (not a sequence) then the 'in' restriction is not
				 * usable (defaults to 'eq' restriction) since the argument is
				 * an array of objects.
				 */
				if (((LiteralExpr) left).isAtomic())
					return Restrictions.eq(((StepExpr) right).getQName()
							.getLocalPart(), ((LiteralExpr) left).getValue());

				return Restrictions
						.in(((StepExpr) right).getQName().getLocalPart(),
								((LiteralExpr) left).getValueAsArray());
			case Matches:
				if (((ComparisonExpr) expr).getOperands().size() != 2) {
					logger.error("Matches function [{}] expects 2 operands",
							expr);
					throw new ApplicationException(String.format(
							"Matches function [%s] expects 2 operands", expr));
				}

				left = ((ComparisonExpr) expr).getOperands().get(1);
				return Restrictions.ilike(((StepExpr) right).getQName()
						.getLocalPart(), ((LiteralExpr) left)
						.getValueAsString(), MatchMode.ANYWHERE);
			case Exists:
				if (((ComparisonExpr) expr).getOperands().size() != 1) {
					logger.error("Exists function [{}] expects only 1 operand",
							expr);
					throw new ApplicationException(
							String
									.format(
											"Exists function [%s] expects only 1 operand",
											expr));
				}

				return Restrictions.isNotNull(((StepExpr) right).getQName()
						.getLocalPart());
			case Empty:
				if (((ComparisonExpr) expr).getOperands().size() != 1) {
					logger.error("Empty function [{}] expects only 1 operand",
							expr);
					throw new ApplicationException(String.format(
							"Empty function [%s] expects only 1 operand", expr));
				}

				return Restrictions.isNull(((StepExpr) right).getQName()
						.getLocalPart());
			default:
				logger
						.error(
								"Unexpected comparison operator [{}] handling predicates",
								((ComparisonExpr) expr).getOperator());
				throw new ApplicationException(
						String
								.format(
										"Unexpected comparison operator [%s] handling predicates",
										((ComparisonExpr) expr).getOperator()));
			}
		default:
			logger.error("Unexpected expr [{}] type for predicate", expr);
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for predicate", expr));
		}
	}
}
