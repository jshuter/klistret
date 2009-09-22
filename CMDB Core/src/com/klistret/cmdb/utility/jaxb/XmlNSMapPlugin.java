package com.klistret.cmdb.utility.jaxb;

import java.util.Set;

import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.xml.sax.ErrorHandler;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Page
 * http://wiki.glassfish.java.net/Wiki.jsp?page=Jaxb2CommonsUsingAndDeveloping
 * give more information about the extended plugin class
 * 
 * @author Matthew Young
 * 
 */
public class XmlNSMapPlugin extends AbstractParameterizablePlugin {

	private Class<?> elementClass;

	private Class<?> relationClass;

	@Override
	public String getOptionName() {
		return "XxmlNSMap";
	}

	@Override
	public String getUsage() {
		return "" + "-XxmlNSMap:  adds resteasy XmlNsMap annotations for "
				+ "-XxmlNSMap-elementClass:  root class for all element CIs "
				+ "-XxmlNSMap-relationClass: root class for all relation CIs "
				+ ".";
	}

	public void setElementClass(Class<?> elementClass) {
		this.elementClass = elementClass;
	}

	public Class<?> getElementClass() {
		return this.elementClass;
	}

	public void setRelationClass(Class<?> relationClass) {
		this.relationClass = relationClass;
	}

	public Class<?> getRelationClass() {
		return this.relationClass;
	}

	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false);

		provider.addIncludeFilter(new AssignableTypeFilter(this.elementClass));
		Set<BeanDefinition> elementBeans = provider.findCandidateComponents("");

		provider.resetFilters(true);

		provider.addIncludeFilter(new AssignableTypeFilter(this.relationClass));
		Set<BeanDefinition> relationBeans = provider
				.findCandidateComponents("");

		for (ClassOutline co : model.getClasses()) {

		}

		return true;
	}
}
