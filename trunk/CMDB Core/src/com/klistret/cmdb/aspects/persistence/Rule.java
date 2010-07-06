package com.klistret.cmdb.aspects.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rule", propOrder = { "exclusions", "classname", "criterion" })
public class Rule {

	@XmlElement(name = "Exclusions", namespace = "http://www.klistret.com/cmdb/aspects/persistence")
    protected List<String> exclusions;
    @XmlElement(name = "Classname", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
    protected String classname;
    @XmlElement(name = "Criterion", namespace = "http://www.klistret.com/cmdb/aspects/persistence", required = true)
    protected String criterion;
    @XmlAttribute(name = "Order")
    protected Integer order;

    /**
     * Gets the value of the exclusions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exclusions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExclusions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getExclusions() {
        if (exclusions == null) {
            exclusions = new ArrayList<String>();
        }
        return this.exclusions;
    }

    /**
     * Gets the value of the qName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Sets the value of the qName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    /**
     * Gets the value of the criterion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCriterion() {
        return criterion;
    }

    /**
     * Sets the value of the criterion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCriterion(String value) {
        this.criterion = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrder(Integer value) {
        this.order = value;
    }

    /**
     * Sets the value of the exclusions property.
     * 
     * @param exclusions
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }
}
