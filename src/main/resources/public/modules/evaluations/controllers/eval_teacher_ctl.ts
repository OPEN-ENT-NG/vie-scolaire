import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');

declare let _:any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location', '$filter',
    function ($scope, route, $rootScope, $location, $filter) {
        route({
            listDevoirs : function(params){
                if(evaluations.devoirs.all.length === 0){
                    $location.path("/releve");
                    utils.safeApply($scope, null);
                    $location.replace();
                }
                template.open('main', '../templates/evaluations/eval_teacher_dispdevoirs');
                template.open('evaluations', '../templates/evaluations/eval_teacher_listview');
                evaluations.devoirs.getPercentDone();
                utils.safeApply($scope, null);
            },
            viewNotesDevoir : function(params){
                if(evaluations.devoirs.all.length === 0){
                    $location.path("/releve");
                    utils.safeApply($scope, null);
                    $location.replace();
                }
                if (!template.isEmpty('leftSide-userInfo')) template.close('leftSide-userInfo');
                if (!template.isEmpty('leftSide-devoirInfo')) template.close('leftSide-devoirInfo');
                $scope.currentDevoir = _.findWhere(evaluations.devoirs.all, {id : parseInt(params.devoirId)});
                if ($scope.currentDevoir !== undefined) {
                    $scope.currentDevoir.competences.sync().then(() => {
                        utils.safeApply($scope, null);
                    });
                    $scope.currentDevoir.eleves.sync().then(() => {
                        $scope.$broadcast('initHeaderColumn');
                        var _evals = [];
                        for (var i = 0; i < $scope.currentDevoir.eleves.all.length; i++) {
                            if ($scope.currentDevoir.eleves.all[i].evaluation.valeur !== null && $scope.currentDevoir.eleves.all[i].evaluation.valeur !== undefined
                                && $scope.currentDevoir.eleves.all[i].evaluation.valeur !== "") {
                                _evals.push($scope.currentDevoir.eleves.all[i].evaluation);
                            }
                        }
                        utils.safeApply($scope, null);
                        $scope.currentDevoir.calculStats(_evals).then(() => {
                            utils.safeApply($scope, null);
                        });
                    });
                }

                template.open('main', '../templates/evaluations/eval_teacher_viewnotesdevoirs');
                utils.safeApply($scope, null);
            },
            displayReleveNotes : function(params){
                if (!template.isEmpty('leftSide-userInfo')) template.close('leftSide-userInfo');
                if (!template.isEmpty('leftSide-devoirInfo')) template.close('leftSide-devoirInfo');
                if ($scope.releveNote !== undefined && ($scope.search.idMatiere !== $scope.releveNote.idMatiere
                    || $scope.search.idClasse !== $scope.releveNote.idClasse || $scope.search.idPeriode !== $scope.releveNote.idPeriode)) {
                    $scope.releveNote = undefined;
                }
                if ($scope.search.idClasse !== '*' && $scope.search.idMatiere !== '*' && $scope.search.idMatiere !== '*') {
                    $scope.getReleve();
                }

                template.open('main', '../templates/evaluations/eval_teacher_dispreleve');
                utils.safeApply($scope, null);
            }
        });

        $scope.evaluations = evaluations;

        $scope.devoirs = evaluations.devoirs;
        $scope.enseignements = evaluations.enseignements;
        $scope.matieres = evaluations.matieres;
        $scope.releveNotes = evaluations.releveNotes;
        $scope.releveNote = null;
        $scope.periodes = evaluations.periodes;
        $scope.periodes.sync();
        $scope.classes = evaluations.classes;
        $scope.types = evaluations.types;
        $scope.filter = $filter;
        $scope.template = template;
        $scope.currentDevoir = {};
        $scope.search = {
            idMatiere: '*',
            idPeriode : undefined,
            idClasse : '*',
            idSousMatiere : '*',
            idType : '*',
            name : '',
            dateCreation : {
                debut : moment(),
                fin : moment()
            },
            datePublication : {
                debut : moment(),
                fin : moment()
            }
        };
        $scope.informations = {};
        $scope.me = model.me;
        $scope.opened = {
            devoir : -1,
            note : -1,
            criteres : true,
            details : true,
            statistiques : true,
            studentInfo : true,
            devoirInfo : true,
            lightbox : false
        };
        $scope.selected = {
            devoirs : {
                list : [],
                all : false
            }
        };
        $scope.releveNote = undefined;

        evaluations.classes.on('classes-sync', function () {
            utils.safeApply($scope, null);
        });

        $scope.goTo = function(path){
            $location.path(path);
            $location.replace();
        };

        evaluations.periodes.on('sync', function () {
            setCurrentPeriode(model.me.structures[0]).then((defaultPeriode) => {
                $scope.search.idPeriode = (defaultPeriode !== -1) ? defaultPeriode : '*';
                utils.safeApply($scope, null);
            });
        });

        $scope.initDevoir = function () {
            return new Devoir({
                datePublication  : new Date(),
                dateDevoir       : new Date(),
                diviseur         : 20,
                coefficient      : 1,
                idEtablissement  : model.me.structures[0],
                ramenerSur       : false,
                idEtat           : 1,
                owner            : model.me.userId,
                matieresByClasse : [],
                controlledDate   : true
            });
        };

        $scope.controleDate = function () {
            $scope.devoir.controlledDate = (moment($scope.devoir.datePublication).diff(moment($scope.devoir.dateDevoir), "days") >= 0);
        };

        $scope.selectDevoir = function (devoir) {
            var index = _.indexOf($scope.selected.devoirs.list, devoir);
            if(index === -1){
                $scope.selected.devoirs.list.push(devoir);
            }else{
                $scope.selected.devoirs.list = _.without($scope.selected.devoirs.list, devoir);
            }
        };

        $scope.controleNewDevoirForm = function () {
            return !(
                $scope.devoir.controlledDate
                && $scope.devoir.idEtablissement !== undefined
                && $scope.devoir.idClasse !== undefined
                && $scope.devoir.idMatiere !== undefined
                && $scope.devoir.name !== undefined
                && $scope.devoir.libelle !== undefined
                && $scope.devoir.idPeriode !== undefined
                && $scope.devoir.coefficient !== undefined
                && $scope.devoir.coefficient > 0
                && $scope.devoir.diviseur !== undefined
                && $scope.devoir.diviseur > 0
                && $scope.devoir.idType !== undefined
                && $scope.devoir.ramenerSur !== undefined
                && $scope.devoir.idEtat !== undefined
            );
        };

        $scope.$on('majHeaderColumn', function(event, competence){
            $scope.$broadcast('changeHeaderColumn', competence);
        });

        $scope.getEtablissementName = function(etabId){
            return model.me.structureNames[model.me.structures.indexOf(etabId)];
        };

        $scope.selectListDevoir = function (list) {
            for (var i = 0; i < list.length; i++) {
                list[i].selected = !list[i].selected;
            }
        };

        $scope.selectAllDevoirs = function(){
            if ($scope.selected.devoirs.all !== true) {
                $scope.selectListDevoir($scope.selected.devoirs.list);
                $scope.selected.devoirs.list = [];
                return;
            }
            $scope.selected.devoirs.list = $filter('customSearchFilters')($scope.devoirs.all, $scope.search);
            $scope.selectListDevoir($scope.selected.devoirs.list);
        };

        $scope.getSousMatieres = function () {
            var matiere = evaluations.matieres.findWhere({id : $scope.search.idMatiere});
            if (matiere) $scope.selected.matiere = matiere;
        };

        $scope.createDevoir = function () {
            $scope.devoir = $scope.initDevoir();
            $scope.opened.lightbox = true;
            $scope.controlledDate = (moment($scope.devoir.datePublication).diff(moment($scope.devoir.dateDevoir), "days") <= 0);
            _.extend($scope.devoir.enseignements, evaluations.enseignements);
            $scope.devoir.getLastSelectedCompetence(function (res)  {
                $scope.devoir.competencesLastDevoirList = res;
            });
            setCurrentPeriode(model.me.structures[0]).then((defaultPeriode) => {
                $scope.devoir.idPeriode = defaultPeriode;
                utils.safeApply($scope, null);
            });
            $scope.devoir.idType = getDefaultTypDevoir();
            if ($scope.search.idClasse !== '*' && $scope.search.idMatiere !== '*') {
                $scope.devoir.idClasse = $scope.search.idClasse;
                $scope.devoir.idMatiere = $scope.search.idMatiere;
                $scope.setClasseMatieres();
                $scope.selectedMatiere();
            }
            if ($location.path() === "/devoirs/list"){
                $scope.devoir.idType = $scope.search.idType;
                $scope.devoir.idSousMatiere = $scope.search.idSousMatiere;
            }
            template.open('lightboxContainer', '../templates/evaluations/eval_teacher_adddevoir');
            utils.safeApply($scope, null);
        };

        $scope.saveNewDevoir = function () {
            evaluations.competencesDevoir = [];
            for (var i = 0; i < $scope.devoir.enseignements.all.length; i++) {
                for (var j = 0; j < $scope.devoir.enseignements.all[i].competences.all.length; j++) {
                    $scope.devoir.enseignements.all[i].competences.all[j].findSelectedChildren();
                }
            }
            $scope.devoir.competences = evaluations.competencesDevoir;
            $scope.devoir.create().then((res) => {
                evaluations.devoirs.sync();
                evaluations.devoirs.on('sync', function () {
                    if($location.path() === "/devoirs/list"){
                        $location.path("/devoir/"+res.id);
                    }else if ($location.path() === "/releve"){
                        if ($scope.releveNote === undefined || !$scope.releveNote) {
                            $scope.search.idClasse = $scope.devoir.idClasse;
                            $scope.search.idMatiere = $scope.devoir.idMatiere;
                            $scope.search.idPeriode = $scope.devoir.idPeriode;
                            $scope.getReleve();
                        } else {
                            $scope.releveNote.devoirs.sync();
                        }
                    }
                    $scope.opened.lightbox=false;
                    utils.safeApply($scope, null);
                });
            });
        };
        var getClassesMatieres = function (idClasse, callback) {
            var libelleClasse = _.findWhere(evaluations.structures.all[0].classes, {id : idClasse}).name;
            if (libelleClasse !== undefined) {
                callback(evaluations.matieres.where({libelleClasse: libelleClasse}));
            }
        };

        $scope.setClasseMatieres = function () {
            getClassesMatieres($scope.devoir.idClasse, function (matieres) {
                if ($scope.devoir.matieresByClasse.length === 1) $scope.devoir.idMatiere = $scope.devoir.matieresByClasse[0].id;
                $scope.selectedMatiere();
            });
        };

        $scope.filtrerDevoir = function () {
            var formatStr = "DD/MM/YYYY";

            var dateCreationDebut    = moment(moment($scope.search.dateCreation.debut).format(formatStr), formatStr);
            var dateCreationFin      = moment(moment($scope.search.dateCreation.fin).format(formatStr), formatStr);
            var datePublicationDebut = moment(moment($scope.search.datePublication.debut).format(formatStr), formatStr);
            var datePublicationFin   = moment(moment($scope.search.datePublication.fin).format(formatStr), formatStr);
        };

        model.on('apply', function () {
            utils.safeApply($scope, null);
        });

        $scope.getReleve = function () {
            if($scope.search.idClasse !== undefined && $scope.search.idMatiere !== undefined
                && $scope.search.idPeriode !== undefined && $scope.search.idClasse !== '*'
                && $scope.search.idMatiere !== '*' && $scope.search.idPeriode !== '*') {
                var p = {
                    idEtablissement : model.me.structures[0],
                    idClasse : $scope.search.idClasse,
                    idPeriode : parseInt($scope.search.idPeriode),
                    idMatiere : $scope.search.idMatiere
                };
                var rn = evaluations.releveNotes.findWhere(p)
                if (rn === undefined) {
                    if(evaluations.synchronized.classes !== 0) {
                        evaluations.classes.on('classes-sync', function () {
                            var releve = new ReleveNote(p);
                            evaluations.releveNotes.push(releve);
                            $scope.releveNote = releve;
                            $scope.releveNote.sync(function () {
                                utils.safeApply($scope, null);
                            });
                        });
                        return;
                    }
                    var releve = new ReleveNote(p);
                    evaluations.releveNotes.push(releve);
                    $scope.releveNote = releve;
                    $scope.releveNote.sync().then(() => {
                        $scope.releveNote.synchronized.releve = true;
                        $scope.releveNote.calculStatsDevoirs().then(() => {
                            utils.safeApply($scope, null);
                        });
                        $scope.releveNote.calculMoyennesEleves().then(() => {
                            utils.safeApply($scope, null);
                        });
                    });
                } else {
                    $scope.releveNote = rn;
                    utils.safeApply($scope, null);
                }
            }
        };

        $scope.selectedMatiere = function () {
            var matiere = evaluations.matieres.findWhere({id : $scope.devoir.idMatiere});
            if (matiere !== undefined) $scope.devoir.matiere = matiere;
        };

        $scope.controleDatePicker = function () {
            if (moment($scope.search.dateCreation.debut).diff(moment($scope.search.dateCreation.fin)) > 0) {
                $scope.search.dateCreation.fin = moment();
            }
            if (moment($scope.search.datePublication.debut).diff(moment($scope.search.datePublication.fin)) > 0) {
                $scope.search.datePublication.fin = moment();
            }
            utils.safeApply($scope, null);
        };

        var getDefaultTypDevoir = function(){
            return (evaluations.types.findWhere({default : true})).id;
        };

        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };

        $scope.getLibelleClasse = function(idClasse) {
            if(evaluations.structures.all.length === 0 || evaluations.structures.all[0].classes.length === 0) return;
            return _.findWhere(evaluations.structures.all[0].classes, {id : idClasse}).name;
        };

        $scope.saveNoteDevoirEleve = function (evaluation, $event, eleve) {
            var reg = /^[0-9]+(\.[0-9]{1,2})?$/;
            if (evaluation.oldValeur !== undefined && evaluation.oldValeur !== evaluation.valeur) {
                if (evaluation.valeur !== "" &&  evaluation.valeur && reg.test(evaluation.valeur) && evaluation.valeur !== null) {
                    var devoir = evaluations.devoirs.findWhere({id : evaluation.iddevoir});
                    if (devoir !== undefined) {
                        if (parseFloat(evaluation.valeur) <= devoir.diviseur && parseFloat(evaluation.valeur) >= 0) {
                            evaluation.save().then(() => {
                                evaluation.oldValeur = evaluation.valeur;
                                if ($location.$$path === '/releve') {
                                    $scope.calculerMoyenneEleve(eleve);
                                    $scope.calculStatsDevoirReleve(evaluation.iddevoir);
                                } else {
                                    $scope.calculStatsDevoir();
                                }
                                utils.safeApply($scope, null);
                            });
                        } else {
                            notify.error(lang.translate("error.note.outbound")+devoir.diviseur);
                            evaluation.valid = false;
                            $event.target.focus();
                            return;
                        }
                    }
                } else {
                    if (evaluation.id !== undefined && evaluation.valeur === "") {
                        evaluation.delete().then(() => {
                            $scope.calculerMoyenneEleve(eleve);
                            $scope.calculStatsDevoirReleve(evaluation.iddevoir);
                            utils.safeApply($scope, null);
                        });
                    } else {
                        if (evaluation.valeur !== "") {
                            notify.error(lang.translate("error.note.invalid"));
                            evaluation.valid = false;
                            $event.target.focus();
                        }
                    }
                }
            }
        };

        $scope.expand = function(index, bool){
            if($scope.openedDevoir !== index){
                $scope.openedDevoir = index;
            }else{
                if(bool === true){
                    $scope.openedDevoir = -1;
                }
            }
        };

        $scope.calculerMoyenneEleve = function(eleve) {
            eleve.getMoyenne(function () {
                utils.safeApply($scope, null);
            });
        };

        $scope.getInfoCompetencesDevoir = function () {
            $scope.opened.lightbox = true;
            template.open('lightboxContainer', '../templates/evaluations/eval_teacher_dispcompinfo');
        };

        $scope.calculStatsDevoir = function () {
            var evals = [];
            for (var i = 0; i < $scope.currentDevoir.eleves.all.length; i++) {
                if ($scope.currentDevoir.eleves.all[i].evaluation.valeur !== '' &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== undefined &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== null) {
                    evals.push($scope.currentDevoir.eleves.all[i].evaluation);
                    evals[i].ramenersur = $scope.currentDevoir.ramenersur;
                }
            }
            $scope.currentDevoir.calculStats(evals).then(() => {
                utils.safeApply($scope, null);
            });
        };

        $scope.calculStatsDevoirReleve = function (devoirId) {
            var devoir = $scope.releveNote.devoirs.findWhere({id : devoirId});
            if (devoir !== undefined) {
                var evals = [];
                for (var i = 0; i < $scope.releveNote.classe.eleves.all.length; i++) {
                    for (var j = 0; j < $scope.releveNote.classe.eleves.all[i].evaluations.all.length; j++) {
                        if ($scope.releveNote.classe.eleves.all[i].evaluations.all[j].valeur !== ""
                            && $scope.releveNote.classe.eleves.all[i].evaluations.all[j].iddevoir === devoirId) {
                            evals.push($scope.releveNote.classe.eleves.all[i].evaluations.all[j]);
                        }
                    }
                }
                devoir.calculStats(evals).then(() => {
                    utils.safeApply($scope, null);
                });
            }
        };

        $scope.focusMe = function($event) {
            $event.target.select();
        };

        $scope.getDevoirInfo = function (obj) {
            if (template.isEmpty('leftSide-devoirInfo')) template.open('leftSide-devoirInfo', '../templates/evaluations/eval_teacher_dispdevoirinfo');
            if (obj instanceof Devoir) $scope.informations.devoir = obj;
            else if (obj instanceof Evaluation) {
                var devoir = $scope.releveNote.devoirs.findWhere({id : obj.iddevoir});
                if (devoir !== undefined) $scope.informations.devoir = devoir;
            }
        };

        $scope.getEleveInfo = function (eleve) {
            if (template.isEmpty('leftSide-userInfo')) template.open('leftSide-userInfo', '../templates/evaluations/eval_teacher_dispeleveinfo');
            $scope.informations.eleve = eleve;
        };

        var setCurrentPeriode = function (idEtablissement) : Promise<any> {
            return new Promise((resolve, reject) => {
                var formatStr = "DD/MM/YYYY";
                var momentCurrDate = moment(moment().format(formatStr), formatStr);
                $scope.currentPeriodeId = -1;
                for (var i = 0; i < evaluations.periodes.all.length; i++) {
                    var momentCurrPeriodeDebut = moment(moment(evaluations.periodes.all[i].datedebut).format(formatStr), formatStr);
                    var momentCurrPeriodeFin = moment(moment(evaluations.periodes.all[i].datefin).format(formatStr), formatStr);
                    if(momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                        $scope.currentPeriodeId = evaluations.periodes.all[i].id;
                        if (resolve && typeof (resolve) === 'function') {
                            resolve(evaluations.periodes.all[i].id);
                        }
                    }
                }
                if (resolve && typeof (resolve) === 'function') {
                    resolve($scope.currentPeriodeId);
                }
            });
        };
    }
]);
