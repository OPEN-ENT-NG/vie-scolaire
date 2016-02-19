/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
	$routeProvider
		.when('/sansmotifs',{action:'AbsencesSansMotifs'})
		.otherwise({
			redirectTo : '/sansmotifs'
		});
});

function AbsencesController($scope, $rootScope, model, template, route, date){
	/**
	 * Définition des références aux model.
	 */
	$scope.appels = model.appels;
	$scope.classes = model.classes;
	$scope.enseignants = model.enseignants;

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

	route({
		AbsencesSansMotifs: function (params) {
			template.open('main', '../modules/absences/template/absc_personnel_abssm');
		}
	});
}
