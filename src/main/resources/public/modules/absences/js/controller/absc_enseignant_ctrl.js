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
function AbsencesController($scope, $rootScope, $route, model, template, route, date){

	$scope.template = template;
	$scope.routes = $route;

	template.open('absc_teacher_appel_eleves_container', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel_eleves');

	$scope.detailEleveOpen = false;
	$scope.appel = {
		date	: {}
	};


	$scope.getHeure = function (timestampDate) {
		return moment(new Date(timestampDate)).format("HH:mm");
	};

	/**
	 * Ajout un evenement de type absence pour l'élève passé en paramètre
	 * @param poEleve l'objet élève
     */
	$scope.ajouterEvenementAbsence = function(poEleve) {
		var evenementAbsence = $scope.getEvenementEleve(poEleve, 1);

		// creation absence
		if(evenementAbsence === undefined) {
			evenementAbsence = new Evenement();
			evenementAbsence.fk_eleve_id = poEleve.eleve_id;
			evenementAbsence.fk_type_evt_id = 1;
			// TODO compléter
			// TODO à voir si on créé tout d'un coup ou si on créé l'appel puis on met à jour au fur et à mesure
			evenementAbsence.fk_appel_id = $scope.appel.id;

			poEleve.evenements.push(evenementAbsence);

			evenementAbsence.create(function() {
				poEleve.isAbsent = true;
			});
		// suppression absence
		} else {
			evenementAbsence.delete(function() {
				poEleve.isAbsent = false;
				poEleve.evenements.remove(evenementAbsence);
			});
		}
	};

	/**
	 * Retourne l'évenement (absence/retard/depart/incident) d'un élève
	 * selon le type passé en parametre.
	 *
	 * @param poEleve l'élève
	 * @param piTypeEvenement type d'évenement (entier)
	 * @returns l'évenement ouo undefined si aucun évenement trouvé.
     */
	$scope.getEvenementEleve = function(poEleve, piTypeEvenement) {
		var evenementEleve = poEleve.evenements.findWhere({fk_type_evt_id : parseInt(piTypeEvenement)});
		return evenementEleve;
	};


	/**
	 * Action de check d'un incident sur un élève.
	 * Supprime l'évenement de la liste des évenements de l'élève si l'on décoche la checkbox
	 */
	$scope.checkHasIndicent = function () {
		//$scope.currentEleve.hasIncident = !$scope.currentEleve.hasIncident;

		if(!$scope.currentEleve.hasIncident) {
			$scope.supprimerEvenementEleve($scope.currentEleve, $scope.currentEleve.evenementIncident);
		}
	};

	/**
	 * Action de check d'un retard sur un élève.
	 * Supprime l'évenement de la liste des évenements de l'élève si l'on décoche la checkbox
	 */
	$scope.checkHasRetard = function () {
		if(!$scope.currentEleve.hasRetard) {
			$scope.supprimerEvenementEleve($scope.currentEleve, $scope.currentEleve.evenementRetard);
		}
	};

	/**
	 * Action de check d'un départ sur un élève.
	 * Supprime l'évenement de la liste des évenements de l'élève si l'on décoche la checkbox
	 */
	$scope.checkHasDepart = function () {
		if(!$scope.currentEleve.hasDepart) {
			$scope.supprimerEvenementEleve($scope.currentEleve, $scope.currentEleve.evenementDepart);
		}
	};

	/**
	 * Supprime l'évenement d'un élève
	 *
	 * @param poEleve l'élève
	 * @param poEvenement évenement à supprimer
	 */
	$scope.supprimerEvenementEleve = function(poEleve, poEvenement) {
		if(poEleve.evenements !== undefined) {
			poEleve.evenements.remove(poEvenement);
		}
	};

	/**
	 * Ouverture d'un appel suite à la sélection d'une date
	 */
	$scope.selectAppel = function () {
		$scope.currentCours = undefined;
		$scope.ouvrirAppel($scope.appel.date);
	};

	/**
	 * Sélection d'un cours : Affiche le panel central de la liste des élèves
	 * @param cours l'objet cours sélectionné
     */
	$scope.selectCours = function(cours) {
		$scope.currentCours = cours;

		//TODO creer appel en mode "initialisé"
		$scope.currentCours.appel = {};
		$scope.currentCours.appel.id = 1;
		$scope.currentCours.eleves.sync();

		$scope.currentCours.eleves.on('sync', function(){
			$scope.currentCours.eleves.each(function (oEleve) {
				oEleve.evenements.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);
				oEleve.absencePrevs.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);

				oEleve.evenements.on('sync', function() {
					oEleve.isAbsent = oEleve.evenements.findWhere({fk_type_evt_id : 1, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
					oEleve.hasRetard = oEleve.evenements.findWhere({fk_type_evt_id : 2, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
					oEleve.hasDepart = oEleve.evenements.findWhere({fk_type_evt_id : 3, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
					oEleve.hasIncident = oEleve.evenements.findWhere({fk_type_evt_id : 4, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;

					oEleve.absencePrevs.on('sync', function() {
						oEleve.creneaus.sync($scope.currentCours.appel.id);
					});

				});

			});
		});
	};

	/**
	 * Sélection d'un élève : affiche le panel de droit avec la saisie du retard/depart/punition eleve
	 * @param poEleve l'objet eleve sélectionné
     */
	$scope.detailEleveAppel = function(poEleve) {
		$scope.detailEleveOpen = !$scope.detailEleveOpen;
		$scope.currentEleve = poEleve;

		var oEvenementRetard = $scope.getEvenementEleve(poEleve, 2);
		if(oEvenementRetard === undefined) {
			oEvenementRetard = new Evenement();
		}

		var oEvenementDepart = $scope.getEvenementEleve(poEleve, 3);
		if(oEvenementDepart === undefined) {
			oEvenementDepart = new Evenement();
		}

		var oEvenementIncident = $scope.getEvenementEleve(poEleve, 4);
		if(oEvenementIncident === undefined) {
			oEvenementIncident = new Evenement();
		}

		var oEvenementObservation = $scope.getEvenementEleve(poEleve, 5);
		if(oEvenementObservation === undefined) {
			oEvenementObservation = new Evenement();
		}

		$scope.currentEleve.evenementObservation = oEvenementObservation;
		$scope.currentEleve.evenementDepart = oEvenementDepart;
		$scope.currentEleve.evenementRetard = oEvenementRetard;
		$scope.currentEleve.evenementIncident = oEvenementIncident;


		template.open('rightSide_absc_eleve_appel_detail', '../modules/' + gsPrefixAbsences + '/template/absc_eleve_appel_detail');
	};

	$scope.fermerDetailEleve = function() {
		$scope.currentEleve = undefined;
		// booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
		$scope.detailEleveOpen = false;
	};

	/**
	* Charge un appel
	* @param pdtDate la date du jour souhaitée
	*/
	$scope.ouvrirAppel = function (pdtDate) {

		// formatage en string
		//var sDateDebut = "10-02-2016";
		var sDateDebut = moment(pdtDate).format(gsFormatDate);
		$scope.appel.sDateDebut = sDateDebut;

		// calcul jour suivant
		var sDateFin = moment(pdtDate).add(1, 'days').format(gsFormatDate);
		$scope.appel.sDateFin = sDateFin;

		// booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
		$scope.detailEleveOpen = false;

		// chargement des cours de la journée de l'enseignant
		model.courss.sync(model.me.userId, $scope.appel.sDateDebut, $scope.appel.sDateFin);

		// chargement des eleves de  chaque cours
		model.courss.on('sync', function(){
			model.creneaus.sync();
			model.creneaus.on('sync', function(){
				$scope.creneaus = model.creneaus;
			});
		});

		$scope.courss = model.courss;

		model.plages.sync();
		$scope.plages = model.plages;
	};

	route({
		appel: function (params) {
			var dtToday = new Date();
			$scope.ouvrirAppel(dtToday);

			template.open('main', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel');
		}
	});
}
