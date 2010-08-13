package com.klistret.cmdb.utility.xjc.ci;

import java.util.Collections;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class PluginImpl extends Plugin {

	/**
	 * Customization namespace URI.
	 */
	public static final String NS = "http://www.klistret.com/xjc/plugin/ci";

	@Override
	public String getOptionName() {
		return "Xci";
	}
	
	public List<String> getCustomizationURIs() {
        return Collections.singletonList(NS);
    }

    public boolean isCustomizationTagName(String nsUri, String localName) {
        return nsUri.equals(NS) && localName.matches("proxy|element|relation");
    }

	@Override
	public String getUsage() {
		return "  -Xci      :  inject specified CI directives as annotations into the generated code";
	}

	@Override
	public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
			throws SAXException {
		for( ClassOutline co : model.getClasses() ) {
			CPluginCustomization ciProxy = co.target.getCustomizations().find(NS,"proxy");
			if (ciProxy != null) {
				ciProxy.markAsAcknowledged();
			}
			
			CPluginCustomization ciElement = co.target.getCustomizations().find(NS,"element");
			if (ciElement != null) {
				ciElement.markAsAcknowledged();
			}
			
			CPluginCustomization ciRelation = co.target.getCustomizations().find(NS,"relation");
			if (ciRelation != null) {
				ciRelation.markAsAcknowledged();
			}
		}
		
		return true;
	}

}
