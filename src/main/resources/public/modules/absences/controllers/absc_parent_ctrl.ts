import { ng, template } from "entcore";
import * as utils from "../utils/parent";
import { presences } from '../models/absc_parent_mdl';
import {Eleve} from "../models/parent/Eleve";
import {Declaration} from "../models/parent/Declaration";

export let abscParentController = ng.controller('AbscParentController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    async ($scope, route, model, $rootScope, $location, $route) => {

        route({
            Accueil: (params) => {
                template.open('header', '../templates/absences/absc_parent_selectEnfants');
                template.open('menu', '../templates/absences/absc_parent_menu');
                template.open('main', '../templates/absences/absc_parent_acu');
                template.open('lightbox_declaration', '../templates/absences/absc_parent_saisieDeclaration');
                utils.safeApply($scope);
            },
            Declarations : (params) => {
                template.open('header', '../templates/absences/absc_parent_selectEnfants');
                template.open('menu', '../templates/absences/absc_parent_menu');
                template.open('main', '../templates/absences/absc_parent_declarations');
                utils.safeApply($scope);
            },
            Absences : (params) => {
                template.open('header', '../templates/absences/absc_parent_selectEnfants');
                template.open('menu', '../templates/absences/absc_parent_menu');
                template.open('main', '../templates/absences/absc_parent_absences');
                utils.safeApply($scope);
            }
        });

        $scope.evenements = [];
        $scope.lightbox_declaration = false;

        $scope.pOSortParameters = {
            sortType: null,
            sortReverse: false
        };

        $scope.piFilterAbsences = 20;

        $scope.poDateHeure = {
            pdDateDebut: "",
            pdDateFin: "",
            phHeureDebut: "",
            phHeureFin: "",
        };

        // Permet le choix d'un élève et la synchronisation des évènements et déclarations de l'élève
        $scope.chooseChild = async (eleve, number?) => {
            if ($scope.selectedEleve == null || $scope.selectedEleve.id != eleve.id) {
                $scope.selectedEleve = eleve;
                $scope.syncEleve();
                utils.safeApply($scope);
            }
        };

        $scope.syncEleve = async () => {
            let number;
            if($route.current.$$route.action == 'Accueil') {
                number = 10;
            }
            await Promise.all([$scope.selectedEleve.syncEvents(), $scope.selectedEleve.declarations.sync(number)]);
            $scope.evenements = $scope.selectedEleve.evenements.all;
            $scope.declarations = $scope.selectedEleve.declarations.all;
        };

        $rootScope.$on('$routeChangeSuccess', (event, next, current) => {
            if($scope.selectedEleve != null) {
                $scope.syncEleve();
            }
        });

        $scope.formatDate = (dateDt, dateFn?, format?) => {
            let _return = "";
            let _dateDt = moment(dateDt);
            let _format = format ? format : "DD/MM/YYYY HH:mm";
            if(dateFn) {
                let _dateFn = moment(dateFn);

                if(_dateDt.diff(_dateFn, 'days') < 1) {
                    _return = _dateDt.format(_format) + " - " + _dateFn.format('HH:mm');
                } else {
                    _return = _dateDt.format(_format) + " " + _dateFn.format(_format);
                }
            } else {
                _return = _dateDt.format(_format);
            }
            return _return;
        };

        $scope.openLightBoxDeclaration = (decl?: Declaration) => {
            if(!decl) {
                $scope.declaration = new Declaration({
                    id_eleve: $scope.selectedEleve.id,
                    id_etablissement: $scope.structure.id
                });
                $scope.resetDateHeure();
                $scope.updateDateHeure($scope.declaration, true);
            } else {
                $scope.declaration = decl;
            }
            $scope.updateDateHeure($scope.declaration);
            $scope.lightbox_declaration = true;
        };

        $scope.closeLightBoxDeclaration = async () => {
            $scope.lightbox_declaration = false;
            $scope.syncEleve();
            utils.safeApply($scope);
        };

        $scope.updateDateHeure = (decl, updateDecl?) => {
            if(updateDecl === true && decl !== null) {
                decl.timestamp_dt = moment(moment($scope.poDateHeure.pdDateDebut).format("DD/MM/YYYY") + " "
                    + $scope.poDateHeure.phHeureDebut,
                    "DD/MM/YYYY HH:mm").format("YYYY-MM-DD HH:mm");
                decl.timestamp_fn = moment(moment($scope.poDateHeure.pdDateFin).format("DD/MM/YYYY") + " "
                    + $scope.poDateHeure.phHeureFin,
                    "DD/MM/YYYY HH:mm").format("YYYY-MM-DD HH:mm");
            } else if (decl.timestamp_fn && decl.timestamp_dt) {
                $scope.poDateHeure.pdDateDebut = new Date(decl.timestamp_dt);
                $scope.poDateHeure.pdDateFin = new Date(decl.timestamp_fn);
                $scope.poDateHeure.phHeureDebut = moment(decl.timestamp_dt).format('HH:mm');
                $scope.poDateHeure.phHeureFin = moment(decl.timestamp_fn).format('HH:mm');
            }
        };

        $scope.resetDateHeure = () => {
            $scope.poDateHeure.pdDateDebut = new Date();
            $scope.poDateHeure.pdDateFin = new Date();
            $scope.poDateHeure.phHeureDebut = moment().format('HH:mm');
            $scope.poDateHeure.phHeureFin = moment().format('HH:mm');
        };

        $scope.saveDeclaration = async (decl) => {
            await decl.save();
            $scope.closeLightBoxDeclaration();
        };

        $scope.delDeclaration = async (decl) => {
            await decl.delete();
            $scope.closeLightBoxDeclaration();
        };

        await presences.sync();
        $scope.structure = presences.structure;
        await $scope.structure.eleves.sync();
        $scope.eleves = $scope.structure.eleves.all;
        $scope.selectedEleve = _.first($scope.eleves);
        $scope.syncEleve();
        utils.safeApply($scope);
    }]);