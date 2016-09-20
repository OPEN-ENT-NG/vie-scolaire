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

import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {evaluations, Periode, Evaluations, Devoir, Eleve, Matiere} from '../models/eval_parent_mdl';

let moment = require('moment');

declare let _:any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        route({
            displayReleveNotes : function(params){
                template.open('main', '../templates/evaluations/eval_parent_dispreleve');
            }
        });

        $scope.searchReleve = {
            eleve : null,
            periode : null
        };

        $scope.showNoteLightBox = {
            bool : false
        };

        if(model.me.type === "PERSRELELEVE"){
            $scope.eleves = evaluations.eleves;
        }else{
            $scope.periodes = evaluations.periodes;
            $scope.searchReleve.eleve = model.me;
        }
        $scope.matieres = evaluations.matieres;
        $scope.me = model.me;

        $scope.getPeriodes = function () {
            if($scope.searchReleve.eleve !== null){
                // TODO Recuperer l'etablissement courant et non le 1er etab dans la liste
                setCurrentPeriode($scope.searchReleve.eleve.structures[0]);
            }
        };

        $scope.getMatieres = function(){
            if($scope.searchReleve.eleve !== null){
                evaluations.matieres.sync($scope.searchReleve.eleve.id);
            }
        };

        $scope.getFormatedDate = function(date){
            return moment(date).format("DD/MM/YYYY");
        };

        $scope.noteMatiereEleve = function(idMatiere){
            return $scope.dataReleve.devoirs.findWhere({ idmatiere : idMatiere });
        };

        $scope.chooseChild = function(idEleve){
            $scope.searchReleve.eleve = evaluations.eleves.findWhere({id : idEleve});
            $scope.getPeriodes();
            $scope.getMatieres();
            $scope.showNoteLightBox.bool = false;
        };

        $scope.calculMoyenne = function(idMatiere){
            var notes = $scope.dataReleve.devoirs.where({idmatiere : idMatiere});
            if(notes !== undefined){
                $scope.searchReleve.periode.calculMoyenne().then((moyenne) => {
                    return moyenne;
                });
            }else{
                return;
            }
        };

        $scope.$on('syncPeriodes', function(){
            setCurrentPeriode(model.me.structures[0]);
            $scope.safeApply();
        });

        $scope.loadReleveNote = function(){
            var periode;
            if($scope.searchReleve.periode !== null) {
                if(model.me.type === 'PERSRELELEVE'){
                    var eleve = evaluations.eleves.findWhere({id : $scope.searchReleve.eleve.id});
                    periode = eleve.periodes.findWhere({id : $scope.searchReleve.periode.id});
                    periode.devoirs.sync($scope.searchReleve.eleve.structures[0], $scope.searchReleve.eleve.id);
                    periode.devoirs.on('sync', function(){
                        $scope.dataReleve = periode;
                    });
                }else{
                    periode = evaluations.periodes.findWhere({id : $scope.searchReleve.periode.id});
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
                periodes = evaluations.periodes;
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
                        $scope.safeApply();
                        $scope.loadReleveNote();
                    }
                }
            });
        };

        if(model.me.type === 'ELEVE'){
            $scope.getPeriodes();
            evaluations.matieres.sync(model.me.userId);
        }else if(model.me.type === 'PERSRELELEVE'){
            evaluations.eleves.one('sync', function(){
                if(evaluations.eleves.all.length > 1){
                    template.open('lightboxContainer', '../templates/evaluations/eval_parent_dispenfants');
                    $scope.showNoteLightBox.bool = true;
                }else{
                    $scope.searchReleve.eleve = evaluations.eleves.all[0];
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
    }
]);