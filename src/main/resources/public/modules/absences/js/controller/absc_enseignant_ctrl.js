var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';

var gsFormatDate = 'DD-MM-YYYY';
var gsFormatTimestampWithoutTimeZone = "YYYY-MM-DDTHH:mm:ss.SSSS";

var giIdEvenementAbsence = 1;
var giIdEvenementRetard = 2;
var giIdEvenementDepart = 3;
var giIdEvenementIncident = 4;
var giIdEvenementObservation = 5;

var giIdMotifSansMotif = 8;

var giIdEtatAppelInit = 1;
var giIdEtatAppelEnCours = 2;
var giIdEtatAppelFait = 3;

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

	/**
	 * Message pour les fonctionnalié pas encore développées
	 */
	$scope.alertNonImplementee = function() {
		alert("Fonctionnalité non implémentée actuellement.")
	};

	/**
	 * Calcule le nombre d'élèves présents et le renseigne dans $scope.currentCours.nbPresents
	 */
	$scope.calculerNbElevesPresents = function() {
		var oElevesAbsents = $scope.currentCours.eleves.where({isAbsent : true});
		var iNbAbsents = 0;
		if(oElevesAbsents !== undefined) {
			iNbAbsents = oElevesAbsents.length;
		}
		$scope.currentCours.nbPresents = $scope.currentCours.nbEleves - iNbAbsents;
	};

	$scope.getHeure = function (timestampDate) {
		return moment(new Date(timestampDate)).format("HH:mm");
	};

	/**
	 * Ajout un evenement de type absence pour l'élève passé en paramètre
	 * @param poEleve l'objet élève
     */
	$scope.ajouterEvenementAbsence = function(poEleve) {
		var evenementAbsence = $scope.getEvenementEleve(poEleve, giIdEvenementAbsence);

		// creation absence
		if(evenementAbsence === undefined) {
			evenementAbsence = new Evenement();
			evenementAbsence.evenement_saisie_cpe = false;
			evenementAbsence.fk_eleve_id = poEleve.eleve_id;
			evenementAbsence.fk_appel_id = $scope.currentCours.appel.appel_id;
			evenementAbsence.fk_type_evt_id = giIdEvenementAbsence;
			evenementAbsence.fk_motif_id = giIdMotifSansMotif;

			evenementAbsence.create(function(piEvenementId) {
				evenementAbsence.evenement_id = piEvenementId;
				poEleve.isAbsent = true;
				poEleve.evenements.push(evenementAbsence);

				// l'état de l'appel repasse en cours
				$scope.changerEtatAppel(giIdEtatAppelEnCours);
			});
		// suppression absence
		} else {
			evenementAbsence.delete(function() {
				poEleve.isAbsent = false;
				$scope.supprimerEvenementEleve(poEleve, evenementAbsence);

				// l'état de l'appel repasse en cours
				$scope.changerEtatAppel(giIdEtatAppelEnCours);
			});
		}
		$scope.calculerNbElevesPresents();
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
	 * Action de check d'un évenement de retard/départ/incident sur un élève.
	 * Si m'on coche la checkbox, créé l'évenement en base s'il n'existe pas encore.
	 * Si l'on décoche la checkbox, supprime l'évenement de la liste des évenements de l'élève.
	 * @param pbIsChecked booleen permettant de savoir si la checbox est cochée ou non.
	 * @param poEvenement l'évenement.
	 */
	$scope.checkEvenement = function (pbIsChecked, poEvenement) {
		if(pbIsChecked && poEvenement.evenement_id === undefined) {

			var oMomentDebutCours = moment($scope.currentCours.cours_timestamp_dt);
			var sHeureAujourDhui = moment().format("HH:mm");

			// initalisation des heures selon l'heure courante et la date du cours
			if(poEvenement.fk_type_evt_id === giIdEvenementDepart) {
				poEvenement.evenement_heure_depart = sHeureAujourDhui;
			} else if(poEvenement.fk_type_evt_id === giIdEvenementRetard) {
				poEvenement.evenement_heure_arrivee = sHeureAujourDhui;
			}

			$scope.mapToTimestamp(poEvenement, oMomentDebutCours);

			poEvenement.create(function(piEvenementId) {
				poEvenement.evenement_id = piEvenementId;

				// l'état de l'appel repasse en cours
				$scope.changerEtatAppel(giIdEtatAppelEnCours);
			});
		}else {
			poEvenement.delete(function() {
				$scope.supprimerEvenementEleve($scope.currentEleve, poEvenement);

				// l'état de l'appel repasse en cours
				$scope.changerEtatAppel(giIdEtatAppelEnCours);
			});
		}
	};

	/**
	 * Met à jour un évenement en BDD
	 * @param poEvenement l'évenement.
     */
	$scope.updateEvenement = function(poEvenement) {
		// conversion de l'heure saisie en timestamp
		var oMomentDebutCours = moment($scope.currentCours.cours_timestamp_dt);
		$scope.mapToTimestamp(poEvenement, oMomentDebutCours);
		poEvenement.save(function(piEvenementId) {
			poEvenement.evenement_id = piEvenementId;

			// l'état de l'appel repasse en cours
			$scope.changerEtatAppel(giIdEtatAppelEnCours);
		});
	};

	/**
	 * Selon l'évenement récupère l'heure de départ ou l'heure d'arrivée et la convertie en timestamp
	 * pour la renseigner dans le champ correspondant (poEvenement.evenement_timestamp_depart ou poEvenement.evenement_timestamp_arrive).
	 *
	 * @param poEvenement l'evenement.
	 * @param poMomentDebutCours objet moment.js représentant la date de début du cours (nécessaire pour avoir la date du jour).
     */
	$scope.mapToTimestamp = function (poEvenement, poMomentDebutCours) {
		// initalisation des heures selon l'heure courante et la date du cours
		if(poEvenement.fk_type_evt_id === giIdEvenementDepart) {
			var oEvenementTimestampDepart = moment(poMomentDebutCours, gsFormatTimestampWithoutTimeZone).hour(poEvenement.evenement_heure_depart.split(":")[0]).minute(poEvenement.evenement_heure_depart.split(":")[1]);
			poEvenement.evenement_timestamp_depart = oEvenementTimestampDepart.format(gsFormatTimestampWithoutTimeZone)

		} else if(poEvenement.fk_type_evt_id === giIdEvenementRetard) {
			var oEvenementTimestampArrive = moment(poMomentDebutCours, gsFormatTimestampWithoutTimeZone).hour(poEvenement.evenement_heure_arrivee.split(":")[0]).minute(poEvenement.evenement_heure_arrivee.split(":")[1]);
			poEvenement.evenement_timestamp_arrive = oEvenementTimestampArrive.format(gsFormatTimestampWithoutTimeZone);
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
			poEvenement.evenement_id = undefined;
			poEleve.evenements.remove(poEvenement);

			poEleve.creneaus.sync($scope.currentCours.appel.appel_id);
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

		// réinitialsiation des valeurs pour ne pas afficher le panel detail eleve lorsque l'on change d'appel.
		$scope.currentEleve = undefined;
		$scope.detailEleveOpen = false;

		// Recuperation de l'appel associé (création en mode Init s'il n'existe pas)
		$scope.currentCours.appel.sync();

		$scope.currentCours.eleves.sync();

		$scope.currentCours.eleves.on('sync', function(){
			$scope.currentCours.nbPresents = 0;
			$scope.currentCours.nbEleves = 0;
			if($scope.currentCours.eleves !== undefined) {
				$scope.currentCours.nbEleves = $scope.currentCours.eleves.all.length;
			}

			//$scope.calculerNbElevesPresents();

			$scope.currentCours.eleves.each(function (oEleve) {
				oEleve.evenements.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);
				oEleve.absencePrevs.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);

				oEleve.evenements.on('sync', function() {
					oEleve.isAbsent = oEleve.evenements.findWhere({fk_type_evt_id : giIdEvenementAbsence, fk_appel_id : $scope.currentCours.appel.appel_id}) !== undefined;
					oEleve.hasRetard = oEleve.evenements.findWhere({fk_type_evt_id :giIdEvenementRetard, fk_appel_id : $scope.currentCours.appel.appel_id}) !== undefined;
					oEleve.hasDepart = oEleve.evenements.findWhere({fk_type_evt_id : giIdEvenementDepart, fk_appel_id : $scope.currentCours.appel.appel_id}) !== undefined;
					oEleve.hasIncident = oEleve.evenements.findWhere({fk_type_evt_id : giIdEvenementIncident, fk_appel_id : $scope.currentCours.appel.appel_id}) !== undefined;

					if(!oEleve.isAbsent && !oEleve.hasDepart && !oEleve.hasRetard) {
						$scope.currentCours.nbPresents++;
					}

					oEleve.absencePrevs.on('sync', function() {
						oEleve.creneaus.sync($scope.currentCours.appel.appel_id);
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
		$scope.detailEleveOpen = $scope.currentEleve === undefined ||
				($scope.currentEleve !==undefined && $scope.currentEleve.eleve_id !== poEleve.eleve_id);

		if($scope.detailEleveOpen) {
			$scope.currentEleve = poEleve;
		} else {
			$scope.currentEleve = undefined;
			return;
		}

		var oEvenementRetard = $scope.getEvenementEleve(poEleve, giIdEvenementRetard);
		if(oEvenementRetard === undefined) {
			oEvenementRetard = new Evenement();
			oEvenementRetard.evenement_saisie_cpe = false;
			oEvenementRetard.fk_eleve_id = poEleve.eleve_id;
			oEvenementRetard.fk_appel_id = $scope.currentCours.appel.appel_id;
			oEvenementRetard.fk_type_evt_id = giIdEvenementRetard;
			oEvenementRetard.fk_motif_id = giIdMotifSansMotif;
		}

		var oEvenementDepart = $scope.getEvenementEleve(poEleve, giIdEvenementDepart);
		if(oEvenementDepart === undefined) {
			oEvenementDepart = new Evenement();
			oEvenementDepart.evenement_saisie_cpe = false;
			oEvenementDepart.fk_eleve_id = poEleve.eleve_id;
			oEvenementDepart.fk_appel_id = $scope.currentCours.appel.appel_id;
			oEvenementDepart.fk_type_evt_id = giIdEvenementDepart;
			oEvenementDepart.fk_motif_id = giIdMotifSansMotif;
		}

		var oEvenementIncident = $scope.getEvenementEleve(poEleve, giIdEvenementIncident);
		if(oEvenementIncident === undefined) {
			oEvenementIncident = new Evenement();
			oEvenementIncident.evenement_saisie_cpe = false;
			oEvenementIncident.fk_eleve_id = poEleve.eleve_id;
			oEvenementIncident.fk_appel_id = $scope.currentCours.appel.appel_id;
			oEvenementIncident.fk_type_evt_id = giIdEvenementIncident;
			oEvenementIncident.fk_motif_id = giIdMotifSansMotif;
		}

		var oEvenementObservation = $scope.getEvenementEleve(poEleve, giIdEvenementObservation);
		if(oEvenementObservation === undefined) {
			oEvenementObservation = new Evenement();
			oEvenementObservation.evenement_saisie_cpe = false;
			oEvenementObservation.fk_eleve_id = poEleve.eleve_id;
			oEvenementObservation.fk_appel_id = $scope.currentCours.appel.appel_id;
			oEvenementObservation.fk_type_evt_id = giIdEvenementObservation;
			oEvenementObservation.fk_motif_id = giIdMotifSansMotif;
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

	/**
	 * Passage de l'état d'un appel à "Fait"
	 */
	$scope.terminerAppel = function() {
		$scope.changerEtatAppel(giIdEtatAppelFait);
		$scope.currentCours.appel.update();
	};

	/**
	 * Change l'état d'un appel.
	 * @param piIdEtatAppel l'identifiant de l'état souhaité.
     */
	$scope.changerEtatAppel = function (piIdEtatAppel) {
		$scope.currentCours.appel.fk_etat_appel_id = piIdEtatAppel;
		$scope.currentCours.appel.update();
		$scope.safeApply();
	};

	route({
		appel: function (params) {
			var dtToday = new Date();
			$scope.ouvrirAppel(dtToday);

			template.open('main', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel');
		}
	});
}
