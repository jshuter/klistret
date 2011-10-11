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
package com.klistret.cmdb.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.Element;
import com.klistret.cmdb.ci.pojo.Relation;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.identification.pojo.Blueprint;
import com.klistret.cmdb.identification.pojo.Criterion;
import com.klistret.cmdb.utility.jaxb.CIBean;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

public class IdentificationServiceImpl implements IdentificationService {

	private static final Logger logger = LoggerFactory
			.getLogger(IdentificationServiceImpl.class);

	/**
	 * Dependency injection
	 */
	protected URL url;

	/**
	 * JAXB Context
	 */
	private JAXBContext jaxbContext;

	/**
	 * JAXB Unmarshaller
	 */
	private Unmarshaller unmarshaller;

	/**
	 * Blueprint bean for taxonomy
	 */
	private Blueprint blueprint;

	/**
	 * Element service (dependency injection)
	 */
	private ElementService elementService;

	/**
	 * Cache of path expressions for QName which identify CIs
	 */
	private Map<QName, List<PathExpression[]>> cache = new HashMap<QName, List<PathExpression[]>>();

	/**
	 * XQuery that gets the types in the identification XML and the rules
	 * associated with each type to locate the underlying criteria for
	 * identification. Criteria are returned by type (according to the order of
	 * the type array passed to the query) with empty order attributes being
	 * first.
	 */
	private static String criterionQuery = "declare default element namespace \'http://www.klistret.com/cmdb/identification\'; "
			+ "for $type at $typeIndex in (%s) "
			+ "for $rule in /Blueprint/Identification[@Type eq $type]/CriterionRule "
			+ "for $criterion in /Blueprint/Criterion[@Name = $rule/@Name] "
			+ "order by $typeIndex, $rule/@Order empty least "
			+ "return $criterion";

	/**
	 * 
	 */
	private static final String activeElementQuery = "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[empty(pojo:toTimeStamp)]";

	/**
	 * 
	 */
	private String elementTypeQuery = "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element/pojo:type[pojo:name eq \"%s\"]";

	/**
	 * 
	 */
	private String elementIdQuery = "declare namespace pojo=\"http://www.klistret.com/cmdb/ci/pojo\"; /pojo:Element[pojo:id ne %d]";

	/**
	 * QName syntax as a regular expression
	 */
	static final Pattern typeSyntax = Pattern.compile("\\{(.*)\\}(.*)");

	/**
	 * Set URL then roll data into blueprint bean for taxonomy
	 * 
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
		this.cache.clear();

		try {
			jaxbContext = JAXBContext.newInstance(Blueprint.class,
					Criterion.class);

			unmarshaller = jaxbContext.createUnmarshaller();
			blueprint = (Blueprint) unmarshaller.unmarshal(url);
		} catch (JAXBException e) {
			logger.error(
					"Unable to unmarshal URL [{}] into blueprint bean for identification",
					url);
			throw new InfrastructureException(
					String.format(
							"Unable to unmarshal URL [%s] into blueprint bean for identification: ",
							url), e);
		}
	}

	/**
	 * Set element service (dependency injection)
	 * 
	 * @param elementService
	 */
	public void setElementService(ElementService elementService) {
		this.elementService = elementService;
	}

	/**
	 * Determine if there are similar elements
	 * 
	 * @return Integer
	 */
	public Integer identified(Element element) {
		List<String> criterion = getFullCriterion(element);

		/**
		 * Exit returning false if no criterion exists for the particular
		 * element
		 */
		if (criterion == null) {
			logger.debug("Exiting identified method because no valid xpath expressions exist for the criteria list");
			return 0;
		}

		/**
		 * Find similar elements
		 */
		Integer count = elementService.count(criterion);
		if (count > 0) {
			logger.debug(
					"Element is identical to other elements [count: {}] according to identification rules ",
					count);
			return count;
		}

		return 0;
	}

