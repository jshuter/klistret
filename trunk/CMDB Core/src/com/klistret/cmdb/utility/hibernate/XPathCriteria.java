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
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
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
import com.klistret.cmdb.utility.saxon.BaseExpression;
import com.klistret.cmdb.utility.saxon.ComparisonExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.FunctionCall;
import com.klistret.cmdb.utility.saxon.LiteralExpr;
import com.klistret.cmdb.utility.saxon.OrExpr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.RelativePathExpr;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * XPath criteria takes one or more XPath filters (expressions) and builds a
 * Hibernate criteria. The basic logic is simple (the code however is a bit
 * messy). Each XPath expression is mapped to what is called a Hibernate
 * relative path that either ends in a normal Hibernate property or an XML
 * column (whereby the rest of the expression is truncated but latent in the
 * underlying step). The Hibernate relative path despite the overhead makes it
 * easier to create Hibernate criteria aliases every time the forward direction
 * in the XPath crosses over a Hibernate entity or association.
 * 
 * Every filter is evaluated then translated (translate method) into a Hibernate
 * relative path (process method). Processing expects relative path expressions
 * only. Paths may be absolute or relative and are processed in relation to
 * their context (which allows for handling paths within predicates). Afterwards
 * the Hibernate relative path is built into a Hibernate criteria using aliases
 * against a single criteria instance (thus the need for the alias cache to
 * prevent duplicates).
 * 
 * @author Matthew Young
 * 
 */
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
	 * Hibernate Criteria
	 */
	private Criteria criteria;

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
	 * Alias mask
	 */
	private String aliasMask = "a0";

	/**
	 * Alias cache (HibernateStep path is the key, alias is the value)
	 */
	private Map<String, String> aliasCache = new HashMap<String, String>();

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
		 * Absolute paths are candidates for root Hibernate criteria
		 */
		private boolean absolute = false;

		/**
		 * Root entity name
		 */
		private String name;

		/**
		 * Hibernate relative path may be shorter in depth if they end in a
		 * property that is an XML column
		 */
		private boolean truncated = false;

		/**
		 * Every relative path must have a context
		 */
		private HibernateStep context;

		/**
		 * Get Hibernate steps
		 * 
		 * @return List
		 */
		public List<HibernateStep> getHiberateSteps() {
			return this.hSteps;
		}

		/**
		 * Does the relative path contain an Hibernate property for an XML
		 * column
		 * 
		 * @return boolean
		 */
		public boolean hasXML() {
			for (HibernateStep hStep : hSteps)
				if (hStep.isXml())
					return true;

			return false;
		}

		/**
		 * Is the path absolute
		 * 
		 * @return boolean
		 */
		public boolean isAbsolute() {
			return this.absolute;
		}

		/**
		 * Set absolute
		 * 
		 * @param value
		 */
		public void setAbsolute(boolean value) {
			this.absolute = value;
		}

		/**
		 * Has the path been truncated (are there remaining steps beyond the XML
		 * property)
		 * 
		 * @return boolean
		 */
		public boolean isTruncated() {
			return this.truncated;
		}

		/**
		 * Set truncated
		 * 
		 * @param value
		 */
		public void setTruncated(boolean value) {
			this.truncated = value;
		}

		/**
		 * Get last HibernateStep, null if the internal array is empty
		 * 
		 * @return
		 */
		public HibernateStep getLastHibernateStep() {
			if (hSteps.size() > 0)
				return hSteps.get(hSteps.size() - 1);

			return null;
		}

		/**
		 * Get first HibernateStep, null if the internal array is empty
		 * 
		 * @return
		 */
		public HibernateStep getFirstHibernateStep() {
			if (hSteps.size() > 0)
				return hSteps.get(0);

			return null;
		}

		/**
		 * Get name of the root entity
		 * 
		 * @return
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Set name of the root entity
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Get context
		 * 
		 * @return
		 */
		public HibernateStep getContext() {
			return this.context;
		}

		/**
		 * Set context
		 * 
		 * @param context
		 */
		public void setContext(HibernateStep context) {
			this.context = context;
		}

		/**
		 * Display information
		 */
		public String toString() {
			if (hSteps.size() > 0) {
				String p = null;
				for (HibernateStep s : hSteps)
					p = p == null ? s.getName() : String.format("%s.%s", p,
							s.getName());

				return String
						.format("context [%s], path [%s], absolute [%s], truncated [%s]",
								context, p, absolute, truncated);
			}

			return "empty";
		}
	}

	/**
	 * Hibernate wrapper (inner class) around steps
	 * 
	 */
	@SuppressWarnings("unused")
	private class HibernateStep {
		private Step step;

		private CIBean ciBean;

		private HibernateStep next;

		private HibernateStep previous;

		private String path;

		private String name;

		private Type type = Type.Property;

		private boolean xml = false;

		private BaseExpression baseExpression;

		public Step getStep() {
			return this.step;
		}

		public void setStep(Step step) {
			this.step = step;
		}

		public CIBean getCIBean() {
			return this.ciBean;
		}

		public void setCIBean(CIBean ciBean) {
			this.ciBean = ciBean;
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

		public BaseExpression getBaseExpression() {
			return this.baseExpression;
		}

		public void setBaseExpression(BaseExpression baseExpression) {
			this.baseExpression = baseExpression;
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
			return String.format("path: %s, qname: %s, bean class: %s",
					path == null ? "unknown" : path, step == null ? "unknown"
							: step.getQName(), ciBean == null ? "unknown"
							: ciBean.getJavaClass());
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
	 * Merge an expression filter after translating the expression first into
	 * PathExpression and then a Hibernate relative path that can be processed.
	 * 
	 * @param expression
	 */
	public void merge(String expression) {
		logger.debug("Merging filter expression [{}]", expression);

		PathExpression pe = new PathExpression(expression);
		RelativePathExpr rpe = pe.getRelativePath();

		HibernateRelativePath hrp = translate(rpe);

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
	 * Creates a Hibernate projection from an aggregate expression
	 * 
	 * @param expression
	 */
	public Projection aggregate(String expression) {
		logger.debug("Creating projection based on aggregate expression [{}]",
				expression);

		FunctionCall fc = new FunctionCall(expression);
		RelativePathExpr rpe = fc.getRelativePath();

		HibernateRelativePath hrp = translate(rpe);

		/**
		 * Confirm the last Hibernate step is a Hibernate property
		 */
		HibernateStep last = hrp.getLastHibernateStep();
		if (last.getType() != Type.Property)
			throw new ApplicationException(
					"Aggregation must act either on a Hibernate property or an XML column");

		/**
		 * Property name with alias
		 */
		String alias = aliasCache.get(last.getPrevious().getPath());
		String propertyName = alias == null ? last.getName() : String.format(
				"%s.%s", alias, last.getName());

		/**
		 * Only sum, avg, max, and min supported
		 */
		switch (fc.getFunction()) {
		case sum:
			return last.isXml() ? new XPathAggregation("sum", propertyName,
					last.getStep()) : Projections.sum(propertyName);
		case avg:
			return last.isXml() ? new XPathAggregation("avg", propertyName,
					last.getStep()) : Projections.avg(propertyName);
		case max:
			return last.isXml() ? new XPathAggregation("max", propertyName,
					last.getStep()) : Projections.max(propertyName);
		case min:
			return last.isXml() ? new XPathAggregation("min", propertyName,
					last.getStep()) : Projections.min(propertyName);
		default:
			throw new InfrastructureException(String.format(
					"Function call [%s] not handled.", fc.getFunction()));
		}
	}

	/**
	 * Translates an absolute relative path either from a path or aggregate
	 * expression
	 * 
	 * @param rpe
	 * @return
	 */
	private HibernateRelativePath translate(RelativePathExpr rpe) {
		BaseExpression be = rpe.getBaseExpression();

		/**
		 * Every expression has to be absolute (ie. starts with a slash)
		 */
		if (!rpe.hasRoot())
			throw new ApplicationException(String.format(
					"Expression [%s] has no root", be.getXPath()));

		/**
		 * The Root expression must be the first Step in the expression
		 */
		if (rpe.getExpr(0).getType() != Expr.Type.Root)
			throw new ApplicationException(
					String.format(
							"Expression [%s] expects an absolute path (root is not first step)",
							be.getXPath()));

		/**
		 * Multiple Root expression may not occur
		 */
		if (rpe.hasMultipleRoot())
			throw new ApplicationException(String.format(
					"Expression [%s] contains multiple root steps",
					be.getXPath()));

		/**
		 * More than the initial steps ("/" or "//") must exist to define a
		 * Hibernate criteria (based on either a class or entity name) plus the
		 * first Step has to be a Step not for example an Irresolute.
		 */
		if (!(rpe.getDepth() > 1) && rpe.getExpr(1).getType() == Expr.Type.Step)
			throw new ApplicationException(
					String.format(
							"Expression [%s] has no context step (depth greater than signular)",
							be.getXPath()), new UnsupportedOperationException());

		/**
		 * Context step must be an element with valid QName and metadata
		 */
		if (rpe.getExpr(1).getType() != Expr.Type.Step)
			throw new ApplicationException(
					String.format(
							"Expression [%s] context step is not Step (rather Irresolute or other)",
							be.getXPath()));

		StepExpr step = (StepExpr) rpe.getExpr(1);
		if (step.getPrimaryNodeKind() != StepExpr.PrimaryNodeKind.Element
				|| step.getQName() == null)
			throw new ApplicationException(
					String.format(
							"Expression [%s] context step not an XML Element with a valid QName",
							be.getXPath()));

		/**
		 * Is context step a CI Bean?
		 */
		CIBean bean = ciContext.getBean(step.getQName());
		if (bean == null)
			throw new ApplicationException(String.format(
					"Expression [%s] context step not a registered CI bean",
					be.getXPath()));

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
		hStep.setCIBean(bean);
		hStep.setName(step.getQName().getLocalPart());
		hStep.setPath(step.getQName().getLocalPart());
		hStep.setBaseExpression(be);

		HibernateRelativePath hrp = process(rpe, hStep);
		hrp.setName(cm.getEntityName());

		return hrp;
	}

	/**
	 * Translate a relative path expression into a Hibernate relative path
	 * 
	 * @param rpe
	 * @param hStep
	 */
	protected HibernateRelativePath process(RelativePathExpr rpe,
			HibernateStep context) {
		logger.debug("Processing relative path [{}]", rpe.getXPath());

		HibernateRelativePath hRPath = new HibernateRelativePath();
		hRPath.setContext(context);

		/**
		 * Offset to the first step after the context step if the relative path
		 * is an absolute. Assumption is that absolute paths are grounds for the
		 * start of a Hibernate criteria.
		 */
		int contextDepth = 0;
		if (rpe.getFirstExpr().getType() == Expr.Type.Root) {
			logger.debug(
					"Offsetting processing by 2 adding the passed context [{}] as the first Hibernate step",
					context);
			contextDepth = 2;

			hRPath.setAbsolute(true);
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

				CIBean bean = context.getCIBean();
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

				/**
				 * Type
				 */
				if (propertyType.isEntityType())
					hStep.setType(Type.Entity);

				if (propertyType.isAssociationType())
					hStep.setType(Type.Association);

				/**
				 * Store the CIBean corresponding to the property by type not
				 * name. Store as well the alias.
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

					hStep.setCIBean(other);
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
				 * Make sure the PathExpression is stored
				 */
				hStep.setBaseExpression(context.getBaseExpression());

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

		logger.debug("Hibernate relative path: {}", hRPath);
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
		criteria = session.createCriteria(ciContext.getBean(
				context.getStep().getQName()).getJavaClass());

		/**
		 * Build up the criteria from each Hibernate relative path
		 */
		for (HibernateRelativePath hRPath : hibernateRelativePaths) {
			/**
			 * Check that last step has predicates if the Hibernate relative
			 * path isn't truncated (which happens with XML properties only).
			 */
			HibernateStep last = hRPath.getLastHibernateStep();
			Step step = last.getStep();

			if (!hRPath.isTruncated()
					&& !((StepExpr) last.getStep()).hasPredicates())
				throw new ApplicationException(
						"Last Hibernate step does not end in a normal property with predicates");

			build(hRPath);

			/**
			 * XML properties result in a restriction
			 */
			if (last.isXml() && last.getPrevious() != null) {
				String alias = aliasCache.get(last.getPrevious().getPath());
				String propertyName = alias == null ? last.getName() : String
						.format("%s.%s", alias, last.getName());

				criteria.add(new XPathRestriction(propertyName, step));
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
				 * Aliases (right now) have a one-to-one mapping to paths. If
				 * the path as no associated alias then a sub criteria is
				 * created and the alias is stored.
				 */
				HibernateStep context = hStep.getPrevious() == null ? hRPath
						.getContext() : hStep.getPrevious();
				if (context == null)
					throw new InfrastructureException(String.format(
							"Hibernate Step [%s] has no context", hStep));

				if (hStep != context
						&& !aliasCache.containsKey(hStep.getPath())) {
					String contextAlias = aliasCache.get(context.getPath());
					String associationPath = contextAlias == null ? hStep
							.getName() : String.format("%s.%s", contextAlias,
							hStep.getName());

					criteria.createAlias(associationPath, aliasMask);

					logger.debug(
							"Adding key: {} and value: {} to the alias cache",
							hStep.getPath(), aliasMask);
					aliasCache.put(hStep.getPath(), aliasMask);
					aliasMask = incrementMask(aliasMask);
				}

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
		logger.debug("Adding restrictions by predicate to context [{}]",
				context);

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
			/**
			 * Find the literal
			 */
			LiteralExpr literal = getLiteralOperand(((ComparisonExpr) predicate)
					.getOperands());

			/**
			 * Find the relative path making a Hibernate relative path
			 */
			RelativePathExpr rpe = getRelativePathOperand(((ComparisonExpr) predicate)
					.getOperands());
			HibernateRelativePath hRPath = process(rpe, context);
			build(hRPath);

			HibernateStep last = hRPath.getLastHibernateStep();

			/**
			 * Property name with alias prefix
			 */
			String alias = last.getPrevious() == null ? aliasCache.get(context
					.getPath()) : aliasCache.get(last.getPrevious().getPath());
			String propertyName = alias == null ? last.getName() : String
					.format("%s.%s", alias, last.getName());

			/**
			 * Paths with XML properties (always the last step if present)
			 * return a XPath restriction.
			 */
			if (hRPath.hasXML()) {
				if (!hRPath.isTruncated())
					throw new ApplicationException(
							String.format(
									"Predicate relative path ending in an XML property [%s] must be truncated",
									last));

				/**
				 * Last Hibernate step of the Hibernate path marks the property
				 * which the restriction acts on.
				 */
				StepExpr step = (StepExpr) last.getStep();

				/**
				 * A new XPath is created from the last step downwards till the
				 * step prior to the ending step.
				 */
				String xpath = null;
				for (int depth = step.getDepth(); depth < step
						.getRelativePath().getDepth() - 1; depth++) {
					Expr expr = step.getRelativePath().getExpr(depth);
					xpath = xpath == null ? expr.getXPath() : String.format(
							"%s/%s", xpath, expr.getXPath());
				}

				Step ending = (Step) step.getRelativePath().getLastExpr();

				/**
				 * A new comparison is generated
				 */
				List<Expr> operands = new ArrayList<Expr>();
				operands.add(ending);
				operands.add(literal);

				xpath = String.format("%s[%s]", xpath, ComparisonExpr.getXPath(
						((ComparisonExpr) predicate).getOperator(), operands,
						false));
				xpath = String.format("%s%s", context.getBaseExpression()
						.getProlog(), xpath);

				PathExpression other = new PathExpression(xpath);
				return new XPathRestriction(propertyName, (Step) other
						.getRelativePath().getFirstExpr());
			}

			if (((StepExpr) last.getStep()).hasPredicates())
				throw new ApplicationException(
						String.format(
								"Comparisons against Hibernate properties may not have an underlying step [] with predicates",
								last.getStep()));

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

				Object[] javaValues = new Object[literal.getValues().length];
				for (int index = 0; index < literal.getValues().length; index++)
					javaValues[index] = ((com.klistret.cmdb.utility.saxon.Value) literal
							.getValues()[index]).getJavaValue();

				return Restrictions.in(propertyName, javaValues);
			case Matches:
				if (((ComparisonExpr) predicate).getOperands().size() != 2)
					throw new ApplicationException(String.format(
							"Matches function [%s] expects 2 operands",
							predicate));

				logger.debug("Value: {}", literal.getValue().getText());
				return Restrictions.ilike(propertyName, literal.getValue()
						.getText(), MatchMode.EXACT);
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

	/**
	 * Increment mask
	 * 
	 * @param mask
	 * @return
	 */
	private String incrementMask(String mask) {
		char last = mask.charAt(mask.length() - 1);

		return mask.substring(0, mask.length() - 1) + ++last;
	}
}