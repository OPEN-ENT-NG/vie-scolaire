//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.07.06 at 05:11:16 PM CEST 
//


package fr.openent.evaluations.bean.lsun;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CodeParcours.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CodeParcours">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PAR_AVN"/>
 *     &lt;enumeration value="PAR_CIT"/>
 *     &lt;enumeration value="PAR_ART"/>
 *     &lt;enumeration value="PAR_SAN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CodeParcours")
@XmlEnum
public enum CodeParcours {

    PAR_AVN,
    PAR_CIT,
    PAR_ART,
    PAR_SAN;

    public String value() {
        return name();
    }

    public static CodeParcours fromValue(String v) {
        return valueOf(v);
    }

}
