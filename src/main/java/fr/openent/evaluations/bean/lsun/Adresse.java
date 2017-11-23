//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.07.06 at 05:11:16 PM CEST 
//


package fr.openent.evaluations.bean.lsun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Adresse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Adresse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ligne1" use="required" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine50" />
 *       &lt;attribute name="ligne2" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine50" />
 *       &lt;attribute name="ligne3" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine50" />
 *       &lt;attribute name="ligne4" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine50" />
 *       &lt;attribute name="code-postal" use="required" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine10" />
 *       &lt;attribute name="commune" use="required" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine100" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Adresse")
public class Adresse {

    @XmlAttribute(name = "ligne1", required = true)
    protected String ligne1;
    @XmlAttribute(name = "ligne2")
    protected String ligne2;
    @XmlAttribute(name = "ligne3")
    protected String ligne3;
    @XmlAttribute(name = "ligne4")
    protected String ligne4;
    @XmlAttribute(name = "code-postal", required = true)
    protected String codePostal;
    @XmlAttribute(name = "commune", required = true)
    protected String commune;

    public Adresse(){}
    public Adresse(String ligne1,String codePostal, String commune){
        this.ligne1=ligne1;
        this.codePostal=codePostal;
        this.commune=commune;
    }
    /**
     * Gets the value of the ligne1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLigne1() {
        return ligne1;
    }

    /**
     * Sets the value of the ligne1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLigne1(String value) {
        this.ligne1 = value;
    }

    /**
     * Gets the value of the ligne2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLigne2() {
        return ligne2;
    }

    /**
     * Sets the value of the ligne2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLigne2(String value) {
        this.ligne2 = value;
    }

    /**
     * Gets the value of the ligne3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLigne3() {
        return ligne3;
    }

    /**
     * Sets the value of the ligne3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLigne3(String value) {
        this.ligne3 = value;
    }

    /**
     * Gets the value of the ligne4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLigne4() {
        return ligne4;
    }

    /**
     * Sets the value of the ligne4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLigne4(String value) {
        this.ligne4 = value;
    }

    /**
     * Gets the value of the codePostal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodePostal() {
        return codePostal;
    }

    /**
     * Sets the value of the codePostal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodePostal(String value) {
       // if(value!=null) {
            this.codePostal = value;
       /* }else{
            this.codePostal = "00000";
        }*/
    }

    /**
     * Gets the value of the commune property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommune() {
        return commune;
    }

    /**
     * Sets the value of the commune property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommune(String value) {
        this.commune = value;
    }

}