package com.klistret.cmdb.utility.xjc.ci;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = Const.LN_Relation, propOrder = {
    "connection"
})
@XmlRootElement(name = Const.LN_Relation, namespace = Const.NS)
public class Relation {

	@XmlElement(name = Const.LN_Connection, namespace = Const.NS)
	protected List<Connection> connection;
	
	public List<Connection> getConnection() {
		if (connection == null) {
			connection = new ArrayList<Connection>();
		}
		return connection;
	}
	
	public void setConnection(List<Connection> connection) {
        this.connection = connection;
    }
}
