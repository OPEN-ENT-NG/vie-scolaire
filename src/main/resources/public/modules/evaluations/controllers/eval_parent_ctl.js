/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

/**
 * Created by ledunoiss on 08/08/2016.
 */
routes.define(function($routeProvider){
    $routeProvider
        .when('/releve', {action:'displayReleveNotes'})
        .otherwise({
            redirectTo : '/releve'
        });
});

function EvaluationsController($scope, $rootScope, $location, model, template, route, date, $filter){

    $scope.searchReleve = {
        eleve : null,
        periode : null
    };

    $scope.showNoteLightBox = {
        bool : false
    };

    if(model.me.type === "PERSRELELEVE"){
        $scope.eleves = model.eleves;
    }else{
        $scope.periodes = model.periodes;
        $scope.searchReleve.eleve = model.me;
    }
    $scope.matieres = model.matieres;
    $scope.me = model.me;

    $scope.getPeriodes = function () {
        if($scope.searchReleve.eleve !== null){
            // TODO Recuperer l'etablissement courant et non le 1er etab dans la liste
            setCurrentPeriode($scope.searchReleve.eleve.structures[0]);
        }
    };

    $scope.getMatieres = function(){
        if($scope.searchReleve.eleve !== null){
            model.matieres.sync($scope.searchReleve.eleve.id);
        }
    };

    $scope.getFormatedDate = function(date){
        return Behaviours.applicationsBehaviours.notes.getFormatedDate(date, "DD/MM/YYYY");
    };

    $scope.noteMatiereEleve = function(idMatiere){
        return $scope.dataReleve.devoirs.findWhere({ idmatiere : idMatiere });
    };

    $scope.chooseChild = function(idEleve){
        $scope.searchReleve.eleve = model.eleves.findWhere({id : idEleve});
        $scope.getPeriodes();
        $scope.getMatieres();
        $scope.showNoteLightBox.bool = false;
    };

    $scope.calculMoyenne = function(idMatiere){
        var notes = $scope.dataReleve.devoirs.where({idmatiere : idMatiere});
        if(notes !== undefined){
            var moyenne = Behaviours.applicationsBehaviours.notes.calculMoyenne(notes);
            return moyenne;
        }else{
            return;
        }
    };

    /**
     * On écoute sur le synchronisation des périodes.
     */
    //model.periodes.on('sync', function(){
    //	setCurrentPeriode(model.me.structures[0]);
    //	$scope.safeApply();
    //});
    $scope.$on('syncPeriodes', function(){
        setCurrentPeriode(model.me.structures[0]);
        $scope.safeApply();
    })

    $scope.loadReleveNote = function(){
        var periode;
        if($scope.searchReleve.periode !== null) {
            if(model.me.type === 'PERSRELELEVE'){
                var eleve = model.eleves.findWhere({id : $scope.searchReleve.eleve.id});
                periode = eleve.periodes.findWhere({id : $scope.searchReleve.periode.id});
                periode.devoirs.sync($scope.searchReleve.eleve.structures[0], $scope.searchReleve.eleve.id);
                periode.devoirs.on('sync', function(){
                    $scope.dataReleve = periode;
                });
            }else{
                periode = model.periodes.findWhere({id : $scope.searchReleve.periode.id});
                periode.devoirs.sync($scope.searchReleve.eleve.structures[0], $scope.searchReleve.eleve.userId);
                periode.devoirs.on('sync', function(){
                    $scope.dataReleve = periode;
                });
            }
        }
    };
    /**
     * Charge la liste des periodes dans $scope.periodes et détermine la période en cours et positionne
     * son id dans $scope.currentPeriodeId
     * @param idEtablissement identifant de l'établissement concerné
     */
    var setCurrentPeriode = function(idEtablissement) {
        // récupération des périodes et filtre sur celle en cours
        var periodes;
        if(model.me.type === 'PERSRELELEVE'){
            periodes = $scope.searchReleve.eleve.periodes;
        }else{
            periodes = model.periodes;
        }
        periodes.sync(idEtablissement);
        periodes.one('sync', function(){
            var formatStr = "DD/MM/YYYY";
            var momentCurrDate = moment(moment().format(formatStr), formatStr);
            $scope.currentPeriodeId = -1;

            for(var i=0; i<periodes.all.length; i++) {
                var momentCurrPeriodeDebut = moment(moment(periodes.all[i].datedebut).format(formatStr), formatStr);
                var momentCurrPeriodeFin = moment(moment(periodes.all[i].datefin).format(formatStr), formatStr);
                if(momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                    $scope.searchReleve.periode = periodes.findWhere({id : periodes.all[i].id});
                    $scope.$apply();
                    $scope.loadReleveNote();
                }
            }
        });
    };

    if(model.me.type === 'ELEVE'){
        $scope.getPeriodes();
        model.matieres.sync(model.me.userId);
    }else if(model.me.type === 'PERSRELELEVE'){
        model.eleves.one('sync', function(){
            if(model.eleves.all.length > 1){
                template.open('lightboxContainer', '../templates/evaluations/eval_parent_dispenfants');
                $scope.showNoteLightBox.bool = true;
            }else{
                $scope.searchReleve.eleve = model.eleves.all[0];
                $scope.getPeriodes();
                $scope.getMatieres();
            }
        });
    }

    $scope.isCurrentPeriode = function(periode) {
        var isCurrentPeriodeBln = (periode.id === $scope.currentPeriodeId);
        return isCurrentPeriodeBln;
    };

    // Impression du releve de l'eleve
    $scope.getReleve = function() {
        if(model.me.type === 'ELEVE'){
            $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id,$scope.searchReleve.eleve.userId);
        } else {
            $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id,$scope.searchReleve.eleve.id);
        }
    };

    route({
        displayReleveNotes : function(params){
            template.open('main', '../templates/evaluations/eval_parent_dispreleve');
        }
    });

}
