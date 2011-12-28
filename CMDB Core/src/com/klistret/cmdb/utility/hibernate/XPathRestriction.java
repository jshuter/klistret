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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.TypedValue;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;
import com.klistret.cmdb.utility.saxon.IrresoluteExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.Value;

/**
 * Implements Hibernate Criterion for XPath expressions acting on a property
 * given a Step. The initial step's name is switched out with a wildcard since
 * the underlying schema type is unknown.
 * 
 * @author Matthew Young
 * 
 */
@SuppressWarnings("serial")
public class XPathRestriction implements Criterion {
	private static final Logger logger = LoggerFactory
			.getLogger(XPathRestriction.class);

	/**
	 * Binding property for Hibernate criteria (as stated in mapping files)
	 */
	private final String propertyName;

	/**
	 * Expression step
	 */
	private final Step step;

	/**
	 * TypedValues containing the string values for literals
	 */
	private List<TypedValue> typedValues = new ArrayList<TypedValue>();

	/**
	 * Everything is a string despite the explicit casting
	 */
	private static final Type stringType = new StringType();

	/**
	 * Variable reference that associates the database column to the XPath.
	 */
	private static final String variableReference = "this";

	/**
	 * Single Quotes pattern
	 */
	private static final Pattern singleQuotes = Pattern
			.compile("'((?:[^']+|'')*)'");

	/**
	 * VARCHAR size limitation
	 */
	private static int varcharLimit = 255;

	/**
	 * Base mask for literal variables
	 */
	private static final String baseMask = "v0";

	/**
	 * Constructor transfers over arguments to properties (variable refenese
	 * defaults to "this")
	 * 
	 * @param propertyName
	 * @param step
	 */
	public XPathRestriction(String propertyName, Step step) {
		this.propertyName = propertyName;
		this.step = step;
	}

	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		/**
		 * Establish dialect, property information about this column
		 */
		Dialect dialect = criteriaQuery.getFactory().getDialect();
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
				propertyName);

		if (columns.length != 1) {
			logger.error(
					"XMLEXISTS may only be used with single-column properties [property: {}]",
					propertyName);
			throw new HibernateException(
					"XMLEXISTS may only be used with single-column properties");
		}

		/**
		 * Path Expression
		 */
		PathExpression pathExpression = (PathExpression)step.getRelativePath()
				.getBaseExpression();

		/**
		 * Property type is generalized to wild-card "*" leaving only the
		 * predicate
		 */
		String axis = String.format("%s:%s", step.getQName().getPrefix(), step
				.getQName().getLocalPart());

		/**
		 * Passing clause
		 */
		String passingClause = String.format("PASSING %s AS \"%s\"",
				columns[0], variableReference);

		/**
		 * Setup XPath first with the variable reference (acts a root), the axis
		 * is replaced and then XPath is built up again either from generated
		 * string or the raw XPath for each step (depending on if it is a
		 * readable step or irresolute).
		 */
		String xpath = String.format("$%s", variableReference);

		String sqlMask = baseMask;
		for (Expr expr : step.getRelativePath().getSteps()) {
			if (expr instanceof Step) {
				if (((Step) expr).getDepth() >= step.getDepth()) {
					if (expr instanceof StepExpr) {
						xpath = String.format("%s/%s", xpath,
								expr.getXPath(true));

						for (Value value : ((StepExpr) expr).getValues()) {
							if (value.getText().length() > varcharLimit)
								throw new ApplicationException(
										String.format(
												"Literal value [%s] is larger than VARCHAR limiation [%d]",
												value.getText(), varcharLimit));

							String xpathMask = value.getMask();
							passingClause = String.format(
									"%s, CAST (? AS VARCHAR(%d)) AS \"%s\"",
									passingClause, varcharLimit, xpathMask);

							typedValues.add(new TypedValue(stringType, value
									.getText(), EntityMode.POJO));
							logger.debug(
									"Adding StringType [value: {}] to restriction with variable [{}]",
									value.getText(), xpathMask);

							/**
							 * Use a common mask to reduce the variation in
							 * generated SQL
							 */
							xpath = xpath.replaceAll(xpathMask, sqlMask);
							passingClause = passingClause.replaceAll(xpathMask,
									sqlMask);
							logger.debug(
									"Replaced XPath mask {} with a common SQL mask {}",
									xpathMask, sqlMask);

							sqlMask = incrementMask(sqlMask);
						}
					}
					if (expr instanceof IrresoluteExpr) {
						xpath = String.format(
								"%s/%s",
								xpath,
								pathExpression.getRelativePath().getRawXPath(
										((Step) expr).getDepth()));
					}
				}
			}
		}

		xpath = xpath.replaceFirst(axis, "*");
		logger.debug(
				"XPath [{}] prior prefixing default function declaration and namespace declarations",
				xpath);

		/**
		 * Concatenate namespace declarations
		 */
		for (String namespace : pathExpression.getNamespaces())
			xpath = namespace.concat(xpath);

		/**
		 * Concatenate default element namespace declaration
		 */
		if (pathExpression.getDefaultElementNamespace() != null)
			xpath = pathExpression.getDefaultElementNamespace().concat(xpath);

		if (dialect instanceof DB2Dialect) {
			/**
			 * DB2 only allows SQL with double quotes (or at least that is the
			 * extend of my knowledge)
			 */
			Matcher sq = singleQuotes.matcher(xpath);
			if (sq.find())
				throw new ApplicationException(
						String.format(
								"XPath [%s] contains surrounding single quotes which DB2 does not allow",
								xpath), new UnsupportedOperationException());
		}

		/**
		 * Return the XMLEXISTS predicate
		 */
		return String.format("XMLEXISTS(\'%s\' %s)", xpath, passingClause);
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria,
			CriteriaQuery criteriaQuery) throws HibernateException {
		return typedValues.toArray(new TypedValue[0]);
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