	/**
	 * Creates path expressions from the XPath statement defined within the
	 * Criterion element and performs a series of controls so the XPaths are
	 * selection statements.
	 * 
	 * @param xdmNode
	 * @return Criteria
	 * @throws JAXBException
	 */
	private PathExpression[] getPathExpressionsFromCriterion(XdmNode xdmNode)
			throws JAXBException {
		List<PathExpression> expressions = new ArrayList<PathExpression>();

		/**
		 * Unmarshall based on node information
		 */
		NodeInfo nodeInfo = xdmNode.getUnderlyingNode();
		Criterion criterion = (Criterion) unmarshaller
				.unmarshal(NodeOverNodeInfo.wrap(nodeInfo));
		logger.debug("Evaluating criterion [{}] into path expressions",
				criterion.getName());

		/**
		 * For each expression in the criterion build a path expression and
		 * allow the expression if it has a root, an axis depth beyond the
		 * root), and ends in a step without predicates.
		 */
		try {
			for (String expression : criterion.getExpression()) {
				PathExpression expr = new PathExpression(expression);

				/**
				 * Relative path must have a root as the initial step
				 */
				if (!(expr.getRelativePath().hasRoot() && expr
						.getRelativePath().getFirstExpr().getType() == Expr.Type.Root)) {
					logger.debug("Criterion expression [{}] must have a root",
							expr.getXPath());
					throw new ApplicationException(String.format(
							"Criterion expression [%s] must have a root",
							expr.getXPath()));
				}

				/**
				 * Depth must be greater than an initial step
				 */
				if (expr.getRelativePath().getDepth() == 1) {
					logger.debug(
							"Criterion expression [{}] must contain more than root",
							expr.getXPath());
					throw new ApplicationException(
							String.format(
									"Criterion expression [%s] must contain more than root",
									expr.getXPath()));
				}

				/**
				 * Last step must be an axis
				 */
				Expr last = expr.getRelativePath().getLastExpr();
				if (!last.getType().equals(Expr.Type.Step)) {
					logger.debug(
							"Criterion expression [{}] must end in a step",
							expr.getXPath());
					throw new ApplicationException(String.format(
							"Criterion expression [%s] must end in a step",
							expr.getXPath()));
				}

				/**
				 * Last step may not have predicates
				 */
				if (((StepExpr) last).hasPredicates()) {
					logger.debug(
							"Criterion expression [{}] can not have a last step with predicates",
							expr.getXPath());
					throw new ApplicationException(
							String.format(
									"Criterion expression [%s] can not have a last step with predicate",
									expr.getXPath()));
				}

				expressions.add(expr);
			}
		} catch (ApplicationException e) {
			logger.warn(
					"Ignoring criterion [{}] because failure to get a valid path expression [{}]",
					criterion.getName(), e);
		}

		return expressions.toArray(new PathExpression[0]);
	}

	/**
	 * Execute the xquery to return an ordered list of criterion expressions
	 * (xpath statements) for the identified bean and it's base types.
	 * 
	 * @param selfAndBaseClassNames
	 * @return Criteria
	 */
	private List<PathExpression[]> evaluateQuery(String selfAndBaseClassNames) {
		List<PathExpression[]> criteria = new ArrayList<PathExpression[]>();

		String xquery = String.format(criterionQuery, selfAndBaseClassNames);

		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();

		try {
			XQueryExecutable xqexec = xqc.compile(xquery);
			XQueryEvaluator xqeval = xqexec.load();

			JAXBSource jaxbSource = new JAXBSource(jaxbContext, blueprint);

			xqeval.setSource(jaxbSource);
			XdmValue results = xqeval.evaluate();

			if (results instanceof XdmItem && results.size() == 1) {
				logger.debug("XQuery returning criterion to single result object");

				criteria.add(getPathExpressionsFromCriterion((XdmNode) results));
			}

			if (results.size() > 1) {
				logger.debug("XQuery returning criterion to multiple result objects");

				XdmSequenceIterator resultsIterator = results.iterator();
				while (resultsIterator.hasNext())
					criteria.add(getPathExpressionsFromCriterion(((XdmNode) resultsIterator
							.next())));
			}
		} catch (SaxonApiException e) {
			logger.error(
					"Unable to evaluate XQuery [{}] against identification rules: {}",
					xquery, e);
			throw new ApplicationException(
					String.format(
							"Unable to evaluate XQuery [%s] against identification rules: {%s}",
							xquery, e));
		} catch (JAXBException e) {
			logger.error(
					"Unable to evaluate XQuery [{}] against identification rules: {}",
					xquery, e);
			throw new ApplicationException(
					String.format(
							"Unable to evaluate XQuery [%s] against identification rules: {%s}",
							xquery, e));
		}

		return criteria;
	}

	/**
	 * Are the criteria cached for the bean?
	 * 
	 * @param qname
	 * @return true/false
	 */
	private boolean isCached(QName qname) {
		for (Map.Entry<QName, List<PathExpression[]>> entry : cache.entrySet()) {
			if (qname.equals(entry.getKey()))
				return true;
		}

		return false;
	}

	/**
	 * Adds a criteria to the cache per bean
	 * 
	 * @param bean
	 */
	private void addCriteria(CIBean bean) {
		/**
		 * Determine the class names for the bean and ancestors (extend the
		 * identification rules defined to the bean's base types)
		 */
		String selfAndBaseClassNames = String.format("\'%s\'", bean.getType());

		QName baseType = bean.getBaseType();
		while (CIContext.getCIContext().isBean(baseType)) {
			CIBean base = CIContext.getCIContext().getBean(baseType);

			selfAndBaseClassNames = selfAndBaseClassNames.concat(String.format(
					",\'%s\'", base.getType()));

			baseType = base.getBaseType();
		}

		/**
		 * Add criteria to the cache
		 */
		List<PathExpression[]> criteria = evaluateQuery(selfAndBaseClassNames);
		cache.put(bean.getType(), criteria);
	}

	/**
	 * Returns a cached criteria
	 * 
	 * @param bean
	 * @return Criteria
	 */
	private synchronized List<PathExpression[]> getCriteria(CIBean bean) {
		if (!isCached(bean.getType()))
			addCriteria(bean);

		return cache.get(bean.getType());
	}

