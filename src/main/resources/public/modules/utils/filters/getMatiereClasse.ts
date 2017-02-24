/**
 * Created by ledunoiss on 20/09/2016.
 */
import {ng} from 'entcore/entcore';

export let getMatiereClasseFilter = ng.filter('getMatiereClasse', function () {
    return function (matieres, idClasse, classes, search, listeMatiere) {
        if (idClasse === '*' || idClasse === undefined) return matieres;
        if (classes.all.length > 0) {
            var currentClasse = _.findWhere(classes.all, {id : idClasse});
            if(currentClasse !== undefined) {
                var libelleClasse =currentClasse.name;
                if (libelleClasse !== undefined) {
                    return _.where(listeMatiere.all, {libelleClasse: libelleClasse});
                }
            }
        }
    }
});