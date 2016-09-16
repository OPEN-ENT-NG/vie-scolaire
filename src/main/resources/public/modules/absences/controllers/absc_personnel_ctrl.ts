import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {vieScolaire, Appel, Classe, Eleve, Enseignant, Evenement, Justificatif, Matiere, Motif, Responsable, VieScolaire} from '../models/absc_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let absencesController = ng.controller('AbsencesController', [
	'$scope', 'route', 'model', '$rootScope', '$location',
	function ($scope, route, model, $rootScope, $location) {
		route({
			AbsencesSansMotifs: function (params) {
				template.open('main', '../templates/absences/absc_personnel_abssm');
				$scope.display.menu = false;
			},
			AppelsOublies : function(params){
				template.open('main', '../templates/absences/absc_personnel_appo');
				$scope.display.menu = false;
			},
			Redirect : function(params){
				$scope.goToPage('/viescolaire');
			}
		});

		template.open('menu', '../templates/absences/absc_personnel_menu');
		template.open('header', '../templates/absences/absc_personnel_header');

		$scope.appels = vieScolaire.appels;
		$scope.classes = vieScolaire.classes;
		$scope.enseignants = vieScolaire.enseignants;
		$scope.evenements = vieScolaire.evenements;
		$scope.motifs = vieScolaire.motifs;
		$scope.justificatifs = vieScolaire.justificatifs;

		$scope.display = {
			responsables : false,
			menu : false
		};

		/**
		 * Critères de tris
		 */
		$scope.pOSortParameters = {
			sortType : '',
			sortReverse : false
		};

		/**
		 * Critères de recherches personnels
		 */
		$scope.pORecherche = {};

		/**
		 * Définition des périodes
		 */
		$scope.periode = {
			debut : new Date(),
			fin : new Date()
		};

		$rootScope.$on('$routeChangeSuccess', ($currentRoute, $previousRoute, $location) => {
			$scope.safeApply();
		});

		$scope.loadData = function(){
			if(($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
				if($location.path() === "/sansmotifs"){
					vieScolaire.evenements.sync($scope.periode.debut, $scope.periode.fin);
				}else if($location.path() === "/appels/noneffectues"){
					vieScolaire.appels.sync($scope.periode.debut, $scope.periode.fin);
				}
			}
		};

		vieScolaire.classes.on('sync', function(){
			vieScolaire.classes.map(function(classe){
				classe.selected = true;
				return classe;
			});
		});

		vieScolaire.enseignants.on('sync', function(){
			vieScolaire.enseignants.map(function(enseignant){
				enseignant.selected = true;
				return enseignant;
			});
		});

		vieScolaire.motifs.on('sync', function(){
			vieScolaire.motifs.synced = true;
		});

		$scope.goToPage = function(path){
			location.replace(path);
		};

		$scope.goToState = function(path){
			$location.path(path);
			$location.replace();
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

		$scope.personnelFilter = function(event){
			return $scope.classeFilter(event) && $scope.enseignantFilter(event);
		};
		$scope.classeFilter = function(event){
			return ($scope.classes.findWhere({classe_id : event.classe_id, selected: true}) !== undefined);
		};

		$scope.enseignantFilter = function(event){
			return ($scope.enseignants.findWhere({personnel_id : event.personnel_id, selected: true}) !== undefined);
		};

		$scope.alert = function(message){
			alert(message);
		};
	}
]);