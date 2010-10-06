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

package com.klistret.cmdb.aspects.persistence;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

import org.jvnet.jaxb.reflection.util.QNameMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.jaxb.BeanMetadata;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.jaxb.CIContextHelper;

import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * Identification first executes a XQuery to get an ordered list of criterion
 * for either a class name or QName (i.e. so called criteria). The XPath
 * expressions in that list are transformed into PathExpression (resolute only)
 * and added to an internal cache. This class then provides based on an object
 * valid XPath expressions to find similar persistent objects.
 * 
 * @author Matthew Young
 * 
 */
public class Identification {

	private static final Logger logger = LoggerFactory
			.getLogger(Identification.class);

	/**
	 * XQuery returning criterion objects in order by the Order attribute and CI
	 * extension
	 */
	private static String criterionQuery = "declare default element namespace \'http://www.klistret.com/cmdb/aspects/persistence\'; "
			+ "for $classname at $classnameIndex in (%s) "
			+ "let $rule := /PersistenceRules/Rule[Classname/. = $classname and not(Exclusions/. = %s)] "
			+ "for $criterion in /PersistenceRules/Criterion[@Name = $rule/Criterion/.] "
			+ "order by $classnameIndex, $rule/@Order empty greatest "
			+ "return $criterion";

	/**
	 * Persitence rules document
	 */
	private PersistenceRules persistenceRules;
	
	/**
	 * 
	 */
	private CIContext ciContext = CIContext.getCIContext();

	/**
	 * Internal JAXB context (persistence rules/criterion)
	 */
	private JAXBContext jaxbContext;

	/**
	 * Internal cache (qname keyed)
	 */
	private QNameMap<List<PathExpression[]>> cache = new QNameMap<List<PathExpression[]>>();

	/**
	 * Constructor creates a JAXB context
	 */
	public Identification() {
		try {
			logger
					.debug("Creating a JAXContext containing the PersistenceRules and Criterion classes");
			jaxbContext = JAXBContext
					.newInstance(
							com.klistret.cmdb.aspects.persistence.PersistenceRules.class,
							com.klistret.cmdb.aspects.persistence.Criterion.class);
		} catch (JAXBException e) {
			logger.error("Unable to create JAXBContext instance: {}", e);
			throw new ApplicationException(
					"Unable to create JAXBContext instance", e);
		}
	}

