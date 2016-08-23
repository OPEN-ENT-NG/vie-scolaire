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
/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
    $routeProvider
        .when('/devoirs/list',{action:'listDevoirs'})
        .when('/devoir/:devoirId', {action:'viewNotesDevoir'})
        .when('/releve', {action:'displayReleveNotes'})
        .otherwise({
            redirectTo : '/releve'
        });
});

/**
 Wrapper controller
 ------------------
 Main controller.
 **/
function EvaluationsController($scope, $rootScope, $location, model, template, route, date, $filter){
    $scope.showNoteLightBox = {
        bool : false
    };
    $scope.selectedDevoirs = [];
    $scope.devoirs = model.devoirs;
    $scope.notes = model.notes;
    $scope.me = model.me;
    $scope.enseignements = model.enseignements;
    $scope.devoir = new Devoir({
        datePublication : new Date(),
        dateDevoir      : new Date(),
        diviseur        : 20,
        coefficient     : 1,
        idEtablissement : model.me.structures[0],
        ramenerSur      : false
    });
    $scope.date = {creation : {},publication : {}};
    $scope.search = {
        idclasse      : "*",
        idmatiere     : "*",
        idsousmatiere : "*",
        idtype        : "*",
        idperiode     : "*"
    };
    $scope.searchReleve = {
        idclasse  : "*",
        idmatiere : "*",
        idperiode : "*"
    };

    $scope.selected = {
        allDevoir : false
    };

    $scope.controlledDate = false;

    $scope.openedDevoir = -1;
    $scope.openedNote = -1;
    $scope.openedCriteres = true;
    $scope.openedDetails = true;
    $scope.openedStatistiques = true;
    $scope.openedStudentInfo = true;
    $scope.openedDevoirInfo = true;
    $scope.template = template;
    template.open('test', '../modules/evaluations/template/eval_teacher_listview');

    $scope.alert = function(message){
        alert(message);
    };

    $scope.goTo = function(path){
        $location.path(path);
        $location.replace();
    };

    $scope.rand = function(devoir){
        devoir.percent = Math.floor((Math.random() * 100));
    };

    $scope.selectDevoir = function(devoir){
        var index = _.indexOf($scope.selectedDevoirs, devoir);
        if(index === -1){
            $scope.selectedDevoirs.push(devoir);
        }else{
            $scope.selectedDevoirs = _.difference($scope.selectedDevoirs, devoir);
        }
    };

    $scope.selectAllDevoir = function(){
        $scope.selected.allDevoir = !$scope.selected.allDevoir;
        _.each($scope.devoirsFiltres, function(devoir){
            devoir.selected = $scope.selected.allDevoir;
            $scope.selectedDevoirs.push(devoir);
        });
        if($scope.selected.allDevoir === false){
            $scope.selectedDevoirs = [];
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

    $scope.expandNote = function(index){
        if($scope.openedNote !== index){
            $scope.openedNote = index;
        }
    };

    $scope.$on('getSelectedAllDevoir', function($event){
        $scope.selectAllDevoir();
    });

    //$scope.$on('selectAllCompetences', function($event, evaluation){
    //	$scope.$broadcast('allCompetences', evaluation);
    //});
    $scope.$on('majHeaderColumn', function(event, competence){
        $scope.$broadcast('changeHeaderColumn', competence);
    });
    $scope.matiereParClasse = function(matiere){
        return $scope.devoir.idClasse !== undefined
            && _.findWhere($scope.matieres, {libelleClasse : _.findWhere($scope.classes, {id : $scope.devoir.idClasse }).name, id: matiere.id	}) !== undefined;
    };

    $scope.setClasseMatieres = function(){
        if($scope.etabMatiereDevoir !== undefined){
            if($scope.devoir.idClasse !== undefined && $scope.devoir.idClasse !=="*"){
                var libelleClasse = _.findWhere($scope.classes, {id : $scope.devoir.idClasse});
                $scope.listeMatieres = _.where($scope.etabMatiereDevoir, {libelleClasse : libelleClasse.name});
                $scope.devoir.idMatiere = $scope.listeMatieres[0].id;
                $scope.addDevoirGetSousMatiere();
            }
        }else{
            loadMatieres(model.me.userId,function(res){
                $scope.etabMatiereDevoir = _.where(res, {idEtablissement : $scope.devoir.idEtablissement});
                $scope.$apply();
                if($scope.devoir.idClasse !== undefined){
                    var libelleClasse = _.findWhere($scope.classes, {id : $scope.devoir.idClasse});
                    $scope.listeMatieres = _.where($scope.etabMatiereDevoir, {libelleClasse : libelleClasse.name});
                    $scope.devoir.idMatiere = $scope.listeMatieres[0].id;
                    $scope.addDevoirGetSousMatiere();
                }
            });
        }
    };

    $scope.setMatieresFilter = function(){
        if($scope.matieres !== undefined){
            if($scope.search.idclasse === "*"){
                $scope.listeDevoirMatieres = $scope.matieres;
            }else{
                $scope.listeDevoirMatieres = _.where($scope.matieres, { libelleClasse : (_.findWhere($scope.classes, {id : $scope.search.idclasse}).name)});
            }
            $scope.safeApply();
        }else{
            loadMatieres(model.me.userId, function(res){
                $scope.matieres = JSON.parse(res);
                if($scope.search.idclasse === "*"){
                    $scope.listeDevoirMatieres = $scope.matieres;
                }else{
                    $scope.listeDevoirMatieres = _.where($scope.matieres, { libelleClasse : (_.findWhere($scope.classes, {id : $scope.search.idclasse}).name)});
                }
                $scope.$apply();
            });
        }
    };

    /**
     * Charge la liste des periodes dans $scope.periodes et détermine la période en cours et positionne
     * son id dans $scope.notesiodeId
     * @param idEtablissement identifant de l'établissement concerné
     */
    var setCurrentPeriode = function(idEtablissement, callback) {
        // récupération des périodes et filtre sur celle en cours
        http().getJson('/viescolaire/evaluations/periodes?idEtablissement='+idEtablissement).done(function(res){
            $scope.periodes = res;
            $scope.$apply();

            var formatStr = "DD/MM/YYYY";
            var momentCurrDate = moment(moment().format(formatStr), formatStr);
            $scope.currentPeriodeId = -1;

            for(var i=0; i<$scope.periodes.length; i++) {
                var momentCurrPeriodeDebut = moment(moment($scope.periodes[i].datedebut).format(formatStr), formatStr);
                var momentCurrPeriodeFin = moment(moment($scope.periodes[i].datefin).format(formatStr), formatStr);
                if(momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                    $scope.currentPeriodeId = $scope.periodes[i].id;
                    callback($scope.periodes[i].id);
                }
            }
        });
    };

    /**
     * Renvoie le type de devoir par défaut dans l'établissement courant
     * @param liste des types
     * @param le callback de retour
     */
    var getDefaultType = function(types){
        return _.findWhere(types, {default : true}).id;
    };

    setCurrentPeriode(model.me.structures[0], function(defaultPeriode){
        /*
         initialisation de la période en cours sur tous les écrans :
         - Filtres
         - Ecran de création d'un devoir
         - Revelé
         */
        $scope.search.idperiode = defaultPeriode;
        $scope.searchReleve.idperiode = defaultPeriode;
        $scope.$apply();
    });

    model.devoirs.on('sync', function(){
        // Copie de la liste des devoirs pour pouvoir filtrer dessus
        $scope.devoirsFiltres = model.devoirs.all;
    });

    $scope.addNewDevoir = {};

    $scope.splitClasse = function(classe){
        return classe.split("$")[1];
    };

    $scope.setRamenerSur = function(){
        if($scope.devoir.diviseur !== 20){
            $scope.devoir.ramenerSur = true;
        }
    };

    $scope.getEtablissementName = function(etabId){
        return model.me.structureNames[model.me.structures.indexOf(etabId)];
    };

    $scope.updateEtabInfo = function(){
        http().getJson('/viescolaire/evaluations/types?idEtablissement='+$scope.devoir.idEtablissement).done(function(res){
            $scope.etabTypeDevoir = res;
            $scope.devoir.type = getDefaultType(res);
            $scope.$apply();
        });
        http().getJson('/viescolaire/evaluations/periodes?idEtablissement='+$scope.devoir.idEtablissement).done(function(res){
            $scope.etabPeriodeDevoir = res;
            $scope.$apply();
        });
        $scope.etabSousMatiereDevoir = model.matieres.where({idEtablissement : $scope.devoir.idEtablissement});
    };

    $scope.getSousMatieres = function() {
        if($scope.search.idmatiere !== '*') {
            loadSousMatieres($scope.search.idmatiere, model.me.structures[0], function(res){
                $scope.sousMatieres = res;
                $scope.$apply();
            });
        }
    };

    loadMatieres = function(userId, callback){
        http().getJson('/viescolaire/evaluations/matieres?idEnseignant='+model.me.userId).done(callback);
    };

    loadSousMatieres = function(idMatiere, idEtablissement, callback){
        var res = model.matieres.findWhere({id : idMatiere});
        callback(res.sousMatieres.all);
    };

    $scope.addDevoirGetSousMatiere = function(){
        loadSousMatieres($scope.devoir.idMatiere, $scope.devoir.idEtablissement, function(res){
            $scope.etabSousMatiereDevoir = res;
            $scope.safeApply();
        });
    };

    $scope.createDevoir = function(){

        model.competencesDevoir = [];

        // pour chaque enseignements on recupere les competences selectionnees
        $scope.devoir.enseignements.each(function(enseignement){
            enseignement.competences.each(function(competence){
                    competence.findSelectedChildren();
                }
            );


        });

        var devoir             = new Devoir();
        devoir.name            = $scope.devoir.name;
        devoir.owner           = model.me.userId;
        devoir.libelle         = $scope.devoir.libelle;
        devoir.idClasse        = $scope.devoir.idClasse;
        devoir.idSousMatiere   = $scope.devoir.idSousMatiere;
        devoir.idPeriode       = $scope.devoir.periode;
        devoir.idType          = $scope.devoir.type;
        devoir.idMatiere       = $scope.devoir.idMatiere;
        devoir.idEtat          = 1;
        devoir.datePublication = $scope.devoir.datePublication;
        devoir.idEtablissement = $scope.devoir.idEtablissement;
        devoir.diviseur        = $scope.devoir.diviseur;
        devoir.coefficient     = $scope.devoir.coefficient;
        devoir.dateDevoir      = $scope.devoir.dateDevoir;
        devoir.ramenerSur      = $scope.devoir.ramenerSur;
        devoir.competences     = model.competencesDevoir;
        devoir.create(function(res){
            model.devoirs.sync();
            model.devoirs.on("sync", function(){
                if($location.path() === "/devoirs/list"){
                    $location.path("/devoir/"+res.id);
                }else if ($location.path() === "/releve"){
                    $scope.searchReleve.idclasse  = $scope.devoir.idClasse;
                    $scope.searchReleve.idmatiere = $scope.devoir.idMatiere;
                    $scope.searchReleve.idperiode = $scope.devoir.periode;
                    if($scope.releveNote === undefined){
                        $scope.releveNote = new ReleveNote({idEtablissement : $scope.devoir.idEtablissement, idClasse : $scope.devoir.idClasse, idPeriode : $scope.devoir.periode, idMatiere : $scope.devoir.idMatiere});
                    }
                    $scope.releveNote.sync();
                    $scope.submitFilterReleveDeNotes();
                    $scope.filterClassesMatieres();
                }
                $scope.devoir = {
                    datePublication : new Date(),
                    dateDevoir      : new Date(),
                    diviseur        : 20,
                    coefficient     : 1,
                    idEtablissement : model.me.structures[0],
                    ramenerSur      : false,
                    enseignements 	: new Collection()
                };
                $scope.$apply();
                $location.replace();
                $scope.showNoteLightBox.bool = false;
            });
        });
    };

    // Initialise la note de la case du relevé si celle-ci est null
    $scope.initNoteReleve = function(note){
        if(note.valeur === null){
            note.valeur = "";
        }
    };
    // Sauvegarde de la note d'un eleve pour un devoir donne
    $scope.saveNoteDevoirEleve = function(noteDevoirEleve, $event) {
        var reg = /^[0-9]+(\.[0-9]{1,2})?$/;
        setTimeout(function(){
            if(noteDevoirEleve.valeur !== "" && noteDevoirEleve.valeur !== null && reg.test(noteDevoirEleve.valeur)){
                if($location.path()==='/releve'){
                    var devoir = _.findWhere(model.devoirs.all, {id : noteDevoirEleve.iddevoir});
                    if(parseFloat(noteDevoirEleve.valeur) <= devoir.diviseur && parseFloat(noteDevoirEleve.valeur) >= 0){
                        /*noteDevoirEleve.save(noteDevoirEleve.iddevoir);
                         studentNote.valid = true;*/
                        noteDevoirEleve.save(noteDevoirEleve.iddevoir, function(id){
                            if(!noteDevoirEleve.id){
                                noteDevoirEleve.id = id;
                            }
                            noteDevoirEleve.valid = true;
                            $scope.safeApply();
                        });
                    }else{
                        notify.error(lang.translate("error.note.outbound")+devoir.diviseur);
                        noteDevoirEleve.valid = false;
                        $event.target.focus();
                    }
                }else{
                    if(parseFloat(noteDevoirEleve.valeur) <= $scope.currentDevoir.diviseur && parseFloat(noteDevoirEleve.valeur) >= 0){
                        noteDevoirEleve.save(noteDevoirEleve.iddevoir, function(id){
                            if(!noteDevoirEleve.id){
                                noteDevoirEleve.id = id;
                            }
                            noteDevoirEleve.valid = true;
                            $scope.safeApply();
                        });
                    }else{
                        notify.error(lang.translate("error.note.outbound")+$scope.currentDevoir.diviseur);
                        noteDevoirEleve.valid = false;
                        $event.target.focus();
                    }
                }
            } else {
                if(noteDevoirEleve.id !== undefined && noteDevoirEleve.valeur === "" && noteDevoirEleve.id) {
                    noteDevoirEleve.delete(noteDevoirEleve.id);
                    noteDevoirEleve.valid = true;
                }else{
                    if(noteDevoirEleve.valeur !== ""){
                        notify.error(lang.translate("error.note.invalid"));
                        noteDevoirEleve.valid = false;
                        $event.target.focus();
                    }
                }
            }
            $scope.safeApply();
        }, 1);

    };

    // Recuperation des notes de l'eleve pour le devoir en cours de visualisation
    $scope.getNoteByEleveId = function(eleveId){
        var note = _.findWhere($scope.noteDevoirEleves, {idEleve : eleveId});
        if(note !== undefined) {
            return note.value;
        }
        return "";
    };

    // Methode de filtre sur les dates de creation et de publication des devoirs
    $scope.filtrerDevoir = function() {
        var formatStr = "DD/MM/YYYY";

        var dateCreationDebut    = moment(moment($scope.date.creation.debut).format(formatStr), formatStr);
        var dateCreationFin      = moment(moment($scope.date.creation.fin).format(formatStr), formatStr);
        var datePublicationDebut = moment(moment($scope.date.publication.debut).format(formatStr), formatStr);
        var datePublicationFin   = moment(moment($scope.date.publication.fin).format(formatStr), formatStr);

        var devoirsFiltres = _.filter(model.devoirs.all, function(devoir){
            //prédicat de filtre
            var dateCreation = moment(moment(devoir.date).format(formatStr), formatStr);
            var datePublication = moment(moment(devoir.datepublication).format(formatStr), formatStr);
            return (datePublicationDebut.diff(datePublication, "days") <= 0 && datePublication.diff(datePublicationFin, "days") <=0) &&
                (dateCreationDebut.diff(dateCreation, "days") <= 0 && dateCreation.diff(dateCreationFin, "days") <=0);
        });

        $scope.devoirsFiltres = devoirsFiltres;

    };

    $scope.equalityCustomComparator = function (actual, expected) {
        if (expected === "*") {
            return true;
        }
        return String(actual).toUpperCase().match(String(expected).toUpperCase()) !== null;
    };

    /**
     * Calcul les statistiques du devoir en cours de la vue view-notes-devoir
     * @return la moyenne du devoir, la note minimal et maximal et le pourcentage d'avancement du devoir (par référence)
     **/
    $scope.calculerStatsDevoir = function() {
        var listeNotes = [];
        for(var i = 0; i < $scope.listStudentNoteCurrentDevoirModel.length; i++){
            listeNotes.push($scope.listStudentNoteCurrentDevoirModel[i].studentNote);
        }
        Behaviours.applicationsBehaviours["viescolaire.evaluations"].calculStatsDevoir($scope.currentDevoir, listeNotes);
    };

    $scope.getLibelleTypedevoir = function (idtype) {
        var type = _.findWhere($scope.etabTypeDevoir, {id : idtype});
        return type.nom;
    };

    $scope.getLibelleMatiere = function (idmatiere) {
        var matiere = model.matieres.findWhere({id : idmatiere});
        if(matiere !== undefined){
            return matiere.name;
        }else{
            return;
        }
    };

    $scope.getSousMatieres = function() {
        if($scope.search.idmatiere == '*') {
            $scope.sousMatieres = [];
        } else {
            loadSousMatieres($scope.search.idmatiere, null, function(res){
                $scope.sousMatieres = res;
                $scope.safeApply();
            });
        }
    };

    $scope.filterClassesMatieres = function(){
        if($scope.searchReleve.idclasse !== undefined && $scope.searchReleve.idclasse !== "*"){
            var libelleClasse = _.findWhere($scope.classes, {id : $scope.searchReleve.idclasse});
            $scope.releveListeMatiere = _.where($scope.matieres, {libelleClasse : libelleClasse.name});
            if($scope.releveListeMatiere.length > 0){
                $scope.searchReleve.idmatiere = $scope.releveListeMatiere[0].id;
                $scope.submitFilterReleveDeNotes();
            }else{
                $scope.searchReleve.idmatiere = '*';
            }
            if(!$scope.$$phase){
                $scope.$apply();
            }
        }
    };

    $scope.getLastCompetencesSelected = function () {
        http().getJson('/viescolaire/evaluations/competences/last/devoir/').done(function(competencesLastDevoirList){
            $scope.devoir.competencesLastDevoirList = competencesLastDevoirList;
        });
    };

    $scope.newDevoir = function(){
        $scope.me = model.me;
        $scope.showNoteLightBox.bool = true;
        $scope.controleDate();
        model.enseignements.sync();
        _.extend($scope.devoir.enseignements, model.enseignements);

        // Recuperation du dernier devoir cree par l'utilisateur afin de determiner les dernieres
        // competences selectionnees
        $scope.getLastCompetencesSelected();

        setCurrentPeriode(model.me.structures[0], function(defaultPeriode){ $scope.devoir.periode = defaultPeriode; /*$scope.$apply();*/});
        if($scope.etabTypeDevoir !== undefined){
            $scope.devoir.type = getDefaultType($scope.etabTypeDevoir);
        }else{
            http().getJson('/viescolaire/evaluations/types?idEtablissement='+model.me.structures[0]).done(function(res){
                $scope.devoir.type = getDefaultType($scope.etabTypeDevoir);
                $scope.safeApply();
            });
        }

        if($location.path() === '/releve'){
            $scope.devoir.idClasse = $scope.searchReleve.idclasse;
            $scope.devoir.idMatiere = $scope.searchReleve.idmatiere;
            $scope.setClasseMatieres($scope.searchReleve.idmatiere);
            //	$scope.$apply();
        }else{
            $scope.devoir.idClasse = $scope.search.idclasse;
            $scope.devoir.idMatiere = $scope.search.idmatiere;
            $scope.setClasseMatieres($scope.search.idmatiere);
            $scope.devoir.idSousMatiere = $scope.search.idsousmatiere;
        }
        template.open('lightboxContainer', '../modules/evaluations/template/eval_teacher_adddevoir');
        $scope.updateEtabInfo();
    };

    var isSousMatInArray = function(sousMat, sousMatieresDevoirs) {
        var found = sousMatieresDevoirs.some(function (el) {
            return (el.id === sousMat.id) && (el.libelle === sousMat.libelle);
        });
        return found;
    };

    var getSousMatieresJson = function(idmatiere){
        var mat = (model.matieres.findWhere({id : idmatiere}));
        if(mat !== undefined) {
            var res = mat.sousMatieres.all;
            if(res!== undefined){
                for (var i = 0; i < res.length; i++) {
                    var sousMat = res[i];
                    var found = isSousMatInArray(sousMat, $scope.sousMatieresDevoirs);
                    if(!found) {
                        $scope.sousMatieresDevoirs.push(sousMat);
                    }
                }
                $scope.safeApply();
            }
        }
    };;

    $scope.controleDate = function(){
        $scope.controlledDate = (moment($scope.devoir.datePublication).diff(moment($scope.devoir.dateDevoir), "days") < 0);
    };

    var getSousMatieresDevoirs = function() {
        $scope.sousMatieresDevoirs = [];

        for (var i = 0; i < model.devoirs.all.length; i++) {
            var devoir = model.devoirs.all[i];
            var sousMat = {
                id : devoir.idsousmatiere,
                libelle : devoir._sousmatiere_libelle
            };
            if(!isSousMatInArray(sousMat, $scope.sousMatieresDevoirs)){
                getSousMatieresJson(devoir.idmatiere);
            }
        }
    };

    $scope.getLibelleSousMatiere = function (idmatiere, idsousmatiere) {
        // var sousMatiere =
        var matiere = model.matieres.findWhere({id : idmatiere});
        if(matiere !== undefined){
            var sousMatiere = matiere.sousMatieres.findWhere({id : idsousmatiere});
            if(sousMatiere !== undefined){
                return sousMatiere.libelle;
            }
        }
    };

    $scope.getDateFormated = function(date) {
        return Behaviours.applicationsBehaviours["viescolaire.evaluations"].getFormatedDate(date, "DD/MM/YYYY");
    };

    $scope.getShortDateFormated = function(date) {
        return Behaviours.applicationsBehaviours["viescolaire.evaluations"].getFormatedDate(date, "DD/MM/YY");
    };

    $scope.getLibelleClasse = function(idClasse){
        return (_.findWhere($scope.classes, {id : idClasse})).name;
    };

    var getLibelleClasse = function(idClasse) {
        http().getJson("/directory/class/"+idClasse).done(function(classe) {
            $scope.classes.push(classe);
            $scope.$apply();
            if ($location.path() === "/releve" && $scope.classes.length !== 0 && $scope.searchReleve.idclasse !== $scope.classes[0].id){
                $scope.searchReleve.idclasse = $scope.classes[0].id;
                $scope.filterClassesMatieres();
            }
        });
    };

    var getClassesProf = function() {
        if($scope.classes === undefined){
            $scope.classes= [];
            for (var i = 0; i < model.me.classes.length; i++) {
                getLibelleClasse(model.me.classes[i]);
            }
        }
    };

    $scope.getNoteDevoirEleve = function(indexEleve, indexDevoir, idDevoir, idEleve) {
        // note de l'eleve pour le devoir en cours de parcours
        http().getJson('/viescolaire/evaluations/devoir/' + idDevoir + '/note?idEleve=' + idEleve).done(function (noteEleve) {

            if(noteEleve.length === 0){
                $scope.listStudentClasse[indexEleve].notesDevoirs[indexDevoir] = new Note();
                $scope.listStudentClasse[indexEleve].notesDevoirs[indexDevoir].iddevoir = idDevoir;
                $scope.listStudentClasse[indexEleve].notesDevoirs[indexDevoir].valeur = "";
                $scope.listStudentClasse[indexEleve].notesDevoirs[indexDevoir].ideleve = idEleve;
            }else{
                var note = new Note();
                note.castNote(noteEleve[0]);
                $scope.listStudentClasse[indexEleve].notesDevoirs[indexDevoir] = note;
            }

            $scope.nbCallNotesDevoirEleves += 1;

            var nbNotesToCalc = $scope.listStudentClasse.length * $scope.devoirsReleveNotes.length;

            // test pour savoir si on a caclule toutes les notes du relevé
            if($scope.nbCallNotesDevoirEleves === nbNotesToCalc) {
                // si oui on calcule les statistiques
                for (var j = 0; j < $scope.listStudentClasse.length; j++) {
                    calculerMoyenneEleve($scope.listStudentClasse[j]);
                }

                for (var k = 0; k < $scope.listStudentClasse.length; k++) {
                    calculerRangEleve($scope.listStudentClasse[k]);
                }

                for (var i = 0; i < $scope.devoirsReleveNotes.length; i++) {
                    $scope.calculerStatsDevoirReleve(i);
                }

                $scope.$apply();
            }
        });
    };

    $scope.$on('getInfoCompetencesDevoir', function($event){
        $scope.showNoteLightBox.bool = true;
        template.open('lightboxContainer', '../modules/evaluations/template/eval_teacher_dispcompinfo');
    });

    $scope.calculerStatsDevoirReleve = function(idDevoir) {
        var devoir = $scope.releveNote.devoirs.findWhere({id : idDevoir});
        var listeNotes = [];
        $scope.releveNote.eleves.each(function(eleve){
            var note = eleve.notes.findWhere({iddevoir : devoir.id});
            if(note !== undefined){
                listeNotes.push(note.toReleveJson());
            }
        });
        listeNotes = _.filter(listeNotes, function(note){ return note.valeur !== "" && note.valeur !== null && !isNaN(note.valeur); });
        devoir.percentDone = 0;
        if(listeNotes.length !== 0){
            http().postJson('/viescolaire/evaluations/moyenne', { 'notes' :  listeNotes }).done(function(res){
                devoir.moyenne = String(res.moyenne).replace(",",".");
                devoir.max = String((_.max(listeNotes, function(note){ return note.valeur; })).valeur).replace(",",".");
                devoir.min = String((_.min(listeNotes, function(note){ return note.valeur; })).valeur).replace(",",".");
                devoir.percentDone = Math.round((listeNotes.length/$scope.releveNote.eleves.all.length)*100);
                $scope.safeApply();
            });
        }
        $scope.safeApply();
    };

    $scope.calculerMoyenneEleve = function(eleve) {
        eleve.notes.on('change', function(){
            var a = [], t = eleve.notes;
            t = t.filter(function(note){
                return !isNaN(parseFloat(note.valeur));
            });
            _.each(t, function(note){
                a.push(note.toReleveJson());
            });
            if(a.length !== 0){
                http().postJson('/viescolaire/evaluations/moyenne', { 'notes' :  a }).done(function(res){
                    eleve.moyenne = String(res.moyenne).replace(",",".");
                    $scope.safeApply();
                });
            } else {
                eleve.moyenne = "";
            }
        });
        if(eleve.notes.all.length !== 0){
            eleve.notes.trigger('change');
        }
    };

    var calculerRangEleve = function(student) {
        if(student.moyenne === "") {
            student.rang = "";
        } else {
            var listeMoyennes = [];
            student.rang = "";
            var rangEleve = 1;

            for(var i=0; i< $scope.listStudentClasse.length; i++) {
                var currStudent = $scope.listStudentClasse[i];
                if(currStudent.moyenne !== undefined && currStudent.moyenne !== "") {
                    if(currStudent.moyenne > student.moyenne) {
                        rangEleve++;
                    }
                }
            }

            student.rang = rangEleve;
        }
    };

    var calculerRangEleves = function() {
        for (var j = 0; j < $scope.listStudentClasse.length; j++) {
            calculerRangEleve($scope.listStudentClasse[j]);
        }
    };

    $scope.calculerStatsEleve = function(student) {
        calculerMoyenneEleve(student);
        calculerRangEleves();
    };

    $scope.submitFilterReleveDeNotes = function () {
        if($scope.searchReleve.idclasse !== undefined && $scope.searchReleve.idmatiere !== undefined && $scope.searchReleve.idperiode !== undefined && $scope.searchReleve.idclasse !== '*' && $scope.searchReleve.idmatiere !== '*' && $scope.searchReleve.idperiode !== '*') {
            var idEtablissement = model.me.structures[0];
            $scope.getReleveDeNotes(idEtablissement, $scope.searchReleve.idclasse, $scope.searchReleve.idperiode, $scope.searchReleve.idmatiere);
        }
        template.close('leftSide-devoirInfo', '../modules/absences/template/eval_teacher_dispdevoirinfo');
        template.close('leftSide-userInfo', '../modules/absences/template/eval_teacher_dispeleveinfo');
    };

    $scope.getReleveDeNotes = function (idetablissement, idclasse, idperiode, idmatiere) {

        // récupération des devoirs pour un établissement, une classe, une matière et une période donnée
        // var urlDevoirs = '/notes/devoir/'+idEtablissement+'/'+idClasse+'/'+idMatiere+'/'+idPeriode;
        // http().getJson(urlDevoirs).done(function(res){
        // 	$scope.devoirsReleveNotes = res;
        // 	$scope.$apply();
        //
        // 	// Recuperation des notes des eleves seulement s'il y a des devoirs pour ces filtres
        // 	if($scope.devoirsReleveNotes.length > 0) {
        //
        // 		// Recuperation des utilisateurs de la classe
        // 		http().getJson("/directory/class/" + idClasse + "/users").done(function (listStudentClasse) {
        // 			if (listStudentClasse !== undefined) {
        // 				// filtre sur le type étudiant
        // 				$scope.listStudentClasse = _.where(listStudentClasse, {type: 'Student'});
        //
        // 				// compteur pour savoir combien de notes ont etes calculees
        // 				$scope.nbCallNotesDevoirEleves = 0;
        //
        // 				// Pour chaque eleve on recupere sa note sur chaque devoir
        // 				for (var j = 0; j < $scope.listStudentClasse.length; j++) {
        //
        // 					var student = $scope.listStudentClasse[j];
        // 					student.notesDevoirs = [];
        //
        // 					for (var i = 0; i < $scope.devoirsReleveNotes.length; i++) {
        // 						var currDevoir = $scope.devoirsReleveNotes[i];
        // 						$scope.getNoteDevoirEleve(j, i, currDevoir.id, student.id);
        // 					}
        // 				}²
        // 			}
        //
        // 			$scope.searchReleve = {
        // 				idclasse : idClasse,
        // 				idmatiere : idMatiere,
        // 				idperiode : idPeriode
        // 			};
        // 			$scope.$apply();
        // 		});
        // 	}
        // });
        if(model.releveNotes.findWhere({idEtablissement : idetablissement, idClasse : idclasse, idPeriode : idperiode, idMatiere : idmatiere}) === undefined){
            var releve = new ReleveNote({idEtablissement : idetablissement, idClasse : idclasse, idPeriode : idperiode, idMatiere : idmatiere});
            model.releveNotes.push(releve);
            $scope.releveNote = releve;
        }else{
            $scope.releveNote = model.releveNotes.findWhere({idEtablissement : idetablissement, idClasse : idclasse, idPeriode : idperiode, idMatiere : idmatiere});
        }
        $scope.releveNote.eleves.on('change', function(){
            $scope.safeApply();
        });
        $scope.releveNote.eleves.on('calculMoyenne', function(){
            $scope.elevesReleveSynchronised++;
            if($scope.elevesReleveSynchronised === $scope.releveNote.eleves.all.length){
                $scope.releveNote.devoirs.each(function(devoir){
                    $scope.calculerStatsDevoirReleve(devoir.id);
                });
                $scope.elevesReleveSynchronised = 0;
            }
        });
    };

    $scope.getDevoirStudentModel = function(idEleve, devoir){
        var model = devoir.notes.findWhere({ideleve : idEleve});
        $scope.safeApply();
        return model;
    };

    $scope.closeInfo = function(type){
        if(type === 'devoir'){
            template.close('leftSide-devoirInfo');
        }else{
            template.close('leftSide-userInfo');
        }
    };

    $scope.getReleveDeNotesInit = function () {

        getClassesProf();

        var userId = model.me.userId;
        // TODO Recuperer l'etablissement courant et non le 1er etab dans la liste
        var	idEtablissement = model.me.structures[0];
        $scope.matieres = model.matieres.all;
    };

    /**
     * Fonction pour déterminer si la periode doit être sélectionnée
     * @param periode la période à tester
     * @returns {boolean} true si c'est la période en cours, false sinon
     */
    $scope.isCurrentPeriode = function(periode) {
        var isCurrentPeriodeBln = (periode.id === $scope.currentPeriodeId);
        if(isCurrentPeriodeBln) {
            //maj du scope pour déclencher les filtres
            //$scope.search.idPeriode = periode.id;
            //$scope.devoir.periode = periode.id;
        }
        return isCurrentPeriodeBln;
    };

    /**
     * Fonction pour déterminer si le type doit être sélectionné
     * @param type le type à tester
     * @returns {boolean} true si c'est le type par défaut, false sinon
     */
    $scope.isDefaultType = function(type) {
        var booleen = (type.nom === 'Evaluation');
        if(booleen) {
            //maj du scope pour déclencher les filtres
            //$scope.search.idtype = type.id;
            //$scope.devoir.type = type.id;
        }
        return booleen;
    };

    $scope.focusMe = function($event) {
        $event.target.select();
    };

    $scope.getEleveInfo = function(eleveId){
        template.open('leftSide-userInfo', '../modules/evaluations/template/eval_teacher_dispeleveinfo');
        if($scope.currentUserInfo === undefined || $scope.currentUserInfo.u.data.id !== eleveId){
            $scope.currentUserInfo = undefined;
            http().getJson("/viescolaire/evaluations/informations/"+eleveId).done(function(res){
                $scope.currentUserInfo = res;
                $scope.$apply();
            });
        }
    };

    $scope.$on('getDevoirInfo', function($event, id){
        $scope.getDevoirInfo(id);
    });

    $scope.$on('getGotTo', function($event,path){
        $scope.goTo(path);
    });

    $scope.getDevoirInfo = function(devoirId){
        template.open('leftSide-devoirInfo', '../modules/evaluations/template/eval_teacher_dispdevoirinfo');
        $scope.currentDevoirInfo = undefined;
        $scope.currentDevoirInfo = _.findWhere($scope.releveNote.devoirs.all, {id : devoirId});
    };

    $scope.broadcastBox = function(domaine){
        domaine.selectChildren(domaine.selected, function(){
            $scope.safeApply();
        });
    };

    $scope.safeApply = function(fn) {
        var phase = this.$root.$$phase;
        if(phase == '$apply' || phase == '$digest') {
            if(fn && (typeof(fn) === 'function')) {
                fn();
            }
        } else {
            this.$apply(fn);
        }
    };

    var addCompetencesFilsRecursive = function(competencesNotesEleveSorted, competencesNotesEleve, idPere) {
        // recherche des fils dont le pere a pour identifiant idPere
        var competencesNotesEleveFils = _.where(competencesNotesEleve, {idparent: idPere});

        // tant qu'il y a des fils on a appel recursivement la méthode
        if(competencesNotesEleveFils !== undefined && competencesNotesEleveFils.length > 0) {
            // parcours horizontal pour ajouter les fils à la suite du parent dans competencesNotesEleveSorted
            for(var i=0; i< competencesNotesEleveFils.length; i++) {
                // ajout du fils et recuperation de ses sous fils
                competencesNotesEleveSorted.push(competencesNotesEleveFils[i]);
                addCompetencesFilsRecursive(competencesNotesEleveSorted, competencesNotesEleve, competencesNotesEleveFils[i].idcompetence);
            }
        }
    };

    $scope.sortCompetencesByHierarchie = function(competencesNotesEleve) {

        var competencesNotesEleveSorted = [];
        var competencesTraitees = [];

        // parcours de toutes les competences
        for(var i=0; i< competencesNotesEleve.length; i++) {

            var competenceNotesEleve = competencesNotesEleve[i];
            var parent = _.findWhere(competencesNotesEleve, {idcompetence: competenceNotesEleve.idparent});
            var competenceDejaAjoutee = _.findWhere(competencesNotesEleveSorted, {idcompetence: competenceNotesEleve.idcompetence});

            // ajout et recherche des fils des competences racines ou sans parent
            if ( competenceDejaAjoutee === undefined && parent === undefined ) {
                competencesNotesEleveSorted.push(competenceNotesEleve);

                // On recherche d'abord les competences racine (idparent = 0) et ajoute à la suite leurs fils et sous fils
                addCompetencesFilsRecursive(competencesNotesEleveSorted, competencesNotesEleve, competenceNotesEleve.idcompetence);
            }
        }

        return competencesNotesEleveSorted;
    };

    $scope.loadCompetencesNotes = function(i, devoirId) {
        http().getJson("/viescolaire/evaluations/competences/note?iddevoir=" + devoirId + "&ideleve=" + $scope.listStudentNoteCurrentDevoirModel[i].id).done(function (competencesNotesElevesLoad) {
            var competencesNotesEleve = competencesNotesElevesLoad;

            // trie des competence du devoir
            $scope.currentDevoir.competences.all = _.sortBy($scope.currentDevoir.competences.all, 'idcompetence');

            // si elles n'existent pas on parcour les competences du devoir et on ajoute sur l'eleve
            // la liste des competencesnotes correspondantes
            if (competencesNotesEleve === undefined || competencesNotesEleve.length === 0) {
                $scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve = [];
                for (var j = 0; j < $scope.currentDevoir.competences.all.length; j++) {
                    var competenceDevoir = $scope.currentDevoir.competences.all[j];
                    var competenceNote = new CompetenceNote();
                    competenceNote.idcompetence = competenceDevoir.idcompetence;
                    competenceNote.iddevoir = parseInt($scope.listStudentNoteCurrentDevoirModel[i].studentNote.iddevoir);
                    competenceNote.evaluation = -1;
                    competenceNote.nom = competenceDevoir.nom;
                    competenceNote.ideleve = $scope.listStudentNoteCurrentDevoirModel[i].studentNote.ideleve;
                    competenceNote.idparent = competenceDevoir.idparent;
                    competenceNote.idtype = competenceDevoir.idtype;
                    $scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve.push(competenceNote);
                }
            } else {
                // si il ya deja des competences qui ont ete evaluee sur l'eleve, alors,
                // on recupere seulement le delta
                for (var j = 0; j < $scope.currentDevoir.competences.all.length; j++) {
                    var competenceDevoir = $scope.currentDevoir.competences.all[j];
                    var competenceNote = _.findWhere(competencesNotesEleve, {idcompetence: competenceDevoir.idcompetence});

                    // creation de la competencenote si elle n'existe pas
                    if (competenceNote === undefined) {
                        competenceNote = new CompetenceNote();
                        competenceNote.idcompetence = competenceDevoir.idcompetence;
                        competenceNote.iddevoir = parseInt($scope.listStudentNoteCurrentDevoirModel[i].studentNote.iddevoir);
                        competenceNote.evaluation = -1;
                        competenceNote.nom = competenceDevoir.nom;
                        competenceNote.ideleve = $scope.listStudentNoteCurrentDevoirModel[i].studentNote.ideleve;
                        competenceNote.idparent = competenceDevoir.idparent;
                        competenceNote.idtype = competenceDevoir.idtype;
                        competencesNotesEleve.push(competenceNote);
                    }
                }
                $scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve = competencesNotesEleve;
            }
            //$scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve= $scope.sortCompetencesByHierarchie($scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve);

            // tri des competences de l'eleves afin d'être dans le même ordre que les compêtences du devoir
            $scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve = _.sortBy($scope.listStudentNoteCurrentDevoirModel[i].competencesNotesEleve, 'idcompetence');
            $scope.safeApply();
        });
    };

    route({
        listDevoirs : function(params){
            if(model.me.type === 'ENSEIGNANT'){

                if(model.devoirs.all.length === 0){
                    $location.path("/releve");
                    $location.replace();
                    $scope.safeApply();
                }

                $scope.openedDevoir = -1;

                $scope.search = {
                    idclasse      : "*",
                    idmatiere     : "*",
                    idsousmatiere : "*",
                    idtype        : "*",
                    idperiode     : "*",
                    name          : ""
                };

                // TODO Recuperer l'etablissement courant et non le 1er etab dans la liste
                // des structures de l'utilisateur
                http().getJson('/viescolaire/evaluations/types?idEtablissement='+model.me.structures[0]).done(function(res){
                    $scope.etabTypeDevoir = res;
                    $scope.search.idtype = getDefaultType(res);
                    $scope.$apply();
                });

                getClassesProf();
                $scope.safeApply();
                template.open('main', '../modules/evaluations/template/eval_teacher_dispdevoirs');
                model.matieres.one('sync', function(){
                    $scope.listeDevoirMatieres = model.matieres.all;
                    $scope.safeApply();
                });
                model.devoirs.on('sync', function(){

                    // Copie de la liste des devoirs pour pouvoir filtrer dessus
                    $scope.devoirsFiltres = model.devoirs.all;
                    getSousMatieresDevoirs();
                    $scope.safeApply();
                });
            }else{
                // template.open('main', '../modules/absences/template/display-notes');
            }
        },
        viewNotesDevoir : function(params){
            template.close('leftSide-userInfo');
            if(model.devoirs.all.length === 0){
                $location.path("/releve");
                $scope.$apply();
                $location.replace();
            }
            // Recuperation du devoir courant
            $scope.currentDevoir =  _.findWhere(model.devoirs.all, {id: parseInt(params.devoirId)});
            $scope.currentDevoir.competences.sync();
            $scope.currentDevoir.compteur = undefined;
            $scope.openedNote = -1;


            // Recuperation des utilisateurs de la classe
            http().getJson("/directory/class/"+$scope.currentDevoir.idclasse+"/users").done(function(listStudentCurrentDevoir) {
                if(listStudentCurrentDevoir!==undefined){
                    $scope.listStudentNoteCurrentDevoirModel = _.where(listStudentCurrentDevoir, { type : 'Student'});


                    // Récupéraation des notes enregistrées sur le devoir
                    http().getJson("/viescolaire/evaluations/devoir/"+$scope.currentDevoir.id+"/notes").done(function(data){
                        var hasOneNote = false;
                        for(var i = 0; i < $scope.listStudentNoteCurrentDevoirModel.length; i++){
                            var noteEleve = _.findWhere(data, {ideleve : $scope.listStudentNoteCurrentDevoirModel[i].id});
                            if(noteEleve === undefined){
                                noteEleve = new Note();
                                noteEleve.iddevoir = params.devoirId;
                                noteEleve.valeur = "";
                                noteEleve.ideleve = $scope.listStudentNoteCurrentDevoirModel[i].id;
                                noteEleve.appreciation = "";
                            }else{
                                var objectNoteEleve = new Note();
                                objectNoteEleve.castNote(noteEleve);
                                noteEleve = objectNoteEleve;
                                hasOneNote = true;
                            }
                            $scope.listStudentNoteCurrentDevoirModel[i].studentNote = noteEleve;

                            // competences evaluee de l'eleve
                            $scope.loadCompetencesNotes(i, params.devoirId);

                        }

                        $scope.calculerStatsDevoir();
                        $scope.$apply();
                    });
                }
            });

            template.open('main', '../modules/evaluations/template/eval_teacher_viewnotesdevoirs');
        },
        displayReleveNotes : function(params){
            //if(model.devoirs.all.length === 0){
            //	$location.path("/devoirs/list");
            //	$location.replace();
            //}
            template.close('leftSide-userInfo');
            template.close('leftSide-devoirInfo');
            $scope.elevesReleveSynchronised = 0 ;
            $scope.releveNote = undefined;
            $scope.devoirsReleveNotes = undefined;
            $scope.listStudentClasse = [];
            if( $scope.searchReleve.idclasse !="*" && $scope.searchReleve.idmatiere !="*" ){
                $scope.submitFilterReleveDeNotes();
                $scope.releveNote.sync();
            }

            setCurrentPeriode(model.me.structures[0], function(defaultPeriode){
                $scope.searchReleve.idperiode = defaultPeriode;
                $scope.$apply();
            });

            model.matieres.one('sync', function(){
                $scope.listeDevoirMatieres = model.matieres.all;
                $scope.safeApply();
            });
            model.devoirs.on('sync', function(){

                // Copie de la liste des devoirs pour pouvoir filtrer dessus
                $scope.devoirsFiltres = model.devoirs.all;

                getSousMatieresDevoirs();
                $scope.getReleveDeNotesInit();
            });

            template.open('main', '../modules/evaluations/template/eval_teacher_dispreleve');
        }
    });
}
