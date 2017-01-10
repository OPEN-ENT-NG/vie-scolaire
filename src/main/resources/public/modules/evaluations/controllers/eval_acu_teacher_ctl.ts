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

    }
]);