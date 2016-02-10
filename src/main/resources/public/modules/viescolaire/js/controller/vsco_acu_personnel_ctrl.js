/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
	$routeProvider
			.when('/viescolaire/accueil',{action:'accueil'})
			.otherwise({
				//redirectTo : '/devoirs/list'
				redirectTo : '/viescolaire/accueil'
			});
});

/**
	Wrapper controller
	------------------
	Main controller.
**/
function ViescolaireController($scope, $rootScope, model, template, route, date){

	route({
		accueil: function (params) {
			template.open('main', '../modules/viescolaire/template/vsco_acu_personnel');
		}
	});
}
