var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';
var gsFormatDate = 'DD-MM-YYYY';

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

	$scope.template = template;
	template.open('absc_teacher_appel_eleves_container', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel_eleves');

	$scope.detailEleveOpen = false;
	$scope.appel = {
		date	: {}
	};
	/**
	 * Ouverture d'un appel suite à la sélection d'une date
	 */
	$scope.selectAppel = function () {
		var dtDateSelectionnee = $scope.appel.date;
		$scope.ouvrirAppel(sDate);
	}

	/**
	 * Sélection d'un cours : Affiche le panel central de la liste des élèves
	 * @param cours l'objet cours sélectionné
     */
	$scope.selectCours = function(cours) {
		$scope.currentCours = cours;
	};

	/**
	 * Sélection d'un élève : affiche le panel de droit avec la saisie du retard/depart/punition eleve
	 * @param eleve l'objet eleve sélectionné
     */
	$scope.detailEleveAppel = function(eleve) {
		$scope.detailEleveOpen = true;
		$scope.currentEleve = eleve;
		template.open('rightSide_absc_eleve_appel_detail', '../modules/' + gsPrefixAbsences + '/template/absc_eleve_appel_detail');
	};

	/**
	 * Charge un appel
	 * @param pdtDate la date du jour souhaitée
     */
	$scope.ouvrirAppel = function (pdtDate) {

		// formatage en string
		//var sDateDebut = "10-02-2016";
		var sDateDebut = moment(pdtDate).format(gsFormatDate);

		// calcul jour suivant
		var sDateFin = moment(pdtDate).add(1, 'days').format(gsFormatDate);

		// booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
		$scope.detailEleveOpen = false;

		// chargement des cours de la journée de l'enseignant
		model.courss.sync(model.me.userId, sDateDebut, sDateFin);

		// chargement des eleves de  chaque cours
		model.courss.on('sync', function(){
			// TODO ne charger que lors du clic sur un cours
			model.courss.each(function(oCours){
				oCours.eleves.sync();

				oCours.eleves.on('sync', function(){
					oCours.eleves.each(function (oEleve) {
						oEleve.evenements.sync(sDateDebut, sDateFin);
					});
				});

			})
		});

		$scope.courss = model.courss;
	};

	route({
		appel: function (params) {
			var dtToday = new Date();
			$scope.ouvrirAppel(dtToday);

			template.open('main', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel');
		}
	});
}
