import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');
declare let _:any;

export let evalAcuTeacherController = ng.controller('EvalAcuTeacherController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        $scope.evaluations = evaluations;
        $scope.devoirs= [];

        // TODO chercher ses evaluations non terminees
        $scope.evaluations.devoirs.getPercentDone();
        for(var i=0; i< $scope.evaluations.devoirs.all.length; i++){
            if($scope.evaluations.devoirs.all[i].percent != 100){
                $scope.devoirs.push($scope.evaluations.devoirs.all[i]);
                utils.safeApply($scope);
            }
        }
        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };


        /**
         * Retourne un tableau contenant les informations des devoirs pour une classe donnée et un critère donné
         * @param idClasse identifiant de la classe
         * @param information critère
         * @returns {any[]} tableau contenant les informations
         */
        $scope.getDevoirsInformations = function (idClasse : string, information : string) : any[] {
            if (!evaluations.devoirs.percentDone) return;
            let devoirs = _.where($scope.devoirs, {id_classe : idClasse});
            devoirs = _.filter(devoirs, devoir => (devoir.percent !== undefined && devoir.percent !== 100));
            return _.pluck(devoirs, information);
        };

        /**
         *  Options des graphiques
         */
        $scope.optionsGraphics = {
            scales: {
                yAxes: [{
                    ticks: {
                        size: 0,
                        max: 100,
                        min: 0,
                        stepSize: 20,
                    },
                }],
                xAxes: [{
                    display:false,

                }]
            }
        }
    }
]);