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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.klistret.cmdb.utility.jaxb.CIBean;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.jaxb.CIProperty;
import com.klistret.cmdb.utility.saxon.AndExpr;
import com.klistret.cmdb.utility.saxon.ComparisonExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.LiteralExpr;
import com.klistret.cmdb.utility.saxon.OrExpr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.RelativePathExpr;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * XPath criteria excepts "common" XPaths that share the same first step, have
 * an initial Root step (with no other Roots) and a depth that is greater than
 * just the first step. There is single cached Hibernate criteria representing
 * the first step and thereafter child Hibernate criteria are created if the
 * XPath expression has an axis that is an Entity property (i.e. a relation to
 * another Hibernate Entity). Steps may have predicates that generally become
 * restrictions on the Hibernate criteria for the particular step. Predicates
 * may even have relative paths for Comparison expressions which must end in
 * either a node corresponding to a Hibernate property or an XPath that can be
 * feed to an XPathRestriction.
 * 
 * @author Matthew Young
 * 
 */
public class XPathCriteria {

	private static final Logger logger = LoggerFactory
			.getLogger(XPathCriteria.class);

	/**
	 * XPaths inclusive prologs
	 */
	private List<String> xpaths;

	/**
	 * Hibernate session
	 */
	private Session session;

	/**
	 * 
	 */
	private StepExpr commonsStep;

	/**
	 * CI metadata
	 */
	private CIContext ciContext = CIContext.getCIContext();

	/**
	 * Criteria store
	 */
	private Map<String, Criteria> criteriaStore = new HashMap<String, Criteria>();

	/**
	 * 
	 * @param xpaths
	 * @param session
	 */
	public XPathCriteria(List<String> xpaths, Session session) {
		this.xpaths = xpaths;
		this.session = session;

		makeCriteria();
	}

	/**
	 * Get XPaths sent to the constructor
	 * 
	 * @return XPaths (string array)
	 */
	public List<String> getXPaths() {
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
	 * Get Hibernate criteria based on the contextBean
	 * 
	 * @return Hibernate criteria
	 */
	public Criteria getCriteria() {
		return criteriaStore.get(commonsStep.getAxisName());
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
			PathExpression pathExpression = new PathExpression(xpath);

			/**
			 * Every expression has to be absolute (ie. starts with a slash)
			 */
			if (!pathExpression.getRelativePath().hasRoot()) {
				throw new ApplicationException(String.format(
						"XPath [%s] has no root expression",
						pathExpression.getXPath()),
						new UnsupportedOperationException());
			}

			/**
			 * The Root expression must be the first Step in the expression
			 */
			if (pathExpression.getRelativePath().getExpr(0).getType() != Expr.Type.Root)
				throw new ApplicationException(
						String.format(
								"XPath [%s] has no root expression as the initial step",
								pathExpression.getXPath()),
						new UnsupportedOperationException());

			/**
			 * Multiple Root expression may not occur
			 */
			if (pathExpression.getRelativePath().hasMultipleRoot())
				throw new ApplicationException(
						String.format(
								"XPath [%s] contains multiple root steps (double slashes likely)",
								pathExpression.getXPath()),
						new UnsupportedOperationException());

			/**
			 * More than the initial steps ("/" or "//") must exist to define a
			 * Hibernate criteria (based on either a class or entity name) plus
			 * the first Step has to be a Step not for example an Irresolute.
			 */
			if (!(pathExpression.getRelativePath().getDepth() > 1)
					&& pathExpression.getRelativePath().getExpr(1).getType() == Expr.Type.Step) {
				throw new ApplicationException(
						String.format(
								"XPath [%s] has no first step (depth greater than signular)",
								pathExpression.getXPath()),
						new UnsupportedOperationException());
			}

			/**
			 * First step must be an element with valid QName and bean Metadata
			 */
			StepExpr firstStep = (StepExpr) pathExpression.getRelativePath()
					.getExpr(1);
			if (firstStep.getPrimaryNodeKind() != StepExpr.PrimaryNodeKind.Element
					|| firstStep.getQName() == null) {
				throw new ApplicationException(String.format(
						"First step [%s] must be an element with valid qname",
						pathExpression.getXPath()),
						new UnsupportedOperationException());
			}

			/**
			 * Is first Step a CI Bean?
			 */
			CIBean firstStepBean = ciContext.getBean(firstStep.getQName());
			if (firstStepBean == null)
				throw new ApplicationException(String.format(
						"First step [%s] must be a CI bean",
						firstStep.getQName()),
						new UnsupportedOperationException());

			/**
			 * Determine if first Step Java class is common
			 */
			if (commonsStep == null)
				commonsStep = firstStep;

			if (!commonsStep.getAxisName().equals(firstStep.getAxisName()))
				throw new ApplicationException(
						String.format(
								"First Bean [%s] is not common across xpath statements [%s]",
								firstStepBean, xpaths),
						new UnsupportedOperationException());

			/**
			 * Add criteria associated to first Step's axis
			 */
			if (!criteriaStore.containsKey(firstStep.getAxisName())) {
				Criteria criteria = session.createCriteria(firstStepBean
						.getJavaClass());
				criteriaStore.put(firstStep.getAxisName(), criteria);

				logger.debug("Added key [{}] to criteria store",
						firstStep.getAxisName());
			}

			/**
			 * Translate the predicates of the first step then recursively the
			 * next steps
			 */
			logger.debug("Translating xpath [{}]", xpath);
			translatePredicate(firstStep, firstStepBean,
					firstStep.getAxisName());

			if (firstStep.getNext() != null)
				translateStep(firstStep.getNext(), firstStepBean,
						firstStep.getAxisName());
		}
	}

