package com.klistret.cmdb.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersistenceRules", propOrder = {
    "criterion",
    "rule"
})
@XmlRootElement(name = "PersistenceRules")
public class PersistenceRules {

	@XmlElement(name = "Criterion", required = true)
    protected List<Criterion> criterion;
    @XmlElement(name = "Rule", required = true)
    protected List<Rule> rule;

    /**
     * Gets the value of the criterion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the criterion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCriterion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Criterion }
     * 
     * 
     */
    public List<Criterion> getCriterion() {
        if (criterion == null) {
            criterion = new ArrayList<Criterion>();
        }
        return this.criterion;
    }

    /**
     * Gets the value of the rule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Rule }
     * 
     * 
     */
    public List<Rule> getRule() {
        if (rule == null) {
            rule = new ArrayList<Rule>();
        }
        return this.rule;
    }

    /**
     * Sets the value of the criterion property.
     * 
     * @param criterion
     *     allowed object is
     *     {@link Criterion }
     *     
     */
    public void setCriterion(List<Criterion> criterion) {
        this.criterion = criterion;
    }

    /**
     * Sets the value of the rule property.
     * 
     * @param rule
     *     allowed object is
     *     {@link Rule }
     *     
     */
    public void setRule(List<Rule> rule) {
        this.rule = rule;
    }
}
