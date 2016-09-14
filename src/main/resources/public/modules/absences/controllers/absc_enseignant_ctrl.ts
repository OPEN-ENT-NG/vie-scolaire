import {notify, idiom as lang, template, routes, model, ng } from '../../entcore/entcore';
import {AbsencePrev, Appel, Cours, Creneau, Eleve, Evenement, Plage, VieScolaire, vieScolaire} from '../models/absc_enseignant_mdl';

let moment = require('moment');
declare let _:any;

export let absencesController = ng.controller('AbsencesController', [
	'$scope', 'route', 'model',
	function ($scope, route, model) {

	}
]);

// var gsPrefixVieScolaire = 'viescolaire';
// var gsPrefixNotes = 'notes';
// var gsPrefixAbsences = 'absences';
//
// var gsFormatDate = 'DD-MM-YYYY';
// var gsFormatTimestampWithoutTimeZone = "YYYY-MM-DDTHH:mm:ss.SSSS";
//
// //var giIdMotifSansMotif = 8;
// //
// var giIdEtatAppelInit = 1;
// var giIdEtatAppelEnCours = 2;
// var giIdEtatAppelFait = 3;
//
// /**
//  Defining internal routes
//  **/
// routes.define(function($routeProvider){
// 	$routeProvider
// 			.when('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel',{action:'appel'})
// 			.otherwise({
// 				redirectTo : '/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel'
// 			});
// });
//
// /**
// 	Wrapper controller
// 	------------------
// 	Main controller.
// **/
// function AbsencesController($scope, $rootScope, $route, model, template, route, date, $timeout){
//
// 	$scope.template = template;
// 	$scope.routes = $route;
// 	$scope.showSuccess = false;
//
// 	/**
// 	 *	Déclaration des type d'évènements
// 	 */
// 	$scope.oEvtType = {
// 		giIdEvenementAbsence : 1,
// 		giIdEvenementRetard : 2,
// 		giIdEvenementDepart : 3,
// 		giIdEvenementIncident : 4,
// 		giIdEvenementObservation : 5,
//         giIdMotifSansMotif : 8
// 	};
//
// 	template.open('absc_teacher_appel_eleves_container', '../templates/' + gsPrefixAbsences + '/absc_teacher_appel_eleves');
//
// 	$scope.detailEleveOpen = {
// 		displayed : false
// 	};
// 	$scope.appel = {
// 		date	: {}
// 	};
// 	$scope.lightbox = {
// 		show : false
// 	};
// 	$scope.oEvtTime = {
// 		depart : '--:--',
// 		retard : '--:--'
// 	}
//
// 	$scope.safeApply = function(fn) {
// 		var phase = this.$root.$$phase;
// 		if(phase == '$apply' || phase == '$digest') {
// 			if(fn && (typeof(fn) === 'function')) {
// 				fn();
// 			}
// 		} else {
// 			this.$apply(fn);
// 		}
// 	};
//
// 	$scope.formatDate = function(h){
// 		return "00:00";
// 	};
// 	/**
// 	 * Message pour les fonctionnalié pas encore développées
// 	 */
// 	$scope.alertNonImplementee = function() {
// 		alert("Fonctionnalité actuellement non implémentée.")
// 	};
//
// 	/**
// 	 * Calcule le nombre d'élèves présents et le renseigne dans $scope.currentCours.nbPresents
// 	 */
// 	$scope.calculerNbElevesPresents = function() {
// 		$scope.currentCours.nbPresents = 0;
// 		var oElevesAbsents = $scope.currentCours.eleves.where({isAbsent : true});
// 		var iNbAbsents = 0;
// 		if(oElevesAbsents !== undefined) {
// 			iNbAbsents = oElevesAbsents.length;
// 		}
// 		$scope.currentCours.nbPresents = $scope.currentCours.nbEleves - iNbAbsents;
// 	};
//
// 	$scope.getHeure = function (timestampDate) {
// 		return moment(new Date(timestampDate)).format("HH:mm");
// 	};
//
// 	/**
// 	 * Ajout un evenement de type absence pour l'élève passé en paramètre
// 	 * @param poEleve l'objet élève
//      */
// 	$scope.ajouterEvenementAbsence = function(poEleve) {
//         $scope.currentEleve = poEleve;
// 		var evenementAbsence = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementAbsence);
//
// 		// creation absence
// 		if(evenementAbsence === undefined) {
// 			evenementAbsence = new Evenement();
// 			evenementAbsence.evenement_saisie_cpe = false;
// 			evenementAbsence.fk_eleve_id = poEleve.eleve_id;
// 			evenementAbsence.fk_appel_id = $scope.currentCours.appel.id;
// 			evenementAbsence.fk_type_evt_id = $scope.oEvtType.giIdEvenementAbsence;
// 			evenementAbsence.fk_motif_id = $scope.oEvtType.giIdMotifSansMotif;
//
// 			evenementAbsence.create(function(piEvenementId) {
// 				evenementAbsence.id = piEvenementId;
// 				poEleve.isAbsent = !poEleve.isAbsent;
// 				poEleve.evenements.push(evenementAbsence);
// 				$scope.removeEvtNAbsc(poEleve);
//                 $scope.addEvtPlage(evenementAbsence);
// 				// l'état de l'appel repasse en cours
// 				$scope.changerEtatAppel(giIdEtatAppelEnCours);
// 				$scope.calculerNbElevesPresents();
// 			});
// 		// suppression absence
// 		} else {
// 			evenementAbsence.delete(function() {
// 				poEleve.isAbsent = false;
// 				evenementAbsence.id = undefined;
//                 poEleve.evenements.remove(evenementAbsence);
// 				$scope.supprimerEvenementEleve(poEleve, evenementAbsence);
// 				// l'état de l'appel repasse en cours
// 				$scope.changerEtatAppel(giIdEtatAppelEnCours);
// 				$scope.calculerNbElevesPresents();
// 			});
// 		}
// 	};
//
// 	/**
// 	 *  Supprime les  évènements Retard, Départ et Incident si l'élève est déclaré absent.
// 	 * @param poEleve Objet Eleve référencé
// 	 */
// 	$scope.removeEvtNAbsc = function(poEleve){
// 		var tEvenementDepart = poEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementDepart});
// 		var tEvenementRetard = poEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementRetard});
// 		var tEvenementIncident = poEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementIncident});
// 		if(tEvenementDepart !== undefined){
// 			poEleve.evenements.remove(tEvenementDepart);
// 			tEvenementDepart.delete();
// 			$scope.supprimerEvenementEleve(poEleve, tEvenementDepart);
// 			if(poEleve.evenementDepart !== undefined){
// 				poEleve.evenementDepart.evenement_id = undefined;
// 				poEleve.hasDepart = false;
// 			}
// 		}
// 		if(tEvenementRetard !== undefined){
// 			poEleve.evenements.remove(tEvenementRetard);
// 			tEvenementRetard.delete();
// 			$scope.supprimerEvenementEleve(poEleve, tEvenementRetard);
// 			if(poEleve.evenementRetard !== undefined){
// 				poEleve.evenementRetard.evenement_id = undefined;
// 				poEleve.hasRetard = false;
// 			}
// 		}
// 		if(tEvenementIncident !== undefined){
// 			poEleve.evenements.remove(tEvenementIncident);
// 			tEvenementIncident.delete();
// 			$scope.supprimerEvenementEleve(poEleve, tEvenementIncident);
// 			if(poEleve.evenementIncident !== undefined){
// 				poEleve.evenementIncident.evenement_id = undefined;
// 				poEleve.hasIncident = false;
// 			}
// 		}
// 		$scope.safeApply();
// 	};
//
// 	/**
// 	 * Retourne l'évenement (absence/retard/depart/incident) d'un élève
// 	 * selon le type passé en parametre.
// 	 *
// 	 * @param poEleve l'élève
// 	 * @param piTypeEvenement type d'évenement (entier)
// 	 * @returns l'évenement ouo undefined si aucun évenement trouvé.
//      */
// 	$scope.getEvenementEleve = function(poEleve, piTypeEvenement) {
// 		return  poEleve.evenements.findWhere({fk_type_evt_id : parseInt(piTypeEvenement)});
// 	};
//
//
// 	/**
// 	 * Action de check d'un évenement de retard/départ/incident sur un élève.
// 	 * Si m'on coche la checkbox, créé l'évenement en base s'il n'existe pas encore.
// 	 * Si l'on décoche la checkbox, supprime l'évenement de la liste des évenements de l'élève.
// 	 * @param pbIsChecked booleen permettant de savoir si la checbox est cochée ou non.
// 	 * @param poEvenement l'évenement.
// 	 */
// 	$scope.checkEvenement = function (pbIsChecked, poEvenement) {
// 		if(pbIsChecked) {
// 			var evenementAbsence = $scope.getEvenementEleve($scope.currentEleve, $scope.oEvtType.giIdEvenementAbsence);
// 			if(evenementAbsence !== undefined){
// 				$scope.ajouterEvenementAbsence($scope.currentEleve);
// 			}
// 			var oMomentDebutCours = moment($scope.currentCours.cours_timestamp_dt);
// 			var sHeureAujourDhui = moment().format("HH:mm");
//
// 			// initalisation des heures selon l'heure courante et la date du cours
// 			if(poEvenement.fk_type_evt_id === $scope.oEvtType.giIdEvenementDepart) {
// 				poEvenement.evenement_heure_depart = sHeureAujourDhui;
// 				$scope.oEvtTime.depart = sHeureAujourDhui;
// 			} else if(poEvenement.fk_type_evt_id === $scope.oEvtType.giIdEvenementRetard) {
// 				poEvenement.evenement_heure_arrivee = sHeureAujourDhui;
// 				$scope.oEvtTime.retard = sHeureAujourDhui;
// 			}
//
// 			$scope.mapToTimestamp(poEvenement, oMomentDebutCours);
//
// 			poEvenement.save(function(pnEvenementId) {
//                 $scope.setIdToValue(poEvenement, pnEvenementId);
//                 poEvenement.id = pnEvenementId;
//                 $scope.addEvtPlage(poEvenement);
//                 $scope.currentEleve.evenements.push(poEvenement);
//                 $scope.currentEleve.evenementsJour.push(poEvenement);
// 				// l'état de l'appel repasse en cours
// 				$scope.changerEtatAppel(giIdEtatAppelEnCours);
// 			});
// 		}else {
// 			poEvenement.delete(function() {
// 				if(poEvenement.fk_type_evt_id === $scope.oEvtType.giIdEvenementDepart) {
// 					$scope.oEvtTime.depart = "--:--";
// 				} else if(poEvenement.fk_type_evt_id === $scope.oEvtType.giIdEvenementRetard) {
// 					$scope.oEvtTime.retard = "--:--";
// 				}
// 				$scope.supprimerEvenementEleve($scope.currentEleve, poEvenement);
//                 poEvenement.id = undefined;
//                 $scope.setIdToValue(poEvenement, undefined);
// 				// l'état de l'appel repasse en cours
// 				$scope.changerEtatAppel(giIdEtatAppelEnCours);
// 			});
// 		}
// 	};
//
// 	$scope.saisieCpe = function(psAppelID, oEleve, iTypeEvt){
// 		var o = oEleve.evenements.findWhere({fk_appel_id : psAppelID, fk_type_evt_id : iTypeEvt});
// 		if(o !== undefined){
// 			return o.evenement_saisie_cpe;
// 		}
// 	};
//
//     $scope.addEvtPlage = function(poEvt){
//         var otCours = $scope.currentEleve.courss.findWhere({cours_id : $scope.currentCours.cours_id});
//         var otPlage = $scope.currentEleve.plages.findWhere({heure : parseInt(moment(otCours.cours_timestamp_dt).format('HH'))});
//
//         otPlage.evenements.push(poEvt);
// 		$scope.safeApply();
//     };
//
//     $scope.setIdToValue = function(poEvenement, poValue){
//         switch(poEvenement.fk_type_evt_id){
//             case $scope.oEvtType.giIdEvenementDepart :
//                 $scope.currentEleve.evenementDepart.evenement_id = poValue;
//                 break;
//             case $scope.oEvtType.giIdEvenementRetard :
//                 $scope.currentEleve.evenementRetard.evenement_id = poValue;
//                 break;
//             case $scope.oEvtType.giIdEvenementObservation :
//                 $scope.currentEleve.evenementObservation.evenement_id = poValue;
//                 break;
//         }
//     };
//
// 	/**
// 	 * Met à jour un évenement en BDD
// 	 * @param poEvenement l'évenement.
//      */
// 	$scope.updateEvenement = function(poEvenement, poUpdatedField) {
//         if(poUpdatedField !== 'evenement_commentaire'){
//             var oMomentDebutCours = moment($scope.currentCours.cours_timestamp_dt);
//             $scope.mapToTimestamp(poEvenement, oMomentDebutCours);
//         }else{
//             if(poEvenement[poUpdatedField] === '' || poEvenement[poUpdatedField] === null ||poEvenement[poUpdatedField] === undefined){
// 				console.log($scope.oEvtTime);
//                 if(poEvenement.evenement_id !== undefined){
//                     poEvenement.delete(function() {
//                         $scope.supprimerEvenementEleve($scope.currentEleve, poEvenement);
//                         $scope.setIdToValue(poEvenement, undefined);
//                         // l'état de l'appel repasse en cours
//                         $scope.changerEtatAppel(giIdEtatAppelEnCours);
//                     });
//                 }
//                 return;
//             }
//         }
//         poEvenement.save(function(piEvenementId, pbCreated) {
//             poEvenement.evenement_id = piEvenementId;
//             if(pbCreated){
//                 $scope.addEvtPlage(poEvenement);
//             }
//             // l'état de l'appel repasse en cours
//             $scope.changerEtatAppel(giIdEtatAppelEnCours);
//         });
// 	};
//
// 	/**
// 	 * Selon l'évenement récupère l'heure de départ ou l'heure d'arrivée et la convertie en timestamp
// 	 * pour la renseigner dans le champ correspondant (poEvenement.evenement_timestamp_depart ou poEvenement.evenement_timestamp_arrive).
// 	 *
// 	 * @param poEvenement l'evenement.
// 	 * @param poMomentDebutCours objet moment.js représentant la date de début du cours (nécessaire pour avoir la date du jour).
//      */
// 	$scope.mapToTimestamp = function (poEvenement, poMomentDebutCours) {
// 		// initalisation des heures selon l'heure courante et la date du cours
// 		if(poEvenement.fk_type_evt_id === $scope.oEvtType.giIdEvenementDepart) {
// 			var oEvenementTimestampDepart = moment(poMomentDebutCours, gsFormatTimestampWithoutTimeZone).hour(poEvenement.evenement_heure_depart.split(":")[0]).minute(poEvenement.evenement_heure_depart.split(":")[1]);
// 			poEvenement.evenement_timestamp_depart = oEvenementTimestampDepart.format(gsFormatTimestampWithoutTimeZone)
//
// 		} else if(poEvenement.fk_type_evt_id === $scope.oEvtType.giIdEvenementRetard) {
// 			var oEvenementTimestampArrive = moment(poMomentDebutCours, gsFormatTimestampWithoutTimeZone).hour(poEvenement.evenement_heure_arrivee.split(":")[0]).minute(poEvenement.evenement_heure_arrivee.split(":")[1]);
// 			poEvenement.evenement_timestamp_arrive = oEvenementTimestampArrive.format(gsFormatTimestampWithoutTimeZone);
// 		}
// 	};
//
// 	/**
// 	 * Supprime l'évenement d'un élève
// 	 *
// 	 * @param poEleve l'élève
// 	 * @param poEvenement évenement à supprimer
// 	 */
// 	$scope.supprimerEvenementEleve = function(poEleve, poEvenement) {
//         var otCours = $scope.currentEleve.courss.findWhere({cours_id : $scope.currentCours.cours_id});
//         var otPlage = $scope.currentEleve.plages.findWhere({heure : parseInt(moment(otCours.cours_timestamp_dt).format('HH'))});
//
// 		otPlage.evenements.remove(otPlage.evenements.findWhere({evenement_id : poEvenement.evenement_id}));
//         $scope.safeApply();
// 	};
//
// 	/**
// 	 * Ouverture d'un appel suite à la sélection d'une date
// 	 */
// 	$scope.selectAppel = function () {
// 		$scope.currentCours = undefined;
// 		$scope.ouvrirAppel($scope.appel.date);
// 	};
//
// 	/**
// 	 * Sélection d'un cours : Affiche le panel central de la liste des élèves
// 	 * @param cours l'objet cours sélectionné
//      */
// 	$scope.selectCours = function(cours) {
// 		$scope.currentCours = cours;
//
// 		// réinitialsiation des valeurs pour ne pas afficher le panel detail eleve lorsque l'on change d'appel.
// 		$scope.currentEleve = undefined;
// 		$scope.detailEleveOpen.displayed = false;
//
// 		// Recuperation de l'appel associé (création en mode Init s'il n'existe pas)
// 		$scope.currentCours.appel.sync();
//
// 		$scope.currentCours.eleves.sync();
//
// 		$scope.currentCours.eleves.on("appelSynchronized", function(){
// 			$scope.currentCours.nbPresents = 0;
// 			$scope.currentCours.eleves.each(function (oEleve) {
// 				oEleve.isAbsent = oEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementAbsence, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
// 				oEleve.hasDepart = oEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementDepart, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
// 				oEleve.hasIncident = oEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementIncident, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
// 				oEleve.hasRetard = oEleve.evenements.findWhere({fk_type_evt_id : $scope.oEvtType.giIdEvenementRetard, fk_appel_id : $scope.currentCours.appel.id}) !== undefined;
// 				oEleve.plages.sync($scope.currentCours.appel.id, function(){
//                     $scope.safeApply();
//                 });
// 			});
// 			$scope.currentCours.nbPresents = $scope.currentCours.eleves.all.length - (($scope.currentCours.eleves.where({isAbsent : true})).length);
// 			$scope.currentCours.nbEleves = $scope.currentCours.eleves.all.length;
// 			$scope.safeApply();
// 		});
// 	};
//
// 	$scope.lightboxAppel = function(){
// 		template.open('lightbox', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_help');
// 		$scope.lightbox.show = true;
// 	};
//
// 	$scope.selectCurrentCours = function(){
//         var currentCours = model.courss.filter(function(cours){
//             return (moment().diff(moment(cours.cours_timestamp_dt)) > 0) && (moment().diff(moment(cours.cours_timestamp_fn)) < 0);
//         });
//         if (currentCours.length === 0) return undefined;
//         else return currentCours[0];
// 	};
//
// 	/**
// 	 * Sélection d'un élève : affiche le panel de droit avec la saisie du retard/depart/punition eleve
// 	 * @param poEleve l'objet eleve sélectionné
//      */
// 	$scope.detailEleveAppel = function(poEleve) {
//         // template.close('rightSide_absc_eleve_appel_detail');
// 		$scope.initEvtTime();
// 		$scope.detailEleveOpen.displayed = $scope.currentEleve === undefined ||
// 				($scope.currentEleve !==undefined && $scope.currentEleve.eleve_id !== poEleve.eleve_id);
//
// 		if($scope.detailEleveOpen.displayed) {
// 			$scope.currentEleve = poEleve;
// 		} else {
// 			$scope.currentEleve = undefined;
// 			return;
// 		}
//
// 		var oEvenementRetard = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementRetard);
// 		if(oEvenementRetard === undefined) {
// 			oEvenementRetard = new Evenement();
// 			oEvenementRetard.evenement_saisie_cpe = false;
//             oEvenementRetard.cours_id = $scope.currentCours.cours_id;
// 			oEvenementRetard.fk_eleve_id = poEleve.eleve_id;
// 			oEvenementRetard.fk_appel_id = $scope.currentCours.appel.id;
// 			oEvenementRetard.fk_type_evt_id = $scope.oEvtType.giIdEvenementRetard;
// 			oEvenementRetard.fk_motif_id = $scope.oEvtType.giIdMotifSansMotif;
// 		}else{
// 			$scope.oEvtTime.retard = moment(oEvenementRetard.evenement_timestamp_arrive).format('HH:mm');
// 		}
//
// 		var oEvenementDepart = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementDepart);
// 		if(oEvenementDepart === undefined) {
// 			oEvenementDepart = new Evenement();
// 			oEvenementDepart.evenement_saisie_cpe = false;
//             oEvenementDepart.cours_id = $scope.currentCours.cours_id;
// 			oEvenementDepart.fk_eleve_id = poEleve.eleve_id;
// 			oEvenementDepart.fk_appel_id = $scope.currentCours.appel.id;
// 			oEvenementDepart.fk_type_evt_id = $scope.oEvtType.giIdEvenementDepart;
// 			oEvenementDepart.fk_motif_id = $scope.oEvtType.giIdMotifSansMotif;
// 		}else{
// 			$scope.oEvtTime.depart = moment(oEvenementDepart.evenement_timestamp_depart).format('HH:mm');
// 		}
//
// 		var oEvenementIncident = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementIncident);
// 		if(oEvenementIncident === undefined) {
// 			oEvenementIncident = new Evenement();
// 			oEvenementIncident.evenement_saisie_cpe = false;
//             oEvenementIncident.cours_id = $scope.currentCours.cours_id;
// 			oEvenementIncident.fk_eleve_id = poEleve.eleve_id;
// 			oEvenementIncident.fk_appel_id = $scope.currentCours.appel.id;
// 			oEvenementIncident.fk_type_evt_id = $scope.oEvtType.giIdEvenementIncident;
// 			oEvenementIncident.fk_motif_id = $scope.oEvtType.giIdMotifSansMotif;
// 		}
//
// 		var oEvenementObservation = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementObservation);
// 		if(oEvenementObservation === undefined) {
// 			oEvenementObservation = new Evenement();
//             oEvenementObservation.cours_id = $scope.currentCours.cours_id;
// 			oEvenementObservation.evenement_saisie_cpe = false;
// 			oEvenementObservation.fk_eleve_id = poEleve.eleve_id;
// 			oEvenementObservation.fk_appel_id = $scope.currentCours.appel.id;
// 			oEvenementObservation.fk_type_evt_id = $scope.oEvtType.giIdEvenementObservation;
// 			oEvenementObservation.fk_motif_id = $scope.oEvtType.giIdMotifSansMotif;
// 		}
//
// 		$scope.currentEleve.evenementObservation = oEvenementObservation;
// 		$scope.currentEleve.evenementDepart = oEvenementDepart;
// 		$scope.currentEleve.evenementRetard = oEvenementRetard;
// 		$scope.currentEleve.evenementIncident = oEvenementIncident;
//
// 		$scope.detailEleveOpen.displayed = true;
// 		// template.open('rightSide_absc_eleve_appel_detail', '../modules/' + gsPrefixAbsences + '/template/absc_teacher_appel_student-detail');
// 	};
//
// 	$scope.fermerDetailEleve = function() {
// 		$scope.currentEleve = undefined;
// 		// booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
// 		$scope.detailEleveOpen.displayed = false;
// 	};
//
// 	$scope.initEvtTime = function(){
// 		$scope.oEvtTime = {
// 			depart : '--:--',
// 			retard : '--:--'
// 		}
// 	};
//
// 	/**
// 	* Charge un appel
// 	* @param pdtDate la date du jour souhaitée
// 	*/
// 	$scope.ouvrirAppel = function (pdtDate) {
//
// 		// formatage en string
// 		//var sDateDebut = "10-02-2016";
// 		var sDateDebut = moment(pdtDate).format(gsFormatDate);
// 		$scope.appel.sDateDebut = sDateDebut;
//
// 		// calcul jour suivant
// 		var sDateFin = moment(pdtDate).add(1, 'days').format(gsFormatDate);
// 		$scope.appel.sDateFin = sDateFin;
//
// 		// booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
// 		$scope.detailEleveOpen.displayed = false;
//
// 		// chargement des cours de la journée de l'enseignant
// 		model.courss.sync(model.me.userId, $scope.appel.sDateDebut, $scope.appel.sDateFin);
//
// 		// chargement des eleves de  chaque cours
// 		model.courss.on('sync', function(){
// 			model.creneaus.sync();
// 			model.creneaus.on('sync', function(){
// 				$scope.creneaus = model.creneaus;
//                 var currentCours = $scope.selectCurrentCours();
//                 if(currentCours !== undefined){
//                     $scope.selectCours(currentCours);
//                 }
// 			});
// 		});
//
// 		$scope.courss = model.courss;
//
// 		model.plages.sync();
// 		$scope.plages = model.plages;
// 	};
//
// 	/**
// 	 * Passage de l'état d'un appel à "Fait"
// 	 */
// 	$scope.terminerAppel = function() {
// 		$scope.changerEtatAppel(giIdEtatAppelFait);
// 		$scope.currentCours.appel.update();
// 		$scope.succesMessage("L'appel a bien été marqué comme terminé", 5000);
// 	};
//
// 	$scope.succesMessage = function (psMessage, piDuration) {
// 		$scope.messageSuccess = psMessage;
// 		$scope.showSuccess = true;
// 		$timeout(function(){
// 			$scope.showSuccess = false;
// 		}, piDuration);
// 	};
//
// 	/**
// 	 * Change l'état d'un appel.
// 	 * @param piIdEtatAppel l'identifiant de l'état souhaité.
//      */
// 	$scope.changerEtatAppel = function (piIdEtatAppel) {
// 		$scope.currentCours.appel.fk_etat_appel_id = piIdEtatAppel;
// 		$scope.currentCours.appel.update();
// 		$scope.safeApply();
// 	};
//
// 	route({
// 		appel: function (params) {
// 			var dtToday = new Date();
// 			$scope.ouvrirAppel(dtToday);
//
// 			template.open('main', '../templates/' + gsPrefixAbsences + '/absc_teacher_appel');
// 		}
// 	});
// }
