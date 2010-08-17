package com.klistret.cmdb.utility.xjc.ci;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Reference", propOrder = { "namespace", "localName" })
@XmlSeeAlso( { SourceReference.class })
public abstract class Reference {

	@XmlAttribute(name = "Namespace", required = true)
    protected String namespace;
	
	@XmlAttribute(name = "LocalName", required = true)
    protected String localName;
    
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String value) {
        this.namespace = value;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String value) {
        this.localName = value;
    }
}
