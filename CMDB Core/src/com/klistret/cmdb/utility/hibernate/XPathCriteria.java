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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.saxon.AndExpr;
import com.klistret.cmdb.utility.saxon.ComparisonExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.LiteralExpr;
import com.klistret.cmdb.utility.saxon.OrExpr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * XPaths are the query language. XPaths are translated into JTA criteria (right
 * now only Hibernate but soon to JTA syntax). Each step in slash expressions
 * are mapped either to a Hibernate entity or a column capable of handling XML
 * data. Entities may have restrictions which map to comparison or logical XPath
 * expressions. Irresolute expressions are only excepted with XML columns. The
 * JAXBContextHelper is used to extract metadata and relationships between XML
 * beans, elements and attributes.
 * 
 * @author Matthew Young
 * 
 */
public class XPathCriteria {

	private static final Logger logger = LoggerFactory
			.getLogger(XPathCriteria.class);

	private String[] xpaths;

	private Session session;

	public XPathCriteria(String[] xpaths, Session session) {
		this.xpaths = xpaths;
		this.session = session;
	}

	public String[] getXPaths() {
		return xpaths;
	}

	public Session getSession() {
		return session;
	}

	/**
	 * Construct a Hibernate criteria based on the XPath array
	 * 
	 * @return Hibernate Criteria
	 */
	public Criteria getCriteria() {
		try {
			PathExpression[] expressions = new PathExpression[xpaths.length];

			/**
			 * Top criteria is created here then handed over to the build calls
			 */
			QName container = getContainer(expressions);
			Criteria criteria = session
					.createCriteria(container.getLocalPart());

			/**
			 * Piece together criteria (iterative) from each expression
			 */
			for (PathExpression expression : expressions)
				buildFromExpression(criteria, (Step) expression.getExpr(0));

			return criteria;
		} catch (HibernateException e) {
			throw new InfrastructureException(
					"Unexpected Hibernate expression while forming criteria from XPaths",
					e);
		}
	}

	/**
	 * Each XPath expression should have the same initial step or "container" to
	 * loan a word from Saxon. Every XPath must have a root and at least one
	 * step.
	 * 
	 * @param expressions
	 * @return
	 */
	private QName getContainer(PathExpression[] expressions) {
		QName container = null;

		for (int index = 0; index < xpaths.length; index++) {
			PathExpression expression = new PathExpression(xpaths[index]);
			expressions[index] = expression;

			if (!expression.hasRoot())
				throw new ApplicationException(String.format(
						"XPath [%s] does not have a root expression",
						expression.getXPath()));

			if (!(expression.getDepth() > 1))
				throw new ApplicationException(String.format(
						"XPath [%s] does not have at least one step",
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

	/**
	 * Build criteria iterative for each step in the relative path
	 * 
	 * @param criteria
	 * @param step
	 */
	private void buildFromExpression(Criteria criteria, Step step) {
		/**
		 * Hault iterative process when step is null
		 */
		if (step == null)
			return;

		switch (step.getType()) {
		case Step:
			logger.debug("Criteria creation against step [{}] at depth [{}]",
					step, step.getDepth());

			/**
			 * Containing or first step already has an active criteria (passed
			 * from the first call to this method) so only the predicates need
			 * to be built then iterate to the next step.
			 */
			if (step.getDepth() == 1) {
				logger
						.debug("Containing step build only predicate then iterate");
				if (((StepExpr) step).hasPredicate()) {
					criteria.add(buildFromPredicate(((StepExpr) step)
							.getPredicate()));
				}

				buildFromExpression(criteria, step.getNext());
			}

			/**
			 * Subsequent steps need to create a new criteria to act based on
			 * the passed criteria to this method (done without alias).
			 */
			if (step.getDepth() > 1) {
				Step parent = (Step) step.getPathExpression().getRelativePath()
						.get(step.getDepth() - 1);

				/**
				 * Hibernate class metadata from the step's parent gives the
				 * available properties (associations, collections, normal
				 * attributes).
				 */
				ClassMetadata hClassMetadata = session.getSessionFactory()
						.getClassMetadata(parent.getQName().getLocalPart());

				/**
				 * Property name is the local part of the step's qname
				 */
				String propertyName = step.getQName().getLocalPart();
				if (propertyName == null)
					throw new ApplicationException(String.format(
							"QName local part for step [%s] is null", step));

				/**
				 * Hibernate type needs to return the same underlying class as
				 * the step (add check later)
				 */
				Type propertyType = hClassMetadata
						.getPropertyType(propertyName);
				if (propertyType == null)
					throw new ApplicationException(
							String
									.format(
											"Local part [%s] is not a property of the Hibernate entity [%s]",
											propertyName, hClassMetadata
													.getEntityName()));

				/**
				 * Entities are candidates for criteria creation (based on the
				 * property name not the entity name) and iteration
				 */
				if (propertyType.isEntityType()) {
					logger.debug("Property [{}] is an entity", propertyName);

					Criteria nextCriteria = criteria
							.createCriteria(propertyName);
					if (((StepExpr) step).hasPredicate()) {
						nextCriteria.add(buildFromPredicate(((StepExpr) step)
								.getPredicate()));
					}

					buildFromExpression(nextCriteria, step.getNext());
				}

				/**
				 * Regular properties (non-relationships) are deemed as a XML
				 * column candidate
				 */
				if (!(propertyType.isEntityType())) {
					logger
							.debug(
									"Property [{}] is a non-entity and deemed XML column candidate by default",
									propertyName);

					criteria.add(new XPathRestriction(propertyName, step));
				}

			}
			break;
		/**
		 * Ignore irresolute (in the future it would be nice to even be able to
		 * handle these types of expressions)
		 */
		case Irresolute:
			break;
		/**
		 * Ignore roots but iterate to the next step
		 */
		case Root:
			buildFromExpression(criteria, step.getNext());
			break;
		/**
		 * Relative paths only contain root, steps or irresolutes
		 */
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
			/**
			 * Always at least one operand (step without predicates)
			 */
			Expr right = ((ComparisonExpr) expr).getOperands().get(0);

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
				if (((ComparisonExpr) expr).getOperands().size() != 2)
					throw new ApplicationException(String.format(
							"Matches function [%s] expects 2 operands", expr));

				left = ((ComparisonExpr) expr).getOperands().get(1);
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
