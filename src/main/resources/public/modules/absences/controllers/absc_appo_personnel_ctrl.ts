/**
 * Created by ledunoiss on 18/02/2016.
 */
import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {vieScolaire, Appel, Classe, Eleve, Enseignant, Evenement, Justificatif, Matiere, Motif, Responsable, VieScolaire} from '../models/absc_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let vscoAppoPersonnelController = ng.controller('VscoAppoPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('AbscFiltres', '../templates/absences/absc_personnel_filtres');
        $scope.pOFilterAppel = { //Objet permettant le filtre des appels oubliés / non oubliés
            noneffectues : true
        };
        $scope.psDisplayReponsables = false;
        $scope.selectedAppels = [];
        $scope.periode.fin = new Date();
        vieScolaire.appels.sync($scope.periode.debut, $scope.periode.fin);

        //$scope.appels.on('sync', function(){
        //    $scope.nonEffectues = model.appels.filter(function(appel){return appel.fk_etat_appel_id !== 3});
        //});


        $scope.formatDate = function(pODateDebut, pODateFin){
            return (moment(pODateDebut).format('DD/MM/YYYY')+" "+moment(pODateDebut).format('HH:mm')+"-"+moment(pODateFin).format('HH:mm'))
        };

        $scope.appelFilterFunction = function(appel){
            if($scope.pOFilterAppel.noneffectues){
                return (appel.fk_etat_appel_id !== 3);
            }else{
                return true;
            }
        };

        $scope.applyAppelFilter = function(){
            $scope.appelFilter = $scope.pOFilterAppel.noneffectues ? $scope.appelFilterFunction : null;
        };

        $scope.checkAppel = function(appel){
            var index = _.indexOf($scope.selectedAppels, appel);
            if( index === -1){
                $scope.selectedAppels.push(appel);
            }else{
                $scope.selectedAppels.splice(index, 1);
            }
        };

        // On set le filter sur les appels non effectues.
        $scope.appelFilter = $scope.appelFilterFunction;
    }
]);