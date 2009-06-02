package com.klistret.cmdb.utility;

import java.io.IOException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.xmlbeans.IdentificationDocument;
import com.klistret.cmdb.utility.xmlbeans.Expression;
import com.klistret.cmdb.utility.xmlbeans.SchemaTypeInspection;

public class Identification {

	private static final Logger logger = LoggerFactory
			.getLogger(Identification.class);

	private IdentificationDocument identificationDocument;

	private SchemaTypeInspection schemaTypeInspection;

	public Identification(String resource) {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		try {
			URL url = resolver.getResource(resource).getURL();

			identificationDocument = IdentificationDocument.Factory.parse(url);
		} catch (XmlException e) {
			logger.error(
					"Error parsing identification document {}: resource {}",
					resource, e);
			throw new InfrastructureException(e);
		} catch (IOException e) {
			logger.error(
					"Error parsing identification document {}: resource {}",
					resource, e);
			throw new InfrastructureException(e);
		}
	}

	public void setSchemaTypeInspection(
			SchemaTypeInspection schemaTypeInspection) {
		this.schemaTypeInspection = schemaTypeInspection;
	}

	public SchemaTypeInspection getSchemaTypeInspection() {
		return this.schemaTypeInspection;
	}

	public Expression getPrimaryExpression(XmlObject xmlObject) {
		return null;
	}

	public Expression[] getExpressions(XmlObject xmlObject) {
		return null;
	}
}
