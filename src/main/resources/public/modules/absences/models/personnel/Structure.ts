/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2017.
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

import { Collection, http, idiom as lang } from 'entcore/entcore';

import { DefaultStructure } from '../common/DefaultStructure';
import { Enseignant } from "./Enseignant";
import { Appel } from "./Appel";
import { Classe } from "./Classe";
import { Justificatif } from "./Justificatif";
import { Motif } from "./Motif";
import { Evenement } from "./Evenement";


export class Structure extends DefaultStructure {

    enseignants: Collection<Enseignant>;
    appels: Collection<Appel>;
    classes: Collection<Classe>;
    justificatifs: Collection<Justificatif>;
    motifs: Collection<Motif>;
    evenements: Collection<Evenement>;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.collection(Motif, {
            sync : function () {
                http().getJson('/viescolaire/presences/motifs').done(function (motifs) {
                    this.load(motifs);
                    this.map(function (motif) {
                        motif.justifiant_libelle = motif.justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                        return motif;
                    });
                }.bind(this));
            }
        });
        this.collection(Enseignant, {
            sync : '/viescolaire/enseignants/etablissement'
        });
        this.collection(Appel, {
            sync : function (pODateDebut, pODateFin) {
                if (pODateDebut !== undefined && pODateFin !== undefined) {
                    http().getJson('/viescolaire/presences/appels/' + moment(pODateDebut).format('YYYY-MM-DD') + '/' + moment(pODateFin).format('YYYY-MM-DD')).done(function(data) {
                        this.load(data);
                    }.bind(this));
                }
            }
        });
        this.collection(Evenement, {
            sync : function (psDateDebut, psDateFin) {
                if (psDateDebut !== undefined && psDateDebut !== undefined) {
                    http().getJson('/viescolaire/presences/eleves/evenements/' + moment(psDateDebut).format('YYYY-MM-DD') + '/' + moment(psDateFin).format('YYYY-MM-DD')).done(function(data){
                        let aLoadedData = [];
                        _.map(data, function(e) {
                            e.date = moment(e.timestamp_dt).format('YYYY-MM-DD');
                            return e;
                        });
                        let aDates = _.groupBy(data, 'cours_date');
                        for (let k in aDates) {
                            if (!aDates.hasOwnProperty(k)) { continue; }
                            let aEleves = _.groupBy(aDates[k], 'fk_eleve_id');
                            for (let e in aEleves) {
                                if (!aEleves.hasOwnProperty(e)) { continue; }
                                let t = aEleves[e];
                                let tempEleve = {
                                    id : t[0].fk_eleve_id,
                                    nom : t[0].nom,
                                    prenom : t[0].prenom,
                                    date : t[0].date,
                                    displayed : false,
                                    evenements : t
                                };
                                aLoadedData.push(tempEleve);
                            }
                        }
                        this.load(aLoadedData);
                    }.bind(this));
                }
            }
        });
        this.collection(Classe, {
            sync : '/viescolaire/classes/etablissement'
        });
        this.collection(Justificatif, {
            sync : '/viescolaire/presences/justificatifs'
        });
    }

    async sync (): Promise<any> {
            this.justificatifs.sync();
            this.classes.sync();
            this.motifs.sync();
            this.enseignants.sync();
            this.evenements.sync();
    }
}