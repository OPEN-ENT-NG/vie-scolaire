/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
	$routeProvider
			.when('/viescolaire/accueil',{action:'accueil'})
			.otherwise({
				redirectTo : '/viescolaire/accueil'
			});
});

/**
	Wrapper controller
	------------------
	Main controller.
**/
function ViescolaireController($scope, $rootScope, model, template, route, date){
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
		accueil: function (params) {
			template.open('main', '../modules/viescolaire/template/vsco_acu_personnel');
		}
	});
}
