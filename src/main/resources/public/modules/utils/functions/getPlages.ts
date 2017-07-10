import {PLAGES} from '../../absences/constants/plages'
import { Plage } from '../../absences/models/shared/Plage-old'
/**
 * @param arr liste de nombres
 * @returns la moyenne si la liste n'est pas vide
 */
export function getPlage () {
    let oListePlages = [];
    for (let heure = PLAGES.heureDebut; heure <= PLAGES.heureFin; heure++) {
        let oPlage = new Plage();
        oPlage.heure = heure;
        if (heure === PLAGES.heureFin) {
            oPlage.duree = 0; // derniere heure
        } else {
            oPlage.duree = 60; // 60 minutes à rendre configurable ?
        }
        oPlage.style = {
            "width": (1 / (PLAGES.heureFin - PLAGES.heureDebut + 1) ) * 100 + "%"
        };
        oListePlages.push(oPlage);
    }
    return oListePlages;
}