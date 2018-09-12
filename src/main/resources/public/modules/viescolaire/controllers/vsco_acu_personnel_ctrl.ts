/**
 * Created by ledunoiss on 13/09/2016.
 */
import {Behaviours, model, ng, template} from 'entcore';
import { vieScolaire} from '../models/vsco_personnel_mdl';
import * as utils from '../../utils/personnel';
import {Utils} from '../utils/Utils';

let moment = require('moment');
declare let _: any;

export let adminVieScolaireController = ng.controller('VscoAdminController', [
    '$scope', 'route', 'model', '$sce',
    async function ($scope, route, model, $sce) {
    console.log('adminVieScolaireController');
        model.me.workflow.load(['edt', 'competences', 'presences']);
        $scope.template = template;
        $scope.structures = vieScolaire.structures;
        $scope.chargeStructure = (structure) =>  {
            structure.classes.sync();
        };

        $scope.canAccessCompetences = function () {
            return Utils.canAccessCompetences();
        };

        $scope.canAccessPresences = function () {
            return Utils.canAccessPresences();
        };

        $scope.changeSelection = function (elem){
            if (elem) {
                elem = ! elem;
            }
            else {
                elem = true;
            }
            utils.safeApply($scope);
        };
        $scope.selectCycle = function (cycle) {
            $scope.lastSelectedCycle.selected = false;
            cycle.selected = true;
            $scope.lastSelectedCycle = cycle;
            utils.safeApply($scope);
        };
        // Sauvegarder niveau de maitrise
        $scope.saveNiveau = function(level){
            level.save((res) => {
                console.dir(res);
            });
        };

        $scope.openDeletePerso = function () {
            $scope.opened.lightboxDeletePerso = true;
        };

        $scope.deletePerso = function () {
            $scope.structure.deletePerso().then( () => {
                $scope.structure.cycles = vieScolaire.structure.cycles;
                if ($scope.structure.cycles.length > 0) {
                    $scope.lastSelectedCycle = $scope.structure.cycles[0];
                    $scope.lastSelectedCycle.selected = true;
                }
                $scope.opened.lightboxDeletePerso = false;
                utils.safeApply($scope);
            });
        };

        $scope.changeEtablissementAccueil = function (structure) {
            $scope.structure = structure;
            vieScolaire.structure = structure;
            vieScolaire.structure.sync().then(() => {
                if ($scope.currParam === undefined) {
                    $scope.currParam = 0;
                }
                if (vieScolaire.structure.cycles.length > 0) {
                    let id_cycle = vieScolaire.structure.cycles[0].id_cycle;
                    if ($scope.lastSelectedCycle !== undefined) {
                        $scope.lastSelectedCycle.selected = false;
                        id_cycle = $scope.lastSelectedCycle.id_cycle;
                    }
                    _.forEach($scope.structure.cycles, (cycle) => {
                        if(cycle.id_cycle === id_cycle) {
                            cycle.selected = true;
                            $scope.lastSelectedCycle = cycle;
                        }
                        else {
                            cycle.selected = false;
                        }
                    });
                }
                utils.safeApply($scope);
            });
        };

        $scope.formatDate = function(pODateDebut, pODateFin) {
            return (moment(pODateDebut).format('DD/MM/YYYY') + " " +
                moment(pODateDebut).format('HH:mm') + "-" + moment(pODateFin).format('HH:mm'));
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
        $scope.customFilterEns = function (psKeyword,enseignementsFilter,competencesFilter,search) {
            $scope.enseignementsFilter = enseignementsFilter;
            $scope.competencesFilter = competencesFilter;

            return function (enseignement) {

                if (!search.haschange) {
                    return true;
                }


                // on check ici si l'enseignement  match le mot clef recherché pour éviter de rechecker
                // systématiquement dans la méthode récursive
                enseignement.open = utils.containsIgnoreCase(enseignement.nom, psKeyword);
                if (enseignement.open) {
                    let nomHtml = $scope.highlight(enseignement.nom, psKeyword);
                    // mise à jour que si la réelle valeur de la chaine html est
                    // différente ($sce.trustAsHtml renvoie systématiquement une valeur différente)
                    if ($sce.getTrustedHtml($scope.enseignementsFilter[enseignement.id].nomHtml)
                        !== $sce.getTrustedHtml(nomHtml)) {
                        $scope.enseignementsFilter[enseignement.id].nomHtml = nomHtml;
                    }

                } else {
                    $scope.enseignementsFilter[enseignement.id].nomHtml = enseignement.nom;
                }

                // Appel de la méthode récursive pour chercher dans les enseignements et compétences
                // / sous compétences /
                // sous sous compétences / ...
                $scope.enseignementsSearchFunctionRec(enseignement, psKeyword);

                // dans tous les cas, à la fin, on retourne l'enseignement "racine"
                return true;
            };
        };

        /**
         * Methode récursive qui determine si un enseignement / une compétence / une sous compétence
         *                                                  / une sous sous compétence ...
         * match le mot clef recherché et doit être dépliée dans les résultats de recherche
         *
         * @param item un enseignement / une compétence / une sous compétence / une sous sous compétence / ...
         * @param psKeyword le mot clef recherché
         */
        $scope.enseignementsSearchFunctionRec = function (item, psKeyword) {

            // Condition d'arret de l'appel récursif : pas de sous compétences (on est sur une feuille de l'arbre)
            if (item.competences !== undefined) {

                // Parcours de chaque compétences / sous compétences
                for (let i = 0; i < item.competences.all.length; i++) {
                    let sousCompetence = item.competences.all[i];
                    let matchDomaine = false;

                    // check si la compétence / sous compétence match le mot clef
                    // on la déplie / replie en conséquence
                    sousCompetence.open = utils.containsIgnoreCase(sousCompetence.nom, psKeyword);

                    if (sousCompetence.code_domaine !== null) {
                        if (matchDomaine = utils.containsIgnoreCase(sousCompetence.code_domaine, psKeyword)) {
                            sousCompetence.open = true;
                        }
                    }

                    if (sousCompetence.open) {

                        let nomHtml = $scope.highlight(sousCompetence.nom, psKeyword);
                        let DisplayNomSousCompetence = nomHtml;

                        if (sousCompetence.code_domaine != null) {
                            let nomDomaine;
                            if (matchDomaine) {
                                nomDomaine = $scope.highlight(sousCompetence.code_domaine, psKeyword);
                            } else {
                                nomDomaine = sousCompetence.code_domaine;
                            }
                            DisplayNomSousCompetence = nomHtml;
                        }
                        // mise à jour que si la réelle valeur de la chaine html est différente
                        // ($sce.trustAsHtml renvoie systématiquement une valeur différente)
                        if ($sce.getTrustedHtml($scope.competencesFilter[sousCompetence.id + "_"
                            + sousCompetence.id_enseignement].nomHtml) !== $sce.getTrustedHtml(nomHtml)) {
                            if ($scope.competencesFilter[sousCompetence.id + "_" + sousCompetence.id_enseignement]
                                !== undefined) {
                                $scope.competencesFilter[sousCompetence.id + "_"
                                + sousCompetence.id_enseignement].nomHtml = DisplayNomSousCompetence;
                            }
                        }

                    } else {
                        if ($scope.competencesFilter[sousCompetence.id + "_" + sousCompetence.id_enseignement]
                            !== undefined) {
                            $scope.competencesFilter[sousCompetence.id + "_" + sousCompetence.id_enseignement].nomHtml
                                = sousCompetence.nom;
                        }
                    }

                    // si elle match le mot clef on déplie également les parents
                    if (sousCompetence.open) {
                        item.open = true;
                        let parent = item.composer;

                        while (parent !== undefined) {
                            parent.open = true;
                            parent = parent.composer;
                        }
                    }


                    // et on check sur les compétences de l'item en cours de parcours
                    $scope.enseignementsSearchFunctionRec(sousCompetence, psKeyword);
                }
            }
        };


        /**
         * Retourne une chaine avec toutes les occurences du mot clef trouvées
         * surlignées (encadrement via des balises html)
         *
         * @param psText le texte où rechercher
         * @param psKeyword le mot clef à rechercher
         * @returns le texte avec les occurences trouvées surlignées
         */
        $scope.highlight = function (psText, psKeyword) {
            let psTextLocal = psText;

            if (!psKeyword) {
                return $sce.trustAsHtml(psText);
            }
            return $sce.trustAsHtml(psTextLocal.replace(new RegExp(psKeyword, 'gi'),
                '<span class="highlightedText">$&</span>'));
        };

        $scope.customFilterComp = function (competence,search) {

            return function (competence) {

                if (!search.haschange) {
                    return true;
                }


                // on check ici si l'enseignement  match le mot clef recherché pour éviter de rechecker
                // systématiquement dans la méthode récursive
                competence.open = utils.containsIgnoreCase(competence.nom, search.keyword);
                if (competence.open) {
                    let nomhtml = $scope.highlight(competence.nom, search.keyword);
                    // mise à jour que si la réelle valeur de la chaine html est
                    // différente ($sce.trustAsHtml renvoie systématiquement une valeur différente)
                    if ($sce.getTrustedHtml(competence.nomhtml)
                        !== $sce.getTrustedHtml(nomhtml)) {
                        competence.nomhtml = nomhtml;
                        utils.safeApply($scope);
                    }
                    competence.hide = false;
                }
                else {
                    competence.hide = true;
                }

                return true;
            };
        };
    }
]);