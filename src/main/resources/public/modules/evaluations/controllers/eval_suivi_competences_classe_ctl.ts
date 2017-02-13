/**
 * Created by ledunoiss on 27/10/2016.
 */

import {ng, template, model} from 'entcore/entcore';
import {SuiviCompetence, Domaine,SuiviCompetenceClasse} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

declare let _:any;

export let evalSuiviCompetenceClasseCtl = ng.controller('EvalSuiviCompetenceClasseCtl', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$route',
    function ($scope, route, $rootScope, $location, $filter, $route) {
        template.open('container', '../templates/layouts/2_10_layout');
        template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
        template.open('content', '../templates/evaluations/enseignants/suivi_competences_classe/content');

        delete $scope.informations.eleve;
        $scope.route = $route;
        $scope.search.classe = "";
        $scope.suiviFilter = {
            mine : 'false'
        };

        $scope.selected.colors = {
            0 : true,
            1 : true,
            2 : true,
            3 : true,
            4 : true
        };

        /**
         *
         * Affiche le domaine suivant (de niveau 0) et ses
         * sous domaines.
         *
         */
        $scope.afficherDomaineSuivant = function () {
            for (var i = 0; i < $scope.suiviCompetence.domaines.all.length; i++) {
                var domaine = $scope.suiviCompetence.domaines.all[i];
                if( i> 0) {
                    var domainePrec = $scope.suiviCompetence.domaines.all[i - 1];
                    if(domainePrec.visible && !domaine.visible) {
                        domaine.visible = true;
                        domaine.setVisibleSousDomaines(true);
                        return;
                    }
                }
            }
        };

        /**
         * Créer une suivi de compétence
         */
        $scope.selectSuivi = function (state) {

            $scope.informations.classe = $scope.search.classe;
            if ($scope.informations.classe !== null && $scope.search.classe !== "") {
                $scope.suiviCompetence = new SuiviCompetenceClasse($scope.search.classe, $scope.search.periode);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync();
                    if ($scope.opened.detailCompetenceSuivi) {
                        $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                        if (!$scope.detailCompetence) $scope.backToSuivi();
                    }

                    // On stocke l'ensemble des élèves de la classe dan une Map

                    var mapEleves = {};
                    for (var i = 0; i < $scope.search.classe.eleves.all.length; i++) {
                        mapEleves[$scope.search.classe.eleves.all[i].id]= $scope.search.classe.eleves.all[i];
                    }
                    $scope.search.classe.mapEleves = mapEleves;
                    utils.safeApply($scope);
                    if($scope.displayFromEleve) delete $scope.displayFromEleve;

                    template.close('suivi-competence-content');
                    utils.safeApply($scope);
                    template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_classe/content_vue_suivi_classe');
                    utils.safeApply($scope);

                });
            }
        };
        if( $scope.displayFromEleve !== true) {
            $scope.opened.detailCompetenceSuivi = false;
            $scope.suiviCompetence = {};
            $scope.mapEleves = {};
            $scope.suiviCompetence = {};
            $scope.search.classe = "";

        }
        else {
            $scope.selectSuivi($scope.route.current.$$route.originalPath);
        }

        $scope.getMaxEvaluations = function (idEleve) {
            var evaluations = $scope.suiviFilter.mine == 'true'
                ? _.where($scope.detailCompetence.competencesEvaluations, {id_eleve : idEleve, owner : model.me.userId})
                : _.where($scope.detailCompetence.competencesEvaluations, {id_eleve : idEleve});
            if (evaluations.length > 0) {
                return _.max(evaluations, function (evaluation) { return evaluation.evaluation; });
            }
        };

        $scope.pOFilterEval = {
            limitTo : 2
        };

        /**
         * Retourne la classe en fonction de l'évaluation obtenue pour la compétence donnée
         * @param eleveId identifiant de l'élève
         * @returns {String} Nom de la classe
         */
        $scope.getEvaluationResult = function (eleveId) {
            var evaluation = $scope.getMaxEvaluations(eleveId);
            if (evaluation !== -Infinity) {
                switch (evaluation.evaluation) {
                    case 0 : return "border-red";
                    case 1 : return "border-orange";
                    case 2 : return "border-yellow";
                    case 3 : return "border-green";
                    default : return "border-grey";
                }
            }
        };

        $scope.FilterColor = function (item){
            var evaluation = $scope.getMaxEvaluations(item.id);
            if (evaluation !== -Infinity)
                return $scope.selected.colors[evaluation.evaluation + 1];
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
         * Lance la séquence d'ouverture du détail d'une compétence
         * @param competence Compétence à ouvrir
         */
        $scope.openDetailCompetence = function (competence) {
            $scope.detailCompetence = competence;
            template.open("suivi-competence-detail", "../templates/evaluations/enseignants/suivi_competences_classe/detail_vue_classe");
            utils.scrollTo('top');
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
        $scope.incrementClasse = function (num) {
            var index = searchIndex($scope.classes.all, $scope.search.classe);
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < $scope.classes.all.length) {
                $scope.search.classe = $scope.classes.all[index + parseInt(num)];
                $scope.selectSuivi('/competences/classe');
                utils.safeApply($scope);
            }
        };


        $scope.Display = {EvaluatedCompetences : false};
        $scope.ClasseFilterNotEvaluated = function (MaCompetence) {
            if($scope.Display.EvaluatedCompetences === true){
                let _t = MaCompetence.competencesEvaluations;
                if ($scope.suiviFilter.mine === 'true' || $scope.suiviFilter.mine === true) {
                    _t = _.filter(MaCompetence.competencesEvaluations, function (evaluation) {
                        if (evaluation.owner !== undefined && evaluation.owner === $scope.me.userId)
                            return evaluation;
                    });
                }
                let EvaluatedOK = false;
                _.map(_t,function(competenceNote){
                    if(competenceNote.evaluation != -1){
                        EvaluatedOK = true;
                    }
                });
                return EvaluatedOK;
            }else{
                return true;
            }
        }




}
]);
