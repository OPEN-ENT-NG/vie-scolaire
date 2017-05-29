import { Collection, idiom as lang} from 'entcore/entcore';
import {createActiveStructure, deleteActiveStructure} from "../../../utils/functions/activeStructures";
import {DefaultStructure} from '../common/DefaultStructure';

import { Motif } from '../../../absences/models/personnel/Motif';
import {Categorie} from "../../../absences/models/personnel/Categorie";
import {CategorieAppel} from "../../../absences/models/personnel/CategorieAppel";
import {Structure as StructurePresence } from '../../../absences/models/personnel/Structure';
import {MotifAppel} from "../../../absences/models/personnel/MotifAppel";


export class Structure extends DefaultStructure {
    // Fields
    id: string;
    isActived = {presence: false, evaluation: false};
    motifs: Collection<Motif>;
    categories: Collection<Categorie>;
    motifAppels: Collection<MotifAppel>;
    categorieAppels: Collection<CategorieAppel>;

    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
    async sync(): Promise<any> {
        let presence = new StructurePresence();
        this.collection(Motif, {
            sync : async function () {
                // Récupération des motifs pour l'établissement en cours
                let that = this.composer;
                return new Promise((resolve, reject) => {
                    let url = presence.api.MOTIF_ABS.synchronization + '?idEtablissement=' + that.id;
                    http().getJson(url).done(function (motifs) {
                        that.motifs.load(motifs);
                        that.motifs.map((motif) => {
                            motif.justifiant_libelle = motif.justifiant ?
                                lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                            return motif;
                        });
                        resolve();
                    });
                });
            }
        });
        this.collection(Categorie, {
            sync : async function () {
                let that = this.composer;
                // Récupération (sous forme d'arbre) des catégories des motifs d'absence pour l'établissement en cours
                return new Promise((resolve, reject) => {
                    let url = presence.api.MOTIF_ABS.categorie + '?idEtablissement=' + that.id;

                    http().getJson(url).done(function (categories) {
                        let _categorieTree = categories;
                        if ( categories.length > 0 ) {
                            _.map(_categorieTree, (categorie) => {
                                // récupération des motifs-fils de la catégorie courante
                                let _currentMotifs = that.motifs.filter( function(motif) {
                                    return motif.id_categorie === categorie.id;
                                });
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
            sync : async function () {
                // Récupération des motifs pour l'établissement en cours
                let that = this.composer;
                return new Promise((resolve, reject) => {
                    let url = presence.api.MOTIF_APPEL.synchronization + '?idEtablissement=' + that.id;
                    http().getJson(url).done(function (motifs) {
                        that.motifAppels.load(motifs);
                        that.motifAppels.map((motif) => {
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
            sync : async function () {
                let that = this.composer;
                // Récupération (sous forme d'arbre) des catégories des motifs d'absence pour l'établissement en cours
                return new Promise((resolve, reject) => {
                    let url = presence.api.MOTIF_APPEL.categorie + '?idEtablissement=' + that.id;

                    http().getJson(url).done(function (categories) {
                        let _categorieTree = categories;
                        if ( categories.length > 0 ) {
                            _.map(_categorieTree, (categorie) => {
                                // récupération des motifs-fils de la catégorie courante
                                let _currentMotifs = that.motifAppels.filter( function(motif) {
                                    return motif.id_categorie === categorie.id;
                                });
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


        // motifs et Catégorie d'appel
        await this.motifAppels.sync();
        await this.categorieAppels.sync();
        // motifs et Catégrorie d'absences
        await this.motifs.sync();
        await this.categories.sync();
    }

    async  activate(module: string, isActif, idStructure) {
        if (!isActif) {
            let res = await deleteActiveStructure(module, idStructure);
        }
        else {
            let res = await createActiveStructure(module, idStructure);
        }
    }
}
