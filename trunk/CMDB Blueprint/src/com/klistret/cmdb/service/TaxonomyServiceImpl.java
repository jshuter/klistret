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
package com.klistret.cmdb.service;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klistret.cmdb.exception.InfrastructureException;
import com.klistret.cmdb.taxonomy.pojo.Blueprint;
import com.klistret.cmdb.taxonomy.pojo.Element;
import com.klistret.cmdb.taxonomy.pojo.Relation;
import com.klistret.cmdb.taxonomy.pojo.Property;
import com.klistret.cmdb.taxonomy.pojo.Taxonomy;
import com.klistret.cmdb.taxonomy.pojo.Granularity;
import com.klistret.cmdb.utility.jaxb.CIContext;
import com.klistret.cmdb.utility.saxon.PathExpression;

/**
 * 
 * @author Matthew Young
 *
 */
public class TaxonomyServiceImpl implements TaxonomyService {

	private static final Logger logger = LoggerFactory
			.getLogger(TaxonomyServiceImpl.class);

	/**
	 * Dependency injection
	 */
	protected URL url;

	/**
	 * JAXB Context
	 */
	private JAXBContext jaxbContext;

	/**
	 * Blueprint bean for taxonomy
	 */
	private Blueprint blueprint;

	/**
	 * Internal cache with taxonomy/granularity keys for a list of element
	 * entities.
	 */
	private Map<String, List<Element>> cache = new HashMap<String, List<Element>>();

	/**
	 * Set URL then roll data into blueprint bean for taxonomy
	 * 
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
		this.cache.clear();

		try {
			jaxbContext = JAXBContext.newInstance(Blueprint.class,
					Element.class);

			Unmarshaller um = jaxbContext.createUnmarshaller();
			blueprint = (Blueprint) um.unmarshal(url);
		} catch (JAXBException e) {
			logger.error(
					"Unable to unmarshal URL [{}] into blueprint bean for taxonomy",
					url);
			throw new InfrastructureException(
					String.format(
							"Unable to unmarshal URL [%s] into blueprint bean for taxonomy: ",
							url), e);
		}
	}

	/**
	 * Get list of taxonomy names
	 * 
	 * @return List
	 */
	public List<String> getTaxonomies() {
		List<String> names = new ArrayList<String>(blueprint.getTaxonomy()
				.size());

		for (Taxonomy taxonomy : blueprint.getTaxonomy())
			names.add(taxonomy.getName());

		return names;
	}

	/**
	 * Get list of granularity names for a particular taxonomy unit
	 * 
	 * @return List
	 */
	public List<String> getGranularities(String taxonomyName) {
		if (taxonomyName == null)
			return null;

		for (Taxonomy taxonomy : blueprint.getTaxonomy()) {
			if (taxonomy.getName().equals(taxonomyName)) {
				List<String> names = new ArrayList<String>(taxonomy
						.getGranularity().size());

				for (Granularity granularity : taxonomy.getGranularity())
					names.add(granularity.getName());

				return names;
			}
		}

		return null;
	}

	/**
	 * Get all element entities for a given taxonomy unit and granularity
	 * including all granularity extensions. Cache is checked first. If no key
	 * is found then elements are added to the cache.
	 * 
	 * @return List
	 */
	public List<Element> getElements(String taxonomyName, String granularityName) {
		if (taxonomyName == null || granularityName == null)
			return null;

		String key = String.format("%s:%s", taxonomyName, granularityName);
		if (!cache.containsKey(key))
			cache.put(key, addElements(taxonomyName, granularityName));

		return cache.get(key);
	}

