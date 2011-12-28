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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
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

public class XPathCriteria {

	private static final Logger logger = LoggerFactory
			.getLogger(XPathCriteria.class);

	/**
	 * Hibernate session
	 */
	private Session session;

	/**
	 * CI metadata
	 */
	private CIContext ciContext = CIContext.getCIContext();

	/**
	 * Wrapped relative paths around the passed expressions
	 */
	private List<HibernateRelativePath> hibernateRelativePaths = new ArrayList<HibernateRelativePath>();

	/**
	 * Hibernate property type
	 */
	private enum Type {
		Entity, Association, Property
	};

	/**
	 * Criteria store
	 */
	private Map<String, Criteria> cache = new HashMap<String, Criteria>();

	/**
	 * Hibernate wrapper (inner class) around relative paths (even single steps
	 * within a predicate are wrapped as relative paths).
	 */
	@SuppressWarnings("unused")
	private class HibernateRelativePath {
		/**
		 * List Hibernate steps
		 */
		private List<HibernateStep> hSteps = new ArrayList<HibernateStep>();

		/**
		 * Root denotes a starting entity for Hibernate criteria
		 */
		private boolean root = false;

		/**
		 * Root entity name
		 */
		private String name;

		/**
		 * Hibernate relative path may be shorter in depth if they end in a
		 * property that is an XML column
		 */
		private boolean truncated = false;

		public List<HibernateStep> getHiberateSteps() {
			return this.hSteps;
		}

		public boolean hasXML() {
			for (HibernateStep hStep : hSteps)
				if (hStep.isXml())
					return true;

			return false;
		}

		public boolean isRoot() {
			return this.root;
		}

		public void setRoot(boolean value) {
			this.root = value;
		}

		public boolean isTruncated() {
			return this.truncated;
		}

		public void setTruncated(boolean value) {
			this.truncated = value;
		}

		public HibernateStep getLastHibernateStep() {
			if (hSteps.size() > 0)
				return hSteps.get(hSteps.size() - 1);

			return null;
		}

