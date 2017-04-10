/**
 * Created by ledunoiss on 27/10/2016.
 */

import {ng, template, model} from 'entcore/entcore';
import {SuiviCompetence, Devoir, CompetenceNote, evaluations} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';



declare let _:any;

export let evalSuiviCompetenceEleveCtl = ng.controller('EvalSuiviCompetenceEleveCtl', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$route', '$timeout',
    function ($scope, route, $rootScope, $location, $filter, $route, $timeout) {
        //rajout de la periode Annee
        if($scope.periodes === undefined){
        $scope.periodes.sync();
        $scope.periodes.on('sync', function () {
            $scope.periodesList = {
                "type": "select",
                "name": "Service",
                "value": $scope.periodeParDefault(),
                "values": []
            };
            _.map($scope.periodes.all, function (periode) {
                $scope.periodesList.values.push(periode);
            });
            $scope.periodesList.values.push({libelle: $scope.translate('viescolaire.utils.annee'), id: undefined});
            template.open('container', '../templates/layouts/2_10_layout');
            template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
            template.open('content', '../templates/evaluations/enseignants/suivi_competences_eleve/content');
            template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
        });

        }
        else{
                $scope.periodesList = {
                    "type": "select",
                    "name": "Service",
                    "value": $scope.periodeParDefault(),
                    "values": []
                };
                _.map($scope.periodes.all, function (periode) {
                    $scope.periodesList.values.push(periode);
                });
                if($scope.displayFromClass){
                    $scope.periodesList.value = $scope.search.periode;
                }
            $scope.periodesList.values.push({libelle: $scope.translate('viescolaire.utils.annee'), id: undefined});
            template.open('container', '../templates/layouts/2_10_layout');
            template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
            template.open('content', '../templates/evaluations/enseignants/suivi_competences_eleve/content');
            template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');

        }



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
                id_etablissement: $scope.evaluations.structure.id,
                ramener_sur: false,
                id_etat: 1,
                owner: model.me.userId,
                is_evaluated: false,
                id_classe: null,
                id_periode: $scope.search.periode.id,
                id_type: 1, // TODO modifier en optional foreign key
                id_matiere: "",
                id_sousmatiere: null,
                competences: [],
                controlledDate: false
            });
            $scope.EvaluationLibreCharge= {
                matieres : [_.findWhere(evaluations.matieres.all,{idEtablissement: $scope.evaluations.structure.id})] ,
                sousmatiere : [],
                periode : $scope.search.periode
            };


            var competenceEvaluee = new CompetenceNote({
                evaluation: -1,
                id_competence: $scope.detailCompetence.id,
                id_eleve: $scope.informations.eleve.id,
                owner: model.me.userId
            });
            evaluationLibre.competences.all.push($scope.detailCompetence.id);
            evaluationLibre.competenceEvaluee = competenceEvaluee;
            return evaluationLibre;
        };




        $scope.$watch(function () {
            if($scope.evaluationLibre != undefined)
                return  $scope.evaluationLibre.id_matiere;
        }, function (newValue) {
            if(newValue !== "" && newValue !== undefined && newValue !== null){
                let mamatiere =  _.findWhere($scope.EvaluationLibreCharge.matieres,{id: $scope.evaluationLibre.id_matiere});
                if(mamatiere != undefined)
                    $scope.EvaluationLibreCharge.sousmatiere = mamatiere.sousMatieres.all;
            }else if(newValue === null ){
                $scope.EvaluationLibreCharge.sousmatiere = []
            }
        });

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
        $scope.switchColor = function () {
            // recupération de la compétence (il n'y en a qu'une)
            var competenceEvaluee = $scope.evaluationLibre.competenceEvaluee;
            if (competenceEvaluee.evaluation === -1) {
                competenceEvaluee.evaluation = 3;
            } else {
                competenceEvaluee.evaluation = competenceEvaluee.evaluation - 1;
            }
        };

        /**
         *  Sauvegarde d'une évaluation libre
         */
        $scope.saveNewEvaluationLibre = function () {
            $scope.evaluationLibre.date = $scope.getDateFormated($scope.evaluationLibre.dateDevoir);
            $scope.evaluationLibre.date_publication = $scope.getDateFormated($scope.evaluationLibre.datePublication);
            $scope.evaluationLibre.id_periode = $scope.EvaluationLibreCharge.periode.id;

            // fermeture popup
            $scope.opened.lightboxEvalLibre = false;

            // message de succes
            $scope.messages.successEvalLibre = true;
            $scope.evaluationLibre.create().then(function (res) {


                // refresh du suivi élève
                $scope.suiviCompetence = new SuiviCompetence($scope.search.eleve, $scope.search.periode, $scope.search.classe,$scope.evaluations.structure);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync().then(() => {
                        $scope.suiviCompetence.setMoyenneCompetences($scope.suiviFilter.mine);
                        $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                        $scope.initChartsEval();

                        utils.safeApply($scope);
                    });
                });
                $scope.initSliderBFC();
                utils.safeApply($scope);

            });
        };

        /**
         * Controle que la date de publication du devoir n'est pas inférieur à la date du devoir.
         * Et que la date de creation est comprise dans la période
         */
        $scope.controleDate = function () {
            let current_periode = $scope.periodes.findWhere({id: $scope.EvaluationLibreCharge.periode.id});
            let start_datePeriode = current_periode.timestamp_dt;
            let end_datePeriode = current_periode.timestamp_fn;
            let date_saisie = current_periode.date_fin_saisie;

            if (moment(date_saisie).diff(moment($scope.evaluationLibre.dateDevoir), "days") >= 0) {
                $scope.endSaisieFree = false;
                utils.safeApply($scope);
            }
            else {
                $scope.endSaisieFree = true;
                utils.safeApply($scope);
            }

            $scope.evaluationLibre.controlledDate = (moment($scope.evaluationLibre.datePublication).diff(moment($scope.evaluationLibre.dateDevoir), "days") >= 0)
                && (moment($scope.evaluationLibre.dateDevoir).diff(moment(start_datePeriode), "days") >= 0)
                && (moment(end_datePeriode).diff(moment($scope.evaluationLibre.dateDevoir), "days") >= 0)
                && (moment(date_saisie).diff(moment($scope.evaluationLibre.dateDevoir), "days") >= 0);
        };

        /**
         * Controle la validité du formulaire de création d'une évaluation libre.
         * @returns {boolean} Validité du formulaire
         */
        $scope.controleNewEvaluationLibreForm = function () {
            return $scope.evaluationLibre == undefined || !(
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
        $scope.refreshSlider = function () {
            $timeout(function () {
                $scope.$broadcast('rzSliderForceRender');
            });
        };

        /**
         * Supprime un BFC créé par un chef d'établissement
         */
        $scope.deleteBFC = function () {
            this.domaine.bfc.deleteBilanFinDeCycle().then((res) => {
                if (res.rows === 1) {
                    this.domaine.bfc = undefined;
                    this.domaine.slider.value =  this.domaine.moyenne;
                }
                utils.safeApply($scope);
            });
        };
        /**
         *
         * Affiche le domaine suivant (de niveau 0) et ses
         * sous domaines.
         *
         */
        /* Methode plus utilisée
         $scope.afficherDomaineSuivant = function () {
         for (var i = 0; i < $scope.suiviCompetence.domaines.all.length; i++) {
         var domaine = $scope.suiviCompetence.domaines.all[i];
         if (i > 0) {
         var domainePrec = $scope.suiviCompetence.domaines.all[i - 1];
         if (domainePrec.visible && !domaine.visible) {
         domaine.visible = true;
         domaine.setVisibleSousDomaines(true);
         return;
         }
         }
         }
         };*/

        /**
         *
         * Méthode qui n'affiche que le 1er domaine
         *
         */
        /* Methode plus utilisée
         $scope.initAffichageDomaines = function () {
         for (var i = 0; i < $scope.suiviCompetence.domaines.all.length; i++) {
         var domaine = $scope.suiviCompetence.domaines.all[i];
         var bPremierDomaine = (i == 0);
         domaine.visible = bPremierDomaine;
         domaine.setVisibleSousDomaines(bPremierDomaine);
         }
         };*/

        /**
         * Créer une suivi de compétence
         */
        $scope.selectSuivi = function () {
            $scope.selected.grey = true;
            if ($scope.search.classe.eleves.findWhere({id: $scope.search.eleve.id}) === undefined) {
                $scope.search.eleve = "";
                delete $scope.suiviCompetence;
                return;
            }
            $scope.informations.eleve = $scope.search.eleve;
            if ($scope.informations.eleve !== null && $scope.search.eleve !== "") {
                $scope.suiviCompetence = new SuiviCompetence($scope.search.eleve, $scope.search.periode, $scope.search.classe,$scope.evaluations.structure);
                $scope.suiviCompetence.sync().then(() => {
                    // On récupère d'abord les bilans de fin de cycle enregistrés par le chef d'établissement
                    $scope.suiviCompetence.bilanFinDeCycles.all =[];
                    $scope.suiviCompetence.bilanFinDeCycles.sync().then(() => {
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
                    });
                    $scope.initSliderBFC();
                    $scope.informations.eleve.suiviCompetences.push($scope.suiviCompetence);
                    $scope.template.close('suivi-competence-content');
                    utils.safeApply($scope);
                    $scope.template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
                    if ($scope.displayFromClass) delete $scope.displayFromClass;
                    utils.safeApply($scope);
                });
            }

        };
        $scope.initSliderBFC = function () {
            $scope.suiviCompetence.getConversionTable($scope.evaluations.structure.id,$scope.search.classe.id).then(
                function(data){
                    return $scope.suiviCompetence.tableConversions;
                }
            );
        };
        $scope.updateSuiviEleve = (Eleve) => {
            $scope.selected.grey = true;
            $scope.search.classe = _.findWhere(evaluations.classes.all,{ 'id': Eleve.idClasse} );
            $scope.search.classe.eleves.sync().then(() =>{
                $scope.search.eleve =  _.findWhere($scope.search.classe.eleves.all,{'id': Eleve.id});
                $scope.selectSuivi($scope.route.current.$$route.originalPath);
                utils.safeApply($scope);
            });
        };
        $scope.initSuivi = () => {
            if ($scope.displayFromClass !== true) {
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
            if (newValue !== oldValue) {
                $scope.initSuivi();
            }
        });





        $scope.pOFilterEval = {
            limitTo: 2
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
            $scope.initChartsEval();
            template.open("suivi-competence-detail", "../templates/evaluations/enseignants/suivi_competences_eleve/detail_vue_graph");
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
        $scope.EvaluationExiste = function (list) {
            let ListOfOwner = _.map(list, function (item) {
                if (item.owner === $scope.me.userId)
                    return item;
            });
            if (ListOfOwner.length === 0) {
                return true;
            } else {
                return false;
            }
        };
        /**
         * Remplace l'élève recherché par le nouveau suite à l'incrémentation de l'index
         * @param num pas d'incrémentation. Peut être positif ou négatif
         */
        $scope.incrementEleve = function (num) {
            $scope.selected.grey = true;
            var index = searchIndex($scope.search.classe.eleves.all, $scope.search.eleve);
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < $scope.search.classe.eleves.all.length) {
                $scope.search.eleve = $scope.search.classe.eleves.all[index + parseInt(num)];
                $scope.selectSuivi();
                utils.safeApply($scope);
            }
        };
        $scope.textPeriode = "Hors periode scolaire";

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

        $scope.search.periode = $scope.periodeParDefault();

        $scope.chartOptionsEval = {
            series : ['Evaluation'],
            tooltipLabels : [],
            options: {
                tooltips: {
                    callbacks: {
                        label: function(tooltipItems, data) {
                            return $scope.chartOptionsEval.tooltipLabels[tooltipItems.index];
                        }
                    }
                },
                elements:{
                    point :{
                        radius : 10,

                    },
                    line : {
                        fill: false,
                        borderDash : [0, 15]
                    }
                },
                maintainAspectRatio : false,
                scales: {
                    responsive: true,
                    yAxes: [{
                        gridLines : {display : false,
                            color : '#000000'},
                        pointRadius: 10,
                        type: 'linear',
                        display: true,
                        ticks: {
                            max: 6,
                            min: 0,
                            fontColor : 'black',
                            stepSize: 1,
                            padding : 20,
                            callback: function (value, index, values) {
                                if(value === 1) {
                                    return "Compétence non évaluée" ;
                                }
                                else if(value === 2) {
                                    return "Maîtrise insuffisante" ;
                                }
                                else if(value === 3) {
                                    return "Maîtrise fragile" ;
                                }
                                else if(value === 4) {
                                    return "Maîtrise satisfaisante" ;
                                }
                                else if(value === 5){
                                    return "Très bonne maîtrise" ;
                                }
                                else{
                                    return " " ;
                                }
                                // return parseFloat(value).toFixed(2) + '%';
                            }
                        },
                    }],
                    xAxes: [{
                        type: 'category',
                        display:true,
                        responsive: false,
                        gridLines:{
                            display : false,
                            offsetGridLines : false,
                            color : '#000000'
                        },
                        ticks: {
                            labelOffset : 30,
                            minRotation : 20, // rotation des labels
                            autoSkip: true,
                            maxTicksLimit: 20,
                            fontColor : 'black'
                        }
                    }]
                }
            },
            //les données des axes X: et Y:
            datasets: {
                labels: [],
                data: []
            },
            //les couleurs des points
            colors: []
        };

        /**
         *
         */
        $scope.initChartsEval = function () {
            if ($scope.detailCompetence !== undefined) {
                var ListEval = _.map($scope.detailCompetence.competencesEvaluations, function (evalu) {
                    if ($scope.filterOwnerSuivi(evalu)) {
                        return evalu;
                    }
                });
                //initialisation et rajout de la 1er colomn vide
                $scope.chartOptionsEval.tooltipLabels = [];
                $scope.chartOptionsEval.tooltipLabels.push(' ');
                $scope.chartOptionsEval.datasets.data = [];
                $scope.chartOptionsEval.datasets.data.push(-10);
                $scope.chartOptionsEval.datasets.labels = [];
                $scope.chartOptionsEval.datasets.labels.push(" ");
                $scope.chartOptionsEval.colors = [];
                $scope.chartOptionsEval.colors.push('#FFFFFF');
                ListEval =  _.sortBy(ListEval, function(evalu){ return evalu.evaluation_date; });
                for (let i = 0; i < ListEval.length; i++) {
                    $scope.chartOptionsEval.datasets.data.push(ListEval[i].evaluation + 2);
                    $scope.chartOptionsEval.datasets.labels.push($scope.getDateFormated(ListEval[i].evaluation_date));
                    let colorValue;
                    if(ListEval[i].evaluation == 0){colorValue = '#E13A3A';}
                    else if(ListEval[i].evaluation == 1){colorValue = '#FF8500';}
                    else if(ListEval[i].evaluation == 2){colorValue = '#ECBE30';}
                    else if(ListEval[i].evaluation == 3){colorValue = '#46BFAF';}
                    else{colorValue = '#555555';}
                    $scope.chartOptionsEval.colors.push(colorValue);
                    $scope.chartOptionsEval.tooltipLabels.push(ListEval[i].evaluation_libelle+' : '+ListEval[i].owner_name);

                }

                //rajout de la dernière colomn vide
                $scope.chartOptionsEval.datasets.data.push(-10);
                $scope.chartOptionsEval.datasets.labels.push(" ");
                $scope.chartOptionsEval.colors.push('#FFFFFF');
                $scope.chartOptionsEval.tooltipLabels.push(' ');
            }
        };
        $scope.$watch($scope.detailCompetence, function () {
            $scope.initChartsEval();
        });

        $scope.selected.grey = true;

        $scope.FilterNotEvaluated = function (MaCompetence) {
            if($scope.selected.grey === true){
                let _t = MaCompetence.competencesEvaluations;
                if ($scope.suiviFilter.mine === 'true' || $scope.suiviFilter.mine === true) {
                    _t = _.filter(MaCompetence.competencesEvaluations, function (evaluation) {
                        if (evaluation.owner !== undefined && evaluation.owner === $scope.me.userId)
                            return evaluation;
                    });
                }


                let max = _.max(_t, function (evaluation) {
                    return evaluation.evaluation;
                });
                if (typeof max === 'object' ) {
                    return (!(max.evaluation == -1));
                } else {
                    return false;
                }

            }else{
                return true;
            }

        };


    }
]);
