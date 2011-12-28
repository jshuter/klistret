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

import org.hibernate.Session;
import org.hibernate.criterion.Projection;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.jaxb.CIBean;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.FunctionCall;
import com.klistret.cmdb.utility.saxon.RelativePathExpr;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * 
 * @author Matthew Young
 * 
 */
public class XPathAggregation {

	/**
	 * XPath aggregation
	 */
	private String xpath;

	/**
	 * Hibernate session
	 */
	private Session session;

	/**
	 * CI metadata
	 */
	private CIContext ciContext = CIContext.getCIContext();

	/**
	 * Hibernate projection
	 */
	private Projection aggregation;
	
	/**
	 * 
	 */
	private FunctionCall call;

	/**
	 * Constructor
	 * 
	 * @param xpath
	 * @param session
	 */
	public XPathAggregation(String xpath, Session session) {
		this.xpath = xpath;
		this.session = session;

		makeProjection();
	}

	/**
	 * Get XPath aggregation
	 * 
	 * @return
	 */
	public String getXPath() {
		return this.xpath;
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
	 * Get Hibernate projection
	 * 
	 * @return
	 */
	public Projection getProjection() {
		return this.aggregation;
	}

	/**
	 * Compiles the XPath into a function call and uses the relative path to
	 * build a Hibernate projection.
	 */
	private void makeProjection() {
		call = new FunctionCall(this.xpath);

		RelativePathExpr rpe = call.getRelativePath();

		/**
		 * Every expression has to be absolute (i.e. starts with a slash)
		 */
		if (!rpe.hasRoot())
			throw new ApplicationException(String.format(
					"XPath [%s] has no root expression", call.getXPath()),
					new UnsupportedOperationException());

		/**
		 * The Root expression must be the first Step in the expression
		 */
		if (rpe.getExpr(0).getType() != Expr.Type.Root)
			throw new ApplicationException(String.format(
					"XPath [%s] has no root expression as the initial step",
					call.getXPath()), new UnsupportedOperationException());

		/**
		 * Multiple Root expression may not occur
		 */
		if (rpe.hasMultipleRoot())
			throw new ApplicationException(
					String.format(
							"XPath [%s] contains multiple root steps (double slashes likely)",
							call.getXPath()),
					new UnsupportedOperationException());

		/**
		 * More than the initial steps ("/" or "//") must exist to travel along
		 * the property hierarchy.
		 */
		if (!(rpe.getDepth() > 1) && rpe.getExpr(1).getType() == Expr.Type.Step)
			throw new ApplicationException(
					String.format(
							"XPath [%s] has no first step (depth greater than signular)",
							call.getXPath()),
					new UnsupportedOperationException());

		/**
		 * Recursively find the projection
		 */
		aggregation = getProjection((StepExpr)rpe.getFirstExpr());
	}

	/**
	 * 
	 * @param step
	 * @return
	 */
	private Projection getProjection(StepExpr step) {
		/**
		 * Step must be an element with valid QName and bean Metadata
		 */
		if (step.getPrimaryNodeKind() != StepExpr.PrimaryNodeKind.Element
				|| step.getQName() == null)
			throw new ApplicationException(String.format(
					"First step [%s] must be an element with valid qname",
					call.getXPath()), new UnsupportedOperationException());

		/**
		 * Is step a CI Bean?
		 */
		CIBean stepBean = ciContext.getBean(step.getQName());
		if (stepBean == null)
			throw new ApplicationException(String.format(
					"Step [%s] must be a CI bean", step.getQName()),
					new UnsupportedOperationException());
		return null;
	}
}
