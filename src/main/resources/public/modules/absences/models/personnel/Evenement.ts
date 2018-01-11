import { Evenement as SharedEvenement } from '../shared/Evenement';
import { notify } from 'entcore/entcore';

export class Evenement extends SharedEvenement {
    libelle: string;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }

    static savePeriodeAbsence(idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevIdToUpdate, arrayAbscPrevIdToDelete,
                           arrayEventIdToUpdate, arrayEventToCreate, arrayCoursToCreate): Promise<any> {
        return new Promise((resolve, reject) => {
            let Url = '/viescolaire/presences/zone/absence';
            let resource = {
                idEleve: idEleve,
                idMotif: idMotif,
                arrayAbscPrevToCreate: arrayAbscPrevToCreate, // array of Json Object avec champ: dateDebut, dateFin
                arrayAbscPrevToUpdate: arrayAbscPrevIdToUpdate, // array of Json Object avec champ: dateDebut, dateFin, id, id_motif
                arrayAbscPrevToDelete: arrayAbscPrevIdToDelete, // array of Json Object avec champ: id et autres
                arrayEventIdToUpdate: arrayEventIdToUpdate, // Tableau d'id
                arrayEventToCreate: arrayEventToCreate, // Tableau d'objet avec le champ : id_cours
                arrayCoursToCreate: arrayCoursToCreate // Tableau d'objet avec les champs : dateDebut, dateFin, salle, id_matiere, id_classe, id_personnel, id_etablissement
            };
            http().putJson(Url, resource)
                .done(() => {
                    if (resolve && typeof resolve === 'function') {
                        notify.success('Enregistrement réussi');
                        resolve();
                    }
                })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        notify.error("Problème lors de l'enregistrement de la période d'absence.");
                        reject();
                    }
                });
        });
    }
}