package com.klistret.cmdb.utility.jaxb;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.xml.sax.ErrorHandler;

import com.sun.tools.xjc.Plugin;
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
public class XmlNSMapPlugin extends Plugin {

	@Override
	public String getOptionName() {
		return "XxmlNSMap";
	}

	@Override
	public String getUsage() {
		return ""
				+ "-XxmlNSMap:  adds resteasy XmlNsMap annotations for element/relation pojo ";
	}

	public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
				false);

		provider.addIncludeFilter(new AssignableTypeFilter(
				com.klistret.cmdb.Element.class));

		try {
			Set<BeanDefinition> elementBeans = provider
					.findCandidateComponents(com.klistret.cmdb.Element.class
							.getPackage().getName());

			int size = elementBeans.size();
		} catch (Exception e) {
			String message = e.getMessage();
		}

		provider.resetFilters(true);

		provider.addIncludeFilter(new AssignableTypeFilter(
				com.klistret.cmdb.Relation.class));
		Set<BeanDefinition> relationBeans = provider
				.findCandidateComponents("com/klistret/cmdb");

		for (ClassOutline co : model.getClasses()) {

		}

		return true;
	}
}
