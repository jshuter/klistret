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
package com.klistret.cmdb.utility.saxon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.ApplicationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;

/**
 * A relative path expression is the basis for path expressions except it does
 * not encapsulate initial steps ( "/" or "//"). No compilation of an XPath is
 * performed in this class only the logic to recursively explain the Saxon
 * representation into a series of steps more suited to the needs of Klistret.
 * 
 * @author Matthew Young
 * 
 */
public class RelativePathExpr implements Expr {
	private static final Logger logger = LoggerFactory
			.getLogger(RelativePathExpr.class);

	/**
	 * 
	 */
	protected BaseExpression baseExpression;

	/**
	 * 
	 */
	protected boolean hasRoot = false;

	/**
	 * 
	 */
	private Expression expression;

	/**
	 * 
	 */
	private Configuration configuration;

	/**
	 * Denotes presence of irresolute steps
	 */
	protected boolean hasIrresolute = false;

	/**
	 * Project specific representation of relative paths [StepExpr (("/" | "//")
	 * StepExpr)*]
	 */
	protected List<Expr> steps = new ArrayList<Expr>();

	/**
	 * Raw strings for each step in the relative path
	 */
	private String[] xpathSplit;

	/**
	 * Constructor mimicking Expr
	 * 
	 * @param expression
	 * @param configuration
	 */
	public RelativePathExpr(Expression expression, Configuration configuration) {
		this.expression = expression;
		this.configuration = configuration;
		explain(expression);
	}

	/**
	 * Constructor with owning BaseExpression (splits the original XPath with
	 * prolog into an array of strings mirroring the compiled steps).
	 * 
	 * @param expression
	 * @param configuration
	 * @param pathExpression
	 */
	public RelativePathExpr(Expression expression, Configuration configuration,
			BaseExpression baseExpression) {
		this(expression, configuration);

		this.baseExpression = baseExpression;
		this.xpathSplit = RelativePathExpr.split(baseExpression
				.getXPathWithoutProlog());

		if (this.xpathSplit.length != getDepth())
			throw new ApplicationException(
					String.format(
							"Number of uncompiled xpath steps [%d] as strings is not equal to the number of compiled steps [%d] in the relative path",
							this.xpathSplit.length, getDepth()));
	}

	/**
	 * Return Saxon expression
	 */
	public Expression getExpression() {
		return this.expression;
	}

	/**
	 * Return Saxon configuration
	 */
	public Configuration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Returns RelativePath as expression type
	 */
	public Type getType() {
		return Type.RelativePath;
	}

	/**
	 * Generates XPath without masking literal values
	 * 
	 * @return XPath
	 */
	public String getXPath() {
		return getXPath(false);
	}

	/**
	 * Raw XPath by depth
	 * 
	 * @param start
	 * @param stop
	 * @return
	 */
	public String getRawXPath(int depth) {
		return this.xpathSplit[depth];
	}

	/**
	 * Generates XPath by concatenating the XPath for individual steps with a
	 * "/" delimiter.
	 * 
	 * @return XPath
	 */
	public String getXPath(boolean maskLiteral) {
		String xpath = null;

		for (Expr expr : steps) {
			xpath = xpath == null ? expr.getXPath(maskLiteral) : String.format(
					"%s/%s", xpath, expr.getXPath(maskLiteral));
		}
		return xpath;
	}

	/**
	 * Relative paths are a list of steps (ie Step or Irresolute) delimited by a
	 * single slash.
	 * 
	 * @return Relative path expression
	 */
	public List<Expr> getSteps() {
		return steps;
	}

	/**
	 * Get a step by depth within the relative path
	 * 
	 * @param index
	 * @return Expression
	 */
	public Expr getExpr(int depth) {
		return steps.get(depth);
	}

	/**
	 * Get last step
	 * 
	 * @return Expression
	 */
	public Expr getLastExpr() {
		return steps.size() > 0 ? steps.get(steps.size() - 1) : null;
	}

	/**
	 * Get first expression
	 * 
	 * @return
	 */
	public Expr getFirstExpr() {
		return getExpr(0);
	}

	/**
	 * Get QName for a particular step expression in the relative path (null for
	 * root or irresolute)
	 * 
	 * @param depth
	 * @return QName
	 */
	public QName getQName(int depth) {
		if (getExpr(depth).getType() == Expr.Type.Step)
			return ((StepExpr) getExpr(depth)).getQName();

		return null;
	}

	/**
	 * Existence of an irresolute path
	 * 
	 * @return
	 */
	public boolean hasIrresolute() {
		return this.hasIrresolute;
	}

	/**
	 * Is a Root expression present
	 * 
	 * @return
	 */
	public boolean hasRoot() {
		return this.hasRoot;
	}