	/**
	 * Recursively creates Hibernate criterion (restrictions) that funneled into
	 * a parent restrictions added to the Step's Hibernate criteria. All
	 * predicate expressions are assumed to operate on the same XPath axis
	 * (which means RelativePaths aren't allowed).
	 * 
	 * @param predicate
	 * @param step
	 * @param critiera
	 */
	private Criterion translateStepPredicate(Expr predicate, CIBean contextBean) {
		logger.debug("Translating predicate [type: {}] for context [{}]",
				predicate.getType().name(), contextBean.getType());

		ClassMetadata hContextMetadata = session.getSessionFactory()
				.getClassMetadata(contextBean.getJavaClass());

		switch (predicate.getType()) {
		case Or:
			/**
			 * Recursively breaks down the OrExpr by translating on the first
			 * and second operands
			 */
			if (((OrExpr) predicate).getOperands().size() != 2)
				throw new ApplicationException(String.format(
						"OrExpr expression [%s] expects 2 operands", predicate));

			return Restrictions.or(
					translateStepPredicate(((OrExpr) predicate).getOperands()
							.get(0), contextBean),
					translateStepPredicate(((OrExpr) predicate).getOperands()
							.get(1), contextBean));

		case And:
			/**
			 * Recursively breaks down the AndExpr by translating on the first
			 * and second operands
			 */
			if (((AndExpr) predicate).getOperands().size() != 2)
				throw new ApplicationException(
						String.format(
								"AndExpr expression [%s] expects 2 operands",
								predicate));

			return Restrictions.and(
					translateStepPredicate(((AndExpr) predicate).getOperands()
							.get(0), contextBean),
					translateStepPredicate(((AndExpr) predicate).getOperands()
							.get(1), contextBean));

		case Comparison:
			StepExpr stepOperand = getStepOperand(((ComparisonExpr) predicate)
					.getOperands());

			LiteralExpr literalOperand = getLiteralOperand(((ComparisonExpr) predicate)
					.getOperands());

			if (stepOperand == null)
				throw new ApplicationException(String.format(
						"No Step operand found for Comparison predicate [%s]",
						predicate));

			logger.debug("Predicate is comparison [{}] against step [{}]",
					((ComparisonExpr) predicate).getOperator().name(),
					stepOperand.getQName());

			/**
			 * Step operand must correspond to a Hibernate property
			 */
			Type propertyType = wrapGetPropertyType(hContextMetadata,
					stepOperand.getQName().getLocalPart());
			if (propertyType == null)
				throw new ApplicationException(
						String.format(
								"Local part [%s] is not a property of the Hibernate entity [%s]",
								stepOperand.getQName().getLocalPart(),
								hContextMetadata.getEntityName()));

			/**
			 * Step operand must exist in the bean metadata
			 */
			if (!contextBean.hasPropertyByName(stepOperand.getQName()))
				throw new ApplicationException(String.format(
						"Local part [%s] is not a property of the Bean [%s]",
						stepOperand.getQName().getLocalPart(), contextBean));

			switch (((ComparisonExpr) predicate).getOperator()) {
			case ValueEquals:
				return Restrictions.eq(stepOperand.getQName().getLocalPart(),
						literalOperand.getValue());
			case ValueGreaterThan:
				return Restrictions.gt(stepOperand.getQName().getLocalPart(),
						literalOperand.getValue());
			case ValueGreaterThanOrEquals:
				return Restrictions.ge(stepOperand.getQName().getLocalPart(),
						literalOperand.getValue());
			case ValueLessThan:
				return Restrictions.lt(stepOperand.getQName().getLocalPart(),
						literalOperand.getValue());
			case ValueLessThanOrEquals:
				return Restrictions.le(stepOperand.getQName().getLocalPart(),
						literalOperand.getValue());
			case ValueNotEquals:
				return Restrictions.ne(stepOperand.getQName().getLocalPart(),
						literalOperand.getValue());
			case GeneralEquals:
				/**
				 * If atomic (not a sequence) then the 'in' restriction is not
				 * usable (defaults to 'eq' restriction) since the argument is
				 * an array of objects.
				 */
				if (literalOperand.isAtomic())
					return Restrictions.eq(stepOperand.getQName()
							.getLocalPart(), literalOperand.getValue());

				return Restrictions.in(stepOperand.getQName().getLocalPart(),
						literalOperand.getValueAsArray());
			case Matches:
				if (((ComparisonExpr) predicate).getOperands().size() != 2)
					throw new ApplicationException(String.format(
							"Matches function [%s] expects 2 operands",
							predicate));

				return Restrictions.ilike(
						stepOperand.getQName().getLocalPart(),
						literalOperand.getValueAsString(), MatchMode.ANYWHERE);
			case Exists:
				if (((ComparisonExpr) predicate).getOperands().size() != 1)
					throw new ApplicationException(String.format(
							"Exists function [%s] expects only 1 operand",
							predicate));

				return Restrictions.isNotNull(stepOperand.getQName()
						.getLocalPart());
			case Empty:
				if (((ComparisonExpr) predicate).getOperands().size() != 1)
					throw new ApplicationException(String.format(
							"Empty function [%s] expects only 1 operand",
							predicate));

				return Restrictions.isNull(stepOperand.getQName()
						.getLocalPart());
			default:
				throw new ApplicationException(
						String.format(
								"Unexpected comparison operator [%s] handling predicates",
								((ComparisonExpr) predicate).getOperator()));
			}
		default:
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for predicate", predicate));
		}
	}

