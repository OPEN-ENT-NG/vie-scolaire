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
 * <p>Java class for ModaliteElection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ModaliteElection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="S"/>
 *     &lt;enumeration value="O"/>
 *     &lt;enumeration value="F"/>
 *     &lt;enumeration value="L"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="X"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ModaliteElection")
@XmlEnum
public enum ModaliteElection {

    S,
    O,
    F,
    L,
    R,
    X;

    public String value() {
        return name();
    }

    public static ModaliteElection fromValue(String v) {
        return valueOf(v);
    }

}