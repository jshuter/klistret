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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.utility.saxon.BaseExpression;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.IrresoluteExpr;
import com.klistret.cmdb.utility.saxon.Step;
import com.klistret.cmdb.utility.saxon.StepExpr;

/**
 * 
 * @author Matthew Young
 * 
 */
@SuppressWarnings("serial")
public class XPathAggregation extends SimpleProjection {
	private static final Logger logger = LoggerFactory
			.getLogger(XPathAggregation.class);

	/**
	 * 
	 */
	private final String propertyName;

	/**
	 * 
	 */
	private final String functionName;

	/**
	 * 
	 */
	private final Step step;

	/**
	 * Variable reference that associates the database column to the XPath.
	 */
	private static final String variableReference = "this";

	/**
	 * VARCHAR size limitation
	 */
	private static int varcharLimit = 255;

	/**
	 * Single Quotes pattern
	 */
	private static final Pattern singleQuotes = Pattern
			.compile("'((?:[^']+|'')*)'");

	/**
	 * Constructor
	 * 
	 * @param functionName
	 * @param propertyName
	 * @param step
	 */
	public XPathAggregation(String functionName, String propertyName, Step step) {
		this.functionName = functionName;
		this.propertyName = propertyName;
		this.step = step;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Step getStep() {
		return step;
	}

	public String toSqlString(Criteria criteria, int position,
			CriteriaQuery criteriaQuery) throws HibernateException {
		final String functionFragment = getFunction(criteriaQuery).render(
				StringType.INSTANCE,
				buildFunctionParameterList(criteria, criteriaQuery),
				criteriaQuery.getFactory());
		return functionFragment + " as y" + position + '_';
	}

	/**
	 * Copied from AggregateProjection
	 */
	public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery)
			throws HibernateException {
		return new Type[] { getFunction(criteriaQuery).getReturnType(
				StringType.INSTANCE, criteriaQuery.getFactory()) };
	}

	/**
	 * Copied from AggregateProjection
	 */
	protected SQLFunction getFunction(CriteriaQuery criteriaQuery) {
		return getFunction(getFunctionName(), criteriaQuery);
	}

	/**
	 * Copied from AggregateProjection
	 */
	protected SQLFunction getFunction(String functionName,
			CriteriaQuery criteriaQuery) {
		SQLFunction function = criteriaQuery.getFactory()
				.getSqlFunctionRegistry().findSQLFunction(functionName);
		if (function == null) {
			throw new HibernateException(
					"Unable to locate mapping for function named ["
							+ functionName + "]");
		}
		return function;
	}

	private List<String> buildFunctionParameterList(Criteria criteria,
			CriteriaQuery criteriaQuery) {
		Dialect dialect = criteriaQuery.getFactory().getDialect();
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
				propertyName);

		if (columns.length != 1) {
			logger.error(
					"XMLQUERY may only be used with single-column properties [property: {}]",
					propertyName);
			throw new HibernateException(
					"XMLQUERY may only be used with single-column properties");
		}

		BaseExpression be = step.getRelativePath().getBaseExpression();

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

		for (Expr expr : step.getRelativePath().getSteps()) {
			if (expr instanceof Step) {
				if (((Step) expr).getDepth() >= step.getDepth()) {
					if (expr instanceof StepExpr) {
						xpath = String.format("%s/%s", xpath,
								expr.getXPath(true));
					}
					if (expr instanceof IrresoluteExpr) {
						xpath = String.format(
								"%s/%s",
								xpath,
								step.getRelativePath().getRawXPath(
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
		for (String namespace : be.getNamespaces())
			xpath = namespace.concat(xpath);

		/**
		 * Concatenate default element namespace declaration
		 */
		if (be.getDefaultElementNamespace() != null)
			xpath = be.getDefaultElementNamespace().concat(xpath);

		/**
		 * Dialect controlls
		 */
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
		 * Return the XMLQuery predicate
		 */
		String[] results = { String.format(
				"XMLCAST(XMLQUERY(\'%s\' %s) AS VARCHAR(%d))", xpath,
				passingClause, varcharLimit) };
		return Arrays.asList(results);
	}
}
