/**
 * Created by ledunoiss on 20/09/2016.
 */
import {ng} from 'entcore/entcore';

export let getMatiereClasseFilter = ng.filter('getMatiereClasse', function () {
    return function (matieres, idClasse, classes, search) {
        // if (idClasse === '*' || idClasse === undefined) return matieres;
        // if (classes.all.length > 0) {
        //     var currentClasse = _.findWhere(classes.all, {id : idClasse});
        //     if(currentClasse !== undefined) {
        //         var libelleClasse =currentClasse.name;
        //         if (libelleClasse !== undefined) {
        //             var listMatieresOfClasse = _.where(listeMatiere.all, {libelleClasse: libelleClasse});
        //             if(listMatieresOfClasse === undefined || listMatieresOfClasse.length == 0 ){
        //                 return listeMatiere.all;
        //             }else{
        //                 return listMatieresOfClasse;
        //             }
        //         }
        //     }
        // }
        if (idClasse === '*' || idClasse === undefined) return matieres;
        if (classes.all.length > 0) {
            let classe = classes.findWhere({id : idClasse});
            if (classe !== undefined) {
                let matieresClasse = matieres.filter((matiere) => {
                    return (matiere.libelleClasses.indexOf(classe.externalId) !== -1)
                });
                if (matieresClasse.length > 0) {
                     return matieresClasse;
                }
                return matieres.all;
            }
        }
    }
});