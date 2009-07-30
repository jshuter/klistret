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

package com.klistret.cmdb.utility.xmlbeans;

public interface Expression {

	/**
	 * A VariableReference evaluates to the value to which the variable name is
	 * bound in the set of variable bindings in the context. Only a single
	 * reference is currently allowed.
	 * 
	 * @return VariableReference
	 */
	public String getVariableReference();

	/**
	 * Change the variable name (single context until more than property
	 * expressions are handled)
	 * 
	 * @param variableReference
	 */
	public void setVariableReference(String variableReference);

	/**
	 * This is a namespace URI or "none". The namespace URI, if present, is used
	 * for any unprefixed QName appearing in a position where an element or type
	 * name is expected.
	 * 
	 * @return
	 */
	public String getDefaultElementNamespace();

	public void setDefaultElementNamespace(String defaultElementNamespace);

	/**
	 * This is a namespace URI or "none". The namespace URI, if present, is used
	 * for any unprefixed QName appearing in a position where a function name is
	 * expected.
	 * 
	 * @return
	 */
	public String getDefaultFunctionNamespace();

	public void setDefaultFunctionNamespace(String defaultFunctionNamespace);

	/**
	 * Returns XPath with fn:matches(selecting node,value)
	 * 
	 * @param value
	 * @return XPath
	 */
	public String matches(String value);

	/**
	 * Returns XPath with fn:contains(selecting node,value)
	 * 
	 * @param value
	 * @return XPath
	 */
	public String contains(String value);

	/**
	 * Returns XPath with fn:starts-with(selecting node,value)
	 * 
	 * @param value
	 * @return XPath
	 */
	public String startsWith(String value);

	/**
	 * Returns XPath with fn:ends-with(selecting node,value)
	 * 
	 * @param value
	 * @return XPath
	 */
	public String endsWith(String value);

	/**
	 * Returns XPath with equal operator on selecting node
	 * 
	 * @param value
	 * @return XPath
	 */
	public String equal(String value);

	/**
	 * Returns XPath with not equal operator on selecting node
	 * 
	 * @param value
	 * @return XPath
	 */
	public String notEqual(String value);

	/**
	 * Returns XPath with less than operator on selecting node
	 * 
	 * @param value
	 * @return XPath
	 */
	public String lessThan(String value);

	/**
	 * Returns XPath with less than or equal operator on selecting node
	 * 
	 * @param value
	 * @return XPath
	 */
	public String lessThanOrEqual(String value);

	/**
	 * Returns XPath with greater than operator on selecting node
	 * 
	 * @param value
	 * @return XPath
	 */
	public String greaterThan(String value);

	/**
	 * Returns XPath with greater than or equal operator on selecting node
	 * 
	 * @param value
	 * @return XPath
	 */
	public String greaterThanOrEqual(String value);
}
