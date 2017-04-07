import { model, http, Model, Collection, idiom as lang } from 'entcore/entcore';
import { syncedCollection } from '../../utils/interfaces/syncedCollection';

// Import des classes
import { Appel } from './personnel/Appel';
import { Classe } from './personnel/Classe';
import { Enseignant } from './personnel/Enseignant';
import { Evenement } from './personnel/Evenement';
import { Justificatif } from './personnel/Justificatif';
import { Motif } from './personnel/Motif';

let moment = require('moment');
declare let _: any;

interface Evenements extends Collection<Evenement>, syncedCollection {}

interface Motifs extends Collection<Motif>, syncedCollection {}

class VieScolaire extends Model {
    classes: Collection<Classe>;
    enseignants: Collection<Enseignant>;
    appels: Collection<Appel>;
    evenements: Evenements;
    motifs: Motifs;
    justificatifs: Collection<Justificatif>;

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
                if (pODateDebut !== undefined && pODateFin !== undefined) {
                    http().getJson('/viescolaire/absences/appels/' + moment(pODateDebut).format('YYYY-MM-DD') + '/' + moment(pODateFin).format('YYYY-MM-DD')).done(function(data){
                        this.load(data);
                    }.bind(this));
                }
            }
        });
        this.collection(Evenement, {
            sync : function (psDateDebut, psDateFin) {
                if (psDateDebut !== undefined && psDateDebut !== undefined) {
                    http().getJson('/viescolaire/absences/eleves/evenements/' + moment(psDateDebut).format('YYYY-MM-DD') + '/' + moment(psDateFin).format('YYYY-MM-DD')).done(function(data){
                        let aLoadedData = [];
                        _.map(data, function(e){
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
        } as Evenements);
        this.collection(Motif, {
            sync : function () {
                http().getJson('/viescolaire/absences/motifs').done(function (motifs) {
                    this.load(motifs);
                    this.map(function (motif) {
                        motif.justifiant_libelle = motif.justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
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

let vieScolaire = new VieScolaire();

// Export des classes
export { vieScolaire, Appel, Motif, Justificatif, Evenement, Enseignant, Classe }

model.build = function () {
    (this as any).vieScolaire = vieScolaire;
    vieScolaire.sync();
};


