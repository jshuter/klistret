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
import com.klistret.cmdb.utility.jaxb.CIContextHelper;

import com.klistret.cmdb.utility.saxon.PathExpression;

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
	 * CI Context helper (CI hierarchy)
	 */
	private CIContextHelper ciContextHelper;

	/**
	 * Persitence rules document
	 */
	private PersistenceRules persistenceRules;

	/**
	 * Internal JAXB context (persistence rules/criterion)
	 */
	private JAXBContext jaxbContext;

	/**
	 * 
	 */
	private QNameMap<List<PathExpression[]>> cache = new QNameMap<List<PathExpression[]>>();

	/**
	 * Constructor creates a JAXB context
	 */
	public Identification() {
		try {
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

	public CIContextHelper getCiContextHelper() {
		return ciContextHelper;
	}

	public void setCiContextHelper(CIContextHelper ciContextHelper) {
		this.ciContextHelper = ciContextHelper;
	}

	public PersistenceRules getPersistenceRules() {
		return this.persistenceRules;
	}

	/**
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

	public void setPersistenceRules(PersistenceRules persistenceRules) {
		this.persistenceRules = persistenceRules;
	}

	/**
	 * 
	 * @param qname
	 * @return
	 */
	public List<PathExpression[]> getCriteriaByQName(QName qname) {
		if (!cache.containsKey(qname.getNamespaceURI(), qname.getLocalPart()))
			cache.put(qname, getCriteriaByXMLBean(ciContextHelper
					.getXMLBean(qname)));

		return cache.get(qname);
	}

	/**
	 * 
	 * @param classname
	 * @return
	 */
	public List<PathExpression[]> getCriteriaByClassname(String classname) {
		XMLBean xmlBean = ciContextHelper.getXMLBean(classname);
		if (!cache.containsKey(xmlBean.getType().getNamespaceURI(), xmlBean
				.getType().getLocalPart()))
			cache.put(xmlBean.getType(), getCriteriaByXMLBean(xmlBean));

		return getCriteriaByXMLBean(ciContextHelper.getXMLBean(classname));
	}

	/**
	 * 
	 * @param xmlBean
	 * @return
	 */
	private List<PathExpression[]> getCriteriaByXMLBean(XMLBean xmlBean) {
		String fClassname = String.format("\'%s\'", xmlBean.getClazz()
				.getName());

		String fAncestors = String.format("\'%s\'", xmlBean.getClazz()
				.getName());
		for (XMLBean ancestor : ciContextHelper.getAncestors(xmlBean))
			fAncestors = fAncestors.concat(String.format(",\'%s\'", ancestor
					.getClazz().getName()));

		return getCriteriaByXQuery(fClassname, fAncestors);
	}

	/**
	 * 
	 * @param criteria
	 * @param object
	 * @return
	 */
	public PathExpression[] getCriterionByObject(
			List<PathExpression[]> criteria, Object object) {
		try {
			JAXBSource jaxbSource = new JAXBSource(ciContextHelper
					.getJAXBContext(), object);

			for (PathExpression[] criterion : criteria) {

				for (PathExpression expression : criterion) {
					XPathExpression xexpr = expression.getXPathExpression();

					List<?> results = xexpr.evaluate(jaxbSource);
					results.size();
				}
			}
		} catch (JAXBException e) {
		} catch (XPathException e) {
		}

		return null;
	}

	/**
	 * 
	 * @param fClassname
	 * @param fAncestors
	 * @return
	 */
	private List<PathExpression[]> getCriteriaByXQuery(String fClassname,
			String fAncestors) {
		List<PathExpression[]> criteria = new ArrayList<PathExpression[]>();

		String xquery = String.format(criterionQuery, fAncestors, fClassname);

		Processor processor = new Processor(false);
		XQueryCompiler xqc = processor.newXQueryCompiler();

		try {
			XQueryExecutable xqexec = xqc.compile(xquery);
			XQueryEvaluator xqeval = xqexec.load();

			if (persistenceRules == null) {
				logger.error("Persitence rules (document) is null");
				throw new ApplicationException(
						"Persitence rules (document) is null");
			}
			JAXBSource jaxbSource = new JAXBSource(jaxbContext,
					persistenceRules);

			xqeval.setSource(jaxbSource);
			XdmValue results = xqeval.evaluate();

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			// single value
			if (results instanceof XdmItem && results.size() == 1) {
				PathExpression[] pathExpressions = getPathExpressions(
						(XdmNode) results, unmarshaller);
				if (pathExpressions != null)
					criteria.add(pathExpressions);
			}

			// sequence
			if (results.size() > 1) {
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

		try {
			List<PathExpression> pathExpressions = new ArrayList<PathExpression>();

			for (String expression : criterion.getExpressions())
				pathExpressions.add(new PathExpression(expression));

			return pathExpressions.toArray(new PathExpression[0]);
		} catch (ApplicationException e) {
			logger
					.debug(
							"Ignoring criterion [{}] because failure to create path expression [{}]",
							criterion.getName(), e);
		}

		return null;
	}
}
