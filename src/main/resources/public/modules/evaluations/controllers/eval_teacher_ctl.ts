import { model, notify, idiom as lang, ng, template } from 'entcore/entcore';
import {Devoir, Evaluation, evaluations, ReleveNote, GestionRemplacement, Remplacement} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';
import {Collection} from "../../entcore/modelDefinitions";
import {lightbox} from "../../entcore/directives/lightbox";

let moment = require('moment');

declare let _:any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$sce', '$compile', '$timeout',
    function ($scope, route, $rootScope, $location, $filter, $sce, $compile, $timeout) {
        route({

            accueil : function(params){
                $scope.cleanRoot();

                // Chefs d'établissement
                $scope.isChefEtab = model.me.type === 'PERSEDUCNAT' &&
                    model.me.functions !== undefined &&
                    model.me.functions.DIR !== undefined &&
                    model.me.functions.DIR.code === 'DIR';

                template.open('main', '../templates/evaluations/enseignants/eval_acu_teacher');

            },

            listRemplacements : function(){
                $scope.cleanRoot();
                $scope.listRemplacements();

            },

            createDevoir : function(params){
                $scope.cleanRoot();
                $scope.createDevoir();
                evaluations.enseignements.on('sync', function() {
                    $scope.initFilter(true);
                });
            },

            editDevoir : function (params) {
                $scope.cleanRoot();
                var devoirTmp = $scope.devoirs.findWhere({id: parseInt(params.idDevoir)});
                $scope.devoir = $scope.initDevoir();
                $scope.devoir.id_groupe = devoirTmp.id_groupe;
                $scope.devoir.id = devoirTmp.id;
                $scope.devoir.name = devoirTmp.name;
                $scope.devoir.owner =  devoirTmp.owner;
                $scope.devoir.libelle =devoirTmp.libelle;
                $scope.devoir.id_sousmatiere = devoirTmp.id_sousmatiere;
                $scope.devoir.id_type = parseInt(devoirTmp.id_type);
                $scope.devoir.id_matiere  = devoirTmp.id_matiere;
                $scope.devoir.id_etat  = parseInt(devoirTmp.id_etat);
                $scope.devoir.date_publication = new Date(devoirTmp.date_publication);
                $scope.devoir.id_etablissement = devoirTmp.id_etablissement;
                $scope.devoir.diviseur = devoirTmp.diviseur;
                $scope.devoir.coefficient = parseInt(devoirTmp.coefficient);
                $scope.devoir.date = new Date(devoirTmp.date);
                $scope.devoir.ramener_sur = devoirTmp.ramener_sur;
                $scope.devoir.is_evaluated = devoirTmp.is_evaluated;
                $scope.oldIs_Evaluated = devoirTmp.is_evaluated;
                $scope.devoir.dateDevoir = new Date($scope.devoir.date);
                $scope.devoir.datePublication = new Date($scope.devoir.date_publication);
                $scope.devoir.id_periode = devoirTmp.id_periode;
                $scope.devoir.controlledDate = true;
                $scope.firstConfirmSuppSkill = false;
                $scope.secondConfirmSuppSkill = false;
                $scope.evaluatedDisabel = false;
                $scope.allCompetences = devoirTmp.competences;
                $scope.evaluatedCompetence = $scope.evaluationOfSkilles($scope.allCompetences,devoirTmp);
                $scope.devoir.competences.sync().then(() => {
                    $scope.createDevoir();
                    $scope.evaluations.competencesDevoir = $scope.devoir.competences.all;

                    evaluations.enseignements.on('sync', function() {
                        $scope.initFilter(true);

                        //tableau des connaissances à cocher éventuellement
                        var parentToCheck = [];

                        for (var i = 0; i < $scope.evaluations.competencesDevoir.length; i++) {
                            for (let j = 0; j < $scope.evaluations.enseignements.all.length; j++) {
                                if ($scope.competencesFilter[$scope.evaluations.competencesDevoir[i].id_competence + '_'
                                    + $scope.evaluations.enseignements.all[j].id] !== undefined) {
                                    //selection des competences du devoir
                                    $scope.competencesFilter[$scope.evaluations.competencesDevoir[i].id_competence
                                    + '_' + $scope.evaluations.enseignements.all[j].id].isSelected = true;

                                    $scope.evaluations.competencesDevoir[i].id
                                        = $scope.evaluations.competencesDevoir[i].id_competence;
                                    $scope.devoir.competences.all[i].id = $scope.devoir.competences.all[i].id_competence;

                                    //remplissage des connaissances parent  à cocher éventuellement
                                    var parentCo = $scope.competencesFilter[$scope.evaluations.competencesDevoir[i].id_parent
                                    + '_' + $scope.evaluations.enseignements.all[j].id];
                                    if (parentToCheck.indexOf(parentCo) === -1 && parentCo !== undefined) {
                                        parentToCheck.push(parentCo);
                                    }

                                    utils.safeApply($scope);
                                }
                            }
                        }

                        //On coche la connaissance si elle n'a aucun fils sélectionné
                        for (var i = 0; i < parentToCheck.length; i++) {
                            var checkIt = true;
                            for (let j in  $scope.competencesFilter) {
                                if ($scope.competencesFilter.hasOwnProperty(j)) {
                                    var currComp = $scope.competencesFilter[j];
                                    if (currComp !== undefined && currComp.data.id_parent === parentToCheck[i].data.id) {
                                        checkIt = currComp.isSelected;
                                    }
                                    // si on rencontre un fils non selectionné on arrête de chercher
                                    if (!checkIt) {
                                        break;
                                    }
                                }
                            }
                            if (checkIt) {
                                parentToCheck[i].isSelected = true;
                                parentToCheck[i].id = parentToCheck[i].id_competence;
                            }
                            else {
                                parentToCheck[i].isSelected = false;
                                parentToCheck[i].id = parentToCheck[i].id_competence;
                            }
                            //depliement de l'enseignement pour les compétences sélectionnées du devoir à modifier
                            var enseignementToOpen =$scope.devoir.enseignements.all.find(
                                function (elem) { return elem.id === parentToCheck[i].data.id_enseignement});
                            enseignementToOpen.open =true;

                            //depliement des connaissances parent des compétences du devoir à modifier
                            parentToCheck[i].data.open = true;
                            utils.safeApply($scope);
                        }
                    });
                    template.open('main', '../templates/evaluations/enseignants/creation_devoir/display_creation_devoir');
                    utils.safeApply($scope);
                });
            },

            listDevoirs : function(params){
                $scope.cleanRoot();
                template.open('main', '../templates/evaluations/enseignants/liste_devoirs/display_devoirs_structure');
                template.open('evaluations', '../templates/evaluations/enseignants/liste_devoirs/list_view');
                if (!evaluations.devoirs.percentDone) evaluations.devoirs.getPercentDone();
                utils.safeApply($scope);
            },
            viewNotesDevoir : function(params){
                $scope.cleanRoot();
                window.scrollTo(0, 0);
                $scope.resetSelected();
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
                            if ($scope.currentDevoir.eleves.all[i].evaluation.valeur !== null
                                && $scope.currentDevoir.eleves.all[i].evaluation.valeur !== undefined
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
                $scope.cleanRoot();
                // Affichage des criteres par défaut quand on arrive sur le releve
                $scope.openLeftMenu("opened.criteres", false);
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
                $scope.cleanRoot();
                if( params.idEleve != undefined && params.idClasse != undefined ){
                    //console.log(params.idEleve);
                    // console.log(params.idClasse);
                    $scope.search.classe = _.findWhere(evaluations.classes.all,{ 'id': params.idClasse} );
                    $scope.search.eleve =  _.findWhere($scope.search.classe.eleves.all,{'id': params.idEleve});
                    if($scope.displayFromClass)  $scope.displayFromClass= false;
                    $scope.displayFromClass = true;
                }
                template.open('main', '../templates/evaluations/enseignants/suivi_competences_eleve/container');
                if($scope.informations.eleve === undefined){
                    $scope.informations.eleve = null;
                }
                $scope.sortType     = 'title'; // set the default sort type
                $scope.sortReverse  = false;  // set the default sort order

            },
            displaySuiviCompetencesClasse : function () {
                $scope.cleanRoot();
                template.open('main', '../templates/evaluations/enseignants/suivi_competences_classe/container');
                $scope.sortType     = 'title'; // set the default sort type
                $scope.sortReverse  = false;  // set the default sort order
            }
        });



        $scope.evaluations = evaluations;
        $scope.competencesSearchKeyWord = "";
        $scope.devoirs = evaluations.devoirs;
        $scope.enseignements = evaluations.enseignements;
        $scope.bSelectAllEnseignements = false;
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
            name : ''
        };

        $scope.informations = {};
        $scope.messages = {
            successEvalLibre : false
        };
        $scope.me = model.me;
        $scope.opened = {
            devoir : -1,
            note : -1,
            criteres : true,
            details : true,
            statistiques : true,
            studentInfo : true,
            devoirInfo : true,
            lightbox : false,
            lightboxEvalLibre : false,
            lightboxs : {
                updateDevoir : {
                    firstConfirmSupp : false,
                    secondConfirmSupp : false,
                    evaluatedSkillDisabel : false
                },
                createDevoir : {
                    firstConfirmSupp : false,
                    secondConfirmSupp : false
                }
            },
            accOp : 0,
            evaluation : {
                suppretionMsg1 : false,
                suppretionMsg2 : false,
            }
        };
        $scope.text = "";
        $scope.selected = {
            devoirs : {
                list : [],
                listwithEvaluatedSkills :[],
                listwithEvaluatedMarks : [],
                all : false
            },
            eleve : null,
            eleves : {
                list : [],
                all : false
            },
            competences : {
                list : [],
                all : false
            }
        };
        $scope.eleves = [];
        if (evaluations.synchronized.classes !== 0) {
            evaluations.classes.on('classes-sync', () => {
                for (let i = 0; i < evaluations.classes.all.length; i++) {
                    let elevesOfclass = _.map(evaluations.classes.all[i].eleves.all, function(eleve){
                        return _.extend(eleve,{
                                classEleve : evaluations.classes.all[i]
                            }
                        );
                    });
                    $scope.eleves = _.union($scope.eleves, elevesOfclass);
                }


            });
        } else {
            for (let i = 0; i < evaluations.classes.all.length; i++) {
                let elevesOfclass = _.map(evaluations.classes.all[i].eleves.all, function(eleve){
                    return _.extend(eleve,{
                            classEleve : evaluations.classes.all[i]
                        }
                    );
                });
                $scope.eleves = _.union($scope.eleves, elevesOfclass);
            }

        }
        /**
         * cette function permet d'extraire les competences evalué du devoir
         * @param Skills : les competences du devoir
         * @param Devoir : le devoir à examiner
         * @returns {Array} of skills
         */
        $scope.evaluationOfSkilles = function (Skills, Devoir) {
            let Myarray=[];

            if(Skills.all.length > 0){
                for( let i=0; i <  Skills.all.length; i++){
                    let isEvaluated = false;
                    _.map(Devoir.eleves.all,function (eleve) {
                        if( eleve.evaluation.competenceNotes.findWhere({id_competence: Skills.all[i].id_competence }).evaluation !== -1){
                            isEvaluated = true;
                        }
                    });
                    if (isEvaluated)
                        Myarray.push( Skills.all[i]);
                }
                return Myarray;
            }
        };
        $scope.afficherRecap = function () {
            if($scope.opened.accOp === 1 ){
                $scope.opened.accOp = 0;
            }else {
                $scope.opened.accOp = 1;
            }

        };

        $scope.confirmSuppretion = function () {
            if ($scope.selected.devoirs.list.length > 0) {
                $scope.opened.evaluation.suppretionMsg1 = true;
            }
        };
        $scope.textSuppretionMsg2 = {
            Text1 : lang.translate('evaluations.devoir.recaputilatif.suppression.text1'),
            Text2 : lang.translate('evaluations.devoir.recaputilatif.suppression.text2'),
            Text3 : lang.translate('evaluations.devoir.recaputilatif.suppression.text3'),
            Text4 : lang.translate('evaluations.devoir.recaputilatif.suppression.text4'),
            Text5 : lang.translate('evaluations.devoir.recaputilatif.suppression.text5'),
            Text6 : lang.translate('evaluations.devoir.recaputilatif.suppression.text6'),
            TextFin : lang.translate('evaluations.devoir.recaputilatif.suppression.confirmation')
        };


        $scope.firstConfirmationSuppDevoir = function () {
            if($scope.selected.devoirs.list.length > 0) {
                let idDevoir = [];
                _.map($scope.selected.devoirs.list, function (devoir) {
                    idDevoir.push(devoir.id);
                });

                //verification si le/les devoirs ne contiennes pas une compétence evalué
                $scope.devoirs.areEvaluatedDevoirs(idDevoir).then((res) => {

                    $scope.selected.devoirs.listwithEvaluatedSkills = [];
                    $scope.selected.devoirs.listwithEvaluatedMarks = [];
                    for (let i = 0; i < res.length; i++) {
                        if (res[i].nbevalskill > 0 && res[i].nbevalskill != null) {
                            $scope.selected.devoirs.listwithEvaluatedSkills.push(
                                {
                                    id: res[i].id,
                                    nbevalskill: res[i].nbevalskill,
                                    name: _.findWhere($scope.devoirs.all, {id: res[i].id}).name
                                });

                        }

                        if (res[i].nbevalnum > 0 && res[i].nbevalnum != null) {
                            $scope.selected.devoirs.listwithEvaluatedMarks.push({
                                id: res[i].id,
                                nbevalnum: res[i].nbevalnum,
                                name: _.findWhere($scope.devoirs.all, {id: res[i].id}).name
                            });

                        }
                    }
                    $scope.opened.evaluation.suppretionMsg1 = false;
                    if ($scope.selected.devoirs.listwithEvaluatedSkills.length > 0 || $scope.selected.devoirs.listwithEvaluatedMarks.length > 0) {
                        $scope.opened.evaluation.suppretionMsg2 = true;
                    }else{
                        $scope.deleteDevoir();
                    }

                    utils.safeApply($scope);
                });
            }
        };
        /* $scope.$watch(function() { return $scope.opened.evaluation.suppretionMsg1; }, function (newValue, oldValue) {
         if (newValue===false && oldValue){
         if ($scope.selected.devoirs.listwithEvaluatedSkills.length > 0 || $scope.selected.devoirs.listwithEvaluatedMarks.length > 0) {
         $scope.opened.evaluation.suppretionMsg2 = true;
         }
         }

         });*/
        $scope.conditionAffichageText = function (NumText) {
            if(NumText === 1){
                if(($scope.selected.devoirs.listwithEvaluatedSkills.length + $scope.selected.devoirs.listwithEvaluatedMarks.length ) > 16 && $scope.selected.devoirs.listwithEvaluatedSkills.length !== 0 && $scope.selected.devoirs.listwithEvaluatedMarks.length!== 0){
                    return true;
                }else{
                    return false;
                }
            }else if(NumText === 2){
                if($scope.selected.devoirs.listwithEvaluatedMarks.length  > 16 && $scope.selected.devoirs.listwithEvaluatedSkills.length === 0){
                    return true;
                }else{
                    return false;
                }
            }else if(NumText === 3){
                if($scope.selected.devoirs.listwithEvaluatedSkills.length > 16 && $scope.selected.devoirs.listwithEvaluatedMarks.length  === 0){
                    return true;
                }else{
                    return false;
                }
            }else if(NumText === 4){
                if($scope.selected.devoirs.listwithEvaluatedSkills.length < 16 && $scope.selected.devoirs.listwithEvaluatedMarks.length  === 0){
                    return true;
                }else{
                    return false;
                }
            }else if(NumText === 5){
                if($scope.selected.devoirs.listwithEvaluatedMarks.length < 16 && $scope.selected.devoirs.listwithEvaluatedSkills.length === 0){
                    return true;
                }else{
                    return false;
                }
            }else if(NumText === 6){
                if(($scope.selected.devoirs.listwithEvaluatedSkills.length + $scope.selected.devoirs.listwithEvaluatedMarks.length) < 16 && $scope.selected.devoirs.listwithEvaluatedSkills.length !== 0 && $scope.selected.devoirs.listwithEvaluatedMarks.length!== 0){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }

        };
        $scope.annulerSuppretion = function () {
            $scope.opened.evaluation.suppretionMsg2 = false;
            $scope.opened.evaluation.suppretionMsg1 = false;
        };

        $scope.releveNote = undefined;
        evaluations.devoirs.on('sync', function () {
            $scope.mapIdLibelleDevoir = _.object(_.map($scope.devoirs.all, function(item) {
                return [item.id, item.name]
            }));
        });

        evaluations.classes.on('classes-sync', function () {
            utils.safeApply($scope);
        });

        $scope.goTo = function(path,id){
            $location.path(path);
            if(id != undefined)
                $location.search(id);
            $location.replace();
            utils.safeApply($scope);
        };

        evaluations.periodes.on('sync', function () {
            setCurrentPeriode().then((defaultPeriode) => {
                $scope.search.periode = (defaultPeriode !== -1) ? defaultPeriode : '*';
                utils.safeApply($scope);
            });
        });

        $scope.resetSelected = function () {
            $scope.selected = {
                devoirs : {
                    list : [],
                    listwithEvaluatedSkills :[{

                    }],
                    listwithEvaluatedMarks : [],
                    all : false
                },
                eleve : null,
                eleves : {
                    list : [],
                    all : false
                },
                competences : {
                    list : [],
                    all : false
                }
            };
        };

        /**
         * Initialise un nouveau devoir.
         */
        $scope.initDevoir = function () {
            return new Devoir({
                name : undefined,
                date_publication  : new Date(),
                date       : new Date(),
                diviseur         : 20,
                coefficient      : 1,
                id_etablissement  : model.me.structures[0],
                ramener_sur       : false,
                id_etat           : 1,
                owner            : model.me.userId,
                matieresByClasse : [],
                controlledDate   : true,
                is_evaluated         : false
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
                && $scope.devoir.id_groupe !== undefined
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
                && ($scope.devoir.is_evaluated || $scope.evaluations.competencesDevoir.length > 0)
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
         * @param bool booleen
         */
        $scope.selectElement = function (list, bool) {
            for (var i = 0; i < list.length; i++) {
                if (bool !== undefined && list[i].selected === bool) continue;
                list[i].selected = !list[i].selected;
            }
        };



        /**
         * Sélectionne/Déselectionne tous les devoirs de l'utilisateur
         */
        $scope.selectAllDevoirs = function(){
            if ($scope.selected.devoirs.all !== true) {
                $scope.selectElement($scope.selected.devoirs.list, false);
                $scope.selected.devoirs.list = [];
                return;
            }
            $scope.selected.devoirs.list = $filter('customSearchFilters')($scope.devoirs.all, $scope.search);
            $scope.selectElement($scope.selected.devoirs.list, true);
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
         * @param pbInitSelected boolean d'initialisation
         */
        $scope.initFilterRec = function (poCompetences, pbInitSelected) {
            if(poCompetences !== undefined) {
                var _b = false;
                var comp : any = null;
                for (var i = 0; i < poCompetences.all.length; i++) {
                    var currCompetence = poCompetences.all[i];
                    comp = _.findWhere(poCompetences.all, {id : poCompetences.all[i].id}) !== undefined
                    if (comp !== undefined) _b = false;
                    $scope.competencesFilter[currCompetence.id+"_"+currCompetence.id_enseignement] = {
                        isSelected : _b,
                        nomHtml :  $scope.buildCompetenceNom(currCompetence),
                        data : currCompetence
                    };

                    $scope.initFilterRec(currCompetence.competences, pbInitSelected);
                }
            }
        };


        /**
         * Construis le nom d'une compétence préfixée de la codification du domaine dont elle est rattachée.
         * Si pas de domaine rattaché, seul le nom est retourné
         * @param poCompetence la compétence
         * @returns {le nom construis sous forme d'une chaine de caractères}
         */
        $scope.buildCompetenceNom = function(poCompetence) {

            if(poCompetence.code_domaine !== null && poCompetence.code_domaine !== undefined) {
                return poCompetence.code_domaine+ " - " +poCompetence.nom;
            } else{
                return poCompetence.nom;
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
            if($scope.enseignementsFilter !== undefined){
                return ($scope.enseignementsFilter[enseignement.id] && $scope.enseignementsFilter[enseignement.id].isSelected);
            }
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
         * Sélectionne/Désélectionne tous les enseignements dans l'écran de filtre des compétences
         * lors de la création d'un devoir.
         *
         */
        $scope.selectUnselectEnseignements = function(){
            $scope.selectEnseignements($scope.bSelectAllEnseignements);
            $scope.bSelectAllEnseignements = !$scope.bSelectAllEnseignements;
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
                        var DisplayNomSousCompetence = nomHtml;
                        if(sousCompetence.code_domaine!=null){
                            DisplayNomSousCompetence = sousCompetence.code_domaine + " - "+ nomHtml;
                        }
                        // mise à jour que si la réelle valeur de la chaine html est différente ($sce.trustAsHtml renvoie systématiquement une valeur différente)
                        if($sce.getTrustedHtml($scope.competencesFilter[sousCompetence.id+"_"+sousCompetence.id_enseignement].nomHtml) !== $sce.getTrustedHtml(nomHtml)) {
                            $scope.competencesFilter[sousCompetence.id+"_"+sousCompetence.id_enseignement].nomHtml = DisplayNomSousCompetence;
                        }

                    } else {
                        $scope.competencesFilter[sousCompetence.id+"_"+sousCompetence.id_enseignement].nomHtml = $scope.buildCompetenceNom(sousCompetence);
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
         * Charge les enseignements et les compétences en fonction de la classe.
         * @param psIdClasse identifiant de la classe sélectionnée.
         */
        $scope.loadEnseignementsByClasse = function (classe_Id) {
            var newIdCycle = $scope.getClasseData(classe_Id, 'id_cycle');
            var currentIdCycle = null;
            for (let i = 0; i < $scope.enseignements.all.length && currentIdCycle == null; i++) {
                if ($scope.enseignements.all[i].data.competences_1 !== undefined &&
                    $scope.enseignements.all[i].data.competences_1 !== null) {
                    for (let j = 0; j < $scope.enseignements.all[i].data.competences_1.length && currentIdCycle == null; j++) {
                        currentIdCycle = $scope.enseignements.all[i].data.competences_1[j].id_cycle;
                    }
                }
            }
            if (currentIdCycle !== null && currentIdCycle !== newIdCycle) {
                evaluations.enseignements.sync($scope.devoir.id_groupes);
                evaluations.enseignements.on('sync', function () {
                    //suppression des compétences qui n'appartiennent pas au cycle
                    var newCompentenceDevoir = [];

                    for (var o in $scope.competencesFilter) {
                        if ($scope.competencesFilter[o].isSelected === true
                            && $scope.competencesFilter[o].data.id_cycle === currentIdCycle) {
                            if ($scope.competencesFilter[o].data.id_parent !== 0) {
                                newCompentenceDevoir.push($scope.competencesFilter[o].data);
                            }
                        }
                    }

                    evaluations.competencesDevoir = newCompentenceDevoir;

                    utils.safeApply($scope);
                });
            }
        };

        /**
         * Séquence de création d'un devoir
         */
//TODO Déplacer cette séquence dans la séquence du router
        $scope.createDevoir = function () {
            if($location.path() === "/devoir/create") {
                $scope.devoir = $scope.initDevoir();
            }
            //$scope.opened.lightbox = true;
            $scope.controlledDate = (moment($scope.devoir.date_publication).diff(moment($scope.devoir.date), "days") <= 0);
            // resynchronisation de la liste pour eviter les problemes de references et de checkbox precedements cochees
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

            //Séquence non exécutée lors de la modification d'un devoir
            if($scope.devoir.id_periode !== undefined) {
                setCurrentPeriode().then((defaultPeriode) => {
                    $scope.devoir.id_periode = defaultPeriode.id;
                    utils.safeApply($scope);
                });
            }
            if($scope.devoir.id_type === undefined) {
                $scope.devoir.id_type = getDefaultTypDevoir();
            }
            if($scope.devoir.id_groupe === undefined) {
                if ($scope.search.classe.id !== '*' && $scope.search.matiere !== '*') {
                    $scope.devoir.id_groupe = $scope.search.classe.id;
                    $scope.devoir.id_matiere = $scope.search.matiere.id;
                    $scope.setClasseMatieres();
                    $scope.selectedMatiere();
                } else {
                    // selection de la premiere classe par defaut
                    $scope.devoir.id_groupe = $scope.classes.all[0].id;
                    // selection de la premiere matière associée à la classe
                    $scope.setClasseMatieres();
                }
            }

            // Chargement des enseignements et compétences en fonction de la classe
            evaluations.enseignements.sync($scope.devoir.id_groupe);

            if ($location.path() === "/devoirs/list") {
                $scope.devoir.id_type = $scope.search.type.id;
                $scope.devoir.id_sousmatiere = $scope.search.sousmatiere.id;
            }


            //template.open('lightboxContainer', '../templates/evaluations/enseignants/creation_devoir/display_creation_devoir');
            if($location.path() !== "/devoir/"+$scope.devoir.id+"/edit") {
                template.open('main', '../templates/evaluations/enseignants/creation_devoir/display_creation_devoir');
                utils.safeApply($scope);
            }


        };


// on ecoute sur l'evenement checkConnaissances
// ie on doit ajouter/supprimer toutes les sous competences dans le recap
        $scope.$on('checkConnaissances', function(event, parentItem){
            parentItem.competences.each(function(e){
                if($scope.competencesFilter[parentItem.id+"_"+parentItem.id_enseignement].isSelected === true) {
                    // check si on a pas deja ajoute pour eviter les doublons
                    var competence = _.findWhere(evaluations.competencesDevoir, {id : e.id});

                    // on ajoute que si la compétence n'existe pas (cela peut arriver si on a la même compétence sous un ensignement différent par exemple)
                    if(competence === undefined) {
                        //if(!_.contains(evaluations.competencesDevoir, e)) {
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
            if($scope.competencesFilter[item.id+"_"+item.id_enseignement].isSelected === true) {
                // check si on a pas deja ajoute pour eviter les doublons
                var competence = _.findWhere(evaluations.competencesDevoir, {id : item.id});

                // on ajoute que si la compétence n'existe pas (cela peut arriver si on a la même compétence sous un ensignement différent par exemple)
                if(competence === undefined) {
                    //if(!_.contains(evaluations.competencesDevoir, item)) {
                    evaluations.competencesDevoir.push(item);
                }
            } else {
                evaluations.competencesDevoir = _.reject(evaluations.competencesDevoir, function(comp){ return comp.id === item.id; });
            }
        });

// create the timer variable
        var timer;

// mouseenter event
        $scope.showIt = function (item) {
            timer = $timeout(function () {
                item.hoveringRecap = true;
            }, 350);
        };

// mouseleave event
        $scope.hideIt = function (item) {
            $timeout.cancel(timer);
            item.hoveringRecap = false;
        };


        $scope.deleteDevoir = function () {
            if($scope.selected.devoirs.list.length > 0){
                $scope.selected.devoirs.list.forEach(function(d) {
                        d.remove().then((res) => {
                            evaluations.devoirs.sync();
                            evaluations.devoirs.on('sync', function () {
                                $scope.opened.lightbox = false;
                                var index= $scope.selected.devoirs.list.indexOf(d);
                                if(index > -1) {
                                    $scope.selected.devoirs.list = _.without($scope.selected.devoirs.list, d);
                                }
                                $scope.goTo('/devoirs/list');
                                utils.safeApply($scope);
                            });
                        })
                            .catch(() => {
                                notify.error("evaluation.delete.devoir.error");
                            });
                    }
                );
            }
            $scope.opened.evaluation.suppretionMsg2 = false;
        };

        $scope.cancelUpdateDevoir = function () {
            $scope.firstConfirmSuppSkill = false;
            $scope.secondConfirmSuppSkill = false;
            $scope.evaluatedDisabel = false;
            $scope.opened.lightboxs.updateDevoir.firstConfirmSupp = false ;
            $scope.opened.lightboxs.updateDevoir.secondConfirmSupp = false ;
            $scope.opened.lightboxs.updateDevoir.evaluatedSkillDisabel = false;

        };
        $scope.ConfirmeUpdateDevoir = function () {
            if($scope.opened.lightboxs.updateDevoir.firstConfirmSupp === true){
                $scope.firstConfirmSuppSkill = true;
                $scope.opened.lightboxs.updateDevoir.secondConfirmSupp = true;
                $scope.opened.lightboxs.updateDevoir.firstConfirmSupp = false;
            }else if($scope.opened.lightboxs.updateDevoir.secondConfirmSupp === true){
                $scope.secondConfirmSuppSkill = true;
                $scope.opened.lightboxs.updateDevoir.secondConfirmSupp = false;
            }else  if($scope.opened.lightboxs.updateDevoir.evaluatedSkillDisabel){
                $scope.evaluatedDisabel = true;
                $scope.opened.lightboxs.updateDevoir.evaluatedSkillDisabel = false;
            }
        };
        /**
         *
         */
        $scope.beforSaveDevoir = function () {
            $scope.competencesSupp = [];
            $scope.evaluatedCompetencesSupp = [];
            //
            if( $location.path() === "/devoir/"+$scope.devoir.id+"/edit"){
                //les compétences à supprimer
                for( let i=0; i < $scope.allCompetences.all.length ; i++){
                    let maCompetence = _.findWhere(evaluations.competencesDevoir, {id: $scope.allCompetences.all[i].id_competence } );
                    if(maCompetence === undefined ){
                        $scope.competencesSupp.push($scope.allCompetences.all[i]);
                    }
                }
                $scope.devoir.isEvaluatedDevoir($scope.devoir.id).then((res)=>{
                    $scope.devoir.evaluationDevoirs;
                    //si il y a des competences à supprimer

                    if ($scope.competencesSupp.length > 0) {

                        //est ce que les competences sont evalué
                        let competence;
                        for(let i=0; i < $scope.competencesSupp.length ; i++){
                            competence =  _.findWhere($scope.devoir.evaluationDevoirs.all,{id: String($scope.competencesSupp[i].id_competence), typeeval: 'TypeEvalSkill' });
                            if(competence !== undefined){
                                $scope.evaluatedCompetencesSupp.push($scope.competencesSupp[i]);
                            }
                        }
                        if( $scope.evaluatedCompetencesSupp.length > 0)
                            $scope.opened.lightboxs.updateDevoir.firstConfirmSupp = true;
                        else{
                            $scope.firstConfirmSuppSkill = true;
                            $scope.secondConfirmSuppSkill = true;
                        }
                    }else{
                        $scope.firstConfirmSuppSkill = true;
                        $scope.secondConfirmSuppSkill = true;
                    }
                });


            }else{
                $scope.firstConfirmSuppSkill = true;
                $scope.secondConfirmSuppSkill = true;
                $scope.evaluatedDisabel = true;
            }

        };

        $scope.listnerSaveNewDevoir = function () {
            if ($scope.firstConfirmSuppSkill === true && $scope.secondConfirmSuppSkill === true && $scope.evaluatedDisabel === true) {
                $scope.saveNewDevoir();
                $scope.firstConfirmSuppSkill = false;
                $scope.secondConfirmSuppSkill = false;
                $scope.evaluatedDisabel = false;
            }
        };
        $scope.$watch(function() { return $scope.firstConfirmSuppSkill; }, function (newValue, oldValue) {
            if (newValue){

                $scope.listnerSaveNewDevoir();
            }

        });
        $scope.$watch(function() { return $scope.secondConfirmSuppSkill; }, function (newValue, oldValue) {
            if (newValue){
                if($scope.firstConfirmSuppSkill === true && $scope.secondConfirmSuppSkill === true && $scope.evaluatedDisabel === false) {
                    if ($scope.oldIs_Evaluated === true && $scope.devoir.is_evaluated === false && ( _.findWhere($scope.devoir.evaluationDevoirs.all,{ typeeval: 'TypeEvalNum' }) !== undefined ) ) {
                        $scope.opened.lightboxs.updateDevoir.evaluatedSkillDisabel = true;
                    } else
                        $scope.evaluatedDisabel = true;
                }
                $scope.listnerSaveNewDevoir();}
        });
        $scope.$watch(function() { return $scope.evaluatedDisabel; }, function (newValue, oldValue) {
            if (newValue)
                $scope.listnerSaveNewDevoir();
        });
        /**
         *  Sauvegarde du devoir à la suite du formulaire de création
         */
        $scope.saveNewDevoir = function () {
            $scope.devoir.date = $scope.getDateFormated($scope.devoir.dateDevoir);
            $scope.devoir.date_publication = $scope.getDateFormated($scope.devoir.datePublication);

            // Pour la sauvegarde on ne recupere que les id des competences
            if($location.path() !== "/devoir/"+$scope.devoir.id+"/edit") {
                if (evaluations.competencesDevoir !== undefined) {
                    $scope.devoir.competences = [];
                    for (var i = 0; i < evaluations.competencesDevoir.length; i++) {
                        $scope.devoir.competences.push(evaluations.competencesDevoir[i].id);
                    }
                }
            }
            else{
                $scope.devoir.coefficient = parseInt($scope.devoir.coefficient);
                if (evaluations.competencesDevoir !== undefined) {
                    $scope.devoir.competencesAdd= [];
                    $scope.devoir.competencesRem = [];

                    //recherche des competences a ajouter
                    for (let i = 0; i < evaluations.competencesDevoir.length; i++) {
                        var toAdd = true;
                        for(let j =0; j < $scope.devoir.competences.all.length; j++) {
                            if ($scope.devoir.competences.all[j].id_competence
                                === evaluations.competencesDevoir[i].id) {
                                toAdd = false;
                                break;
                            }
                        }
                        if(toAdd){
                            $scope.devoir.competencesAdd.push(evaluations.competencesDevoir[i].id);
                        }
                    }
                    //Recherche des competences a supprimer
                    for(let j =0; j < $scope.devoir.competences.all.length; j++ ){
                        var toDel = true;
                        for (let i = 0; i < evaluations.competencesDevoir.length; i++) {
                            if($scope.devoir.competences.all[j].id ===
                                evaluations.competencesDevoir[i].id){
                                toDel = false;
                                break;
                            }
                        }
                        if(toDel){
                            $scope.devoir.competencesRem.push($scope.devoir.competences.all[j].id);
                        }
                    }


                }
                utils.safeApply($scope);
            }
            $scope.devoir.save($scope.devoir.competencesAdd, $scope.devoir.competencesRem).then((res) => {
                evaluations.devoirs.sync().then(() => {
                    if ($location.path() === "/devoirs/list" || $location.path() === "/devoir/create") {
                        if (res !== undefined) {
                            $location.path("/devoir/" + res.id);
                        }

                    } else if ($location.path() === "/releve") {
                        if ($scope.releveNote === undefined || !$scope.releveNote) {
                            $scope.search.classe.id = $scope.devoir.id_groupe;
                            $scope.search.matiere.id = $scope.devoir.id_matiere;
                            $scope.search.periode.id = $scope.devoir.id_periode;
                            $scope.getReleve();
                        } else {
                            $scope.releveNote.devoirs.sync();
                        }
                    }
                    else if ($location.path() === "/devoir/" + $scope.devoir.id + "/edit") {
                        $location.path("/devoir/" + $scope.devoir.id);
                    }
                    $scope.opened.lightbox = false;
                    utils.safeApply($scope);
                });
            });
        };

        /**
         * Récupère les matières enseignées sur la classe donnée
         * @param idClasse Identifiant de la classe
         * @returns {Promise<T>} Promesse de retour
         */
        let getClassesMatieres = function (idClasse) {
            return new Promise((resolve, reject) => {
                let classe = $scope.classes.findWhere({id : idClasse});
                if (classe !== undefined) {
                    if (resolve && typeof resolve === 'function') {
                        resolve($scope.matieres.filter((matiere) => {
                            return (matiere.libelleClasses.indexOf(classe.externalId) !== -1)
                        }));
                    }
                } else {
                    reject();
                }
            });
        };

        /**
         * Set les matière en fonction de l'identifiant de la classe
         */
        $scope.setClasseMatieres = function () {
            getClassesMatieres($scope.devoir.id_groupe).then((matieres) => {
                $scope.devoir.matieresByClasse = matieres;
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


        $scope.openLeftMenu = function openLeftMenu(psMenu, pbAfficherMenu) {

            pbAfficherMenu = !pbAfficherMenu;

            if(psMenu === "openedDevoirInfo") {
                $scope.openedDevoirInfo = pbAfficherMenu;
            }else if(psMenu === "openedStudentInfo") {
                $scope.openedStudentInfo = pbAfficherMenu;
            }else  if(psMenu === "opened.criteres") {
                $scope.opened.criteres = pbAfficherMenu;
            }else {
                console.error("Parametre psMenu inconnu : psMenu="+psMenu);
            }


            // Dans le cas du relevé de notes, on replie les 2 autres menus dans
            // un problème d'espace vertical
            if ($location.$$path === '/releve') {

                if(pbAfficherMenu) {
                    if(psMenu === "openedDevoirInfo") {
                        $scope.openedStudentInfo = false;
                        $scope.opened.criteres = false;
                    }

                    if(psMenu === "openedStudentInfo") {
                        $scope.openedDevoirInfo = false;
                        $scope.opened.criteres = false;
                    }

                    if(psMenu === "opened.criteres") {
                        $scope.openedDevoirInfo = false;
                        $scope.openedStudentInfo = false;
                    }
                }
            }
        };


        /**
         * Séquence de récupération d'un relevé de note
         */
        $scope.getReleve = function () {
            if($scope.search.classe !== undefined && $scope.search.classe.id !== undefined
                && $scope.search.matiere !== undefined && $scope.search.matiere.id !== undefined
                && $scope.search.periode !== undefined
                && $scope.search.classe !== undefined && $scope.search.classe.id !== '*'
                && $scope.search.matiere !== '*' && $scope.search.periode !== '*') {
                var p = {
                    idEtablissement : model.me.structures[0],
                    idClasse : $scope.search.classe.id,
                    idPeriode : parseInt($scope.search.periode.id),
                    idMatiere : $scope.search.matiere.id
                };
                // var rn = evaluations.releveNotes.findWhere(p);
                // if (rn === undefined) {
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
                // } else {
                //     $scope.releveNote = rn;
                //     utils.safeApply($scope);
                // }

                $scope.openedStudentInfo = false;
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
         * Retourne la données de la classe passé en paramètre
         * @param idClasse identifiant de la classe
         * @param key clé à renvoyer
         * @returns {any} la valeur de la clé passée en paramètre
         */
        $scope.getClasseData = (idClasse, key) => {
            if (idClasse == null || idClasse === '' || $scope.classes.all.length === 0) return '';
            let classe = $scope.classes.findWhere({id : idClasse});
            if (classe !== undefined && classe.hasOwnProperty(key)) {
                return classe[key];
            } else {
                return '';
            }
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
            let type = _.findWhere($scope.types.all, {id : parseInt(idType)});
            if (type !== undefined && type.hasOwnProperty('nom')) return type.nom;
            else return ''
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
            if (idSousMatiere == null || idSousMatiere === "" || idSousMatiere == undefined) return "";
            $scope.selectedMatiere();
            return _.findWhere($scope.devoir.matiere.sousMatieres.all, {id : parseInt(idSousMatiere)}).libelle;
        };


        /*  $scope.getLibelleDevoir = function (id) {
         var devoir = $scope.devoirs.findWhere({id : id});
         if (devoir !== undefined) return devoir.name;
         };
         */
        $scope.getLibelleDevoir = function (id) {
            if($scope.mapIdLibelleDevoir !== undefined) return $scope.mapIdLibelleDevoir[parseInt(id)];
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
                            if(evaluation.data.id !== undefined && evaluation.id === undefined){
                                evaluation.id = evaluation.data.id;
                            }
                            evaluation.save().then((res) => {
                                evaluation.valid = true;
                                evaluation.oldValeur = evaluation.valeur;
                                evaluation.oldAppreciation = evaluation.appreciation;
                                evaluation.id = res.id;
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
                        evaluation.delete().then((res) => {
                            if ($location.$$path === '/releve') {
                                $scope.calculerMoyenneEleve(eleve);
                                $scope.calculStatsDevoirReleve(evaluation.id_devoir);
                            } else {
                                if(res.rows === 1) {
                                    evaluation.id = undefined;
                                    evaluation.data.id = undefined;
                                }
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
            $scope.opened.lightbox = false;
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
         * Ouvre le détail de l'élève correspondant à l'index passé en paramètre et affiche l'appréciation
         * @param index index du devoir
         * @param bool état du détail
         */
        $scope.expandAppreciation = function(index, bool){

            if($scope.openedEleve !== index){
                $scope.openedEleve = index;
            }else{
                if(bool === true){
                    $scope.openedEleve = -1;
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
                if ($scope.currentDevoir.eleves.all[i].evaluation !== undefined &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== '' &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== undefined &&
                    $scope.currentDevoir.eleves.all[i].evaluation.valeur !== null) {
                    evals.push($scope.currentDevoir.eleves.all[i].evaluation);
                    $scope.currentDevoir.eleves.all[i].evaluation.ramener_sur = $scope.currentDevoir.ramener_sur;
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
                            $scope.releveNote.classe.eleves.all[i].evaluations.all[j].is_evaluated = devoir.is_evaluated;
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

            if ($location.$$path === '/releve') {
                $scope.openLeftMenu("openedDevoirInfo", false);
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
        };

        /**
         * Sélectionne un élève et l'ajoute dans la liste d'élèves sélectionnés.
         * @param eleve élève
         */
        $scope.selectEleveListe = function (eleve) {
            $scope.selectObject($scope.selected.eleves.list, eleve);
            $scope.selected.eleves.all = $scope.currentDevoir.eleves.every(function (eleve) { return eleve.selected});
        };

        /**
         * Sélectionne tous les élèves de la liste passée en paramètre
         */
        $scope.selectAllEleveListe = function () {
            if ($scope.selected.eleves.all !== true) {
                for (var i = 0; i < $scope.currentDevoir.eleves.all.length; i++) {
                    $scope.selected.eleves.list.push($scope.currentDevoir.eleves.all[i]);
                }
                $scope.selected.eleves.all = !$scope.selected.eleves.all;
                $scope.selectElement($scope.currentDevoir.eleves.all, $scope.selected.eleves.all);
                return;
            }
            $scope.selected.eleves.list = [];
            $scope.selected.eleves.all = !$scope.selected.eleves.all;
            $scope.selectElement($scope.currentDevoir.eleves.all, $scope.selected.eleves.all);
        };

        /**
         * Ajout ou supprimer l'objet dans la liste
         * @param list liste d'objet
         * @param object objet
         */
        $scope.selectObject = function (list, object) {
            if (list.indexOf(object) === -1) {
                list.push(object);
            }
            else {
                list.splice(list.indexOf(object), 1);
            }
        };

        /**
         * Afficher le suivi d'un élève depuis le suivi de classe
         * @param eleve
         */
        $scope.displaySuiviEleve= function(eleve){
            $scope.informations.eleve = eleve;
            $scope.search.eleve = eleve;
            $scope.selected.eleve = eleve;
            $scope.displayFromClass = true;
            $scope.displayFromEleve = true;
            utils.safeApply($scope);
            $scope.goTo("/competences/eleve");
        };

        $scope.pOFilterEval = {
            limitTo : 22
        };


        /**
         *  Initialiser le filtre de recherche pour faire disparaitre la liste
         *  des élèves
         *
         */
        $scope.cleanRoot = function () {
            var elem = angular.element(".autocomplete");

            for(let i=0; i<elem.length; i++){
                elem[i].style.height="0px";
            }
        };


        /**
         * Affiche la liste des remplacements en cours et initialise le
         * formulaire de creation d'un remplacement
         */
        $scope.listRemplacements = function () {
            $scope.gestionRemplacement = new GestionRemplacement();
            // TODO gérer les établissements ?
            $scope.gestionRemplacement.remplacements.sync();
            $scope.gestionRemplacement.enseignants.sync();

            $scope.gestionRemplacement.sortType     = 'date_debut'; // set the default sort type
            $scope.gestionRemplacement.sortReverse  = false;  // set the default sort order

            template.open('main', '../templates/evaluations/personnels/remplacements/eval_remp_chef_etab');
        };

        /**
         * Sélectionne/Déselectionne tous les remplacemnts
         */
        $scope.selectAllRemplacements = function(){
            if ($scope.gestionRemplacement.selectAll === false) {
                // maj de la vue
                $scope.selectElement($scope.gestionRemplacement.remplacements.all, false);

                // vidage de la sélection
                $scope.gestionRemplacement.selectedRemplacements = [];
            } else {

                // maj de la vue
                $scope.selectElement($scope.gestionRemplacement.remplacements.all, true);

                // ajout à la liste de sélection
                $scope.gestionRemplacement.selectedRemplacements = _.where($scope.gestionRemplacement.remplacements.all, {selected : true});
            }


        };


        /**
         * Supprime les remplacments sélectionnés
         */
        $scope.deleteSelectedRemplacement = function() {

            var iNbSupp = $scope.gestionRemplacement.selectedRemplacements.length;
            var iCpteur = 0;

            for(var i=0; i< iNbSupp ; ++i) {
                var oRemplacement = $scope.gestionRemplacement.selectedRemplacements[i];

                // suppression des remplacments en BDD
                oRemplacement.remove().then(function(poRemplacementSupp) {

                    $scope.gestionRemplacement.remplacements.remove(poRemplacementSupp);
                    $scope.gestionRemplacement.selectedRemplacements

                    iCpteur++;

                    // si toutes les suppressions ont été faites on refresh la vue
                    if(iNbSupp === iCpteur) {

                        // fermeture popup
                        $scope.gestionRemplacement.confirmation = false;

                        // désélection de tous les remplacements
                        $scope.gestionRemplacement.selectAll = false;

                        // vidage de la liste des remplacements sélectionnés
                        $scope.gestionRemplacement.selectedRemplacements = [];

                        utils.safeApply($scope);
                    }
                });

            }
        };


        /**
         * Sélectionne/Déselectionne un remplacment
         * @param poRemplacement le remplacement
         */
        $scope.selectRemplacement = function (poRemplacement) {
            var index = _.indexOf($scope.gestionRemplacement.selectedRemplacements, poRemplacement);

            // ajout dans la liste des remplacements sélectionnés s'il n'y est pas présent
            if(index === -1){
                $scope.gestionRemplacement.selectedRemplacements.push(poRemplacement);
                poRemplacement.selected = true;
            }else{
                // retrait sinon
                $scope.gestionRemplacement.selectedRemplacements = _.without($scope.gestionRemplacement.selectedRemplacements, poRemplacement);
                poRemplacement.selected = false;
            }

            // coche de la checkbox de sélection de tous les remplacements s'ils on tous été sélectionnés (un à un)
            $scope.gestionRemplacement.selectAll = $scope.gestionRemplacement.selectedRemplacements.length > 0 &&
                ($scope.gestionRemplacement.selectedRemplacements.length === $scope.gestionRemplacement.remplacements.all.length);

        };

        /**
         * Vérification de la cohérence de l'ajout du remplacement (verif remplacement déjà existant par exemple)
         *
         * @return true si aucune erreur, false sinon
         */
        $scope.controlerNewRemplacement = function () {
            // var oRemplacements = [];

            // _.each($scope.gestionRemplacement.remplacements.all, function (remp) {
            //     if (oRemplacement.titulaire.id == $scope.gestionRemplacement.remplacement.titulaire.id) {
            //         oRemplacements.push(remp);
            //     }
            // });

            $scope.gestionRemplacement.showError = false;

            for (var i=0; i< $scope.gestionRemplacement.remplacements.all.length; i++) {
                var oRemplacement = $scope.gestionRemplacement.remplacements.all[i];
                if (oRemplacement.titulaire.id == $scope.gestionRemplacement.remplacement.titulaire.id) {

                    // la date de fin du nouveau  remplacement doit etre avant la date de debut d'un remplacement existant
                    var isRemplacementApresExistant = moment($scope.gestionRemplacement.remplacement.date_fin).diff(moment(oRemplacement.date_debut), "days") < 0;

                    // la date de fin d'un remplacement existant doit être avant la date de début d'un nouveau remplacement
                    var isFinApresFinRemplacementExistant = moment(oRemplacement.date_fin).diff(moment($scope.gestionRemplacement.remplacement.date_debut), "days") < 0;

                    // si l'une des 2 conditions n'est pas remplie le remplacement chevauche un remplacent existant
                    if (!(isRemplacementApresExistant || isFinApresFinRemplacementExistant)) {
                        $scope.gestionRemplacement.showError = true;
                        return false;
                    }
                }
            }

            return true;
        };


        /**
         * Enregistre un remplacemnt en base de données
         */
        $scope.saveNewRemplacement = function () {

            // Vérification de la cohérence de l'ajout du remplacement (verif remplacement déjà existant par exemple)
            var hasError = !$scope.controlerNewRemplacement();

            if(hasError) {
                return;
            }

            // TODO Recupere le bon établissement
            $scope.gestionRemplacement.remplacement.id_etablissement = model.me.structures[0];

            // Conversion des dates en string
            /*$scope.gestionRemplacement.remplacement.date_debut = $scope.getDateFormated($scope.gestionRemplacement.remplacement.date_debut);
             $scope.gestionRemplacement.remplacement.date_fin = $scope.getDateFormated($scope.gestionRemplacement.remplacement.date_fin);*/

            // enregistrement du remplacement et refressh de la liste
            $scope.gestionRemplacement.remplacement.create().then(function() {

                // Mise à jour de la liste des remplacements
                $scope.gestionRemplacement.remplacements.sync().then(function() {
                    // Réinitialisation du formulaire d'ajout de remplacement
                    $scope.gestionRemplacement.remplacement.date_debut = new Date();

                    var today = new Date();
                    today.setFullYear(today.getFullYear() + 1);
                    $scope.gestionRemplacement.remplacement.date_fin = today;
                    $scope.gestionRemplacement.remplacement.titulaire = undefined;
                    $scope.gestionRemplacement.remplacement.remplacant = undefined;

                    $scope.gestionRemplacement.selectAll = false;
                    $scope.gestionRemplacement.selectedRemplacements = [];

                    utils.safeApply($scope);
                });


            });
        };


        /**
         * Controle la validité du formulaire de création d'un remplacement
         * @returns {boolean} Validité du formulaire
         */
        $scope.controleNewRemplacementForm = function () {
            return !(
                $scope.gestionRemplacement.remplacement !== undefined
                && $scope.gestionRemplacement.remplacement.titulaire !== undefined
                && $scope.gestionRemplacement.remplacement.remplacant !== undefined
                && $scope.gestionRemplacement.remplacement.titulaire.id !== $scope.gestionRemplacement.remplacement.remplacant.id
                && $scope.gestionRemplacement.remplacement.date_debut !== undefined
                && $scope.gestionRemplacement.remplacement.date_fin !== undefined
                && (moment($scope.gestionRemplacement.remplacement.date_fin).diff(moment($scope.gestionRemplacement.remplacement.date_debut), "days") >= 0)
            );
        };
    }
]);
