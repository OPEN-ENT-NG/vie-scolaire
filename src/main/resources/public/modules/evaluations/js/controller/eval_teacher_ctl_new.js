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
 * Created by ledunoiss on 10/08/2016.
 */
routes.define(function($routeProvider){
    $routeProvider
        .when('/devoirs/list',{action:'listDevoirs'})
        .when('/devoir/:devoirId', {action:'viewNotesDevoir'})
        .when('/releve', {action:'displayReleveNotes'})
        .otherwise({
            redirectTo : '/releve'
        });
});

function EvaluationsController($scope, $rootScope, $location, model, template, route, date, $filter) {
    $scope.devoirs = model.devoirs;
    $scope.enseignements = model.enseignements;
    $scope.matieres = model.matieres;
    $scope.releveNotes = model.releveNotes;
    $scope.releveNote = null;
    $scope.periodes = model.periodes;
    $scope.classes = model.classes;
    $scope.types = model.types;
    $scope.filter = $filter;
    $scope.template = template;
    $scope.search = {
        idMatiere: '*',
        idPeriode : undefined,
        idClasse : '*',
        idSousMatiere : '*',
        idType : '*',
        name : ''
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
        createDevoir : false
    };
    $scope.selected = {
        devoirs : []
    };

    model.periodes.on('sync', function () {
        setCurrentPeriode(model.me.structures[0], function (defaultPeriode) {
            $scope.search.idPeriode = (defaultPeriode !== -1) ? defaultPeriode : '*';
            $scope.safeApply();
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
    }

    $scope.controleDate = function () {
        $scope.devoir.controlledDate = (moment($scope.devoir.datePublication).diff(moment($scope.devoir.dateDevoir), "days") >= 0);
    };

    $scope.selectDevoir = function (devoir) {
        var index = _.indexOf($scope.selected.devoirs, devoir);
        if(index === -1){
            $scope.selected.devoirs.push(devoir);
        }else{
            $scope.selected.devoirs = _.difference($scope.selected.devoirs, devoir);
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

    // Fonction permettant d'éviter l'erreur de $diggest
    $scope.safeApply = function (fn) {
        var phase = this.$root.$$phase;
        if(phase === '$apply' || phase === '$digest') {
            if(fn && (typeof(fn) === 'function')) fn();
        } else this.$apply(fn);
    };

    $scope.getEtablissementName = function(etabId){
        return model.me.structureNames[model.me.structures.indexOf(etabId)];
    };

    $scope.selectAllDevoirs = function(){
        console.log($filter('customSearchFilters')($scope.devoirs, $scope.search));
    };

    $scope.getSousMatieres = function () {
        var matiere = model.matieres.findWhere({id : $scope.search.idMatiere});
        if (matiere) $scope.selected.matiere = matiere;
    };

    $scope.createDevoir = function () {
        $scope.opened.createDevoir = true;
        $scope.controlledDate = (moment($scope.devoir.datePublication).diff(moment($scope.devoir.dateDevoir), "days") <= 0);
        _.extend($scope.devoir.enseignements, model.enseignements);
        $scope.devoir.getLastSelectedCompetence(function (res)  {
            $scope.devoir.competencesLastDevoirList = res;
        });
        setCurrentPeriode(model.me.structures[0], function (defaultPeriode) {
            $scope.devoir.idPeriode = defaultPeriode;
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
        // else{
        //     $scope.devoir.idClasse = $scope.search.idclasse;
        //     $scope.devoir.idMatiere = $scope.search.idmatiere;
        //     $scope.setClasseMatieres($scope.search.idmatiere);
        //     $scope.devoir.idSousMatiere = $scope.search.idsousmatiere;
        // }
        template.open('lightboxContainer', '../modules/evaluations/template/eval_teacher_adddevoir');
        $scope.safeApply();
    };

    $scope.saveNewDevoir = function () {
        model.competencesDevoir = [];
        for (var i = 0; i < $scope.devoir.enseignements.all.length; i++) {
            for (var j = 0; j < $scope.devoir.enseignements.all[i].competences.all.length; j++) {
                $scope.devoir.enseignements.all[i].competences.all[j].findSelectedChildren();
            }
        }
        $scope.devoir.competences = model.competencesDevoir;
        $scope.devoir.create(function (res) {
            model.devoirs.sync();
            model.devoirs.on('sync', function () {
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
                $scope.opened.createDevoir=false;
            });
        });
    }
    var getClassesMatieres = function (idClasse, callback) {
        var libelleClasse = _.findWhere(model.structures.all[0].classes, {id : idClasse}).name;
        if (libelleClasse !== undefined) {
            callback(model.matieres.where({libelleClasse: libelleClasse}));
        }
    };

    $scope.setMatieresFilter = function () {
        if ($scope.search.idClasse === "*") {

        }
    };

    $scope.setClasseMatieres = function () {
        getClassesMatieres($scope.devoir.idClasse, function (matieres) {
            $scope.devoir.matieresByClasse
            if ($scope.devoir.matieresByClasse.length === 1) $scope.devoir.idMatiere = $scope.devoir.matieresByClasse[0].id;
            $scope.selectedMatiere();
        });
    };

    model.on('apply', function () {
        $scope.safeApply();
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
            var rn = model.releveNotes.findWhere(p)
            if (rn === undefined) {
                if(model.synchronized.classes !== 0) {
                    model.classes.on('classes-sync', function () {
                        var releve = new ReleveNote(p);
                        model.releveNotes.push(releve);
                        $scope.releveNote = releve;
                        $scope.releveNote.sync(function () {
                            $scope.safeApply();
                        });
                    });
                    return;
                }
                var releve = new ReleveNote(p);
                model.releveNotes.push(releve);
                $scope.releveNote = releve;
                $scope.releveNote.sync(function () {
                    $scope.safeApply();
                });
            } else {
                $scope.releveNote = rn;
                $scope.safeApply();
            }
        }
    };

    $scope.selectedMatiere = function () {
        var matiere = model.matieres.findWhere({id : $scope.devoir.idMatiere});
        if (matiere !== undefined) $scope.devoir.matiere = matiere;
    };

    var getDefaultTypDevoir = function(){
        return (model.types.findWhere({default : true})).id;
    };

    $scope.getDateFormated = function (date) {
        return Behaviours.applicationsBehaviours["viescolaire.evaluations"].getFormatedDate(date, "DD/MM/YYYY");
    };

    $scope.getLibelleClasse = function(idClasse) {
        if(model.structures.all.length === 0 || model.structures.all[0].classes.length === 0) return;
        return _.findWhere(model.structures.all[0].classes, {id : idClasse}).name;
    };

    $scope.saveNoteDevoirEleve = function (evaluation, $event) {
        var reg = /^[0-9]+(\.[0-9]{1,2})?$/;
        if (evaluation.valeur !== "" &&  evaluation.valeur && reg.test(evaluation.valeur)) {
            var devoir = model.devoirs.findWhere({id : evaluation.iddevoir});
            if (devoir !== undefined) {
                if (parseFloat(evaluation.valeur) <= devoir.diviseur && parseFloat(evaluation.valeur) >= 0) {
                    evaluation.save($scope.safeApply());
                } else {
                    notify.error(lang.translate("error.note.outbound")+devoir.diviseur);
                    evaluation.valid = false;
                    $event.target.focus();
                    return;
                }
            }
        } else {
            if (evaluation.id !== undefined && evaluation.valeur === "") {
                evaluation.delete($scope.safeApply());
            } else {
                if (evaluation.valeur !== "") {
                    notify.error(lang.translate("error.note.invalid"));
                    evaluation.valid = false;
                    $event.target.focus();
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
            $scope.safeApply();
        });
    }

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
            devoir.calculStats(evals, function () {
                $scope.safeApply();
            });
        }
    };

    $scope.focusMe = function($event) {
        $event.target.select();
    };

    $scope.getDevoirInfo = function (obj) {
        if (template.isEmpty('leftSide-devoirInfo')) template.open('leftSide-devoirInfo', '../modules/evaluations/template/eval_teacher_dispdevoirinfo');
        if (obj instanceof Devoir) $scope.informations.devoir = obj;
        else if (obj instanceof Evaluation) {
            var devoir = $scope.releveNote.devoirs.findWhere({id : obj.iddevoir});
            if (devoir !== undefined) $scope.informations.devoir = devoir;
        }
    };

    $scope.getEleveInfo = function (eleve) {
        if (template.isEmpty('leftSide-userInfo')) template.open('leftSide-userInfo', '../modules/evaluations/template/eval_teacher_dispeleveinfo');
        $scope.informations.eleve = eleve;
    }

    var setCurrentPeriode = function (idEtablissement, callback) {
        var formatStr = "DD/MM/YYYY";
        var momentCurrDate = moment(moment().format(formatStr), formatStr);
        $scope.currentPeriodeId = -1;
        for (var i = 0; i < model.periodes.all.length; i++) {
            var momentCurrPeriodeDebut = moment(moment(model.periodes.all[i].datedebut).format(formatStr), formatStr);
            var momentCurrPeriodeFin = moment(moment(model.periodes.all[i].datefin).format(formatStr), formatStr);
            if(momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                $scope.currentPeriodeId = model.periodes.all[i].id;
                callback(model.periodes.all[i].id);
            }
        }
        callback($scope.currentPeriodeId);
    };

    $scope.devoir = $scope.initDevoir();
    route({
        listDevoirs : function(params){
            template.open('main', '../modules/evaluations/template/eval_teacher_dispdevoirs');
            template.open('evaluations', '../modules/evaluations/template/eval_teacher_listview');
        },
        viewNotesDevoir : function(params){

        },
        displayReleveNotes : function(params){
            if (!template.isEmpty('leftSide-userInfo')) template.close('leftSide-userInfo');
            if (!template.isEmpty('leftSide-devoirInfo')) template.close('leftSide-devoirInfo');
            if ($scope.search.idClasse !== '*' && $scope.search.idMatiere !== '*' && $scope.search.idMatiere !== '*') {
                $scope.getReleve();
            }

            template.open('main', '../modules/evaluations/template/eval_teacher_dispreleve');
        }
    });
}