import {Collection} from "entcore/entcore";
import {Devoir} from "./devoir";
import {Matiere} from "./matiere";
import {Enseignant} from "../teacher/eval_teacher_mdl";
import {Periode} from "../../../viescolaire/models/common/Periode";

declare let location: any;
export class PeriodeEleve extends Periode {
    devoirs: Collection<Devoir>;
    matieres: Collection<Matiere>;
    enseignants: Collection<Enseignant>;
    synchronised_matieres: boolean;
    synchronised_enseignants: boolean;

    static get api () {
        return {
            GET_EVALUATIONS : '/viescolaire/evaluations/devoirs?idEtablissement=',
            GET_MATIERES : '/viescolaire/matieres/infos?',
            GET_ENSEIGNANTS: '/viescolaire/enseignants?'
        };
    }

    constructor () {
        super();
        this.synchronised_matieres = false;
        this.synchronised_enseignants = false;
        this.collection(Enseignant, {
            sync: (mapEnseignant) => {
                return new Promise((resolve) => {
                    let uri = PeriodeEleve.api.GET_ENSEIGNANTS;
                    for (let enseignant in mapEnseignant) {
                        uri += '&idUser=' + enseignant;
                    }
                    let that = this;
                    http().get(uri).done((enseignants) => {
                        that.enseignants.load(enseignants);
                        that.synchronised_enseignants = true;
                        resolve();
                    });
                });
            }
        });
        this.collection(Matiere, {
            sync: (mapMatiere) => {
                return new Promise((resolve) => {
                    let uri = PeriodeEleve.api.GET_MATIERES;
                    let that = this;
                    for (let matiere in mapMatiere) {
                        uri = uri + '&idMatiere=' + matiere;
                    }
                    http().get(uri).done((matieres) => {
                        that.matieres.load(matieres);
                        that.synchronised_matieres = true;
                        resolve();
                    });
                });
            }
        });
        this.collection(Devoir, {
            sync: (structureId, userId) => {
                return new Promise((resolve) => {
                    let uri = PeriodeEleve.api.GET_EVALUATIONS
                        + structureId + '&idEleve=' + userId;
                    if (this.id !== undefined) {
                        uri = uri + '&idPeriode=' + this.id;
                    }
                    http().getJson(uri)
                        .done((devoirs) => {
                            this.devoirs.load(devoirs);
                            let matieresDevoirs = _.groupBy(devoirs, 'id_matiere');
                            let enseignants = _.groupBy(devoirs, 'owner');
                            this.synchronised_matieres = false;
                            this.synchronised_enseignants = false;
                            this.enseignants.sync(enseignants).then(() => {
                                if (this.buildMatieres(matieresDevoirs)) {
                                    resolve();
                                }
                            });
                            this.matieres.sync(matieresDevoirs).then(() => {
                                if (this.buildMatieres(matieresDevoirs)) {
                                    resolve();
                                }
                            });
                        });
                });
            }
        });
    }

    buildMatieres (mapMatiere) {
        if (this.synchronised_matieres && this.synchronised_enseignants) {
            for (let o in mapMatiere) {
                mapMatiere[o].forEach(function (element) {
                    let devoir = element;

                    let _matiere = this.matieres.findWhere({id: devoir.id_matiere});
                    let enseignant = this.enseignants.findWhere({id: devoir.owner});
                    if (_.filter(_matiere.ens, {id: enseignant.id}).length === 0) {
                        _matiere.ens.push(enseignant);
                    }
                });
            }
            return true;
        }
        return false;
    }

    getReleve (idPeriode, idUser) {
        let uri = '/viescolaire/evaluations/releve/pdf?idEtablissement=' +
            model.me.structures[0] + '&idUser=' + idUser;
        if (idPeriode !== undefined) {
            uri +=  '&idPeriode=' + idPeriode;
        }
        location.replace(uri);
    }
}