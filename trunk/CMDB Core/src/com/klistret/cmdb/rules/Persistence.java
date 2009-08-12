package com.klistret.cmdb.rules;

import org.apache.xmlbeans.XmlObject;

import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;

/**
 * Interface exists solely to allow for AOP proxy [see
 * http://blog.xebia.com/2006/08/18/the-problem-with-proxy-based-aop-frameworks]
 * 
 * Methods swallowing XmlObjects aren't great candidates for caching. Better to
 * cache the persistence rules for the underlying schema type instead and apply
 * them to the XmlObject for validation. Again, that is why this class got
 * broken into a handler for getting property criteria and a cached variation
 * dealing with schema types. Bad design but it will have to live until a better
 * proxy implementation can be integrated.
 * 
 * @author Matthew Young
 * 
 */
public interface Persistence {
	com.klistret.cmdb.pojo.PropertyCriteria getPropertyCriteria(
			XmlObject xmlObject);

	PropertyExpression[] getPropertyExpressionCriterion(XmlObject xmlObject);
}