	/**
	 * 
	 * @param predicate
	 * @param step
	 * @param hCriteria
	 */
	private void translateRelativePathPredicate(ComparisonExpr predicate,
			Step step, CIBean contextBean, String contextKey) {
		logger.debug(
				"Translating comparison predicate with relative path for context [{}] against key [{}]",
				contextBean.getType(), contextKey);

		RelativePathExpr relativePathOperand = getRelativePathOperand(predicate
				.getOperands());

		LiteralExpr literalOperand = getLiteralOperand(predicate.getOperands());

		if (relativePathOperand == null)
			throw new ApplicationException(
					String.format(
							"No Relative Path operand found for Comparison predicate [%s]",
							predicate));

		if (literalOperand == null)
			throw new ApplicationException(String.format(
					"No Literal operand found for Comparison predicate [%s]",
					predicate));

		if (relativePathOperand.hasIrresolute())
			throw new ApplicationException(
					String.format(
							"Relative path for predicate [%s] contains irresolute path",
							predicate));

		if (relativePathOperand.hasRoot())
			throw new ApplicationException(
					String.format(
							"Relative path for predicate [%s] contains root (single or double paths)",
							predicate));

		Step lastStep = (Step) relativePathOperand.getLastExpr();

		String xpath = null;
		for (int index = 0; index < relativePathOperand.getDepth() - 1; index++) {
			Expr expr = relativePathOperand.getExpr(index);
			xpath = xpath == null ? expr.getXPath() : String.format("%s/%s",
					xpath, expr.getXPath());
		}

		List<Expr> operands = new ArrayList<Expr>();
		operands.add(lastStep);
		operands.add(literalOperand);

		xpath = String.format("%s[%s]", xpath,
				ComparisonExpr.getXPath(predicate.getOperator(), operands));

		xpath = String.format("%s%s", step.getRelativePath()
				.getPathExpression().getProlog(), xpath);

		PathExpression other = new PathExpression(xpath);
		translateStep((Step) other.getRelativePath().getFirstExpr(),
				contextBean, contextKey);
	}

	/**
	 * 
	 * @param step
	 * @param contextBean
	 * @param contextKey
	 */
	private void translatePredicate(Step step, CIBean contextBean,
			String contextKey) {
		logger.debug(
				"Predicate controll [step: {}, context bean: {}, context path: [}]",
				new Object[] { step.getQName(), contextBean.getType(),
						contextKey });

		for (Expr predicate : ((StepExpr) step).getPredicates()) {
			if (predicate.getType() == Expr.Type.Comparison
					&& ((ComparisonExpr) predicate).hasRelativePathOperand()) {
				translateRelativePathPredicate((ComparisonExpr) predicate,
						step, contextBean, contextKey);
			} else {
				criteriaStore.get(contextKey).add(
						translateStepPredicate(predicate, contextBean));
			}
		}
	}

