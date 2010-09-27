package test.com.klistret.cmdb.utility.jaxb;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "choice", propOrder = { "address", "floater" })
public class Choice {

	protected String address;
	protected Float floater;

	public String getAddress() {
		return address;
	}

	public void setAddress(String value) {
		this.address = value;
	}

	public Float getFloater() {
		return floater;
	}

	public void setFloater(Float value) {
		this.floater = value;
	}

}
