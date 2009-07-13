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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.xmlbeans.PersistenceIdentificationRulesDocument;

public class PersistenceIdentificationRules {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistenceIdentificationRules.class);

	private PersistenceIdentificationRulesDocument xmlObjectDocument;

	public PersistenceIdentificationRules(URL url) {
		try {
			this.xmlObjectDocument = (PersistenceIdentificationRulesDocument) XmlObject.Factory
					.parse(url);

		} catch (XmlException e) {
			logger.error("URL [{}] failed parsing; {}", url, e);
			throw new InfrastructureException(e.getMessage());
		} catch (IOException e) {
			logger.error("URL [{}] failed parsing: {}", url, e);
			throw new InfrastructureException(e.getMessage());
		}
	}

	public PropertyExpression[] getPrimaryIdentificationPropertyExpressions(
			XmlObject xmlObject) {
		return getIdentificationPropertyExpressions(xmlObject, 0);
	}

	public PropertyExpression[] getIdentificationPropertyExpressions(
			XmlObject xmlObject, int priority) {
		return null;
	}
}
