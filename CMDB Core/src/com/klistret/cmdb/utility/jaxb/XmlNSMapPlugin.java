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

	final static String elementClassName = "com.klistret.cmdb.Element";

	final static String relationClassName = "com.klistret.cmdb.Relation";

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
		try {
			ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
					false);

			Class<?> elementClass = Class.forName(elementClassName);
			Class<?> relationClass = Class.forName(relationClassName);

			provider.addIncludeFilter(new AssignableTypeFilter(elementClass));

			Set<BeanDefinition> elementBeans = provider
					.findCandidateComponents(elementClass.getPackage()
							.getName());

			provider.resetFilters(true);

			provider.addIncludeFilter(new AssignableTypeFilter(relationClass));
			Set<BeanDefinition> relationBeans = provider
					.findCandidateComponents(relationClass.getPackage()
							.getName());

			for (ClassOutline co : model.getClasses()) {

			}
		} catch (ClassNotFoundException e) {
			return false;
		}

		return true;
	}
}
