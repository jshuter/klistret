package com.klistret.cmdb.utility.xjc.ci;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Connection", propOrder = {
    "source",
    "target"
})
public class Connection {

	@XmlElement(name = Const.LN_Source, namespace = Const.NS)
	protected SourceReference source;
	
	@XmlElement(name = Const.LN_Target, namespace = Const.NS)
	protected TargetReference target;

	public SourceReference getSource() {
		return source;
	}
	
	public void setSource(SourceReference value) {
        this.source = value;
    }
	
	public TargetReference getTarget() {
		return target;
	}
	
	public void setTarget(TargetReference value) {
        this.target = value;
    }
}