	/**
	 * Get criteria by CI qname
	 * 
	 * @param qname
	 * @return Criteria
	 */
	protected List<PathExpression[]> getCriteria(QName qname) {
		CIBean bean = CIContext.getCIContext().getBean(qname);

		return getCriteria(bean);
	}

	/**
	 * Get criteria by CI class name
	 * 
	 * @param className
	 * @return Criteria
	 */
	protected List<PathExpression[]> getCriteria(String className) {
		CIBean bean = CIContext.getCIContext().getBean(className);

		return getCriteria(bean);
	}

	/**
	 * Get basic criterion
	 * 
	 * @param element
	 * @return
	 */
	public List<String> getCriterion(Element element) {
		return getCriterion(element, element.getType().getName());
	}

	/**
	 * Get full criterion taking into account criteria for active elements, the
	 * element type and persistant elements with the same id.
	 * 
	 * @param element
	 * @return
	 */
	public List<String> getFullCriterion(Element element) {
		List<String> criterion = getCriterion(element);

		if (criterion == null)
			return null;

		/**
		 * Only query active elements of the same type
		 */
		criterion.add(activeElementQuery);
		criterion.add(String.format(elementTypeQuery, element.getType()
				.getName()));

		/**
		 * Eliminate elements with the same id if the element is persisted
		 */
		if (element.getId() != null)
			criterion.add(String.format(elementIdQuery, element.getId()));

		return criterion;
	}

	/**
	 * 
	 * @param relation
	 * @return
	 */
	protected List<String> getCriterion(Relation relation) {
		return getCriterion(relation, relation.getType().getName());
	}

	/**
	 * 
	 * @param object
	 * @return Criterion
	 */
	@SuppressWarnings("unchecked")
	private List<String> getCriterion(Object pojo, String type) {
		List<String> criterionWithPredicates = new ArrayList<String>();

		try {
			JAXBSource jaxbSource = new JAXBSource(CIContext.getCIContext()
					.getJAXBContext(), pojo);

			CIBean bean = null;
			Matcher typeMatcher = typeSyntax.matcher(type);
			if (typeMatcher.find()) {
				String namespaceURI = typeMatcher.group(1);
				String localPart = typeMatcher.group(2);

				if (namespaceURI == null || localPart == null) {
					throw new ApplicationException(
							String.format(
									"Either the namespace URI or local part of the type [%s] argument is null",
									type));
				}

				bean = CIContext.getCIContext()
						.getBean(namespaceURI, localPart);
				if (bean == null) {
					throw new ApplicationException(
							"Bean not found within CI Context");
				}
			} else {
				throw new ApplicationException(
						String.format(
								"Type [%s] argument does not match syntax: \\{namespaceURI\\}localPart",
								type));
			}

			/**
			 * Loop through each criterion to get a singular value and build a
			 * new XPath based on the criterion without the last step plus a
			 * predicate on the returned value against the last step's QName.
			 */
			List<PathExpression[]> criteria = getCriteria(bean);
			for (PathExpression[] criterion : criteria) {
				try {
					for (PathExpression expr : criterion) {
						XPathExpression xexpr = expr.getXPathExpression();

						/**
						 * Results must be a single value
						 */
						List<ValueRepresentation> results = xexpr
								.evaluate(jaxbSource);
						if (results.size() == 0) {
							logger.debug(
									"Expression [{}] either returned nothing against the passed object [{}]",
									xexpr, bean);
							throw new ApplicationException(
									String.format(
											"Expression [%s] either returned nothing against the passed object [%s]",
											xexpr, bean));
						}

						/**
						 * Single or multiples values placed into a sequence for
						 * general comparison
						 */
						String valueSequence = null;
						for (ValueRepresentation valueRep : results) {
							valueSequence = valueSequence == null ? String
									.format("\"%s\"", valueRep.getStringValue())
									: String.format("%s, \"%s\"",
											valueSequence,
											valueRep.getStringValue());
						}

						/**
						 * The raw XPath string plus the prolog up to the last
						 * step is concatenated with the last step and the value
						 * sequence as a predicate.
						 */
						Step step = (Step) expr.getRelativePath().getLastExpr();
						String exprWithPredicate = String.format(
								"%s %s[%s = (%s)]", expr.getProlog(),
								expr.getRawXPath(0, step.getDepth() - 1),
								step.getXPath(), valueSequence);
						criterionWithPredicates.add(exprWithPredicate);
					}

					return criterionWithPredicates;
				} catch (ApplicationException e) {
					logger.debug("Continuing to the next criterion");
					criterionWithPredicates.clear();
				}
			}
		} catch (JAXBException e) {
			logger.error(
					"JAXB exception generating source from object [{}]: {}",
					pojo, e);
			throw new ApplicationException("JAXB exception generating source",
					e);
		} catch (XPathException e) {
			logger.error(
					"XPath exception evaluating xpath against JAXB source: {}",
					e);
			throw new ApplicationException(
					"XPath exception evaluating xpath against JAXB source", e);
		}

		logger.debug("No criterion for object [{}] with type [{}]", pojo, type);
		return null;
	}
}
