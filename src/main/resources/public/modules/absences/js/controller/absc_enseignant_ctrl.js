var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';

/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
	$routeProvider
			.when('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel',{action:'appel'})
			.otherwise({
				redirectTo : '/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel'
			});
});

/**
	Wrapper controller
	------------------
	Main controller.
**/
function AbsencesController($scope, $rootScope, model, template, route, date){
	route({
		appel: function (params) {

			var dateDebut = "10-02-2016";
			var dateFin = "11-02-2016";

			// chargement des cours de la journée de l'enseignant
			model.courss.sync(model.me.userId, dateDebut, dateFin);

			// chargement des eleves de  chaque cours
			model.courss.on('sync', function(){
				model.courss.each(function(cours){
					cours.eleves.sync();
				})
			});

			template.open('main', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel');
		}
	});
}
