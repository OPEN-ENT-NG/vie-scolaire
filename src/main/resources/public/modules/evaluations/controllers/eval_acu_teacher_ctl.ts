import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');
declare let _:any;

export let evalAcuTeacherController = ng.controller('EvalAcuTeacherController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        // TODO chercher ses evaluations non terminees
    }
]);