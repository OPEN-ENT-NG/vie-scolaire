import { Collection, idiom as lang} from 'entcore/entcore';
import {createActiveStructure, deleteActiveStructure} from "../../../utils/functions/activeStructures";
import {DefaultStructure} from '../common/DefaultStructure';

import { Motif } from '../../../absences/models/personnel/Motif';
import {Categorie} from "../../../absences/models/personnel/Categorie";
import {CategorieAppel} from "../../../absences/models/personnel/CategorieAppel";
import {MotifAppel} from "../../../absences/models/personnel/MotifAppel";
import {Classe} from "./Classe";
import {Defaultcolors, NiveauCompetence} from "../../../evaluations/models/eval_niveau_comp";
import {Cycle} from "../../../evaluations/models/eval_cycle";
import {TypePeriode} from "../common/TypePeriode";
import {Periode} from "../common/Periode";


export class Structure extends DefaultStructure {
    // Fields
    id: string;
    isActived = {presence: false, evaluation: false};
    typePeriodes: Collection<TypePeriode>;

    // presence
    motifs: Collection<Motif>;
    categories: Collection<Categorie>;
    motifAppels: Collection<MotifAppel>;
    categorieAppels: Collection<CategorieAppel>;
    classes : Collection<Classe>;

    // evaluation
    niveauCompetences: Collection<NiveauCompetence>;
    cycles: Array<Cycle>;

    get api () {
        return  {
            MOTIF_ABS : {
                synchronization : '/viescolaire/presences/motifs?idEtablissement=' + this.id,
                categorie : '/viescolaire/presences/categorie/absences?idEtablissement=' + this.id
            },
            MOTIF_APPEL: {
                synchronization: '/viescolaire/presences/motifs/appel?idEtablissement=' + this.id,
                categorie: '/viescolaire/presences/categorie/appels?idEtablissement=' + this.id,
            },
            CLASSE : {
                synchronization : '/viescolaire/classes?idEtablissement=' + this.id + '&classOnly=True'
            },
            PERIODE : {
                synchronization: '/viescolaire/periodes?idEtablissement=' + this.id,
                type: '/viescolaire/periodes/types',
                update : '/viescolaire/periodes',
                evalOnPeriode : '/viescolaire/periodes/eval?'
            },
            NIVEAU_COMPETENCES : {
                synchronisation: '/viescolaire/evaluations/maitrise/level/' + this.id,
                delete : '/viescolaire/evaluations/maitrise/level/' + this.id

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
            sync: async function () {
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
        this.collection(MotifAppel, {
            sync: async function () {
                // Récupération des motifs pour l'établissement en cours
                let that = this.composer;
                return new Promise((resolve, reject) => {
                    let url = that.api.MOTIF_APPEL.synchronization;
                    http().getJson(url).done(function (motifs) {
                        that.motifAppels.load(motifs);
                        that.motifAppels.map((motif) => {
                            motif.is_appel_oublie = true;
                            motif.justifiant_libelle = motif.justifiant ?
                                lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                            return motif;
                        });
                        resolve();
                    });
                });
            }
        });
        this.collection(CategorieAppel, {
            sync: async function () {
                let that = this.composer;
                // Récupération (sous forme d'arbre) des catégories des motifs d'absence pour l'établissement en cours
                return new Promise((resolve, reject) => {
                    let url = that.api.MOTIF_APPEL.categorie;

                    http().getJson(url).done(function (categories) {
                        let _categorieTree = categories;
                        if (categories.length > 0) {
                            _.map(_categorieTree, (categorie) => {
                                // récupération des motifs-fils de la catégorie courante
                                let _currentMotifs = that.motifAppels.filter(function (motif) {
                                    return motif.id_categorie === categorie.id;
                                });
                                categorie.slided = false;
                                categorie.is_appel_oublie = true;
                                categorie.motifAppels = {
                                    all: _currentMotifs
                                };
                            });
                        }
                        that.categorieAppels.load(_categorieTree);
                        resolve();
                    });
                });
            }
        });
        this.collection(NiveauCompetence, {
            sync: async function () {
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
                        })
                })

            }
        });
    }

    async sync(): Promise<any> {

        // Récupération du niveau de compétences et construction de l'abre des cycles.
        this.getMaitrise();

        // motifs et Catégorie d'appel
        await this.motifAppels.sync();
        await this.categorieAppels.sync();
        // motifs et Catégrorie d'absences
        await this.motifs.sync();
        await this.categories.sync();
        //classes
        await this.classes.sync();
        await this.typePeriodes.sync();
        await this.getPeriodes();
    }

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
                        date_fin_saisie: moment(periode.date_fin_saisie).format("YYYY-MM-DD")
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

    async getMaitrise() {
        await this.niveauCompetences.sync();
        let cycles = [];
        let tree = _.groupBy(this.niveauCompetences.all,"id_cycle");

        _.map(tree, function (node) {
            let cycleNode  = {
                id_cycle: node[0].id_cycle,
                libelle: node[0].cycle,
                selected: false,
                niveauCompetencesArray: _.sortBy(node, function(niv) {
                    return niv.ordre;
                })
            }
            cycleNode.niveauCompetencesArray = cycleNode.niveauCompetencesArray.reverse();
            cycles.push(cycleNode);
        });
        this.cycles = cycles;
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
}
