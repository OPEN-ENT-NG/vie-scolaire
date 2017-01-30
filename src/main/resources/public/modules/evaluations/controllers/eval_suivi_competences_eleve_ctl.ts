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

        $scope.opened.lightboxEvalLibre = false;

        /**
         * Initialise d'une évaluation libre.
         */
        $scope.initEvaluationLibre = function () {
            var today = new Date();
            var evaluationLibre = new Devoir({
                date_publication: today,
                date: today,
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
            $scope.messages.successEvalLibre = false;
            $scope.evaluationLibre = $scope.initEvaluationLibre();
            $scope.controleDate();
            $scope.opened.lightboxEvalLibre = true;
            template.open('lightboxContainerEvalLibre', '../templates/evaluations/enseignants/creation_devoir/display_creation_eval_libre');
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
                // fermeture popup
                $scope.opened.lightboxEvalLibre = false;

                // message de succes
                $scope.messages.successEvalLibre = true;

                // refresh du suivi élève
                $scope.suiviCompetence = new SuiviCompetence($scope.search.eleve, $scope.search.periode, $scope.search.classe);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync().then(() => {
                        $scope.suiviCompetence.setMoyenneCompetences($scope.suiviFilter.mine);
                        $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                        utils.safeApply($scope);
                    });
                });



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
                && $scope.evaluationLibre.id_periode !== null && $scope.evaluationLibre.id_periode !== undefined
                && $scope.evaluationLibre.competenceEvaluee.evaluation !== -1
            );
        };

        $scope.suiviFilter = {
            mine: 'false'
        };

        $scope.opened.detailCompetenceSuivi = false;
        this.refreshSlider = function () {
            $timeout(function () {
                $scope.$broadcast('rzSliderForceRender');
            });
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
         *
         * Méthode qui n'affiche que le 1er domaine
         *
         */
        $scope.initAffichageDomaines = function () {
            for (var i = 0; i < $scope.suiviCompetence.domaines.all.length; i++) {
                var domaine = $scope.suiviCompetence.domaines.all[i];
                var bPremierDomaine = (i == 0);
                domaine.visible = bPremierDomaine;
                domaine.setVisibleSousDomaines(bPremierDomaine);
            }
        };

        /**
         * Créer une suivi de compétence
         */
        $scope.selectSuivi = function () {
            if ($scope.search.classe.eleves.findWhere({id : $scope.search.eleve.id}) === undefined) {
                $scope.search.eleve = "";
                delete $scope.suiviCompetence;
                return;
            }
            $scope.informations.eleve = $scope.search.eleve;
            if ($scope.informations.eleve !== null && $scope.search.eleve !== "") {
                $scope.suiviCompetence = new SuiviCompetence($scope.search.eleve, $scope.search.periode, $scope.search.classe);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.all = [];
                    $scope.suiviCompetence.domaines.sync().then(() => {
                        $scope.suiviCompetence.setMoyenneCompetences($scope.suiviFilter.mine);

                        if ($scope.opened.detailCompetenceSuivi) {
                            $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                            if ($scope.detailCompetence) {
                                $scope.openDetailCompetence($scope.detailCompetence);
                            } else {
                                $scope.backToSuivi();
                            }
                        }
                    });

                    $scope.informations.eleve.suiviCompetences.push($scope.suiviCompetence);

                    $scope.template.close('suivi-competence-content');
                    utils.safeApply($scope);
                    $scope.template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
                    if($scope.displayFromClass) delete $scope.displayFromClass;
                    utils.safeApply($scope);


                });
            }

        };

        $scope.updateSuiviEleve = (Eleve) => {
            let path = '/competences/eleve';
            let idOfpath = {idEleve : Eleve.id, idClasse: Eleve.classEleve.id};
            $scope.goTo(path,idOfpath);
            $scope.initSuivi();

        };
        $scope.initSuivi = () => {
            if( $scope.displayFromClass !== true) {
                $scope.search.eleve = "";
                delete $scope.informations.eleve;
                delete $scope.suiviCompetence;

            } else {
                $scope.selectSuivi($scope.route.current.$$route.originalPath);
                $scope.displayFromEleve = true;
                utils.safeApply($scope);
            }
        };

        $scope.initSuivi();
        $scope.$watch($scope.displayFromClass, function (newValue, oldValue) {
            if(newValue !== oldValue) {
                $scope.initSuivi();
            }
        });

        $scope.pOFilterEval = {
            limitTo : 2
        };

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
            utils.scrollTo('top');
        };

        /**
         * Lance la séquence de retour à la vue globale du suivi de compétence
         */
        $scope.backToSuivi = function () {
            template.close("suivi-competence-detail");
            $scope.opened.detailCompetenceSuivi = false;
            $scope.detailCompetence = null;
            $scope.messages.successEvalLibre = false;
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
        $scope.EvaluationExiste = function (list){
          let  ListOfOwner = _.map(list,function(item){
              if (item.owner === $scope.me.userId)
                  return item;
          });
          if(ListOfOwner.length === 0 ){
                return true;
          }else {return false;}
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
        $scope.textPeriode = "Hors periode scolaire";
        $scope.periodeParDefault = function () {
            let PeriodeParD  = new Date().toISOString();
            let PeriodeSet = false ;
           //let  PeriodeParD = new Date().getFullYear() +"-"+ new Date().getMonth() +1 +"-" +new Date().getDate();

           for(let i=0; i<$scope.periodes.all.length ; i++){
                if(PeriodeParD >= $scope.periodes.all[i].timestamp_dt && PeriodeParD <= $scope.periodes.all[i].timestamp_fn  ){
                    PeriodeSet = true;
                    return $scope.periodes.all[i];
                }
           }
           if( PeriodeSet === false){
               return $scope.textPeriode;
           }
        };
        $scope.search.periode = $scope.periodeParDefault();


    }
]);
