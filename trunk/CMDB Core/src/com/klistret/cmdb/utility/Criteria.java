package com.klistret.cmdb.utility;

import org.hibernate.Session;

public interface Criteria {

	public org.hibernate.Criteria getCriteria(Session session);
	
	public Integer getMaxResults();
	
	public Integer getFirstResult();
	
	// get,set QName which equates to the type
	
	// property paths
	
	// relational paths (initially only with single ID)
}
