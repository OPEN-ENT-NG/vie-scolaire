/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package org.cgi.evaluations.bean;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalNoteDevoir {
    /**
     * Valeur de la note
     */
    private Double note;

    /**
     * Sur combien est la note.
     */
    private Integer diviseur;

    /**
     * Booleen pour savoir s'il faut ramner la note sur le diviseur.
     */
    private Boolean ramenerSur;

    /**
     * Coefficient de la note.
     */
    private Double coefficient;

    public static Integer DIVISEUR_DEFAULT_VALUE = 20;

    /**
     * @param note valeur de la note
     * @param diviseur sur combien est la note.
     * @param ramenerSur booleen pour savoir s'il faut ramner la note sur le diviseur.
     * @param coefficient coefficient de la note.
     */
    public CEvalNoteDevoir(Double note, Integer diviseur, Boolean ramenerSur, Double coefficient) {
        this.note = note;
        this.diviseur = diviseur;
        this.ramenerSur = ramenerSur;
        this.coefficient = coefficient;
    }

    /**
     * Construis un objet {@link CEvalNoteDevoir} avec diviseur initialisé à 20
     *
     * @param note valeur de la note
     * @param ramenerSur booleen pour savoir s'il faut ramner la note sur le diviseur.
     * @param coefficient coefficient de la note.
     */
    public CEvalNoteDevoir(Double note, Boolean ramenerSur, Double coefficient) {
        this.note = note;
        this.diviseur = CEvalNoteDevoir.DIVISEUR_DEFAULT_VALUE;
        this.ramenerSur = ramenerSur;
        this.coefficient = coefficient;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public Integer getDiviseur() {
        return diviseur;
    }

    public void setDiviseur(Integer diviseur) {
        this.diviseur = diviseur;
    }

    public Boolean getRamenerSur() {
        return ramenerSur;
    }

    public void setRamenerSur(Boolean ramenerSur) {
        this.ramenerSur = ramenerSur;
    }

    public Double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Double coefficient) {
        this.coefficient = coefficient;
    }
}