	/**
	 * Finds all elements corresponding to a taxonomy unit and granularity.
	 * 
	 * @param taxonomyName
	 * @param granularityName
	 * @return
	 */
	private List<Element> addElements(String taxonomyName,
			String granularityName) {
		if (taxonomyName == null || granularityName == null)
			return null;

		for (Taxonomy taxonomy : blueprint.getTaxonomy()) {
			if (taxonomy.getName().equals(taxonomyName)) {
				for (Granularity granularity : taxonomy.getGranularity()) {
					if (granularity.getName().equals(granularityName)) {
						List<Element> elements = new ArrayList<Element>();

						/**
						 * Clone the elements if they are valid (remove
						 * duplicate element definitions)
						 */
						for (Element element : granularity.getElement()) {
							Element other = clone(element);

							if (other != null) {
								boolean isExisting = false;
								for (Element existing : elements)
									if (existing.getType().equals(
											other.getType()))
										isExisting = true;

								if (!isExisting)
									elements.add(other);
								else
									logger.warn(
											"ELement type [{}] is defined more than once, duplicates are discarded",
											other.getType());
							}

						}

						/**
						 * Pull in elements from the extended granularity
						 * (consolidate duplicate element definitions)
						 */
						List<Element> others = addElements(taxonomyName,
								granularity.getExtension());

						if (others != null) {
							for (Element other : others) {
								boolean isExisting = false;
								for (Element existing : elements) {
									if (existing.getType().equals(
											other.getType())) {
										isExisting = true;

										for (Relation relation : other
												.getRelation())
											existing.getRelation()
													.add(relation);

										for (Property property : other
												.getProperty())
											existing.getProperty()
													.add(property);
									}
								}

								if (!isExisting)
									elements.add(other);
								else
									other = null;
							}
						}
						return elements;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Clones an element if the type is a valid bean and the child XML nodes are
	 * valid for cloning.
	 * 
	 * @param element
	 * @return
	 */
	private Element clone(Element element) {
		CIContext ciContext = CIContext.getCIContext();

		if (!ciContext.isBean(element.getType())) {
			logger.warn(
					"Element type [{}] is not a CI bean thus not applicable",
					element.getType());
			return null;
		}

		Element copyElement = new Element();
		copyElement.setType(element.getType());

		/**
		 * Add relations
		 */
		for (Relation relation : element.getRelation()) {
			Relation copyRelation = clone(relation);

			if (copyRelation != null)
				copyElement.getRelation().add(copyRelation);
		}

		/**
		 * Add properties
		 */
		for (Property property : element.getProperty()) {
			Property copyProperty = clone(property);

			if (copyProperty != null)
				copyElement.getProperty().add(copyProperty);
		}

		return copyElement;
	}

	/**
	 * Clones a relation if the relation and destination types are valid CI
	 * beans otherwise null is returned.
	 * 
	 * @param relation
	 * @return
	 */
	private Relation clone(Relation relation) {
		CIContext ciContext = CIContext.getCIContext();

		if (!ciContext.isBean(relation.getType())) {
			logger.warn(
					"Relation type [{}] is not a CI bean thus not applicable",
					relation.getType());
			return null;
		}

		if (!ciContext.isBean(relation.getDestination())) {
			logger.warn(
					"Relation destination [{}] is not a CI bean thus not applicable",
					relation.getType());
			return null;
		}

		Relation other = new Relation();
		other.setType(relation.getType());
		other.setDestination(relation.getDestination());

		return other;
	}

	/**
	 * Clones a property if the type is a valid CI bean and the expression is
	 * valid otherwise null is returned.
	 * 
	 * @param property
	 * @return
	 */
	private Property clone(Property property) {
		CIContext ciContext = CIContext.getCIContext();

		if (!ciContext.isBean(property.getType())) {
			logger.warn(
					"Property type [{}] is not a CI bean thus not applicable",
					property.getType());
			return null;
		}

		try {
			if (property.getExpression() != null)
				new PathExpression(property.getExpression());
		} catch (Exception e) {
			logger.warn(
					"Property expression [{}] is not a valid XPath expression: {}",
					property.getExpression(), e.getMessage());
			return null;
		}

		Property other = new Property();
		other.setType(property.getType());
		other.setExpression(property.getExpression());

		return other;
	}
}
