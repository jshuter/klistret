package com.klistret.cmdb.utility.hibernate;

import javax.xml.namespace.QName;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;

import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.pojo.XMLBean;
import com.klistret.cmdb.utility.jaxb.JAXBContextHelper;
import com.klistret.cmdb.utility.saxon.AndExpr;
import com.klistret.cmdb.utility.saxon.ComparisonExpr;
import com.klistret.cmdb.utility.saxon.Expr;
import com.klistret.cmdb.utility.saxon.IrresoluteExpr;
import com.klistret.cmdb.utility.saxon.LiteralExpr;
import com.klistret.cmdb.utility.saxon.OrExpr;
import com.klistret.cmdb.utility.saxon.PathExpression;
import com.klistret.cmdb.utility.saxon.StepExpr;

public class XPathCriteria {

	public static Criteria getCriteria(String[] xpaths,
			JAXBContextHelper jaxbContextHelper, Session session) {

		PathExpression[] expressions = new PathExpression[xpaths.length];

		/**
		 * validate xpath expressions
		 */
		QName container = null;
		for (int index = 0; index < xpaths.length; index++) {
			PathExpression expression = new PathExpression(xpaths[index]);
			expressions[index] = expression;

			if (!expression.hasRoot())
				throw new ApplicationException(String.format(
						"XPath [%s] does not have a root expression",
						expression.getXPath()));

			QName qname = expression.getQName(1);
			if (qname == null)
				throw new ApplicationException(String.format(
						"Containg QName not defined for xpath [%s]", expression
								.getXPath()));

			if (container != null && !container.equals(qname))
				throw new ApplicationException(
						String
								.format(
										"Leading QName [%s] not unique across xpath statements [%s]",
										qname, xpaths));

			container = qname;
		}

		/**
		 * construct hibernate criteria based on the root step
		 */
		XMLBean xmlBean = jaxbContextHelper.getXMLBean(container);
		ClassMetadata hClassMetadata = session.getSessionFactory()
				.getClassMetadata(xmlBean.getName().getLocalPart());

		if (hClassMetadata == null)
			throw new ApplicationException(String.format(
					"Hibernate class does not exist for qname [%s]", container));

		Criteria criteria = session.createCriteria(hClassMetadata
				.getEntityName());

		/**
		 * piece together criteria from each expression
		 */
		for (PathExpression expression : expressions)
			transform(expression, jaxbContextHelper, session, criteria);

		return criteria;
	}

	private static void transform(PathExpression expression,
			JAXBContextHelper jaxbContextHelper, Session session,
			Criteria criteria) {
		// ignore root
		for (int index = 1; index < expression.getRelativePath().size(); index++) {
			Expr expr = expression.getRelativePath().get(index);

			// ignore containing step except for the predicate
			if (index == 1 && expr.getType().equals(Expr.Type.Step)
					&& ((StepExpr) expr).hasPredicate())
				expr = ((StepExpr) expr).getPredicate();

		}
	}

	private Criterion explain(Stage stage) {
		Expr expr = stage.getExpr();

		switch (expr.getType()) {
		case Step:
			return explain((StepExpr) expr);
		case Or:
			return explain((OrExpr) expr);
		case And:
			return explain((AndExpr) expr);
		case Comparison:
			explain((ComparisonExpr) expr);
		case Literal:
			explain((LiteralExpr) expr);
		case Irresolute:
			explain((IrresoluteExpr) expr);
		default:
			throw new ApplicationException(String.format(
					"Unknown expression type [%s]", expr.getType()));
		}
	}

	private Criterion explain(OrExpr expr) {
		if (expr.getOperands().size() != 2)
			throw new ApplicationException(String.format(
					"Or expression [%s] must have only 2 operands", expr));

		return Restrictions.or(explain(expr.getOperands().get(0)), explain(expr
				.getOperands().get(1)));
	}

	private Criterion explain(AndExpr expr) {
		if (expr.getOperands().size() != 2)
			throw new ApplicationException(String.format(
					"And expression [%s] must have only 2 operands", expr));

		return Restrictions.and(explain(expr.getOperands().get(0)),
				explain(expr.getOperands().get(1)));
	}

	private Criterion explain(StepExpr expr) {
		return null;
	}

	private Criterion explain(ComparisonExpr expr) {
		if (expr.getOperands().size() != 1)
			throw new ApplicationException(
					String
							.format(
									"Comparison expression [%s] must have only 1 operand",
									expr));

		Expr operand = expr.getOperands().get(0);

		switch (expr.getOperator()) {
		case ValueEquals:
			return null;
		case Matches:
			return Restrictions.ilike("", "");
		default:
			throw new ApplicationException(String.format(
					"Unknown operator type [%s]", expr.getOperator()));
		}
	}

	private Criterion explain(LiteralExpr expr) {
		return null;
	}

	private Criterion explain(IrresoluteExpr expr) {
		return null;
	}

}
