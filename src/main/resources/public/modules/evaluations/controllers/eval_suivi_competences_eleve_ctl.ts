/**
 * Created by ledunoiss on 27/10/2016.
 */

import {ng, template, model} from 'entcore/entcore';
import {SuiviCompetence, Devoir, CompetenceNote} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

declare let _:any;

export let evalSuiviCompetenceEleveCtl = ng.controller('EvalSuiviCompetenceEleveCtl', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$route', '$timeout',
    function ($scope, route, $rootScope, $location, $filter, $route, $timeout) {


        template.open('container', '../templates/layouts/2_10_layout');
        template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
        template.open('content', '../templates/evaluations/enseignants/suivi_competences_eleve/content');
        template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
        $scope.route = $route;

        $scope.showEvalLibre = false;

        /**
         * Initialise d'une évaluation libre.
         */
        $scope.initEvaluationLibre = function () {
            var evaluationLibre = new Devoir({
                date_publication: new Date(),
                date: new Date(),
                diviseur: 20,
                coefficient: 1,
                id_etablissement: model.me.structures[0],
                ramener_sur: false,
                id_etat: 1,
                owner: model.me.userId,
                is_evaluated: false,
                id_classe: null,
                id_periode: null,
                id_type: 1, // TODO modifier en optional foreign key
                id_matiere: "", // TODO modifier en optional foreign key
                id_sousmatiere: 1, // TODO modifier en optional foreign key
                competences : [],
                controlledDate: false
            });

            var competenceEvaluee = new CompetenceNote({evaluation : -1, id_competence: $scope.detailCompetence.id, id_eleve : $scope.informations.eleve.id, owner : model.me.userId});
            evaluationLibre.competences.all.push($scope.detailCompetence.id);
            evaluationLibre.competenceEvaluee = competenceEvaluee;
            return evaluationLibre;
        };

        /**
         * Ouvre la fenêtre de création d'une évaluation libre
         */
        $scope.createEvaluationLibre = function () {
            $scope.evaluationLibre = $scope.initEvaluationLibre();
            $scope.showEvalLibre = true;
            //template.open('lightboxContainerEvalLibre', '../templates/evaluations/enseignants/creation_devoir/display_creation_eval_libre');
        };

        /**
         * Evaluation de la compétence sur laquelle on est lors d'une évaluation libre
         */
        $scope.switchColor = function(){
            // recupération de la compétence (il n'y en a qu'une)
            var competenceEvaluee = $scope.evaluationLibre.competenceEvaluee;
            if(competenceEvaluee.evaluation === -1){
                competenceEvaluee.evaluation = 3;
            }else{
                competenceEvaluee.evaluation = competenceEvaluee.evaluation -1;
            }
        };

        /**
         *  Sauvegarde d'une évaluation libre
         */
        $scope.saveNewEvaluationLibre = function () {
            $scope.evaluationLibre.create().then(function (res)  {
                $scope.showEvalLibre = false;
            });
        };

        /**
         * Controle que la date de publication du devoir n'est pas inférieur à la date du devoir.
         */
        $scope.controleDate = function () {
            $scope.evaluationLibre.controlledDate = (moment($scope.evaluationLibre.date_publication).diff(moment($scope.evaluationLibre.date), "days") >= 0);
        };

        /**
         * Controle la validité du formulaire de création d'une évaluation libre.
         * @returns {boolean} Validité du formulaire
         */
        $scope.controleNewEvaluationLibreForm = function () {
            return $scope.evaluationLibre == undefined ||
                !(
                $scope.evaluationLibre.controlledDate
                && $scope.evaluationLibre.name !== undefined
                && $scope.evaluationLibre.id_periode !== undefined
                && $scope.evaluationLibre.competenceEvaluee.evaluation !== -1
            );
        };

        $scope.suiviFilter = {
            mine: 'true'
        };
        $scope.opened.detailCompetenceSuivi = false;
        this.refreshSlider = function () {
            $timeout(function () {
                $scope.$broadcast('rzSliderForceRender');
            });
        };

        /**
         * Créer une suivi de compétence
         */
        $scope.selectSuivi = function () {
            $scope.informations.eleve = $scope.search.eleve;
            if ($scope.informations.eleve !== null && $scope.search.eleve !== "") {
                $scope.suiviCompetence = new SuiviCompetence($scope.search.eleve, $scope.search.periode, $scope.search.classe);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync($scope.idCycle).then(() => {
                        $scope.suiviCompetence.setMoyenneCompetences($scope.suiviFilter.mine);
                    });

                    $scope.informations.eleve.suiviCompetences.push($scope.suiviCompetence);
                    if ($scope.opened.detailCompetenceSuivi) {
                        $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                        if (!$scope.detailCompetence) $scope.backToSuivi();
                    }

                    $scope.template.close('suivi-competence-content');
                    utils.safeApply($scope);
                    $scope.template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
                    utils.safeApply($scope);
                    if($scope.displayFromClass) delete $scope.displayFromClass;

                });
            }
        };


        if( $scope.displayFromClass !== true) {
            $scope.search.eleve = "";
            delete $scope.informations.eleve;
            $scope.suiviCompetence = {};

        }
        else {
            $scope.selectSuivi($scope.route.current.$$route.originalPath);
            $scope.displayFromEleve = true;
            utils.safeApply($scope);
        }

        $scope.pOFilterEval = {
            limitTo : 2
        };

        $scope.idCycle = 1;

        /**
         * Filtre permettant de retourner l'évaluation maximum en fonction du paramètre de recherche "Mes Evaluations"
         * @param listeEvaluations Tableau d'évaluations de compétences
         * @returns {(evaluation:any)=>(boolean|boolean)} Retourne true si la compétence courante est la plus haute du tableau listeEvaluations
         */
        $scope.isMaxEvaluation = function (listeEvaluations) {
            return function (evaluation) {
                var _t = listeEvaluations;
                if ($scope.suiviFilter.mine === 'true' || $scope.suiviFilter.mine === true) {
                    _t = _.filter(listeEvaluations, function (competence) {
                        return competence.owner !== undefined && competence.owner === $scope.me.userId;
                    });
                }
                var max = _.max(_t, function (competence) {
                    return competence.evaluation;
                });
                if (typeof max === 'object') {
                    return evaluation.id_competences_notes === max.id_competences_notes;
                } else {
                    return false;
                }
            };
        };

        /**
         * Retourne si l'utilisateur n'est pas le propriétaire de compétences
         * @param listeEvaluations Tableau d'évaluations de compétences
         * @returns {boolean} Retourne true si l'utilisateur n'est pas le propriétaire
         */
        $scope.notEvalutationOwner = function (listeEvaluations) {
            if ($scope.suiviFilter.mine === 'false' || $scope.suiviFilter.mine === false) {
                return false;
            }
            var _t = _.filter(listeEvaluations, function (competence) {
                return competence.owner === undefined || competence.owner === $scope.me.userId;
            });
            return _t.length === 0;
        };


        /*
         Listener sur le template suivi-competence-detail permettant la transition entre la vue détail
         et la vue globale
         */
        template.watch("suivi-competence-detail", function () {
            if (!$scope.opened.detailCompetenceSuivi) {
                $scope.opened.detailCompetenceSuivi = true;
            }
        });

        /**
         * Lance la séquence d'ouverture du détail d'une compétence permettant d'accéder à la vue liste ou graph
         * @param competence Compétence à ouvrir
         */
        $scope.openDetailCompetence = function (competence) {
            $scope.detailCompetence = competence;
            template.open("suivi-competence-detail", "../templates/evaluations/enseignants/suivi_competences_eleve/detail_vue_tableau");
        };

        /**
         * Lance la séquence de retour à la vue globale du suivi de compétence
         */
        $scope.backToSuivi = function () {
            template.close("suivi-competence-detail");
            $scope.opened.detailCompetenceSuivi = false;
            $scope.detailCompetence = null;
        };

        /**
         * Retourne si l'utilisateur est le propriétaire de l'évaluation
         * @param evaluation Evaluation à afficher
         * @returns {boolean} Retourne true si l'utilisateur est le propriétaire de l'évaluation
         */
        $scope.filterOwnerSuivi = function (evaluation) {
            if ($scope.suiviFilter.mine === 'false' || $scope.suiviFilter.mine === false) {
                return true;
            }
            return evaluation.owner === $scope.me.userId;
        };

        /**
         * Recherche l'index de l'objet dans le tableau
         * @param array tableau d'objets
         * @param obj objet
         * @returns {number} index de l'objet
         */
        var searchIndex = function (array, obj) {
            return _.indexOf(array, obj);
        };

        /**
         * Remplace l'élève recherché par le nouveau suite à l'incrémentation de l'index
         * @param num pas d'incrémentation. Peut être positif ou négatif
         */
        $scope.incrementEleve = function (num) {
            var index = searchIndex($scope.search.classe.eleves.all, $scope.search.eleve);
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < $scope.search.classe.eleves.all.length) {
                $scope.search.eleve = $scope.search.classe.eleves.all[index + parseInt(num)];
                $scope.selectSuivi();
                utils.safeApply($scope);
            }
        };
    }
]);
