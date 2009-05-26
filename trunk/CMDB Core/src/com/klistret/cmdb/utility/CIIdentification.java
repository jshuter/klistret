package com.klistret.cmdb.utility;

import java.io.IOException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.xmlbeans.IdentificationDocument;
import com.klistret.cmdb.utility.xmlbeans.Expression;

public class CIIdentification {

	private static final Logger logger = LoggerFactory
			.getLogger(CIIdentification.class);

	private IdentificationDocument identificationDocument;

	public CIIdentification(URL url) {
		try {
			identificationDocument = IdentificationDocument.Factory.parse(url);
		} catch (XmlException e) {
			logger.error("Error parsing identification document {}: {}", url
					.toString(), e);
			throw new InfrastructureException(e);
		} catch (IOException e) {
			logger.error("Error parsing identification document {}: {}", url
					.toString(), e);
			throw new InfrastructureException(e);
		}
	}

	public Expression getPrimaryExpression(XmlObject xmlObject) {
		return null;
	}

	public Expression[] getExpressions(XmlObject xmlObject) {
		return null;
	}
}
