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

package com.klistret.cmdb.plugin.persistence.aspect;

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
import com.klistret.cmdb.plugin.persistence.pojo.Criterion;
import com.klistret.cmdb.plugin.persistence.pojo.Persistence;
import com.klistret.cmdb.utility.jaxb.CIBean;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * 
 * @author Matthew Young
 * 
 */
public class CIIdentification {

	private static final Logger logger = LoggerFactory
			.getLogger(CIIdentification.class);

	/**
	 * JAXB Context
	 */
	private JAXBContext jaxbContext;

	/**
	 * JAXB Unmarshaller
	 */
	private Unmarshaller unmarshaller;

	/**
	 * Persistence XML representation
	 */
	private Persistence persistence;

	/**
	 * Cache of path expressions for QName which identify CIs
	 */
	private Map<QName, List<PathExpression[]>> cache = new HashMap<QName, List<PathExpression[]>>();

	/**
	 * XQuery that gets the types in the persistence XML and the rules
	 * associated with each type to locate the underlying criteria for
	 * identification. Criteria are returned by type (according to the order of
	 * the type array passed to the query) with empty order attributes being
	 * first.
	 */
	private static String criterionQuery = "declare default element namespace \'http://www.klistret.com/cmdb/plugin/persistence\'; "
			+ "for $type at $typeIndex in (%s) "
			+ "let $rule := /Persistence/Identification[@Type = $type] "
			+ "for $criterion in /Persistence/Criterion[@Name = $rule/CriterionRule/@Name] "
			+ "order by $typeIndex, $rule/CriterionRule/@Order empty greatest "
			+ "return $criterion";

	/**
	 * QName syntax as a regular expression
	 */
	static final Pattern typeSyntax = Pattern.compile("\\{(.*)\\}(.*)");

	/**
	 * Constructor building a local JAXB context consisting of the Persistence
	 * and Criterion XML representations
	 * 
	 * @param url
	 */
	protected CIIdentification(URL url) {
		try {
			jaxbContext = JAXBContext.newInstance(Persistence.class,
					Criterion.class);

			unmarshaller = jaxbContext.createUnmarshaller();
			persistence = (Persistence) unmarshaller.unmarshal(url);
		} catch (JAXBException e) {
			logger
					.error(
							"Unable to unmarshal URL [{}] into persistence rules object",
							url);
			throw new ApplicationException(
					String
							.format(
									"Unable to unmarshal URL [%s] into persistence rules object: ",
									url), e);
		}
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

				if (!expr.hasRoot()) {
					logger.debug("Criterion expression [{}] must have a root",
							expr.getXPath());
					throw new ApplicationException(String.format(
							"Criterion expression [%s] must have a root", expr
									.getXPath()));
				}

				if (expr.getDepth() == 1) {
					logger
							.debug(
									"Criterion expression [{}] must contain more than root",
									expr.getXPath());
					throw new ApplicationException(
							String
									.format(
											"Criterion expression [%s] must contain more than root",
											expr.getXPath()));
				}

				Expr last = expr.getLastExpr();
				if (!last.getType().equals(Expr.Type.Step)) {
					logger.debug(
							"Criterion expression [{}] must end in a step",
							expr.getXPath());
					throw new ApplicationException(String.format(
							"Criterion expression [%s] must end in a step",
							expr.getXPath()));
				}

				if (((StepExpr) last).hasPredicates()) {
					logger
							.debug(
									"Criterion expression [{}] can not have a last step with predicates",
									expr.getXPath());
					throw new ApplicationException(
							String
									.format(
											"Criterion expression [%s] can not have a last step with predicate",
											expr.getXPath()));
				}

				expressions.add(expr);
			}
		} catch (ApplicationException e) {
			logger
					.warn(
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

			JAXBSource jaxbSource = new JAXBSource(jaxbContext, persistence);

			xqeval.setSource(jaxbSource);
			XdmValue results = xqeval.evaluate();

			if (results instanceof XdmItem && results.size() == 1) {
				logger
						.debug("XQuery returning criterion to single result object");

				criteria
						.add(getPathExpressionsFromCriterion((XdmNode) results));
			}

			if (results.size() > 1) {
				logger
						.debug("XQuery returning criterion to multiple result objects");

				XdmSequenceIterator resultsIterator = results.iterator();
				while (resultsIterator.hasNext())
					criteria
							.add(getPathExpressionsFromCriterion(((XdmNode) resultsIterator
									.next())));
			}
		} catch (SaxonApiException e) {
			logger
					.error(
							"Unable to evaluate XQuery [{}] against persistence rules: {}",
							xquery, e);
			throw new ApplicationException(
					String
							.format(
									"Unable to evaluate XQuery [%s] against persistence rules: {%s}",
									xquery, e));
		} catch (JAXBException e) {
			logger
					.error(
							"Unable to evaluate XQuery [{}] against persistence rules: {}",
							xquery, e);
			throw new ApplicationException(
					String
							.format(
									"Unable to evaluate XQuery [%s] against persistence rules: {%s}",
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
		 * persistence rules defined to the bean's base types)
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
	 * 
	 * @param element
	 * @return
	 */
	protected List<String> getCriterion(Element element) {
		return getCriterion(element, element.getType().getName());
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
							String
									.format(
											"Either the namespace URI or local part of the type [%s] argument is null",
											type));
				}

				bean = CIContext.getCIContext()
						.getBean(namespaceURI, localPart);
				if (bean == null) {
					throw new ApplicationException("Bean not found within CI Context");
				}
			} else {
				throw new ApplicationException(
						String
								.format(
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
							logger
									.debug(
											"Expression [{}] either returned nothing against the passed object [{}]",
											xexpr, bean);
							throw new ApplicationException(
									String
											.format(
													"Expression [%s] either returned nothing against the passed object [%s]",
													xexpr, bean));
						}

						String valueSequence = null;
						for (ValueRepresentation valueRep : results) {
							valueSequence = valueSequence == null ? String
									.format("\"%s\"", valueRep.getStringValue())
									: String.format("%s, \"%s\"",
											valueSequence, valueRep
													.getStringValue());
						}
						String exprWithPredicate = String.format(
								"%s[%s = (%s)]", expr.substringXPath(expr
										.getDepth() - 2), expr.getXPath(expr
										.getDepth() - 1), valueSequence);
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
