import { http, model } from 'entcore/entcore';

declare const _: any;

/**
 * Récupère les événements de l'utilisateur selon une période donnée.
 * @param module Nom du module appelant la fonction. Permet de variabiliser la route appelante.
 * @returns {Promise<T>} Callback de retour.
 */
export function getEvenements (psDateDebut: string, psDateFin: string): Promise<any[]> {
    return new Promise((resolve, reject) => {
        if (psDateDebut !== undefined && psDateDebut !== undefined) {
            http().getJson('/viescolaire/presences/eleves/evenements/' + moment(psDateDebut).format('YYYY-MM-DD') + '/' + moment(psDateFin).format('YYYY-MM-DD'))
                .done((evenements) => {
                    let aLoadedData = [];
                    _.map(evenements, function(e) {
                        e.date = moment(e.timestamp_dt).format('YYYY-MM-DD');
                        return e;
                    });
                    let aDates = _.groupBy(evenements, 'cours_date');
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
                    resolve(aLoadedData);
                })
                .error(() => {
                    reject();
                })
            ;
        }
    });
}