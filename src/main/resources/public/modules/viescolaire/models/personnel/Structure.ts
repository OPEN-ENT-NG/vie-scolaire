/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
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
 */

import {Collection, idiom as lang, moment} from 'entcore';
import {createActiveStructure, deleteActiveStructure} from "../../../utils/functions/activeStructures";
import {DefaultStructure} from '../common/DefaultStructure';

import {Motif} from './Motif';
import {Categorie} from "./Categorie";
import {Classe} from "./Classe";
import {Defaultcolors, NiveauCompetence} from "./eval_niveau_comp";
import {Cycle} from "./eval_cycle";
import {TypePeriode} from "../common/TypePeriode";
import {Utils} from "../../utils/Utils";


export class Structure extends DefaultStructure {
    // Fields
    id: string;
    isActived = {presence: false, evaluation: false};
     typePeriodes: Collection<TypePeriode>;

    // presence
    motifs: Collection<Motif>;
    categories: Collection<Categorie>;
    classes : Collection<Classe>;
    classesGroupes: Collection<Classe>;

    // evaluation
    niveauCompetences: Collection<NiveauCompetence>;
    cycles: Array<Cycle>;

    get api () {
        return  {
            MOTIF_ABS : {
                synchronization : '/presences/motifs?idEtablissement=' + this.id,
                categorie : '/presences/categorie/absences?idEtablissement=' + this.id
            },
            CLASSE : {
                synchronization : '/viescolaire/classes?idEtablissement=' + this.id
                + '&classOnly=True&forAdmin=true'
            },
            PERIODE : {
                synchronization: '/viescolaire/periodes?idEtablissement=' + this.id,
                type: '/viescolaire/periodes/types',
                update : '/viescolaire/periodes',
                evalOnPeriode : '/viescolaire/periodes/eval?'
            },
            NIVEAU_COMPETENCES : {
                synchronisation: '/competences/maitrise/level/' + this.id,
                delete : '/competences/maitrise/level/' + this.id

            },
            ITEM: {
                delete: `/competences/items/${this.id}`
            }
        };
    }

    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.collection(Motif, {
            sync: async function () {
                // Récupération des motifs pour l'établissement en cours
                let that = this.composer;
                return new Promise((resolve, reject) => {
                    let url = that.api.MOTIF_ABS.synchronization;
                    http().getJson(url).done(function (motifs) {
                        that.motifs.load(motifs);
                        that.motifs.map((motif) => {
                            motif.is_appel_oublie = false;
                            motif.justifiant_libelle = motif.justifiant ?
                                lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                            return motif;
                        });
                        resolve();
                    });
                });
            }
        });
        this.collection(Classe, {
            sync: async function (noCompetence?) {
                // Récupération des classes et groupes de l'etab
                let that = this.composer;
                return new Promise((resolve, reject) => {
                    let url = that.api.CLASSE.synchronization;
                    http().getJson(url).done(function (classe) {
                        that.classes.load(classe);
                        resolve();
                    });
                });
            }
        });
        this.collection(TypePeriode, {
            sync: () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.PERIODE.type).done((types) => {
                        this.typePeriodes.load(types);
                        resolve();
                    });
                });
            }
        });
        this.collection(Categorie, {
            sync: async function () {
                let that = this.composer;
                // Récupération (sous forme d'arbre) des catégories des motifs d'absence pour l'établissement en cours
                return new Promise((resolve, reject) => {
                    let url = that.api.MOTIF_ABS.categorie;

                    http().getJson(url).done(function (categories) {
                        let _categorieTree = categories;
                        if (categories.length > 0) {
                            _.map(_categorieTree, (categorie) => {
                                // récupération des motifs-fils de la catégorie courante
                                let _currentMotifs = that.motifs.filter(function (motif) {
                                    return motif.id_categorie === categorie.id;
                                });
                                categorie.slided = false;
                                categorie.is_appel_oublie = false;
                                categorie.motifs = {
                                    all: _currentMotifs
                                };
                            });
                        }
                        that.categories.load(_categorieTree);
                        resolve();
                    });
                });
            }
        });
        this.collection(NiveauCompetence, {
            sync: function () {
                // Récupération (sous forme d'arbre) des niveaux de compétences de l'établissement en cours
                return new Promise((resolve, reject) => {
                    http().getJson(this.composer.api.NIVEAU_COMPETENCES.synchronisation).done(function (niveauCompetences) {
                        _.each(niveauCompetences, (niveauCompetence) => {
                            if (niveauCompetence.couleur === null) {
                                niveauCompetence.couleur = Defaultcolors[niveauCompetence.default];
                            }
                            if (niveauCompetence.lettre === null) {
                                niveauCompetence.lettre = " ";
                            }
                            if(niveauCompetence.libelle === null) {
                                niveauCompetence.libelle = niveauCompetence.default_lib;
                            }
                            niveauCompetence.id_etablissement = this.composer.id;
                        });

                        this.load(niveauCompetences);

                        if (resolve && typeof resolve === 'function') {
                            resolve();
                        }
                    }.bind(this))
                        .error(function () {
                            if (reject && typeof reject === 'function') {
                                reject();
                            }
                        });
                });

            }
        });
    }

    sync() {
        return new Promise((resolve) => {
            let that = this;
             // Récupération (sous forme d'arbre) des niveaux de compétences de l'établissement en cours
            let canAccessCompetences = Utils.canAccessCompetences();
            const promises = [];
            if (canAccessCompetences) {
                // Récupération du niveau de compétences et construction de l'abre des cycles.
                let p1 = new Promise((resolve, reject) => {
                    that.getMaitrise().then(() => {
                        that.classes.sync().then(() => {
                            that.typePeriodes.sync().then(() => {
                                that.getPeriodes().then(() => {
                                    resolve();
                                });
                            });
                        });
                    });
                });
                promises.push(p1);
            }

            let p2 = new Promise((resolve, reject) => {
                that.classes.sync(true).then(() => {
                    that.typePeriodes.sync().then(() => {
                        that.getPeriodes().then(() => {
                            resolve();
                        });
                    });
                });
            });
            promises.push(p2);

            if (promises.length > 0) {
                Promise.all(promises).then(resolve);
            } else {
                resolve();
            }

        })
    };

    async  activate(module: string, isActif, idStructure) {
        if (!isActif) {
            let res = await deleteActiveStructure(module, idStructure);
        }
        else {
            let res = await createActiveStructure(module, idStructure);
        }
    }

    savePeriodes (idClasses, periodes): Promise<any> {
        return new Promise((resolve, reject) => {
            let json = {
                idEtablissement: this.id,
                idClasses: idClasses,
                periodes: _.map(periodes, (periode) => {
                    return {
                        timestamp_dt: moment(periode.timestamp_dt).format("YYYY-MM-DD"),
                        timestamp_fn: moment(periode.timestamp_fn).format("YYYY-MM-DD"),
                        date_fin_saisie: moment(periode.date_fin_saisie).format("YYYY-MM-DD"),
                        date_conseil_classe: moment(periode.date_conseil_classe).format("YYYY-MM-DD"),
                        publication_bulletin: periode.publication_bulletin
                    };
                })
            };
            http().putJson(this.api.PERIODE.update, json).done(() => {
                resolve();
            });
        });
    }

    getPeriodes ():Promise<any> {
        return new Promise((resolve, reject) => {
            http().getJson(this.api.PERIODE.synchronization).done((periodes) => {
                _.each(this.classes.all, (classe) => {
                    classe.periodes.load(_.where(periodes, {id_classe: classe.id}));
                    periodes = _.difference(periodes, classe.periodes.all);
                });
                resolve();
            });
        });
    }

    checkEval(idClasses):Promise<any> {
        return new Promise((resolve, reject) => {
            let url = this.api.PERIODE.evalOnPeriode;
            _.each(idClasses, (idClasse, index) => {
                url += "idClasse=" + idClasse;
                if( index != idClasses.length - 1 ) url +='&' ;
            });
            http().getJson(url).done((boolean) => {
                resolve(boolean.exist);
            });
        });
    }

    getMaitrise():Promise<any> {
        return new Promise((resolve, reject) => {
            var that = this;
            this.niveauCompetences.sync().then(() => {
                let cycles = [];
                let tree = _.groupBy(this.niveauCompetences.all, "id_cycle");

                _.map(tree, function (node) {
                    let cycleNode = {
                        id_cycle: node[0].id_cycle,
                        libelle: node[0].cycle,
                        selected: false,
                        niveauCompetencesArray: _.sortBy(node, function (niv) {
                            return niv.ordre;
                        })
                    };
                    cycleNode.niveauCompetencesArray = cycleNode.niveauCompetencesArray.reverse();
                    cycles.push(cycleNode);
                });
                that.cycles = cycles;
                console.log("cycles loaded");
                resolve()
            });
        });
    }

    deletePerso () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().deleteJson(this.api.NIVEAU_COMPETENCES.delete).done(() => {
                this.getMaitrise().then(() => {
                    if (resolve && (typeof(resolve) === 'function')) {
                        resolve();
                    }
                });
            });
        });
    }
    deletePersoItem () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().deleteJson(this.api.ITEM.delete).done(() => {
                // TODO
                //this.getPersoItem().then(() => {
                if (resolve && (typeof(resolve) === 'function')) {
                    resolve();
                }
                //});
            });
        });
    }

    private libelle = {
        CLASSE: 'Classe',
        GROUPE: "Groupe d'enseignement"
    };
    private castClasses = (classes) => {
        return _.map(classes, (classe) => {
            let libelleClasse;
            if (classe.type_groupe_libelle = classe.type_groupe === 0) {
                libelleClasse = this.libelle.CLASSE;
            } else {
                libelleClasse = this.libelle.GROUPE;
            }
            classe.type_groupe_libelle = libelleClasse;
            classe = new Classe(classe);
            return classe;
        });
    };
}
