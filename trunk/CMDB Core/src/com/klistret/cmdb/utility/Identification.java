package com.klistret.cmdb.utility;

import java.io.IOException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.utility.xmlbeans.Expression;
import com.klistret.cmdb.xmlbeans.IdentificationDocument;

public class Identification {

	private static final Logger logger = LoggerFactory
			.getLogger(Identification.class);

	protected static Identification sInstance;

	protected Identification(IdentificationDocument identification) {

	}

	public synchronized static Identification parse(URL url) {
		if (sInstance == null) {
			IdentificationDocument document;
			try {
				document = (IdentificationDocument) XmlObject.Factory
						.parse(url);

				sInstance = new Identification(document);
			} catch (XmlException e) {
				logger.error("URL [{}] failed parsing; {}", url, e);
				throw new InfrastructureException(e.getMessage());
			} catch (IOException e) {
				logger.error("URL [{}] failed parsing: {}", url, e);
				throw new InfrastructureException(e.getMessage());
			}
		}

		return sInstance;
	}

	public Expression[] getIdentificationExpressions(XmlObject xmlObject) {
		return null;
	}
}
