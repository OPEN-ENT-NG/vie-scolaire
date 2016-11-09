import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');

declare let _:any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location', '$filter',
    function ($scope, route, $rootScope, $location, $filter) {
        route({
            accueil : function(params){
                template.open('main', '../templates/evaluations/eval_acu_teacher');
            },

            createDevoir : function(params){
                $scope.createDevoir();
            },

            listDevoirs : function(params){
                if(evaluations.devoirs.all.length === 0){
                    $location.path("/releve");
                    utils.safeApply($scope, null);
                    $location.replace();
                }
                template.open('main', '../templates/evaluations/enseignants/liste_devoirs/display_devoirs_structure');
                template.open('evaluations', '../templates/evaluations/enseignants/liste_devoirs/list_view');
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

                template.open('main', '../templates/evaluations/enseignants/liste_notes_devoir/display_notes_devoir');
                utils.safeApply($scope, null);
            },
            displayReleveNotes : function(params) {
                if (!template.isEmpty('leftSide-userInfo')) template.close('leftSide-userInfo');
                if (!template.isEmpty('leftSide-devoirInfo')) template.close('leftSide-devoirInfo');
                if ($scope.releveNote !== undefined && ($scope.search.matiere.id !== $scope.releveNote.idMatiere
                    || $scope.search.classe.id !== $scope.releveNote.idClasse || $scope.search.periode.id !== $scope.releveNote.idPeriode)) {
                    $scope.releveNote = undefined;
                }
                if ($scope.search.classe !== '*' && $scope.search.matiere.id !== '*' && $scope.search.periode !== '*') {
                    $scope.getReleve();
                }

                template.open('main', '../templates/evaluations/enseignants/releve_notes/display_releve');
                utils.safeApply($scope, null);
            },
            displaySuiviCompetencesEleve : function (params) {
                template.open('main', '../templates/evaluations/enseignants/suivi_competences_eleve/container');
                $scope.informations.eleve = null;
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
            matiere: '*',
            periode : undefined,
            classe : '*',
            sousmatiere : '*',
            type : '*',
            idEleve : '*',
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
                $scope.search.periode = (defaultPeriode !== -1) ? defaultPeriode : '*';
                utils.safeApply($scope, null);
            });
        });

        $scope.initDevoir = function () {
            return new Devoir({
                date_publication  : new Date(),
                date       : new Date(),
                diviseur         : 20,
                coefficient      : 1,
                id_etablissement  : model.me.structures[0],
                ramener_sur       : false,
                id_etat           : 1,
                owner            : model.me.userId,
                matieresByClasse : [],
                controlledDate   : true
            });
        };

        
        $scope.controleDate = function () {
            $scope.devoir.controlledDate = (moment($scope.devoir.date_publication).diff(moment($scope.devoir.date), "days") >= 0);
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
                && $scope.devoir.id_etablissement !== undefined
                && $scope.devoir.id_classe !== undefined
                && $scope.devoir.id_matiere !== undefined
                && $scope.devoir.name !== undefined
                && $scope.devoir.libelle !== undefined
                && $scope.devoir.id_periode !== undefined
                && $scope.devoir.coefficient !== undefined
                && $scope.devoir.coefficient > 0
                && $scope.devoir.diviseur !== undefined
                && $scope.devoir.diviseur > 0
                && $scope.devoir.id_type !== undefined
                && $scope.devoir.ramener_sur !== undefined
                && $scope.devoir.id_etat !== undefined
            );
        };

        $scope.translate = function (key) {
          return utils.translate(key);
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
            var matiere = evaluations.matieres.findWhere({id : $scope.search.matiere.id});
            if (matiere) $scope.selected.matiere = matiere;
        };

        $scope.initFilter = function () {
            $scope.enseignementsFilter = {};
            for (var i = 0; i < $scope.enseignements.all.length; i++) {
                $scope.enseignementsFilter[$scope.enseignements.all[i].id] = true;
            }
        };

        $scope.enseignementsFilterFunction = function (enseignement) {
            return $scope.enseignementsFilter[enseignement.id];
        };


        // TODO a completer / tester / faire l'appel
        $scope.enseignementsSearchFunction = function (enseignement, motClef) {

            if(enseignement.nom.match("/"+motClef+"/i")) {
                enseignement.opened = true;
            }

            $scope.enseignementsSearchFunctionRec(enseignement, motClef);

            return enseignement;
        };

        // TODO a completer / tester /
        $scope.enseignementsSearchFunctionRec = function (item, motClef) {
            if (item.competences != undefined) {
                for (var i = 0; i < item.competences.all.length; i++) {
                    var sousCompetence = item.competences.all[i];
                    if (sousCompetence.nom.match("/" + motClef + "/i")) {
                        sousCompetence.opened = true;
                        item.opened = true;
                    }
                    $scope.enseignementsSearchFunctionRec(sousCompetence, motClef)
                }
            }
        };


        $scope.createDevoir = function () {
            $scope.devoir = $scope.initDevoir();
            //$scope.opened.lightbox = true;
            $scope.controlledDate = (moment($scope.devoir.date_publication).diff(moment($scope.devoir.date), "days") <= 0);
            // resynchronisation de la liste pour eviter les problemes de references et de checkbox precedements cochees
            evaluations.enseignements.sync();
            utils.safeApply(this, null);
            $scope.initFilter();
            _.extend($scope.devoir.enseignements, $scope.enseignements);


            evaluations.competencesDevoir = [];

            $scope.devoir.getLastSelectedCompetence().then(function (res)  {
                $scope.devoir.competencesLastDevoirList = res;

                /*
                // Ajout des competences precedements selectionnees lors du dernier devoir dans le recapitulatif
                if($scope.devoir.competencesLastDevoirList != undefined) {
                    for (var i = 0; i < $scope.devoir.competencesLastDevoirList.length; i++) {
                        evaluations.competencesDevoir.push($scope.devoir.competencesLastDevoirList[i])
                    }
                }
                utils.safeApply($scope, null);
                */
            });
            setCurrentPeriode(model.me.structures[0]).then((defaultPeriode) => {
                $scope.devoir.id_periode = defaultPeriode.id;
                utils.safeApply($scope, null);
            });
            $scope.devoir.id_type = getDefaultTypDevoir();
            if ($scope.search.classe.id !== '*' && $scope.search.matiere !== '*') {
                $scope.devoir.id_classe = $scope.search.classe.id;
                $scope.devoir.id_matiere = $scope.search.matiere.id;
                $scope.setClasseMatieres();
                $scope.selectedMatiere();
            }
            if ($location.path() === "/devoirs/list"){
                $scope.devoir.id_type = $scope.search.type.id;
                $scope.devoir.id_sousmatiere = $scope.search.sousmatiere.id;
            }

            //template.open('lightboxContainer', '../templates/evaluations/enseignants/creation_devoir/display_creation_devoir');
            template.open('main', '../templates/evaluations/enseignants/creation_devoir/display_creation_devoir');
            utils.safeApply($scope, null);
        };


        // on ecoute sur l'evenement checkConnaissances
        // ie on doit ajouter/supprimer toutes les sous competences dans le recap
        $scope.$on('checkConnaissances', function(event, parentItem){
            parentItem.competences.each(function(e){
                if(parentItem.selected === true) {
                    // check si on a pas deja ajoute pour eviter les doublons
                    if(!_.contains(evaluations.competencesDevoir, e)) {
                        evaluations.competencesDevoir.push(e);
                    }
                } else {
                    evaluations.competencesDevoir = _.reject(evaluations.competencesDevoir, function(comp){ return comp.id === e.id; });
                }
            });
        });

        // on ecoute sur l'evenement checkParent
        // ie on doit ajouter la sous competence selectionnee dans le recap
        $scope.$on('checkParent', function(event, parentItem, item){
            if(item.selected === true) {
                // check si on a pas deja ajoute pour eviter les doublons
                if(!_.contains(evaluations.competencesDevoir, item)) {
                    evaluations.competencesDevoir.push(item);
                }
            } else {
                evaluations.competencesDevoir = _.reject(evaluations.competencesDevoir, function(comp){ return comp.id === item.id; });
            }
        });

        $scope.saveNewDevoir = function () {
            /*evaluations.competencesDevoir = [];
            for (var i = 0; i < $scope.devoir.enseignements.all.length; i++) {
                for (var j = 0; j < $scope.devoir.enseignements.all[i].competences.all.length; j++) {
                    $scope.devoir.enseignements.all[i].competences.all[j].findSelectedChildren();
                }
            }

            $scope.devoir.competences = evaluations.competencesDevoir;
             */


            // Pour la sauvegarde on ne recupere que les id des competences
            $scope.devoir.competences = [];
            for (var i = 0; i < evaluations.competencesDevoir.length; i++) {
                $scope.devoir.competences.push(evaluations.competencesDevoir[i].id);
            }

            $scope.devoir.create().then((res) => {
                evaluations.devoirs.sync();
                evaluations.devoirs.on('sync', function () {
                    if($location.path() === "/devoirs/list" || $location.path() === "/devoir/create"){
                        $location.path("/devoir/"+res.id);
                    }else if ($location.path() === "/releve"){
                        if ($scope.releveNote === undefined || !$scope.releveNote) {
                            $scope.search.classe.id = $scope.devoir.id_classe;
                            $scope.search.matiere.id = $scope.devoir.id_matiere;
                            $scope.search.periode.id = $scope.devoir.id_periode;
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
            getClassesMatieres($scope.devoir.id_classe, function (matieres) {
                if ($scope.devoir.matieresByClasse.length === 1) $scope.devoir.id_matiere = $scope.devoir.matieresByClasse[0].id;
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
            if($scope.search.classe.id !== undefined && $scope.search.matiere.id !== undefined
                && $scope.search.periode !== undefined && $scope.search.classe.id !== '*'
                && $scope.search.matiere !== '*' && $scope.search.periode !== '*') {
                var p = {
                    idEtablissement : model.me.structures[0],
                    idClasse : $scope.search.classe.id,
                    idPeriode : parseInt($scope.search.periode.id),
                    idMatiere : $scope.search.matiere.id
                };
                var rn = evaluations.releveNotes.findWhere(p);
                if (rn === undefined) {
                    if(evaluations.synchronized.classes !== 0) {
                        evaluations.classes.on('classes-sync', function () {
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
            var matiere = evaluations.matieres.findWhere({id : $scope.devoir.id_matiere});
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
            return (evaluations.types.findWhere({default_type : true})).id;
        };

        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };

        $scope.getLibelleClasse = function(idClasse) {
            if (idClasse == null || idClasse === "") return "";
            if(evaluations.structures.all.length === 0 || evaluations.structures.all[0].classes.length === 0) return;
            return _.findWhere(evaluations.structures.all[0].classes, {id : idClasse}).name;
        };

        $scope.getLibellePeriode = function(idPeriode) {
            if (idPeriode == null || idPeriode === "") return "";
            return _.findWhere($scope.periodes.all, {id : parseInt(idPeriode)}).libelle;
        };

        $scope.getLibelleType = function(idType) {
            if (idType == null || idType === "") return "";
            return _.findWhere($scope.types.all, {id : parseInt(idType)}).nom;
        };

        $scope.getLibelleMatiere = function(idMatiere) {
            if (idMatiere == null || idMatiere === "") return "";
            return _.findWhere($scope.matieres.all, { id: idMatiere }).name;
        };

        $scope.getLibelleSousMatiere = function(idSousMatiere) {
            if (idSousMatiere == null || idSousMatiere === "") return "";
            return _.findWhere($scope.devoir.matiere.sousMatieres.all, {id : parseInt(idSousMatiere)}).libelle;
        };


        $scope.saveNoteDevoirEleve = function (evaluation, $event, eleve) {
            var reg = /^[0-9]+(\.[0-9]{1,2})?$/;
            if (evaluation.oldValeur !== undefined && evaluation.oldValeur !== evaluation.valeur) {
                if (evaluation.valeur !== "" &&  evaluation.valeur && reg.test(evaluation.valeur) && evaluation.valeur !== null) {
                    var devoir = evaluations.devoirs.findWhere({id : evaluation.id_devoir});
                    if (devoir !== undefined) {
                        if (parseFloat(evaluation.valeur) <= devoir.diviseur && parseFloat(evaluation.valeur) >= 0) {
                            evaluation.save().then(() => {
                                evaluation.oldValeur = evaluation.valeur;
                                if ($location.$$path === '/releve') {
                                    $scope.calculerMoyenneEleve(eleve);
                                    $scope.calculStatsDevoirReleve(evaluation.id_devoir);
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
                            if ($location.$$path === '/releve') {
                                $scope.calculerMoyenneEleve(eleve);
                                $scope.calculStatsDevoirReleve(evaluation.id_devoir);
                            } else {
                                $scope.calculStatsDevoir();
                            }
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
            eleve.getMoyenne().then(() => {
                utils.safeApply($scope, null);
            });
        };

        $scope.getInfoCompetencesDevoir = function () {
            $scope.opened.lightbox = true;
            template.open('lightboxContainer', '../templates/evaluations/enseignants/informations/display_competences');
        };

        $scope.calculStatsDevoir = function () {
            var evals = [];
            for (var i = 0; i < $scope.currentDevoir.eleves.all.length; i++) {
                if ($scope.currentDevoir.eleves.all[i].evaluation.valeur !== '' &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== undefined &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== null) {
                    evals.push($scope.currentDevoir.eleves.all[i].evaluation);
                    evals[i].ramener_sur = $scope.currentDevoir.ramener_sur;
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
                            && $scope.releveNote.classe.eleves.all[i].evaluations.all[j].id_devoir === devoirId) {
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
            if (template.isEmpty('leftSide-devoirInfo')) template.open('leftSide-devoirInfo', '../templates/evaluations/enseignants/informations/display_devoir');
            if (obj instanceof Devoir) $scope.informations.devoir = obj;
            else if (obj instanceof Evaluation) {
                var devoir = $scope.releveNote.devoirs.findWhere({id : obj.id_devoir});
                if (devoir !== undefined) $scope.informations.devoir = devoir;
            }
        };

        $scope.getEleveInfo = function (eleve) {
            if (template.isEmpty('leftSide-userInfo')) template.open('leftSide-userInfo', '../templates/evaluations/enseignants/informations/display_eleve');
            $scope.informations.eleve = eleve;
        };

        var setCurrentPeriode = function (idEtablissement) : Promise<any> {
            return new Promise((resolve, reject) => {
                var formatStr = "DD/MM/YYYY";
                var momentCurrDate = moment(moment().format(formatStr), formatStr);
                $scope.currentPeriodeId = -1;
                for (var i = 0; i < evaluations.periodes.all.length; i++) {
                    var momentCurrPeriodeDebut = moment(moment(evaluations.periodes.all[i].timestamp_dt).format(formatStr), formatStr);
                    var momentCurrPeriodeFin = moment(moment(evaluations.periodes.all[i].timestamp_fn).format(formatStr), formatStr);
                    if(momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                        $scope.currentPeriodeId = evaluations.periodes.all[i].id;
                        if (resolve && typeof (resolve) === 'function') {
                            resolve(evaluations.periodes.all[i]);
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
