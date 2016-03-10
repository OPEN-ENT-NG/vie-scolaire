/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
	$routeProvider
		.when('/sansmotifs',{action:'AbsencesSansMotifs'})
		.when('/appels/noneffectues', {action:'AppelsOublies'})
		.when('/redirect', {action:'Redirect'})
		.otherwise({
			redirectTo : '/redirect'
		});
});

function AbsencesController($scope, $location, $rootScope, model, template, route, $route, date){
	template.open('menu', '../modules/absences/template/absc_personnel_menu');
    template.open('header', '../modules/absences/template/absc_personnel_header');
	/**
	 * Définition des références aux model.
	 */
	$scope.appels = model.appels;
	$scope.classes = model.classes;
	$scope.enseignants = model.enseignants;
    $scope.evenements = model.evenements;
	$scope.motifs = model.motifs;
	$scope.justificatifs = model.justificatifs;

	$scope.routes = $route;

	$scope.psDisplayReponsables = false;

    $scope.menu = {
        opened : false
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
		debut : new Date(2016, 01, 10),
		fin : new Date(2016, 01, 10)
	};

	$rootScope.$on('$routeChangeSuccess', function($currentRoute, $previousRoute, $location){
		$scope.safeApply();
	});

	$scope.loadData = function(){
		if(($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
			if($location.path() === "/sansmotifs"){
				model.evenements.sync($scope.periode.debut, $scope.periode.fin);
			}else if($location.path() === "/appels/noneffectues"){
				model.appels.sync($scope.periode.debut, $scope.periode.fin);
			}
		}
	};

	model.classes.on('sync', function(){
		model.classes.map(function(classe){
			classe.selected = true;
			return classe;
		});
	});

	model.enseignants.on('sync', function(){
		model.enseignants.map(function(enseignant){
			enseignant.selected = true;
			return enseignant;
		});
	});

	model.motifs.on('sync', function(){
		model.motifs.synced = true;
	});

    $scope.goToPage = function(path){
        location.replace(path);
    };

    $scope.goToState = function(path){
        $location.path(path);
        $location.replace();
    };

	$scope.safeApply = function(fn) {
		var p
		e = this.$root.$$phase;
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

	route({
		AbsencesSansMotifs: function (params) {
			template.open('main', '../modules/absences/template/absc_personnel_abssm');
            $scope.menu.opened = false;
		},
		AppelsOublies : function(params){
			template.open('main', '../modules/absences/template/absc_personnel_appo');
            $scope.menu.opened = false;
		},
		Redirect : function(params){
			$scope.goToPage('/viescolaire');
		}
	});
}
