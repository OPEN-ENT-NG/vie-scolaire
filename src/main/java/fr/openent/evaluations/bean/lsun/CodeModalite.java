//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.07.06 at 05:11:16 PM CEST 
//


package fr.openent.evaluations.bean.lsun;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CodeModalite.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CodeModalite">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PAP"/>
 *     &lt;enumeration value="PPS"/>
 *     &lt;enumeration value="UPE2A"/>
 *     &lt;enumeration value="SEGPA"/>
 *     &lt;enumeration value="ULIS"/>
 *     &lt;enumeration value="PAI"/>
 *     &lt;enumeration value="PPRE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CodeModalite")
@XmlEnum
public enum CodeModalite {

    PAP("PAP"),
    PPS("PPS"),
    @XmlEnumValue("UPE2A")
    UPE_2_A("UPE2A"),
    SEGPA("SEGPA"),
    ULIS("ULIS"),
    PAI("PAI"),
    PPRE("PPRE");
    private final String value;

    CodeModalite(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CodeModalite fromValue(String v) {
        for (CodeModalite c: CodeModalite.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}