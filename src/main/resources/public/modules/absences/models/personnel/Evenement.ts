import { Evenement as SharedEvenement } from '../shared/Evenement';

export class Evenement extends SharedEvenement {
    libelle : string;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }

    updates(EventIds,MotifId): Promise<any> {
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