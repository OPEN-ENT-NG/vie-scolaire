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

function ViescolaireController($scope, $rootScope, model, template, route, date){

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