	/**
	 * Persistence rules document
	 * 
	 * @param url
	 */
	public void setPersistenceRules(URL url) {
		try {
			Unmarshaller um = jaxbContext.createUnmarshaller();

			persistenceRules = (PersistenceRules) um.unmarshal(url);
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
	 * Criteria by QName
	 * 
	 * @param qname
	 * @return List
	 */
	public List<PathExpression[]> getCriteriaByQName(QName qname) {
		if (!cache.containsKey(qname.getNamespaceURI(), qname.getLocalPart()))
			cache.put(qname, getCriteriaByXMLBean(ciContext
					.getBean(qname)));

		return cache.get(qname);
	}

	/**
	 * Criteria by class name
	 * 
	 * @param classname
	 * @return List
	 */
	public List<PathExpression[]> getCriteriaByClassname(String className) {
		BeanMetadata bean = ciContext.getBean(className);
		if (!cache.containsKey(bean.getNamespace(), bean.getLocalName()))
			cache.put(xmlBean.getType(), getCriteriaByXMLBean(bean));

		return getCriteriaByXMLBean(ciContext.getBean(className));
	}

	/**
	 * Criteria by XMLBean (CI representation from CIContextHelper)
	 * 
	 * @param xmlBean
	 * @return
	 */
	private List<PathExpression[]> getCriteriaByXMLBean(XMLBean xmlBean) {
		String fClassname = String.format("\'%s\'", xmlBean.getClazz()
				.getName());

		String fAncestors = String.format("\'%s\'", xmlBean.getClazz()
				.getName());
		for (XMLBean ancestor : ciContext.getAncestors(xmlBean))
			fAncestors = fAncestors.concat(String.format(",\'%s\'", ancestor
					.getClazz().getName()));

		return getCriteriaByXQuery(fClassname, fAncestors);
	}

	/**
	 * PathExpressions passed are executed against the object and if they are
	 * valid (for the entire criterion) then those XPath statements are modified
	 * to filter off the object's values.
	 * 
	 * @param criteria
	 * @param object
	 * @return String[]
	 */
	public String[] getCriterionByObject(List<PathExpression[]> criteria,
			Object object) {
		try {
			JAXBSource jaxbSource = new JAXBSource(ciContextHelper
					.getJAXBContext(), object);

			/**
			 * Loop through the criterion
			 */
			for (PathExpression[] criterion : criteria) {
				try {
					/**
					 * Validate each expression and discard the entire criterion
					 * if an expression has no singular value
					 */
					String[] expressions = new String[criterion.length];
					for (int index = 0; index < criterion.length; index++) {
						XPathExpression xexpr = criterion[index]
								.getXPathExpression();

						List<?> results = xexpr.evaluate(jaxbSource);
						if (results.size() != 1) {
							logger
									.error(
											"Expression [{}] either returned nothing or multiple against the passed object [{}]",
											criterion[index].getXPath(), object);
							throw new ApplicationException(
									String
											.format(
													"Expression [%s] either returned nothing or multiple against the passed object [%s]",
													criterion[index].getXPath(),
													object));
						}

						ValueRepresentation valueRep = (ValueRepresentation) results
								.get(0);

						String expression = String.format("%s[.=\"%s\"]",
								criterion[index].getXPath(), valueRep
										.getStringValue());
						expressions[index] = expression;
					}

					logger.debug("Returning valid expressions [{}] for object",
							expressions);
					return expressions;
				} catch (ApplicationException e) {
					// ingore the criterion
				}
			}
		} catch (JAXBException e) {
			logger.error(
					"JAXB exception generating source from object [{}]: {}",
					object, e);
			throw new ApplicationException("JAXB exception generating source",
					e);
		} catch (XPathException e) {
			logger.error(
					"XPath exception evaluating xpath against JAXB source: {}",
					e);
			throw new ApplicationException(
					"XPath exception evaluating xpath against JAXB source", e);
		}

		logger.debug("No criterion found for object [{}]", object);
		return null;
	}

	/**
	 * Each criterion for a class plus the extended classes (ancestors) is
	 * listed in order then within these criterion each XPath statement is
	 * transformed into a PathExpression.
	 * 
	 * @param fClassname
	 * @param fAncestors
	 * @return
	 */
	private List<PathExpression[]> getCriteriaByXQuery(String fClassname,
			String fAncestors) {
		List<PathExpression[]> criteria = new ArrayList<PathExpression[]>();

		String xquery = String.format(criterionQuery, fAncestors, fClassname);
		logger.debug("Evaluating xquery [{}]", xquery);

		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();

		try {
			XQueryExecutable xqexec = xqc.compile(xquery);
			XQueryEvaluator xqeval = xqexec.load();

			JAXBSource jaxbSource = new JAXBSource(jaxbContext,
					persistenceRules);

			xqeval.setSource(jaxbSource);
			XdmValue results = xqeval.evaluate();

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			// single value
			if (results instanceof XdmItem && results.size() == 1) {
				logger.debug("Single xquery result");

				PathExpression[] pathExpressions = getPathExpressions(
						(XdmNode) results, unmarshaller);
				if (pathExpressions != null)
					criteria.add(pathExpressions);
			}

			// sequence
			if (results.size() > 1) {
				logger.debug("Multiple xquery results");
				XdmSequenceIterator resultsIterator = results.iterator();

				while (resultsIterator.hasNext()) {
					PathExpression[] pathExpressions = getPathExpressions(
							((XdmNode) resultsIterator.next()), unmarshaller);
					if (pathExpressions != null)
						criteria.add(pathExpressions);
				}
			}
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
		}

		return criteria;
	}

	/**
	 * Places restrictions on the types of path expressions allowed (namely,
	 * resolute with root and at least one step with the final step not
	 * containing a filter).
	 * 
	 * @param xdmNode
	 * @param unmarshaller
	 * @return
	 * @throws JAXBException
	 */
	private PathExpression[] getPathExpressions(XdmNode xdmNode,
			Unmarshaller unmarshaller) throws JAXBException {
		NodeInfo nodeInfo = xdmNode.getUnderlyingNode();

		Criterion criterion = (Criterion) unmarshaller
				.unmarshal(NodeOverNodeInfo.wrap(nodeInfo));
		logger.debug("Returning criterion [{}]", criterion.getName());

		try {
			List<PathExpression> pathExpressions = new ArrayList<PathExpression>();

			for (String expression : criterion.getExpressions()) {
				PathExpression pathExpr = new PathExpression(expression);

				if (pathExpr.hasIrresolute()) {
					logger
							.debug(
									"Criterion expression [{}] must be completely resolute (ie. only steps with/without comparison filters)",
									pathExpr.getXPath());
					throw new ApplicationException(
							String
									.format(
											"Criterion expression [%s] must be completely resolute (ie. only steps with/without comparison filters)",
											pathExpr.getXPath()));
				}

				if (!pathExpr.hasRoot()) {
					logger.debug("Criterion expression [{}] must have a root",
							pathExpr.getXPath());
					throw new ApplicationException(String.format(
							"Criterion expression [%s] must have a root",
							pathExpr.getXPath()));
				}

				if (pathExpr.getDepth() == 1) {
					logger
							.debug(
									"Criterion expression [{}] must contain more than root",
									pathExpr.getXPath());
					throw new ApplicationException(
							String
									.format(
											"Criterion expression [%s] must contain more than root",
											pathExpr.getXPath()));
				}

				Expr last = pathExpr.getLastExpr();
				if (last instanceof StepExpr
						&& ((StepExpr) last).hasPredicate()) {
					logger
							.debug(
									"Criterion expression's [{}] last step must not have a predicate/filter",
									pathExpr.getXPath());
					throw new ApplicationException(
							String
									.format(
											"Criterion expression's [%s] last step must not have a predicate/filter",
											pathExpr.getXPath()));
				}

				pathExpressions.add(pathExpr);
			}

			return pathExpressions.toArray(new PathExpression[0]);
		} catch (ApplicationException e) {
			logger
					.debug(
							"Ignoring criterion [{}] because failure to get a valid path expression [{}]",
							criterion.getName(), e);
		}

		logger.debug("Unable to return path expressions for criterion [{}]",
				criterion.getName());
		return null;
	}
}
