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
			evenementAbsence.id_eleve = poEleve.id;
			evenementAbsence.id_type = 1;
			// TODO compléter
			// TODO à voir si on créé tout d'un coup ou si on créé l'appel puis on met à jour au fur et à mesure
			evenementAbsence.id_appel = $scope.appel.id;

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
		var evenementEleve = poEleve.evenements.findWhere({id_type : parseInt(piTypeEvenement)});
		return evenementEleve;
	};

	/**
	 * Ouverture d'un appel suite à la sélection d'une date
	 */
	$scope.selectAppel = function () {
		$scope.ouvrirAppel($scope.appel.date);
	}

	/**
	 * Sélection d'un cours : Affiche le panel central de la liste des élèves
	 * @param cours l'objet cours sélectionné
     */
	$scope.selectCours = function(cours) {
		$scope.currentCours = cours;
		$scope.currentCours.eleves.sync();

		$scope.currentCours.eleves.on('sync', function(){
			$scope.currentCours.eleves.each(function (oEleve) {
				oEleve.evenements.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);
				oEleve.absencePrevs.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);

				oEleve.evenements.on('sync', function() {
					oEleve.isAbsent = oEleve.evenements.findWhere({id_type : 1}) !== undefined;
					oEleve.hasRetard = oEleve.evenements.findWhere({id_type : 2}) !== undefined;
					oEleve.hasDepart = oEleve.evenements.findWhere({id_type : 3}) !== undefined;
					oEleve.hasIncident = oEleve.evenements.findWhere({id_type : 4}) !== undefined;
				});

			});
		});
	};

	/**
	 * Sélection d'un élève : affiche le panel de droit avec la saisie du retard/depart/punition eleve
	 * @param poEleve l'objet eleve sélectionné
     */
	$scope.detailEleveAppel = function(poEleve) {
		$scope.detailEleveOpen = true;
		$scope.currentEleve = poEleve;

		var oEvenementRetard = $scope.getEvenementEleve(poEleve, 2);
		var bHasRetard = oEvenementRetard !== undefined;
		if(!bHasRetard) {
			oEvenementRetard = new Evenement();
		}

		var oEvenementDepart = $scope.getEvenementEleve(poEleve, 3);
		var bHasDepart = oEvenementDepart !== undefined;
		if(!bHasDepart) {
			oEvenementDepart = new Evenement();
		}

		var oEvenementIncident = $scope.getEvenementEleve(poEleve, 4);
		var bHasIndicent = oEvenementIncident !== undefined;
		if(!bHasIndicent) {
			oEvenementIncident = new Evenement();
		}

		var oEvenementObservation = $scope.getEvenementEleve(poEleve, 5);
		if(oEvenementObservation === undefined) {
			oEvenementObservation = new Evenement();
		}

		$scope.currentEleve = {
			hasRetard 				: bHasRetard,
			hasDepart 				: bHasDepart,
			hasIncident 			: bHasIndicent,
			evenementObservation 	: oEvenementObservation,
			evenementDepart 		: oEvenementDepart,
			evenementRetard 		: oEvenementRetard,
			evenementIncident 		: oEvenementIncident

		}

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
			// TODO ne charger que lors du clic sur un cours
			//model.courss.each(function(oCours){
			//	oCours.eleves.sync();
            //
			//	oCours.eleves.on('sync', function(){
			//		oCours.eleves.each(function (oEleve) {
			//			oEleve.evenements.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);
			//			oEleve.absencePrevs.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);
            //
			//			oEleve.evenements.on('sync', function() {
			//				var evenementEleve = oEleve.evenements.findWhere({id_type : 1});
			//				oEleve.isAbsent = evenementEleve !== undefined;
			//			});
            //
			//		});
			//	});
			//})
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