		public HibernateStep getFirstHibernateStep() {
			if (hSteps.size() > 0)
				return hSteps.get(0);

			return null;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * Hibernate wrapper (inner class) around steps
	 */
	@SuppressWarnings("unused")
	private class HibernateStep {
		private Step step;

		private CIBean beanType;

		private HibernateStep next;

		private HibernateStep previous;

		private String path;

		private String name;

		private Type type = Type.Property;

		private boolean xml = false;

		public Step getStep() {
			return this.step;
		}

		public void setStep(Step step) {
			this.step = step;
		}

		public CIBean getBeanType() {
			return this.beanType;
		}

		public void setBeanType(CIBean beanType) {
			this.beanType = beanType;
		}

		public HibernateStep getNext() {
			return this.next;
		}

		public void setNext(HibernateStep next) {
			this.next = next;
		}

		public HibernateStep getPrevious() {
			return this.previous;
		}

		public void setPrevious(HibernateStep previous) {
			this.previous = previous;
		}

		public boolean hasNext() {
			if (this.next == null)
				return false;

			return true;
		}

		public String getPath() {
			return this.path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Type getType() {
			return this.type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public void setXml(boolean value) {
			this.xml = value;
		}

		public boolean isXml() {
			return this.xml;
		}

		/**
		 * Equality is traditional and keyed of the name property
		 */
		public boolean equals(Object aOther) {
			// self comparison
			if (this == aOther)
				return true;

			// non HibernateStep instance
			if (!(aOther instanceof HibernateStep))
				return false;

			// safe casting
			HibernateStep other = (HibernateStep) aOther;

			// Name property must be defined
			if (other.getPath() == null)
				return false;

			// Matches only on name
			if (other.getPath().equals(this.getPath()))
				return true;

			return false;
		}

		/**
		 * Display information
		 */
		public String toString() {
			return String.format("path: %s, step: %s", path, step);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param expression
	 * @param session
	 */
	public XPathCriteria(String expression, Session session) {
		this.session = session;

		merge(expression);
	}

	/**
	 * Constructor
	 * 
	 * @param expressions
	 * @param session
	 */
	public XPathCriteria(List<String> expressions, Session session) {
		this.session = session;

		for (String expression : expressions)
			merge(expression);
	}

	/**
	 * Get Hibernate session
	 * 
	 * @return
	 */
	public Session getSession() {
		return this.session;
	}

	/**
	 * Merge an expression filter after checking that the expression is
	 * absolute, the first step is a root and there are no multiple root steps.
	 * 
	 * @param expression
	 */
	public void merge(String expression) {
		logger.debug("Merging filter expression [{}]", expression);

		PathExpression pe = new PathExpression(expression);
		RelativePathExpr rpe = pe.getRelativePath();

		/**
		 * Every expression has to be absolute (ie. starts with a slash)
		 */
		if (!rpe.hasRoot())
			throw new ApplicationException(String.format(
					"Expression [%s] has no root", pe.getXPath()));

		/**
		 * The Root expression must be the first Step in the expression
		 */
		if (rpe.getExpr(0).getType() != Expr.Type.Root)
			throw new ApplicationException(
					String.format(
							"Expression [%s] expects an absolute path (root is not first step)",
							pe.getXPath()));

		/**
		 * Multiple Root expression may not occur
		 */
		if (rpe.hasMultipleRoot())
			throw new ApplicationException(String.format(
					"Expression [%s] contains multiple root steps",
					pe.getXPath()));

		/**
		 * More than the initial steps ("/" or "//") must exist to define a
		 * Hibernate criteria (based on either a class or entity name) plus the
		 * first Step has to be a Step not for example an Irresolute.
		 */
		if (!(rpe.getDepth() > 1) && rpe.getExpr(1).getType() == Expr.Type.Step)
			throw new ApplicationException(
					String.format(
							"Expression [%s] has no context step (depth greater than signular)",
							pe.getXPath()), new UnsupportedOperationException());

		/**
		 * Context step must be an element with valid QName and metadata
		 */
		StepExpr step = (StepExpr) rpe.getExpr(1);
		if (step.getPrimaryNodeKind() != StepExpr.PrimaryNodeKind.Element
				|| step.getQName() == null)
			throw new ApplicationException(
					String.format(
							"Expression [%s] context step not an XML Element with a valid QName",
							pe.getXPath()));

		/**
		 * Is context step a CI Bean?
		 */
		CIBean bean = ciContext.getBean(step.getQName());
		if (bean == null)
			throw new ApplicationException(String.format(
					"Expression [%s] context step not a registered CI bean",
					pe.getXPath()));

		/**
		 * Context step has to have Hibernate metadata
		 */
		ClassMetadata cm = session.getSessionFactory().getClassMetadata(
				bean.getJavaClass());
		if (cm == null)
			throw new ApplicationException(String.format(
					"Context bean [%s] has no Hibernate metadata", bean));

		/**
		 * Context Hibernate Step
		 */

		HibernateStep hStep = new HibernateStep();
		hStep.setStep(step);
		hStep.setType(Type.Entity);
		hStep.setBeanType(bean);
		hStep.setName(step.getQName().getLocalPart());
		hStep.setPath(step.getQName().getLocalPart());

		HibernateRelativePath hrp = process(rpe, hStep);
		hrp.setName(cm.getEntityName());

		if (hibernateRelativePaths.size() > 0
				&& !(hibernateRelativePaths.get(0).getName().equals(hrp
						.getName())))
			throw new ApplicationException(
					String.format(
							"Expression [%s] has another starting Hibernate entity than %s",
							pe.getXPath(), hrp.getName()));

		hibernateRelativePaths.add(hrp);
	}

	/**
	 * Translate a relative path expression into a Hibernate relative path
	 * 
	 * @param rpe
	 * @param hStep
	 */
	protected HibernateRelativePath process(RelativePathExpr rpe,
			HibernateStep context) {
		HibernateRelativePath hRPath = new HibernateRelativePath();

		/**
		 * Offset to the first step after the context step if the relative path
		 * is an absolute. Assumption is that absolute paths are grounds for the
		 * start of a Hibernate criteria.
		 */
		int contextDepth = 0;
		if (rpe.getFirstExpr().getType() == Expr.Type.Root) {
			contextDepth = 2;

			hRPath.setRoot(true);
			hRPath.getHiberateSteps().add(context);
		}

		/**
		 * Process each step. Non absolute relative paths take their context
		 * from the passed Hibernate step (likely predicate expressions).
		 */
		for (int depth = contextDepth; depth < rpe.getDepth(); depth++) {
			Step step = (Step) rpe.getExpr(depth);

			switch (step.getType()) {
			case Step:
				if (hRPath.getHiberateSteps().size() > 0)
					context = hRPath.getLastHibernateStep();

				CIBean bean = ciContext.getBean(context.getStep().getQName());
				ClassMetadata cm = session.getSessionFactory()
						.getClassMetadata(bean.getJavaClass());

				String property = step.getQName().getLocalPart();
				org.hibernate.type.Type propertyType = getPropertyType(cm,
						property);

				if (propertyType == null)
					throw new ApplicationException(
							String.format(
									"Step [%s] not defined as a property to the Hibernate context [%s]",
									step, cm.getEntityName()));

				HibernateStep hStep = new HibernateStep();
				hStep.setStep(step);
				hStep.setName(property);
				hStep.setPath(String.format("%s.%s", context.getPath(),
						property));

				if (propertyType.isEntityType())
					hStep.setType(Type.Entity);

				if (propertyType.isAssociationType())
					hStep.setType(Type.Association);

				/**
				 * Store the CIBean corresponding to the property by type not
				 * name
				 */
				if (hStep.getType() != Type.Property) {
					CIProperty prop = bean.getPropertyByName(step.getQName());

					/**
					 * Does the property have corresponding CI Bean?
					 */
					CIBean other = ciContext.getBean(prop.getType());
					if (other == null)
						throw new ApplicationException(
								String.format(
										"Step [%s] has no corresponding CI Bean metadata",
										step));

					hStep.setBeanType(other);
				}

				if (propertyType.getName().equals(JAXBUserType.class.getName())) {
					hStep.setXml(true);

					if (step.getNext() != null)
						hRPath.setTruncated(true);
				}

				/**
				 * Connect together the Hibernate steps
				 */
				if (hRPath.getHiberateSteps().size() > 0) {
					HibernateStep last = hRPath.getLastHibernateStep();
					last.setNext(hStep);
					hStep.setPrevious(last);
				}

				/**
				 * Add the Hibernate step
				 */
				hRPath.getHiberateSteps().add(hStep);
				break;
			case Root:
				throw new ApplicationException(
						String.format(
								"Only resolute steps valid [Root encountered at depth: %d]",
								step.getDepth()));
			case Irresolute:
				throw new ApplicationException(
						String.format(
								"Only resolute steps valid [Irresolute encountered at depth: %d]",
								depth));
			default:
				throw new ApplicationException(String.format(String.format(
						"Undefined expression type [%s] at depth",
						step.getType(), depth)));
			}

			if (hRPath.getLastHibernateStep().getType() == Type.Property)
				break;
		}

		return hRPath;
	}

	/**
	 * Get Hibernate property type by looking for the property name in the class
	 * metadata for general properties and the identifier property
	 * 
	 * @param cm
	 * @param name
	 * @return
	 */
	private org.hibernate.type.Type getPropertyType(ClassMetadata cm,
			String name) {

		for (String pn : cm.getPropertyNames())
			if (pn.equals(name))
				return cm.getPropertyType(name);

		if (cm.getIdentifierPropertyName() != null
				&& cm.getIdentifierPropertyName().equals(name))
			return cm.getIdentifierType();

		return null;
	}

	/**
	 * Get Hibernate criteria based on the Hibernate relative paths representing
	 * the XPath filter expressions.
	 * 
	 * @return Criteria
	 */
	public Criteria getCriteria() {
		/**
		 * Clear the cache if called more than once
		 */
		cache.clear();

		/**
		 * At least one expression must have be processed and all should have
		 * the same first Hibernate Step
		 */
		if (hibernateRelativePaths.size() == 0)
			throw new InfrastructureException(
					"Somehow the Hibernate relative paths have been drained.");
		HibernateStep context = hibernateRelativePaths.get(0)
				.getFirstHibernateStep();

		/**
		 * Singular root Hibernate criteria based on the root context step
		 * common across all XPath expressions.
		 */
		Criteria criteria = session.createCriteria(ciContext.getBean(
				context.getStep().getQName()).getJavaClass());
		cache.put(context.getPath(), criteria);

		/**
		 * Build up the criteria from each Hibernate relative path
		 */
		for (HibernateRelativePath hRPath : hibernateRelativePaths) {
			/**
			 * Check that last step has predicates if the Hibernate relative
			 * path isn't truncated (which happens with XML properties only).
			 */
			HibernateStep last = hRPath.getLastHibernateStep();
			if (!hRPath.isTruncated()
					&& !((StepExpr) last.getStep()).hasPredicates())
				throw new ApplicationException(
						"Last Hibernate step does not end in a normal property with predicates");

			build(hRPath);

			/**
			 * XML properties result in a restriction
			 */
			if (last.isXml()) {
				Criteria parent = cache.get(last.getPrevious().getPath());

				Step step = last.getStep();
				parent.add(new XPathRestriction(step.getQName().getLocalPart(),
						step));
			}
		}

		return criteria;
	}

	/**
	 * Build up the criteria off the Hibernate relative path handling only
	 * entities or associations to entities.
	 * 
	 * @param hRPath
	 */
	protected void build(HibernateRelativePath hRPath) {
		/**
		 * Propagate down the Hibernate relative path creating sub criteria if
		 * need and creating restrictions off the predicates unless the step is
		 * a property.
		 */
		for (int depth = 0; depth < hRPath.getHiberateSteps().size(); depth++) {
			HibernateStep hStep = hRPath.getHiberateSteps().get(depth);

			/**
			 * Only entity or association properties (to other entities) can
			 * have predicates
			 */
			if (hStep.getType() != Type.Property) {
				/**
				 * Add a sub criteria based on the Hibernate property name
				 * against the criteria for the previous path.
				 */
				if (!cache.containsKey(hStep.getPath())) {
					Criteria parent = cache.get(hStep.getPrevious().getPath());
					Criteria criteria = parent.createCriteria(hStep.getName());

					cache.put(hStep.getPath(), criteria);
				}
				Criteria criteria = cache.get(hStep.getPath());

				/**
				 * Add restrictions for each predicate
				 */
				for (Expr predicate : ((StepExpr) hStep.getStep())
						.getPredicates())
					criteria.add(getRestriction(predicate, hStep));
			}
		}
	}

	/**
	 * 
	 * @param predicate
	 * @return
	 */
	protected Criterion getRestriction(Expr predicate, HibernateStep context) {
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
					getRestriction(((OrExpr) predicate).getOperands().get(0),
							context),
					getRestriction(((OrExpr) predicate).getOperands().get(1),
							context));
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
					getRestriction(((AndExpr) predicate).getOperands().get(0),
							context),
					getRestriction(((AndExpr) predicate).getOperands().get(1),
							context));
		case Comparison:
			String propertyName = null;

			/**
			 * Find a Step operand, confirm property name since it is not
			 * checked anywhere else.
			 */
			StepExpr step = getStepOperand(((ComparisonExpr) predicate)
					.getOperands());
			if (step != null) {
				propertyName = step.getQName().getLocalPart();

				CIBean bean = context.getBeanType();
				if (!bean.hasPropertyByName(step.getQName()))
					throw new ApplicationException(
							String.format(
									"Local part [%s] is not a property of the Bean [%s]",
									propertyName, bean));
			}

			/**
			 * Find a Relative Path operand and make a Hibernate Relative Path
			 */
			HibernateRelativePath hRPath = null;
			RelativePathExpr rpe = getRelativePathOperand(((ComparisonExpr) predicate)
					.getOperands());
			if (rpe != null)
				hRPath = process(rpe, context);

			/**
			 * Find the literal
			 */
			LiteralExpr literal = getLiteralOperand(((ComparisonExpr) predicate)
					.getOperands());

			if (hRPath != null) {
				if (hRPath.hasXML()) {

				} else {
				}
			}

			logger.debug("Predicate is comparison [{}] against property [{}]",
					((ComparisonExpr) predicate).getOperator().name(),
					propertyName);

			switch (((ComparisonExpr) predicate).getOperator()) {
			case ValueEquals:
				logger.debug("Value: {}", literal.getValue().getJavaValue());
				return Restrictions.eq(propertyName, literal.getValue()
						.getJavaValue());
			case ValueGreaterThan:
				logger.debug("Value: {}", literal.getValue().getJavaValue());
				return Restrictions.gt(propertyName, literal.getValue()
						.getJavaValue());
			case ValueGreaterThanOrEquals:
				logger.debug("Value: {}", literal.getValue().getJavaValue());
				return Restrictions.ge(propertyName, literal.getValue()
						.getJavaValue());
			case ValueLessThan:
				logger.debug("Value: {}", literal.getValue().getJavaValue());
				return Restrictions.lt(propertyName, literal.getValue()
						.getJavaValue());
			case ValueLessThanOrEquals:
				logger.debug("Value: {}", literal.getValue().getJavaValue());
				return Restrictions.le(propertyName, literal.getValue()
						.getJavaValue());
			case ValueNotEquals:
				logger.debug("Value: {}", literal.getValue().getJavaValue());
				return Restrictions.ne(propertyName, literal.getValue()
						.getJavaValue());
			case GeneralEquals:
				/**
				 * If atomic (not a sequence) then the 'in' restriction is not
				 * usable (defaults to 'eq' restriction) since the argument is
				 * an array of objects.
				 */
				if (literal.isAtomic()) {
					logger.debug("Value: {}", literal.getValue().getJavaValue());
					return Restrictions.eq(propertyName, literal.getValue()
							.getJavaValue());
				}

				logger.debug("Values: {}", literal.getValues());
				return Restrictions.in(propertyName, literal.getValues());
			case Matches:
				if (((ComparisonExpr) predicate).getOperands().size() != 2)
					throw new ApplicationException(String.format(
							"Matches function [%s] expects 2 operands",
							predicate));

				logger.debug("Value: {}", literal.getValue().getText());
				return Restrictions.ilike(propertyName, literal.getValue()
						.getText(), MatchMode.ANYWHERE);
			case Exists:
				if (((ComparisonExpr) predicate).getOperands().size() != 1)
					throw new ApplicationException(String.format(
							"Exists function [%s] expects only 1 operand",
							predicate));

				return Restrictions.isNotNull(propertyName);
			case Empty:
				if (((ComparisonExpr) predicate).getOperands().size() != 1)
					throw new ApplicationException(String.format(
							"Empty function [%s] expects only 1 operand",
							predicate));

				return Restrictions.isNull(propertyName);
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
	 * Gets the first Step operand in the list of expressions
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
	 * Gets the first Relative Path operand in the list of expressions
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
	 * Gets the first Literal operand in the list of expressions
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
}