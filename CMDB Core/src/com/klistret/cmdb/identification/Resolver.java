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
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.klistret.cmdb.utility.xmlbeans.PropertyExpression;
import com.klistret.cmdb.xmlbeans.IdentificationDocument;
import com.klistret.cmdb.exception.ApplicationException;
import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.xmlbeans.Binding;
import com.klistret.cmdb.xmlbeans.Criteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resolver {
	private static final Logger logger = LoggerFactory
			.getLogger(Resolver.class);

	private Hashtable<String, Builder> cache = new Hashtable<String, Builder>();

	private IdentificationDocument identification;

	private static int baseTypeDepth = 5;

	public Resolver(IdentificationDocument identification) {
		this.identification = identification;
	}

	public Resolver(URL url) {
		try {
			this.identification = (IdentificationDocument) XmlObject.Factory
					.parse(url);
		} catch (XmlException e) {
			logger.error("URL [{}] failed parsing; {}", url, e);
			throw new InfrastructureException(e.getMessage());
		} catch (IOException e) {
			logger.error("URL [{}] failed parsing: {}", url, e);
			throw new InfrastructureException(e.getMessage());
		}
	}

	private ArrayList<BaseType> getBaseTypes(SchemaType schemaType) {
		ArrayList<BaseType> baseTypes = new ArrayList<BaseType>(baseTypeDepth);

		Integer origin = 0;
		baseTypes.add(new BaseType(schemaType, origin++));

		while (schemaType.getBaseType() != null
				&& !schemaType.getBaseType().isBuiltinType()) {
			schemaType = schemaType.getBaseType();
			baseTypes.add(new BaseType(schemaType, origin++));
		}

		return baseTypes;
	}

	public Builder getIdentificationBuilder(XmlObject xmlObject) {
		if (xmlObject.schemaType().isAbstract())
			throw new ApplicationException(
					String
							.format(
									"identification builders only applicable to non-abstract [%s]",
									xmlObject.schemaType().getFullJavaName()));

		Builder builder = cache.get(xmlObject.schemaType().getFullJavaName());

		if (builder == null) {
			logger
					.debug(
							"initializing IdentifcationBuilder for XmlObject class [{}]",
							xmlObject.schemaType().getFullJavaName());
			builder = new Builder();

			for (BaseType baseType : getBaseTypes(xmlObject.schemaType())) {
				// select bindings after java class name if not excluded

				// note: if saxon jars are in classpath then any
				// xpath on attributes fails
				String bindingsExpression = String
						.format(
								"declare namespace cmdb=\'http://www.klistret.com/cmdb\'; $this/cmdb:Identification/cmdb:Binding[@Class=\'%s\' and not(cmdb:Exclusion = \'%s\')]",
								baseType.getSchemaType().getFullJavaName(),
								xmlObject.schemaType().getFullJavaName());
				logger.debug("xpath selection (bindings): {}",
						bindingsExpression);
				XmlObject[] bindings = identification
						.selectPath(bindingsExpression);

				// unable to cast immediately since the selection may be empty
				// which results in a ClassCaseException
				if (bindings.length == 0) {
					logger.debug("no binding selected for base type [{}]",
							baseType);
					continue;
				}

				// loop through bindings
				for (Binding binding : (Binding[]) bindings) {
					// select criteria for each base type
					String criterionsExpression = String
							.format(
									"declare namespace cmdb=\'http://www.klistret.com/cmdb\'; $this/cmdb:Identification/cmdb:Criteria[@Name=\'%s\']",
									binding.getCriteria());
					logger.debug("xpath selection (criterions): {}",
							criterionsExpression);
					XmlObject[] criterions = identification
							.selectPath(criterionsExpression);

					if (criterions.length == 0) {
						throw new ApplicationException(String.format(
								"identification criteria [%s] does not exist",
								binding.getCriteria()));
					}

					if (criterions.length > 1)
						throw new ApplicationException(
								String
										.format(
												"identification criteria [%s] is not unique [%d instances]",
												binding.getCriteria(),
												criterions.length - 1));

					// initialize xpath builders
					PropertyExpression[] expressions = getPropertyExpressions(
							baseType.getSchemaType(),
							((Criteria) criterions[0]).getPropertyComposite()
									.getPathArray());

					// add builder, priority, origin
					// note: must check if priority is set otherwise
					// 0 is passed
					builder.addIdentifcation(expressions, baseType.getOrigin(),
							binding.isSetPriority() ? binding.getPriority()
									: null);
				}
			}

			cache.put(xmlObject.schemaType().getFullJavaName(), builder);
		}

		return builder;
	}

	private PropertyExpression[] getPropertyExpressions(SchemaType schemaType,
			String[] paths) {
		PropertyExpression[] expressions = new PropertyExpression[paths.length];

		int index = 0;
		for (String path : paths) {
			expressions[index++] = new PropertyExpression(schemaType, path);
		}
		return expressions;
	}

	public class BaseType {
		private SchemaType schemaType;

		private Integer origin;

		public BaseType(SchemaType schemaType, Integer origin) {
			this.schemaType = schemaType;
			this.origin = origin;
		}

		public SchemaType getSchemaType() {
			return this.schemaType;
		}

		public void setSchemaType(SchemaType schemaType) {
			this.schemaType = schemaType;
		}

		public Integer getOrigin() {
			return this.origin;
		}

		public void setOrigin(Integer origin) {
			this.origin = origin;
		}

		public String toString() {
			return String.format("class [%s], origin [%d]", schemaType
					.getFullJavaName(), origin);
		}
	}

}
