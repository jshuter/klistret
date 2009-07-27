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

package com.klistret.cmdb.identification;

import java.io.IOException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.Criteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.pojo.Element;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.xmlbeans.PersistenceRulesDocument;

public class PersistenceRules {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceRules.class);

	private PersistenceRulesDocument rulesDocument;

	public PersistenceRules(URL url) {
		try {
			this.rulesDocument = (PersistenceRulesDocument) XmlObject.Factory
					.parse(url);
		} catch (XmlException e) {
			logger.error("URL [{}] failed parsing; {}", url, e);
			throw new InfrastructureException(e.getMessage());
		} catch (IOException e) {
			logger.error("URL [{}] failed parsing: {}", url, e);
			throw new InfrastructureException(e.getMessage());
		}
	}

	public Criteria getCriteria(Element element) {
		//PropertyExpression[] expressions = getPropertyExpressions(element);

		// construct criteria based on expression array
		return null;
	}

	private PropertyExpression[] getPropertyExpressions(Element element) {
		String baseTypes = "";

		String xquery = String
				.format(
						"declare namespace cmdb=\'http://www.klistret.com/cmdb\'; $this/cmdb:PersistenceRules/cmdb:Binding[matches(@Type, '%s\') and not(cmdb:ExclusionType = \'%s\')]",
						baseTypes, element.getType().getName());
		logger.debug("xquery (bindings): {}", xquery);

		XmlObject[] bindings = rulesDocument.selectPath(xquery);

		return null;
	}
}
