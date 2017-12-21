//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.07.06 at 05:11:16 PM CEST 
//


package fr.openent.evaluations.bean.lsun;

import org.vertx.java.core.json.JsonArray;

import javax.xml.bind.annotation.*;


/**
 * D�crit un responsable d'un �l�ve
 *             
 * 
 * <p>Java class for Responsable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Responsable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adresse" type="{urn:fr:edu:scolarite:lsun:bilans:import}Adresse" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="civilite" use="required" type="{urn:fr:edu:scolarite:lsun:bilans:import}Civilite" />
 *       &lt;attribute name="nom" use="required" type="{urn:fr:edu:scolarite:lsun:bilans:import}NomPrenom" />
 *       &lt;attribute name="prenom" use="required" type="{urn:fr:edu:scolarite:lsun:bilans:import}NomPrenom" />
 *       &lt;attribute name="legal1" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="legal2" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="lien-parente" type="{urn:fr:edu:scolarite:lsun:bilans:import}Chaine40" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Responsable", propOrder = {
    "adresse"
})
public class Responsable {

    protected Adresse adresse;
    @XmlAttribute(name = "civilite", required = true)
    protected Civilite civilite;
    @XmlAttribute(name = "nom", required = true)
    protected String nom;
    @XmlAttribute(name = "prenom", required = true)
    protected String prenom;
    @XmlAttribute(name = "legal1")
    protected Boolean legal1;
    @XmlAttribute(name = "legal2")
    protected Boolean legal2;
    @XmlAttribute(name = "lien-parente")
    protected String lienParente;
    @XmlTransient
    protected String externalId;


    public Responsable(){}
    /*Attention pour la civilité il faudra tenir compte de la class enum Civilte*/
    public Responsable(String externalId, Civilite civilite,String nom, String prenom, JsonArray relatives,Adresse adresse ){
        this.externalId=externalId;
        this.civilite=civilite;
        this.nom=nom;
        this.prenom=prenom;
        this.addProprietesResponsable(relatives);
        this.adresse=adresse;
    }
    public Responsable(String externalId, String nom, String prenom, JsonArray relatives,Adresse adresse ){
        this.externalId=externalId;
        this.nom=nom;
        this.prenom=prenom;
        this.addProprietesResponsable(relatives);
        this.adresse=adresse;
    }
    public Responsable (String externalId, Civilite civilite,String nom, String prenom, JsonArray relatives){
        this.externalId=externalId;
        this.civilite=civilite;
        this.nom=nom;
        this.prenom=prenom;
        this.addProprietesResponsable(relatives);
    }


    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Gets the value of the adresse property.
     *
     * @return possible object is
     * {@link Adresse }
     */
    public Adresse getAdresse() {
        return adresse;
    }

    /**
     * Sets the value of the adresse property.
     *
     * @param value allowed object is
     *              {@link Adresse }
     */
    public void setAdresse(Adresse value) {
        this.adresse = value;
    }

    /**
     * Gets the value of the civilite property.
     *
     * @return possible object is
     * {@link Civilite }
     */
    public Civilite getCivilite() {
        return civilite;
    }

    /**
     * Sets the value of the civilite property.
     *
     * @param value allowed object is
     *              {@link Civilite }
     */
    public void setCivilite(Civilite value) {
        this.civilite = value;
    }

    /**
     * Gets the value of the nom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getNom() {
        return nom;
    }

    /**
     * Sets the value of the nom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNom(String value) {
        this.nom = value;
    }

    /**
     * Gets the value of the prenom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * Sets the value of the prenom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPrenom(String value) {
        this.prenom = value;
    }

    /**
     * Gets the value of the legal1 property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isLegal1() {
        return legal1;
    }

    /**
     * Sets the value of the legal1 property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setLegal1(Boolean value) {
        this.legal1 = value;
    }

    /**
     * Gets the value of the legal2 property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isLegal2() {
        return legal2;
    }

    /**
     * Sets the value of the legal2 property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setLegal2(Boolean value) {
        this.legal2 = value;
    }

    /**
     * Gets the value of the lienParente property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLienParente() {
        return lienParente;
    }

    /**
     * Sets the value of the lienParente property.
     *
     * @param codeParent allowed object is
     *              {@link String }
     */
   /* public void setLienParente(String value) {
        this.lienParente = value;
    }*/

    public void setLienParente(String codeParent) {

        switch (codeParent) {
            case "1":
                if(this.civilite == null) {
                    this.lienParente = "PERE";
                    this.civilite = Civilite.M;
                }
                break;
            case "2":
                if(this.civilite == null) {
                    this.lienParente = "MERE";
                    this.civilite = Civilite.MME;
                }
                break;
            case "3":
                this.lienParente = "TUTEUR";
                break;
            case "4":
                this.lienParente ="AUTRE MEMBRE DE LA FAMILLE";
                break;
            case "5":
                this.lienParente = "DDASS";
                break;
            case "6":
                this.lienParente = "AUTRE CAS";
                break;
            case "7":
                this.lienParente = "ELEVE LUI-MEME";
                break;
            default:
                break;
        }
    }

    public void setLegals(String code){
        Boolean[] tab = new Boolean[2];
        switch (code) {
            case "0":
                setLegal1(false);
                setLegal2(false);
                break;
            case "1":
                setLegal1(true);
                setLegal2(false);
                break;
            case "2":
                setLegal1(false);
                setLegal2(true);
                break;
            default:
                break;
        }

    }
    //méthode qui permet de compléter les attributs legal1, legal2 et lienParente
    public void addProprietesResponsable(JsonArray relatives){
        for (int j = 0; j < relatives.size(); j++) {
            String[] relative = relatives.get(j).toString().split("\\$");
            if (this.externalId.equals(relative[0])) {
                this.setLienParente(relative[1]);
                this.setLegals(relative[3]);
            }
        }
    }


}