	/**
	 * Determine if multiple root expressions
	 * 
	 * @return
	 */
	public boolean hasMultipleRoot() {
		if (this.hasRoot == false)
			return false;

		int count = 0;
		for (Expr expr : getSteps())
			if (expr.getType() == Expr.Type.Root)
				count++;

		if (count > 1)
			return true;

		return false;
	}

	/**
	 * Get number of expressions within the relative path
	 * 
	 * @return Size of the relative path
	 */
	public int getDepth() {
		return steps.size();
	}

	/**
	 * BaseExpression
	 * 
	 * @return
	 */
	public BaseExpression getBaseExpression() {
		return this.baseExpression;
	}

	/**
	 * Nearly identical logic defined in the explain methods of the individual
	 * Saxon expression class extensions
	 * 
	 * @param expression
	 */
	protected void explain(Expression expression) {
		try {
			/**
			 * Slash expressions always consist of a controlling and controlled
			 * expression that can be further explained.
			 */
			if (expression.getClass().getName()
					.equals(SlashExpression.class.getName())) {
				logger.debug("Explaining a Saxon slash expression");
				Expression controlling = ((SlashExpression) expression)
						.getControllingExpression();
				Expression controlled = ((SlashExpression) expression)
						.getControlledExpression();
				explain(controlling);
				explain(controlled);
			}

			/**
			 * Root or Root-to-descendant not allowed per definition in Relative
			 * Path but to please the recursive parsing from Saxon an exception
			 * is made here.
			 */
			else if (expression.getClass().getName()
					.equals(RootExpression.class.getName())) {
				logger.debug("Explaining a Saxon root expression");
				steps.add(new RootExpr((RootExpression) expression,
						configuration));
				hasRoot = true;
			}

			/**
			 * Axis expressions in Saxon have no predicates oddly enough and
			 * those that can't be processed into a simple step are translated
			 * into irresolute expressions
			 */
			else if (expression.getClass().getName()
					.equals(AxisExpression.class.getName())) {
				logger.debug("Explaining a Saxon axis expression");
				steps.add(new StepExpr((AxisExpression) expression,
						configuration));
			}

			/**
			 * Same as axis expressions but Saxon filters also for predicates
			 */
			else if (expression.getClass().getName()
					.equals(FilterExpression.class.getName())) {
				logger.debug("Explaining a Saxon filter expression");
				steps.add(new StepExpr((FilterExpression) expression,
						configuration));
			}

			/**
			 * Default is to capture unresolved expressions as irresolute
			 * exception
			 */
			else {
				logger.debug("Captured unresolved expression [{}]", expression);
				throw new IrresoluteException("Captured unresolved expression");
			}
		} catch (IrresoluteException e) {
			logger.debug("Captured expression as irresolute");
			steps.add(new IrresoluteExpr(expression, configuration));
			hasIrresolute = true;
		}

		/**
		 * Set step information (ie only types of root, step or irresolute)
		 * assigning this path expression, the current XPath, depth and next
		 * step.
		 */
		int depth = steps.size() - 1;
		if (depth >= 0) {
			Step step = ((Step) steps.get(depth));
			step.setRelativePath(this);

			step.setDepth(depth);
			if (depth != 0) {
				((Step) steps.get(depth - 1)).setNext(step);
				step.setPrevious((Step) steps.get(depth - 1));
			}
		}
	}

	/**
	 * Breaks down an Path expression into string representations of individual
	 * steps in the underlying relative path if it exists. Initial steps or
	 * double slashes return empty strings.
	 * 
	 * @param xpath
	 * @return
	 */
	public static String[] split(String xpath) {
		String text = xpath.trim();

		BufferedReader br = new BufferedReader(new StringReader(text));
		char[] charBuffer = new char[(int) text.length()];

		try {
			br.read(charBuffer);
			br.close();
		} catch (IOException e) {
			logger.error(
					"Unable to read XPath [{}] into a character buffer: {}",
					text, e.getMessage());
			return null;
		}

		List<String> results = new ArrayList<String>();

		StringBuilder step = new StringBuilder();
		int openBr = 0;
		for (char character : charBuffer) {
			switch (character) {
			case '/':
				if (openBr == 0) {
					results.add(step.toString());
					step = new StringBuilder();
				} else {
					step.append(character);
				}
				break;
			case '[':
				openBr++;
				step.append(character);
				break;
			case ']':
				openBr--;
				step.append(character);
				break;
			default:
				step.append(character);
			}
		}

		if (step.length() != 0)
			results.add(step.toString());

		if (openBr != 0) {
			logger.error("Predicate brackets uneven for XPath [{}]", text);
			return null;
		}

		return results.toArray(new String[0]);
	}
}
