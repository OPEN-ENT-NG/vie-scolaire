/**
 * Created by ledunoiss on 19/02/2016.
 */
import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {vieScolaire, Appel, Classe, Eleve, Enseignant, Evenement, Justificatif, Matiere, Motif, Responsable, VieScolaire} from '../models/absc_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let abscFiltresPersonnelController = ng.controller('AbscFiltresPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, model, $rootScope, $location) {
        $scope.pOFilterCtrl = {
            enseignants : false,
            classes : false,
            responsables : false
        };

        $scope.switchAll = function(oListe, b){
            oListe.each(function(o){
                o.selected = b;
            });
        };
    }
]);