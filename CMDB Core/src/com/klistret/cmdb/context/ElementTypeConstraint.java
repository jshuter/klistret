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
package com.klistret.cmdb.context;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.ci.pojo.ElementType;
import com.klistret.cmdb.service.ElementTypeService;

import com.klistret.cmdb.utility.jaxb.CIBean;
import com.klistret.cmdb.utility.jaxb.CIContext;

public class ElementTypeConstraint {
	private static final Logger logger = LoggerFactory
			.getLogger(ElementTypeConstraint.class);

	ElementTypeService elementTypeService;

	public ElementTypeService getElementTypeService() {
		return elementTypeService;
	}

	public void setElementTypeService(ElementTypeService elementTypeService) {
		this.elementTypeService = elementTypeService;
	}

	@PostConstruct
	public void execute() {
		QName elementQName = new QName(
				"http://www.klistret.com/cmdb/ci/commons", "Element");

		CIContext ciContext = CIContext.getCIContext();
		Set<CIBean> beans = ciContext.getBeans();

		List<ElementType> results = elementTypeService.find("%");
		logger.debug("Constraint on element types [currently {}].",
				results.size());

		for (CIBean bean : beans) {
			if (bean.isAncestor(elementQName) && bean.isAbstraction() == false) {
				boolean persistant = false;
				for (ElementType elementType : results) {
					if (bean.getType().toString().equals(elementType.getName())
							&& elementType.getToTimeStamp() == null) {
						persistant = true;
						break;
					}
				}

				if (persistant)
					logger.debug(
							"Element type [namespace: {}, localPart: {}] is persisted",
							bean.getType().getNamespaceURI(), bean.getType()
									.getLocalPart());
				else {
					ElementType other = new ElementType();
					other.setName(String.format("{%s}%s", bean.getType()
							.getNamespaceURI(), bean.getType().getLocalPart()));
					other.setCreateTimeStamp(new Date());
					other.setFromTimeStamp(new Date());

					elementTypeService.create(other);
					logger.info(
							"Added element type [namespace: {}, localPart: {}] definition to database",
							bean.getType().getNamespaceURI(), bean.getType()
									.getLocalPart());
				}
			}
		}
	}
}
