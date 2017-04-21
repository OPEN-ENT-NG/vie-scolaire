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
        //rajout de la periode Annee
        $scope.periodes.sync();
        $scope.periodes.on('sync', function () {
            //selection de la periode du suivi à l'initialisation du filtre par période
            if($route.current.params.idPeriode === undefined) {
                $scope.periodesList = {
                    "type": "select",
                    "name": "Service",
                    "value": $scope.periodeParDefault(),
                    "values": []
                };
            }
            else{
                $scope.search.periode = $scope.periodes.findWhere({id: parseInt($route.current.params.idPeriode)});
                if($scope.search.periode === undefined){
                    $scope.search.periode = $scope.periodeParDefault();
                }
                $scope.periodesList = {
                    "type": "select",
                    "name": "Service",
                    "value": $scope.search.periode,
                    "values": []
                };
            }
            _.map($scope.periodes.all, function (periode) {
                $scope.periodesList.values.push(periode);
            });
            $scope.periodesList.values.push({libelle: $scope.translate('viescolaire.utils.annee'), id: undefined});
            $scope.search.periode = $scope.periodesList.value;
            utils.safeApply($scope);

            //sélection de la classe du suivi
            if($route.current.params.idClasse !== undefined){
                if($scope.classes !== undefined){
                    $scope.search.classe = $scope.classes.findWhere({id: $route.current.params.idClasse});
                }
                else{
                    $scope.classes.sync();
                    $scope.classes.on('classes-sync', function () {
                        $scope.search.classe = $scope.classes.findWhere({id: $route.current.params.idClasse});
                    });
                }
            }
            else{
                $scope.search.classe = "";
            }
            delete $scope.informations.eleve;
            $scope.route = $route;


            $scope.suiviFilter = {
                mine : (!$scope.isChefEtab()).toString()
            };

            $scope.selected.colors = {
                0 : true,
                1 : true,
                2 : true,
                3 : true,
                4 : true
            };
            utils.safeApply($scope);

            template.open('container', '../templates/layouts/2_10_layout');
            template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
            template.open('content', '../templates/evaluations/enseignants/suivi_competences_classe/content');
            $scope.selectSuivi($scope.route.current.$$route.originalPath);
            utils.safeApply($scope);
        });

        $scope.switchEtablissementSuivi = () => {
            delete $scope.suiviCompetence;
            delete $scope.informations.classe;
            $scope.changeEtablissement();
        };

        /**
         * Créer une suivi de compétence
         */
        $scope.selectSuivi = function () {
            $scope.Display = {EvaluatedCompetences : true};
            $scope.informations.classe = $scope.search.classe;
            if ($scope.informations.classe !== null && $scope.search.classe !== '' && $scope.search.classe !== '*') {
                $scope.suiviCompetence = new SuiviCompetenceClasse($scope.search.classe, $scope.search.periode);
                // on met à jour le fil d'ariane
                let updatedUrl = '/competences/classe?idClasse='+$scope.search.classe.id;
                if ($scope.search.periode.hasOwnProperty('id') && $scope.search.periode.id !== undefined)
                    updatedUrl += '&idPeriode='+ $scope.search.periode.id;

                $rootScope.$broadcast('change-params', updatedUrl);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync();
                    if ($scope.opened.detailCompetenceSuivi) {
                        if ($scope.detailCompetence !== undefined) {
                            $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                            if (!$scope.detailCompetence) $scope.backToSuivi();
                        } else {
                            $scope.backToSuivi();
                        }
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


        $scope.getMaxEvaluations = function (idEleve) {
            if($scope.detailCompetence === undefined){
                return ;
            }
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
            if (evaluation === undefined){
                return ;
            }
            else if (evaluation !== -Infinity)
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
         * Return la periode scolaire courante
         * @returns {any}
         */
        $scope.periodeParDefault = function () {
            let PeriodeParD = new Date().toISOString();
            let PeriodeSet = false;
            //let  PeriodeParD = new Date().getFullYear() +"-"+ new Date().getMonth() +1 +"-" +new Date().getDate();

            for (let i = 0; i < $scope.periodes.all.length; i++) {
                if (PeriodeParD >= $scope.periodes.all[i].timestamp_dt && PeriodeParD <= $scope.periodes.all[i].timestamp_fn) {
                    PeriodeSet = true;
                    return $scope.periodes.all[i];
                }
            }
            if (PeriodeSet === false) {
                return $scope.textPeriode;
            }
        };

        /**
         * Remplace l'élève recherché par le nouveau suite à l'incrémentation de l'index
         * @param num pas d'incrémentation. Peut être positif ou négatif
         */
        $scope.incrementClasse = function (num) {
            $scope.Display = {EvaluatedCompetences : true};
            var index = searchIndex($scope.classes.all, $scope.search.classe);
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < $scope.classes.all.length) {
                $scope.search.classe = $scope.classes.all[index + parseInt(num)];
                $scope.selectSuivi('/competences/classe');
                utils.safeApply($scope);
            }
        };


        $scope.Display = {EvaluatedCompetences : true};
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
        };

        $scope.selectSuivi();
}
]);
