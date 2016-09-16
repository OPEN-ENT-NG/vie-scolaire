import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang } from 'entcore/entcore';
import { syncedCollection } from '../../utils/interfaces/syncedCollection';

let moment = require('moment');
declare let _:any;
/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. Responsable : Coordonnées des responsables de l'élève.
 *  2. Evenements : Liste des évènements relatifs à l'élève : Absences, retards, départs, ...
 *  3. Eleve : Objet contenant toutes les informations relatives à un élève. Contient une liste de Responables et d'Evenements.
 *  4. Classe : Objet contenant toutes les informations relatives à une Classe. Contient une liste d'élève.
 *  5. Enseignant : Objet contenant toutes les informations relatives à un enseignant.
 *  6. Matiere : Objet contenant toutes les informations relatives à une matière.
 *  7. Appel : Object contenant toutes les informations relatives à un appel fait en classe ou réalisé par le CPE/Personnel d'éducation.
 *  8. Motif : Contient les différents motifs d'absences relatif à l'établissement.
 */

export class Responsable extends Model {}
export class Justificatif extends Model {}
export class Evenement extends Model implements IModel {
    id : number;

    get api () {
        return {
            update : '/viescolaire/absences/evenement/:id/updatemotif'
        }
    }

    update () : Promise<any> {
        return new Promise((resolve, reject) => {
           http().putJson(http().parseUrl(this.api.update)).done((data) => {
              if (resolve && (typeof (resolve) === 'function')) {
                  resolve(data);
              }
           });
        });
    }

    
}
export interface Evenements extends Collection<Evenement>, syncedCollection {}

export class Eleve extends Model {
    responsables : Collection<Responsable>;
    evenements : Collection<Evenement>;

    constructor () {
        super();
        this.collection(Responsable);
        this.collection(Evenement);
    }
}

export class Classe extends Model {
    eleves : Collection<Eleve>;
    selected : boolean;

    constructor () {
        super();
        this.collection(Eleve);
    }
}

export class Enseignant extends Model {
    selected : boolean;
    personnel_nom : string;
    personnel_prenom : string;
}
export class Matiere extends Model {}
export class Appel extends Model {}
export class Motif extends Model {}
export interface Motifs extends Collection<Motif>, syncedCollection {}

export class VieScolaire extends Model {
    classes : Collection<Classe>;
    enseignants : Collection<Enseignant>;
    appels : Collection<Appel>;
    evenements : Evenements;
    motifs : Motifs;
    justificatifs : Collection<Justificatif>;

    constructor () {
        super();
        this.collection(Classe, {
            sync : '/viescolaire/classes/etablissement'
        });
        this.collection(Enseignant, {
            sync : '/viescolaire/enseignants/etablissement'
        });
        this.collection(Appel, {
            sync : function (pODateDebut, pODateFin) {
                if(pODateDebut !== undefined && pODateFin !== undefined){
                    http().getJson('/viescolaire/absences/appels/'+moment(pODateDebut).format('YYYY-MM-DD')+'/'+moment(pODateFin).format('YYYY-MM-DD')).done(function(data){
                        this.load(data);
                    }.bind(this));
                }
            }
        });
        this.collection(Evenement, {
            sync : function (psDateDebut, psDateFin) {
                if(psDateDebut !== undefined && psDateDebut !== undefined){
                    http().getJson('/viescolaire/absences/eleves/evenements/'+moment(psDateDebut).format('YYYY-MM-DD')+'/'+moment(psDateFin).format('YYYY-MM-DD')).done(function(data){
                        var aLoadedData = [];
                        _.map(data, function(e){
                            e.cours_date = moment(e.cours_timestamp_dt).format('YYYY-MM-DD');
                            return e;
                        });
                        var aDates = _.groupBy(data, 'cours_date');
                        for (var k in aDates){
                            var aEleves = _.groupBy(aDates[k], 'fk_eleve_id');
                            for(var e in aEleves){
                                var t = aEleves[e];
                                var tempEleve = {
                                    eleve_id : t[0].fk_eleve_id,
                                    eleve_nom : t[0].eleve_nom,
                                    eleve_prenom : t[0].eleve_prenom,
                                    cours_date : t[0].cours_date,
                                    displayed : false,
                                    evenements : t
                                };
                                aLoadedData.push(tempEleve);
                            }
                        }
                        this.load(aLoadedData);
                        // this.load(data);
                    }.bind(this));
                }
            }
        } as Evenements);
        this.collection(Motif, {
            sync : function () {
                http().getJson('/viescolaire/absences/motifs').done(function (motifs) {
                    this.load(motifs);
                    this.map(function (motif) {
                        motif.motif_justifiant_libelle = motif.motif_justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                        return motif;
                    });
                }.bind(this));
            }
        } as Motifs);
        this.collection(Justificatif, {
            sync : '/viescolaire/absences/justificatifs'
        });
    }

    sync () {
        this.justificatifs.sync();
        this.classes.sync();
        this.motifs.sync();
        this.enseignants.sync();
    }
}

export let vieScolaire = new VieScolaire();

model.build = function () {
    (this as any).vieScolaire = vieScolaire;
    vieScolaire.sync();
};


