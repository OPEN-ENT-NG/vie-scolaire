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

/**
 * Created by ledunoiss on 09/08/2016.
 */
Behaviours.register('viescolaire.evaluations', {
    rights: {},
    dependencies: {},
    resource: function(resource){},
    resourceRights: function(){},
    loadResources: function(callback){},

    /**
     * Fonction de calcul générique de la moyenne
     * @param listesObjets : contient une liste d'ojet, que ce soit des notes ou des matieres contenant chacun : 1 valeur, 1 coefficient
     * et 1 booleen de ramener sur le diviseurM. Dans le cas ou les objets seraient des moyennes, toutes les propriétés ramener sur devront
     * être à false.
     *
     * @param callback : callback de traitement de la moyenne.
     * @param diviseurM : diviseur de la moyenne. Par défaut, cette valeur est égale à 20 (optionnel).
     **/
    calculMoyenne : function(listesObjets, diviseurM){
        if(diviseurM === undefined){
            diviseurM = 20;
        }
        var notes = 0;
        var diviseur = 0;
        _.each(listesObjets, function(obj){
            if(obj.ramenersur === true){
                if(obj.valeur === undefined){
                    notes = notes + ((parseFloat(obj.note) * parseFloat(obj.coefficient))*(diviseurM/parseFloat(obj.diviseur)));
                }else{
                    notes = notes + ((parseFloat(obj.valeur) * parseFloat(obj.coefficient))*(diviseurM/parseFloat(obj.diviseur)));
                }
                diviseur = diviseur + (diviseurM*parseFloat(obj.coefficient));
            }else{
                if(obj.valeur === undefined){
                    notes = notes + (parseFloat(obj.note) * parseFloat(obj.coefficient));
                }else{
                    notes = notes + (parseFloat(obj.valeur) * parseFloat(obj.coefficient));
                }
                diviseur = diviseur + (parseFloat(obj.diviseur) * parseFloat(obj.coefficient));
            }
        });
        var moyenne = (notes/diviseur)*diviseurM;
        return moyenne.toFixed(2);
    },
    /**
     * Fonction de calcul des statistiques d'un devoir
     * @return statistiques du devoir par référence sur l'objet Devoir
     * @param devoir : Référence du devori courant
     * @param listeNotes : Liste contenant les notes du devoir
     **/
    calculStatsDevoir : function(devoir, listeNotes){
        var somme = 0;
        var nbNotes = listeNotes.length;
        var noteMax, noteMin;
        var filteredListeNote = _.filter(listeNotes, function(note){
            return note.valeur !== "" && note.valeur <= devoir.diviseur;
        });
        _.each(filteredListeNote, function(note){
            somme += parseFloat(note.valeur);
            if(noteMin === undefined && noteMax === undefined){
                noteMin = parseFloat(note.valeur);
                noteMax = parseFloat(note.valeur);
            }else if(noteMax < note.valeur){
                noteMax = note.valeur;
            }else if(noteMin > note.valeur){
                noteMin = note.valeur;
            }
        });
        devoir.noteMin = noteMin;
        devoir.noteMax = noteMax;
        devoir.moyenne = somme/filteredListeNote.length;
        devoir.percentDone = Math.round((filteredListeNote.length/nbNotes)*100);
    },

    /**
     * Fonction permettant le formatage d'une date
     * @return la date formatée en 'DD/MM/YYYY'
     * @param date : la date au format base de données
     * @param format le format de la date
     **/
    getFormatedDate: function(date, format){
        return moment(date).format(format);
    }

});
