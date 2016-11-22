import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');

declare let _:any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$sce', '$compile',
    function ($scope, route, $rootScope, $location, $filter, $sce, $compile) {
        route({
            accueil : function(params){
                template.open('main', '../templates/evaluations/enseignants/eval_acu_teacher');
            },

            createDevoir : function(params){
                $scope.createDevoir();
            },

            listDevoirs : function(params){
                if(evaluations.devoirs.all.length === 0){
                    $location.path("/releve");
                    utils.safeApply($scope);
                    $location.replace();
                }
                template.open('main', '../templates/evaluations/enseignants/liste_devoirs/display_devoirs_structure');
                template.open('evaluations', '../templates/evaluations/enseignants/liste_devoirs/list_view');
                evaluations.devoirs.getPercentDone();
                utils.safeApply($scope);
            },
            viewNotesDevoir : function(params){
                if(evaluations.devoirs.all.length === 0){
                    $location.path("/releve");
                    utils.safeApply($scope);
                    $location.replace();
                }
                if (!template.isEmpty('leftSide-userInfo')) template.close('leftSide-userInfo');
                if (!template.isEmpty('leftSide-devoirInfo')) template.close('leftSide-devoirInfo');
                $scope.currentDevoir = _.findWhere(evaluations.devoirs.all, {id : parseInt(params.devoirId)});
                $scope.openedDetails = true;
                $scope.openedStatistiques = true;
                $scope.openedStudentInfo = true;
                if ($scope.currentDevoir !== undefined) {
                    $scope.currentDevoir.competences.sync().then(() => {
                        utils.safeApply($scope);
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
                        utils.safeApply($scope);
                        $scope.currentDevoir.calculStats(_evals).then(() => {
                            utils.safeApply($scope);
                        });
                    });
                }

                template.open('main', '../templates/evaluations/enseignants/liste_notes_devoir/display_notes_devoir');
                utils.safeApply($scope);
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
                utils.safeApply($scope);
            },
            displaySuiviCompetencesEleve : function (params) {
                template.open('main', '../templates/evaluations/enseignants/suivi_competences_eleve/container');
                $scope.informations.eleve = null;
            }
        });

        $scope.evaluations = evaluations;

        $scope.competencesSearchKeyWord = "";

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
            },
            eleve : null
        };
        $scope.releveNote = undefined;

        evaluations.classes.on('classes-sync', function () {
            utils.safeApply($scope);
        });

        $scope.goTo = function(path){
            $location.path(path);
            $location.replace();
        };

        evaluations.periodes.on('sync', function () {
            setCurrentPeriode().then((defaultPeriode) => {
                $scope.search.periode = (defaultPeriode !== -1) ? defaultPeriode : '*';
                utils.safeApply($scope);
            });
        });

        /**
         * Initialise un nouveau devoir.
         */
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

        /**
         * Controle que la date de publication du devoir n'est pas inférieur à la date du devoir.
         */
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

        /**
         * Controle la validité du formulaire de création d'un devoir
         * @returns {boolean} Validité du formulaire
         */
        $scope.controleNewDevoirForm = function () {
            return !(
                $scope.devoir.controlledDate
                && $scope.devoir.id_etablissement !== undefined
                && $scope.devoir.id_classe !== undefined
                && $scope.devoir.id_matiere !== undefined
                && $scope.devoir.name !== undefined
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

        /**
         * Retourne la valeur de la clé i18n
         * @param key Clé i18n
         * @returns {any} Valeur i18n
         */
        $scope.translate = function (key) {
          return utils.translate(key);
        };

        /**
         * Permet de faire la jointure entre les directive de compétences cSkilllsColorColumn et cSkillsNoteDevoir
         */
        $scope.$on('majHeaderColumn', function(event, competence){
            $scope.$broadcast('changeHeaderColumn', competence);
        });

        /**
         * Retourne le nom de la structure en fonction de l'id de la structure
         * @param etabId Identifiant de la structure
         * @returns {any} Nom de la structure
         */
        $scope.getEtablissementName = function(etabId){
            return model.me.structureNames[model.me.structures.indexOf(etabId)];
        };

        /**
         * Sélectionne/Déselectionne chaque objet de la liste
         * @param list liste d'objet
         */
        $scope.selectListDevoir = function (list) {
            for (var i = 0; i < list.length; i++) {
                list[i].selected = !list[i].selected;
            }
        };

        /**
         * Sélectionne/Déselectionne tous les devoirs de l'utilisateur
         */
        $scope.selectAllDevoirs = function(){
            if ($scope.selected.devoirs.all !== true) {
                $scope.selectListDevoir($scope.selected.devoirs.list);
                $scope.selected.devoirs.list = [];
                return;
            }
            $scope.selected.devoirs.list = $filter('customSearchFilters')($scope.devoirs.all, $scope.search);
            $scope.selectListDevoir($scope.selected.devoirs.list);
        };

        /**
         * Récupère toutes les sous matière de la matière recherchée
         */
        $scope.getSousMatieres = function () {
            var matiere = evaluations.matieres.findWhere({id : $scope.search.matiere.id});
            if (matiere) $scope.selected.matiere = matiere;
        };


        /**
         *
         * Initialise tous les enseignements dans l'écran de filtre des compétences
         * lors de la création d'un devoir.
         *
         * @param pbInitSelected booleen indiuant si l'enseignement doit être sélectionnée ou non.
         */
        $scope.initFilter = function (pbInitSelected) {
            $scope.enseignementsFilter = {};
            $scope.competencesFilter = {};
            for (var i = 0; i < $scope.enseignements.all.length; i++) {

                var currEnseignement = $scope.enseignements.all[i];
                $scope.enseignementsFilter[currEnseignement.id] = {
                    isSelected : pbInitSelected,
                    nomHtml :currEnseignement.nom
                };

                // on initialise aussi les compétences
                $scope.initFilterRec(currEnseignement.competences, pbInitSelected)
            }
        };

        /**
         * Initialise le nom html des compétences (pour gérer le surlignement lors des recherches)
         *
         * @param poCompetences la liste des compétences
         */
        $scope.initFilterRec = function (poCompetences, pbInitSelected) {
            if(poCompetences !== undefined) {
                for (var i = 0; i < poCompetences.all.length; i++) {
                    var currCompetence = poCompetences.all[i];
                    $scope.competencesFilter[currCompetence.id] = {
                        //isSelected : pbInitSelected,
                        nomHtml :currCompetence.nom
                    };

                    $scope.initFilterRec(currCompetence.competences, pbInitSelected);
                }
            }
        };

        /**
         * Lance la séquence d'ouverture de l'ajout d'une appréciation pour un élève
         * @param eleve élève
         */
        $scope.addAppreciation = function(eleve) {
            template.open('lightboxContainer', '../templates/evaluations/enseignants/liste_notes_devoir/add_appreciation');
            $scope.selected.eleve = eleve;
            $scope.opened.lightbox = true;
            utils.safeApply($scope);
        };

        /**
         * Methode qui determine si un enseignement doit être affiché ou non
         * (pour chaque enseignement on rentre dans cette fonction et on check le booleen stocké
         * dans le tableau  $scope.enseignementsFilter[])
         *
         * @param enseignement l'enseignement à tester
         * @returns {true si l'enseignement est sélectionné, false sinon.}
         */
        $scope.enseignementsFilterFunction = function (enseignement) {
            // si valeur est rensiegnée on la retourne sinon on considère qu'elle est sélectionné (gestion du CTRL-F5)
            return ($scope.enseignementsFilter[enseignement.id] &&$scope.enseignementsFilter[enseignement.id].isSelected);
        };


        /**
         * Sélectionne/désélectionne tous les enseignements dans l'écran de filtre des compétences
         * lors de la création d'un devoir.
         *
         * @param pbIsSelected booleen pour sélectionner ou désélectionner les enseignements.
         */
        $scope.selectEnseignements = function(pbIsSelected){
            for (var i = 0; i < $scope.enseignements.all.length; i++) {
                var currEnseignement = $scope.enseignements.all[i];
                $scope.enseignementsFilter[currEnseignement.id].isSelected = pbIsSelected;
            }
        };

        /**
         * Sélectionne tous les enseignements dans l'écran de filtre des compétences
         * lors de la création d'un devoir.
         *
         */
        $scope.selectAllEnseignements = function(){
            $scope.selectEnseignements(true);
        };


        /**
         * Désélectionne tous les enseignements dans l'écran de filtre des compétences
         * lors de la création d'un devoir.
         *
         */
        $scope.deselectAllEnseignements = function(){
            $scope.selectEnseignements(false);
        };


        /**
         *
         * Methode qui determine si un enseignement doit être affiché ou non (selon le mot clef saisi)
         *
         * En realité on retourne toujours l'enseignement, il s'agit ici de savoir si on doit le déplier
         * en cas de match de mot clef ou si on le replie.
         *
         * @param psKeyword le mot clef recherché
         * @returns {function(enseignement): (retourne true systématiquement)}
         */
        $scope.enseignementsSearchFunction = function (psKeyword) {

            return function(enseignement) {

                if(!$scope.search.haschange) {
                    return true;
                }

                // on check ici si l'enseignement  match le mot clef recherché pour éviter de rechecker
                // systématiquement dans la méthode récursive
                enseignement.open = utils.containsIgnoreCase(enseignement.nom, psKeyword);
                if(enseignement.open) {
                    var nomHtml = $scope.highlight(enseignement.nom, psKeyword);
                    // mise à jour que si la réelle valeur de la chaine html est différente ($sce.trustAsHtml renvoie systématiquement une valeur différente)
                    if($sce.getTrustedHtml($scope.enseignementsFilter[enseignement.id].nomHtml) !== $sce.getTrustedHtml(nomHtml)) {
                        $scope.enseignementsFilter[enseignement.id].nomHtml = nomHtml;
                    }

                } else {
                    $scope.enseignementsFilter[enseignement.id].nomHtml  = enseignement.nom;
                }

                // Appel de la méthode récursive pour chercher dans les enseignements et compétences / sous compétences /
                // sous sous compétences / ...
                $scope.enseignementsSearchFunctionRec(enseignement, psKeyword);

                // dans tous les cas, à la fin, on retourne l'enseignement "racine"
                return true;
            }
        };


        /**
         * Methode récursive qui determine si un enseignement / une compétence / une sous compétence / une sous sous compétence ...
         * match le mot clef recherché et doit être dépliée dans les résultats de recherche
         *
         * @param item un enseignement / une compétence / une sous compétence / une sous sous compétence / ...
         * @psKeyword le mot clef recherché
         */
        $scope.enseignementsSearchFunctionRec = function (item, psKeyword) {

            // Condition d'arret de l'appel récursif : pas de sous compétences (on est sur une feuille de l'arbre)
            if (item.competences != undefined) {

                // Parcours de chaque compétences / sous compétences
                for (var i = 0; i < item.competences.all.length; i++) {
                    var sousCompetence = item.competences.all[i];

                    // check si la compétence / sous compétence match le mot clef
                    // on la déplie / replie en conséquence
                    sousCompetence.open = utils.containsIgnoreCase(sousCompetence.nom, psKeyword);
                    if(sousCompetence.open) {

                        var nomHtml = $scope.highlight(sousCompetence.nom, psKeyword);
                        // mise à jour que si la réelle valeur de la chaine html est différente ($sce.trustAsHtml renvoie systématiquement une valeur différente)
                        if($sce.getTrustedHtml($scope.competencesFilter[sousCompetence.id].nomHtml) !== $sce.getTrustedHtml(nomHtml)) {
                            $scope.competencesFilter[sousCompetence.id].nomHtml = nomHtml;
                        }

                    } else {
                        $scope.competencesFilter[sousCompetence.id].nomHtml = sousCompetence.nom;
                    }

                    // si elle match le mot clef on déplie également les parents
                    if(sousCompetence.open) {
                        item.open = true;
                        var parent = item.composer;

                        while(parent !== undefined) {
                            parent.open = true;
                            parent = parent.composer;
                        }
                    }


                    // et on check sur les compétences de l'item en cours de parcours
                    $scope.enseignementsSearchFunctionRec(sousCompetence, psKeyword)
                }
            }
        };

        /**
         * Retourne une chaine avec toutes les occurences du mot clef trouvées surlignées (encadrement via des balises html)
         *
         * @param psText le texte où rechercher
         * @param psKeyword le mot clef à rechercher
         * @returns le texte avec les occurences trouvées surlignées
         */
        $scope.highlight = function(psText, psKeyword) {
            var psTextLocal = psText;

            if (!psKeyword) {
                return $sce.trustAsHtml(psText);
            }
            return $sce.trustAsHtml(psTextLocal.replace(new RegExp(psKeyword, 'gi'), '<span class="highlightedText">$&</span>'));
        };

        /**
         * Séquence de création d'un devoir
         */
        //TODO Déplacer cette séquence dans la séquence du router
        $scope.createDevoir = function () {
            $scope.devoir = $scope.initDevoir();
            //$scope.opened.lightbox = true;
            $scope.controlledDate = (moment($scope.devoir.date_publication).diff(moment($scope.devoir.date), "days") <= 0);
            // resynchronisation de la liste pour eviter les problemes de references et de checkbox precedements cochees
            evaluations.enseignements.sync();
            utils.safeApply(this);
            $scope.initFilter(true);
            $scope.search.keyword = "";
            // si le mot clef de recherche n'a pas changé c'est qu'on rentre dans le filtre lors d'un autre
            // evenement (depliement/repliement d'un compétence par exemple)
            // on ne réaplique pas le filtre dans ce cas car on veut déplier l'élément sur lequel on a cliqué
            $scope.$watch('search.keyword', function (newValue, oldValue) {
                $scope.search.haschange = (newValue !== oldValue);
            }, true);
            _.extend($scope.devoir.enseignements, $scope.enseignements);


            evaluations.competencesDevoir = [];

            $scope.devoir.getLastSelectedCompetence().then(function (res)  {
                $scope.devoir.competencesLastDevoirList = res;
            });
            setCurrentPeriode().then((defaultPeriode) => {
                $scope.devoir.id_periode = defaultPeriode.id;
                utils.safeApply($scope);
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
            utils.safeApply($scope);
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

        /**
         *  Sauvegarde du devoir à la suite du formulaire de création
         */
        $scope.saveNewDevoir = function () {
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
                    utils.safeApply($scope);
                });
            });
        };

        /**
         * Récupère les matières enseignées sur la classe donnée
         * @param idClasse Identifiant de la classe
         * @param callback Callback de retour la fonction
         */
        var getClassesMatieres = function (idClasse) {
            return new Promise((resolve, reject) => {
                var libelleClasse = _.findWhere(evaluations.structures.all[0].classes, {id : idClasse}).name;
                if (libelleClasse !== undefined) {
                    if (resolve && typeof(resolve) === 'function') {
                        resolve(evaluations.matieres.where({libelleClasse: libelleClasse}))
                    }
                }
            });
        };

        /**
         * Set les matière en fonction de l'identifiant de la classe
         */
        $scope.setClasseMatieres = function () {
            getClassesMatieres($scope.devoir.id_classe).then((matieres) => {
                if ($scope.devoir.matieresByClasse.length === 1) $scope.devoir.id_matiere = $scope.devoir.matieresByClasse[0].id;
                $scope.selectedMatiere();
            });
        };

        /**
         * Déclenche un safeApply lors de l'event apply du model
         */
        model.on('apply', function () {
            utils.safeApply($scope);
        });

        /**
         * Séquence de récupération d'un relevé de note
         */
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
                                    utils.safeApply($scope);
                                });
                                $scope.releveNote.calculMoyennesEleves().then(() => {
                                    utils.safeApply($scope);
                                });
                                utils.safeApply($scope);
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
                            utils.safeApply($scope);
                        });
                        $scope.releveNote.calculMoyennesEleves().then(() => {
                            utils.safeApply($scope);
                        });
                    });
                } else {
                    $scope.releveNote = rn;
                    utils.safeApply($scope);
                }
            }
        };

        /**
         * Position l'objet matière sur le devoir en cours de création
         */
        $scope.selectedMatiere = function () {
            var matiere = evaluations.matieres.findWhere({id : $scope.devoir.id_matiere});
            if (matiere !== undefined) $scope.devoir.matiere = matiere;
        };

        /**
         * Controle le fonctionnement des filtres dates sur la vue liste des devoirs
         */
        $scope.controleDatePicker = function () {
            if (moment($scope.search.dateCreation.debut).diff(moment($scope.search.dateCreation.fin)) > 0) {
                $scope.search.dateCreation.fin = moment();
            }
            if (moment($scope.search.datePublication.debut).diff(moment($scope.search.datePublication.fin)) > 0) {
                $scope.search.datePublication.fin = moment();
            }
            utils.safeApply($scope);
        };

        /**
         * Retourne l'identifiant du type de devoirs par défaut
         * @returns {number} identifiant du type de devoir par défaut
         */
        var getDefaultTypDevoir = function(){
            return (evaluations.types.findWhere({default_type : true})).id;
        };

        /**
         * Format la date passée en paramètre
         * @param date Date à formatter
         * @returns {any|string} date formattée
         */
        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };

        /**
         * Retourne le libelle de la classe correspondant à l'identifiant passé en paramètre
         * @param idClasse identifiant de la classe
         * @returns {any} libelle de la classe
         */
        $scope.getLibelleClasse = function(idClasse) {
            if (idClasse == null || idClasse === "") return "";
            if(evaluations.structures.all.length === 0 || evaluations.structures.all[0].classes.length === 0) return;
            return _.findWhere(evaluations.structures.all[0].classes, {id : idClasse}).name;
        };

        /**
         * Retourne le libelle de la période correspondant à l'identifiant passé en paramètre
         * @param idPeriode identifiant de la période
         * @returns {any} libelle de la période
         */
        $scope.getLibellePeriode = function(idPeriode) {
            if (idPeriode == null || idPeriode === "") return "";
            return _.findWhere($scope.periodes.all, {id : parseInt(idPeriode)}).libelle;
        };

        /**
         * Retourne le libelle du type de devoir correspondant à l'identifiant passé en paramètre
         * @param idType identifiant du type de devoir
         * @returns {any} libelle du type de devoir
         */
        $scope.getLibelleType = function(idType) {
            if (idType == null || idType === "") return "";
            return _.findWhere($scope.types.all, {id : parseInt(idType)}).nom;
        };

        /**
         * Retourne le libelle de la matière correspondant à l'identifiant passé en paramètre
         * @param idMatiere identifiant de la matière
         * @returns {any} libelle de la matière
         */
        $scope.getLibelleMatiere = function(idMatiere) {
            if (idMatiere == null || idMatiere === "") return "";
            return _.findWhere($scope.matieres.all, { id: idMatiere }).name;
        };

        /**
         * Retourne le libelle de la sous matière correspondant à l'identifiant passé en paramètre
         * @param idSousMatiere identifiant de la sous matière
         * @returns {any} libelle de la sous matière
         */
        $scope.getLibelleSousMatiere = function(idSousMatiere) {
            if (idSousMatiere == null || idSousMatiere === "") return "";
            return _.findWhere($scope.devoir.matiere.sousMatieres.all, {id : parseInt(idSousMatiere)}).libelle;
        };

        /**
         * Séquence d'enregistrement d'une évaluation
         * @param evaluation évaluation à enregistrer
         * @param $event evenement déclenchant
         * @param eleve élève propriétaire de l'évaluation
         */
        $scope.saveNoteDevoirEleve = function (evaluation, $event, eleve) {
            var reg = /^[0-9]+(\.[0-9]{1,2})?$/;
            if ((evaluation.oldValeur !== undefined && evaluation.oldValeur !== evaluation.valeur)
                || evaluation.oldAppreciation !== undefined && evaluation.oldAppreciation !== evaluation.appreciation) {
                if (evaluation.valeur !== "" &&  evaluation.valeur && reg.test(evaluation.valeur) && evaluation.valeur !== null) {
                    var devoir = evaluations.devoirs.findWhere({id : evaluation.id_devoir});
                    if (devoir !== undefined) {
                        if (parseFloat(evaluation.valeur) <= devoir.diviseur && parseFloat(evaluation.valeur) >= 0) {
                            evaluation.save().then(() => {
                                evaluation.oldValeur = evaluation.valeur;
                                evaluation.oldAppreciation = evaluation.appreciation;
                                if ($location.$$path === '/releve') {
                                    $scope.calculerMoyenneEleve(eleve);
                                    $scope.calculStatsDevoirReleve(evaluation.id_devoir);
                                } else {
                                    $scope.calculStatsDevoir();
                                }
                                $scope.opened.lightbox = false;
                                delete $scope.selected.eleve;
                                utils.safeApply($scope);
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
                            utils.safeApply($scope);
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

        /**
         * Ouvre le détail du devoir correspondant à l'index passé en paramètre
         * @param index index du devoir
         * @param bool état du détail
         */
        $scope.expand = function(index, bool){
            if($scope.openedDevoir !== index){
                $scope.openedDevoir = index;
            }else{
                if(bool === true){
                    $scope.openedDevoir = -1;
                }
            }
        };

        /**
         * Calcul la moyenne pour un élève
         * @param eleve élève
         */
        $scope.calculerMoyenneEleve = function(eleve) {
            eleve.getMoyenne().then(() => {
                utils.safeApply($scope);
            });
        };

        /**
         * Ouvre la fenêtre détail des compétences sur un devoir
         */
        $scope.getInfoCompetencesDevoir = function () {
            $scope.opened.lightbox = true;
            template.open('lightboxContainer', '../templates/evaluations/enseignants/informations/display_competences');
        };

        /**
         * Calcul les statistiques du devoir courant
         */
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
                utils.safeApply($scope);
            });
        };

        /**
         * Calcul les statistiques du devoir dont l'identifiant est passé en paramètre
         * @param devoirId identifiant du devoir
         */
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
                    utils.safeApply($scope);
                });
            }
        };

        /**
         * Lance le focus sur la cible de l'évènement
         * @param $event évènement
         */
        $scope.focusMe = function($event) {
            $event.target.select();
        };

        /**
         * Affiche les informations d'un devoir en fonction de l'objet passé en paramètre
          * @param obj objet de type Evaluation ou de type Devoir
         */
        $scope.getDevoirInfo = function (obj) {
            if (template.isEmpty('leftSide-devoirInfo')) template.open('leftSide-devoirInfo', '../templates/evaluations/enseignants/informations/display_devoir');
            if (obj instanceof Devoir) $scope.informations.devoir = obj;
            else if (obj instanceof Evaluation) {
                var devoir = $scope.releveNote.devoirs.findWhere({id : obj.id_devoir});
                if (devoir !== undefined) $scope.informations.devoir = devoir;
            }
        };

        /**
         * Retourne les informations relatives à un élève
         * @param eleve élève
         */
        $scope.getEleveInfo = function (eleve) {
            if (template.isEmpty('leftSide-userInfo')) template.open('leftSide-userInfo', '../templates/evaluations/enseignants/informations/display_eleve');
            $scope.informations.eleve = eleve;
        };

        /**
         * Retourne la période courante
         * @returns {Promise<T>} Promesse retournant l'identifiant de la période courante
         */
        var setCurrentPeriode = function () : Promise<any> {
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

        /**
         * Highlight la compétence survolée
         * @param id identifiant de la compétence
         */
        $scope.highlightCompetence = function (id, bool) {
            var competence = $scope.currentDevoir.competences.findWhere({id_competence : id});
            if (competence && competence !== undefined) {
                competence.hovered = bool;
            }
            return;
        }
    }
]);