	/**
	 * 
	 * @param step
	 * @param critiera
	 * @return
	 */
	private void translateStep(Step step, CIBean contextBean, String contextKey) {
		logger.debug("Translating step [{}] for context[{}]", step.getQName(),
				contextBean.getType());

		switch (step.getType()) {
		case Step:
			/**
			 * Get Hibernate property type
			 */
			Type propertyType = getHibernatePropertyType(step, contextBean);

			/**
			 * If the step is a Hibernate Entity (ie. complex type) then create
			 * a child criteria and handle the predicate otherwise the property
			 * is deemed as a XML column
			 */
			if (propertyType.isEntityType() || propertyType.isAssociationType()) {
				logger.debug(
						"Property [name: {}] is a Hibernate entity/association type",
						step.getQName().getLocalPart());
				CIProperty stepPropertyMetadata = contextBean
						.getPropertyByName(step.getQName());

				/**
				 * Does the property have corresponding CI Bean?
				 */
				CIBean stepBean = ciContext.getBean(stepPropertyMetadata
						.getType());
				if (stepBean == null)
					throw new ApplicationException(String.format(
							"Step [%s] has no corresponding bean metadata",
							step));

				/**
				 * Create criteria key for the current Step
				 */
				String stepKey = String.format("%s/%s", contextKey,
						((StepExpr) step).getAxisName());
				if (!criteriaStore.containsKey(stepKey)) {
					criteriaStore.put(stepKey, criteriaStore.get(contextKey)
							.createCriteria(step.getQName().getLocalPart()));
					logger.debug("Added key [{}] to criteria store", stepKey);
				}

				/**
				 * Translate predicates and then the next step
				 */
				translatePredicate(step, stepBean, stepKey);

				if (step.getNext() != null)
					translateStep(step.getNext(), stepBean, stepKey);

			}

			if (propertyType.getName().equals(JAXBUserType.class.getName())) {
				logger.debug("Property [{}] is a JAXBUserType type", step
						.getQName().getLocalPart());

				criteriaStore.get(contextKey).add(
						new XPathRestriction(step.getQName().getLocalPart(),
								step));
			}

			break;
		case Irresolute:
			throw new ApplicationException(
					String.format(
							"Irresolue expressions [%s] currently not supported",
							step), new UnsupportedOperationException());
			/**
			 * Relative paths only contain root, steps or non-resolute
			 */
		default:
			throw new ApplicationException(String.format(
					"Unexpected expr [%s] type for step", step),
					new UnsupportedOperationException());
		}
	}

	/**
	 * 
	 * @param operands
	 * @return
	 */
	private StepExpr getStepOperand(List<Expr> operands) {
		for (Expr operand : operands)
			if (operand.getType() == Expr.Type.Step)
				return (StepExpr) operand;

		return null;
	}

	/**
	 * 
	 * @param operands
	 * @return
	 */
	private RelativePathExpr getRelativePathOperand(List<Expr> operands) {
		for (Expr operand : operands)
			if (operand.getType() == Expr.Type.RelativePath)
				return (RelativePathExpr) operand;

		return null;
	}

	/**
	 * 
	 * @param operands
	 * @return
	 */
	private LiteralExpr getLiteralOperand(List<Expr> operands) {
		for (Expr operand : operands)
			if (operand.getType() == Expr.Type.Literal)
				return (LiteralExpr) operand;

		return null;
	}

	/**
	 * Determines if the property is defined to the ClassMetadata prior to
	 * calling the getPropertyType method to avoid a HibernateException.
	 * 
	 * @param hcm
	 * @param name
	 * @return Type
	 */
	private Type wrapGetPropertyType(ClassMetadata hcm, String name) {
		for (String pn : hcm.getPropertyNames()) {
			if (pn.equals(name))
				return hcm.getPropertyType(name);
		}

		if (hcm.getIdentifierPropertyName() != null
				&& hcm.getIdentifierPropertyName().equals(name))
			return hcm.getIdentifierType();

		return null;
	}

	/**
	 * 
	 * @param step
	 * @param contextBean
	 * @return
	 */
	private Type getHibernatePropertyType(Step step, CIBean contextBean) {
		ClassMetadata hContextMetadata = session.getSessionFactory()
				.getClassMetadata(contextBean.getJavaClass());
		if (hContextMetadata == null)
			throw new ApplicationException(String.format(
					"Context bean [%s] has no Hibernate metadata", contextBean));

		/**
		 * Step must be a defined property to the context Hibernate entity
		 */
		Type propertyType = wrapGetPropertyType(hContextMetadata, step
				.getQName().getLocalPart());
		if (propertyType == null)
			throw new ApplicationException(
					String.format(
							"Step [%s] is not defined as a property to the context Hibernate entity [%s]",
							step, hContextMetadata.getEntityName()));

		return propertyType;
	}

}
