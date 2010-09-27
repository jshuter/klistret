package test.com.klistret.cmdb.utility.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "start", "limit", "choice" })
@XmlRootElement(name = "Sample")
public class Sample {
	@XmlElement(name = "Limit", required = true, defaultValue = "50", nillable = false)
	protected Integer limit;

	@XmlElement(name = "Start", required = true, defaultValue = "0", nillable = false)
	protected Integer start;

	@XmlElement(name = "Choice")
	protected Choice choice;

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Choice getChoice() {
		return choice;
	}

	public void setChoice(Choice choice) {
		this.choice = choice;
	}
}
