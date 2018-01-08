import { Evenement as SharedEvenement } from '../shared/Evenement';

export class Evenement extends SharedEvenement {
    libelle : string;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }



    // updateMotif(eventId, motifId): Promise<any> {
    //     return new Promise((resolve,reject) => {
    //         http().getJson(this.api.UPDATE_MOTIF).done((data) => {
    //             if (resolve && typeof resolve === 'function') {
    //                 resolve();
    //             }
    //         })
    //             .error(function () {
    //                 if (reject && typeof reject === 'function') {
    //                     reject();
    //                 }
    //             });
    //     });
    // }

    static saveZoneAbsence(idEleve, idMotif, arrayAbscPrevToCreate, arrayAbscPrevIdToUpdate, arrayAbscPrevIdToDelete,
                           arrayEventIdToUpdate, arrayEventToCreate,arrayCoursToCreate): Promise<any> {
        return new Promise((resolve, reject) => {
            let Url = '/viescolaire/presences/zone/absence';
            var resource = {
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
                .done((data) => {
                    if (resolve && typeof resolve === 'function') {

                        console.log("SUCCESS");
                        resolve();
                    }
                })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        console.log("ERROR");
                        reject();
                    }
                });
        });
    }

    static updateMotif(EventIds,MotifId): Promise<any> {
        return new Promise((resolve, reject) => {
            let Url = '/viescolaire/presences/evenements/motif';
            http().putJson(Url, {'EventsIds': EventIds, 'MotifId': MotifId})
                .done((data) => {
                    if (resolve && typeof resolve === 'function') {
                        resolve();
                    }
                })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }
}