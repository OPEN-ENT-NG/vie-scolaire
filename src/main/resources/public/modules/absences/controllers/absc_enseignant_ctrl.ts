import { template, ng } from 'entcore/entcore';
import { presences, Evenement } from '../models/absc_enseignant_mdl';
import { FORMAT } from '../constants/formats';
import {Classe} from "../models/teacher/Classe";

let moment = require('moment');
declare let _: any;

export let absencesController = ng.controller('AbsencesController', [
    '$scope', 'route', 'model', '$location', '$route',
    function ($scope, route, model, $location, $route) {
        const routesActions = {
            appel: (params) => {
                if (presences.structure !== undefined && presences.structure.isSynchronized) {
                    let dtToday = new Date();
                    $scope.ouvrirAppel(dtToday);
                    template.open('main', '../templates/absences/absc_teacher_appel');
                    $scope.safeApply();
                }
            },
            disabled : (params) => {
                template.open('main', '../templates/absences/absc_disabled_structure');
                $scope.safeApply();
            }
        };

        route(routesActions);

        $scope.template = template;


        $scope.etatAppel = {
            giIdEtatAppelInit : 1,
            giIdEtatAppelEnCours : 2,
            giIdEtatAppelFait : 3
        };

        $scope.show = {
            success : false,
            lightbox : false
        };

        $scope.oEvtType = {
            giIdEvenementAbsence : 1,
            giIdEvenementRetard : 2,
            giIdEvenementDepart : 3,
            giIdEvenementIncident : 4,
            giIdEvenementObservation : 5,
            giIdMotifSansMotif : 8
        };

        $scope.detailEleveOpen = {
            displayed : false
        };
        $scope.appel = {
            date	: {}
        };
        $scope.oEvtTime = {
            depart : '--:--',
            retard : '--:--'
        };

        template.open('absc_teacher_appel_eleves_container', '../templates/absences/absc_teacher_appel_eleves');
        $scope.safeApply = function(fn) {
            let phase = this.$root.$$phase;
            if (phase === '$apply' || phase === '$digest') {
                if (fn && (typeof(fn) === 'function')) {
                    fn();
                }
            } else {
                this.$apply(fn);
            }
        };

        $scope.formatDate = function(h) {
            return "00:00";
        };

        /**
         * Message pour les fonctionnalié pas encore développées
         */
        $scope.alertNonImplementee = function() {
            alert("Fonctionnalité actuellement non implémentée.");
        };

        /**
         * Calcule le nombre d'élèves présents et le renseigne dans $scope.currentCours.nbPresents
         */
        $scope.calculerNbElevesPresents = function() {
            $scope.currentCours.nbPresents = 0;
            let oElevesAbsents = $scope.currentCours.eleves.where({isAbsent : true});
            let iNbAbsents = 0;
            if (oElevesAbsents !== undefined) {
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
            $scope.currentEleve = poEleve;
            let evenementAbsence = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementAbsence);

            // creation absence
            if (evenementAbsence === undefined) {
                evenementAbsence = new Evenement();
                evenementAbsence.saisie_cpe = false;
                evenementAbsence.id_eleve = poEleve.id;
                evenementAbsence.id_appel = $scope.currentCours.appel.id;
                evenementAbsence.id_type = $scope.oEvtType.giIdEvenementAbsence;
                evenementAbsence.id_motif = $scope.oEvtType.giIdMotifSansMotif;
                evenementAbsence.id_cours = $scope.currentCours.id;

                evenementAbsence.create().then((piEvenement) => {
                    evenementAbsence.id = piEvenement.id;
                    poEleve.isAbsent = !poEleve.isAbsent;
                    poEleve.evenements.push(evenementAbsence);
                    $scope.removeEvtNAbsc(poEleve);
                    $scope.addEvtPlage(evenementAbsence);
                    // l'état de l'appel repasse en cours
                    $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                    $scope.calculerNbElevesPresents();
                });
                // suppression absence
            } else {
                evenementAbsence.delete().then(() => {
                    poEleve.isAbsent = false;
                    evenementAbsence.id = undefined;
                    poEleve.evenements.remove(evenementAbsence);
                    $scope.supprimerEvenementEleve(poEleve, evenementAbsence);
                    // l'état de l'appel repasse en cours
                    $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                    $scope.calculerNbElevesPresents();
                });
            }
        };

        /**
         *  Supprime les  évènements Retard, Départ et Incident si l'élève est déclaré absent.
         * @param poEleve Objet Eleve référencé
         */
        $scope.removeEvtNAbsc = function(poEleve) {
            let tEvenementDepart = poEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementDepart});
            let tEvenementRetard = poEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementRetard});
            let tEvenementIncident = poEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementIncident});
            if (tEvenementDepart !== undefined) {
                poEleve.evenements.remove(tEvenementDepart);
                tEvenementDepart.delete();
                $scope.supprimerEvenementEleve(poEleve, tEvenementDepart);
                if (poEleve.evenementDepart !== undefined) {
                    poEleve.evenementDepart.id = undefined;
                    poEleve.hasDepart = false;
                }
            }
            if (tEvenementRetard !== undefined) {
                poEleve.evenements.remove(tEvenementRetard);
                tEvenementRetard.delete();
                $scope.supprimerEvenementEleve(poEleve, tEvenementRetard);
                if (poEleve.evenementRetard !== undefined) {
                    poEleve.evenementRetard.id = undefined;
                    poEleve.hasRetard = false;
                }
            }
            if (tEvenementIncident !== undefined) {
                poEleve.evenements.remove(tEvenementIncident);
                tEvenementIncident.delete();
                $scope.supprimerEvenementEleve(poEleve, tEvenementIncident);
                if (poEleve.evenementIncident !== undefined) {
                    poEleve.evenementIncident.id = undefined;
                    poEleve.hasIncident = false;
                }
            }
            $scope.safeApply();
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
            return  poEleve.evenements.findWhere({id_type : parseInt(piTypeEvenement)});
        };


        /**
         * Action de check d'un évenement de retard/départ/incident sur un élève.
         * Si m'on coche la checkbox, créé l'évenement en base s'il n'existe pas encore.
         * Si l'on décoche la checkbox, supprime l'évenement de la liste des évenements de l'élève.
         * @param pbIsChecked booleen permettant de savoir si la checbox est cochée ou non.
         * @param poEvenement l'évenement.
         */
        $scope.checkEvenement = function (pbIsChecked, poEvenement) {
            if (pbIsChecked) {
                let  evenementAbsence = $scope.getEvenementEleve($scope.currentEleve, $scope.oEvtType.giIdEvenementAbsence);
                if (evenementAbsence !== undefined) {
                    $scope.ajouterEvenementAbsence($scope.currentEleve);
                }
                let oMomentDebutCours = moment($scope.currentCours.cours_timestamp_dt);
                let sHeureAujourDhui = moment().format("HH:mm");

                // initalisation des heures selon l'heure courante et la date du cours
                if (poEvenement.id_type === $scope.oEvtType.giIdEvenementDepart) {
                    poEvenement.timestamp_depart = sHeureAujourDhui;
                    $scope.oEvtTime.depart = sHeureAujourDhui;
                } else if (poEvenement.id_type === $scope.oEvtType.giIdEvenementRetard) {
                    poEvenement.timestamp_arrive = sHeureAujourDhui;
                    $scope.oEvtTime.retard = sHeureAujourDhui;
                }

                $scope.mapToTimestamp(poEvenement, oMomentDebutCours);

                poEvenement.save().then( (pnEvenement) => {
                    $scope.setIdToValue(poEvenement, pnEvenement.id);
                    poEvenement.id = pnEvenement.id;
                    $scope.addEvtPlage(poEvenement);
                    $scope.currentEleve.evenements.push(poEvenement);
                    $scope.currentEleve.evenementsJours.push(poEvenement);
                    // l'état de l'appel repasse en cours
                    $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                });
            }else {
                poEvenement.delete().then( () => {
                    if (poEvenement.id_type === $scope.oEvtType.giIdEvenementDepart) {
                        $scope.oEvtTime.depart = "--:--";
                    } else if (poEvenement.id_type === $scope.oEvtType.giIdEvenementRetard) {
                        $scope.oEvtTime.retard = "--:--";
                    }
                    $scope.supprimerEvenementEleve($scope.currentEleve, poEvenement);
                    poEvenement.id = undefined;
                    $scope.setIdToValue(poEvenement, undefined);
                    // l'état de l'appel repasse en cours
                    $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                });
            }
        };

        $scope.saisieCpe = function(psAppelID, oEleve, iTypeEvt) {
            let o = oEleve.evenements.findWhere({id_appel : psAppelID, id_type : iTypeEvt});
            if (o !== undefined) {
                return o.saisie_cpe;
            }
        };

        $scope.addEvtPlage = function(poEvt) {
            let otCours = $scope.currentEleve.courss.findWhere({id : $scope.currentCours.id});
            let otPlage = $scope.currentEleve.plages.findWhere({heure : parseInt(moment(otCours.timestamp_dt).format('HH'))});

            otPlage.evenements.push(poEvt);
            $scope.safeApply();
        };

        $scope.setIdToValue = function(poEvenement, poValue) {
            switch (poEvenement.id_type) {
                case $scope.oEvtType.giIdEvenementDepart :
                    $scope.currentEleve.evenementDepart.id = poValue;
                    break;
                case $scope.oEvtType.giIdEvenementRetard :
                    $scope.currentEleve.evenementRetard.id = poValue;
                    break;
                case $scope.oEvtType.giIdEvenementObservation :
                    $scope.currentEleve.evenementObservation.id = poValue;
                    break;
            }
        };

        /**
         * Met à jour un évenement en BDD
         * @param poEvenement l'évenement.
         * @param poUpdatedField nouveau champs
         */
        $scope.updateEvenement = function(poEvenement, poUpdatedField) {
            if (poUpdatedField !== 'commentaire') {
                let oMomentDebutCours = moment($scope.currentCours.timestamp_dt);
                $scope.mapToTimestamp(poEvenement, oMomentDebutCours);
            } else {
                if (poEvenement[poUpdatedField] === '' || poEvenement[poUpdatedField] === null || poEvenement[poUpdatedField] === undefined) {
                    console.log($scope.oEvtTime);
                    if (poEvenement.id !== undefined) {
                        poEvenement.delete(function() {
                            $scope.supprimerEvenementEleve($scope.currentEleve, poEvenement);
                            $scope.setIdToValue(poEvenement, undefined);
                            // l'état de l'appel repasse en cours
                            $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                        });
                    }
                    return;
                }
            }
            poEvenement.save(function(piEvenementId, pbCreated) {
                poEvenement.id = piEvenementId;
                if (pbCreated) {
                    $scope.addEvtPlage(poEvenement);
                }
                // l'état de l'appel repasse en cours
                $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
            });
        };

        /**
         * Selon l'évenement récupère l'heure de départ ou l'heure d'arrivée et la convertie en timestamp
         * pour la renseigner dans le champ correspondant (poEvenement.timestamp_depart ou poEvenement.timestamp_arrive).
         *
         * @param poEvenement l'evenement.
         * @param poMomentDebutCours objet moment.js représentant la date de début du cours (nécessaire pour avoir la date du jour).
         */
        $scope.mapToTimestamp = function (poEvenement, poMomentDebutCours) {
            // initalisation des heures selon l'heure courante et la date du cours
            if (poEvenement.id_type === $scope.oEvtType.giIdEvenementDepart) {
                let oEvenementTimestampDepart = moment(poMomentDebutCours, FORMAT.timestamp).hour(poEvenement.timestamp_depart.split(":")[0]).minute(poEvenement.timestamp_depart.split(":")[1]);
                poEvenement.timestamp_depart = oEvenementTimestampDepart.format(FORMAT.timestamp);

            } else if (poEvenement.id_type === $scope.oEvtType.giIdEvenementRetard) {
                let oEvenementTimestampArrive = moment(poMomentDebutCours, FORMAT.timestamp).hour(poEvenement.timestamp_arrive.split(":")[0]).minute(poEvenement.timestamp_arrive.split(":")[1]);
                poEvenement.timestamp_arrive = oEvenementTimestampArrive.format(FORMAT.timestamp);
            }
        };

        /**
         * Supprime l'évenement d'un élève
         *
         * @param poEleve l'élève
         * @param poEvenement évenement à supprimer
         */
        $scope.supprimerEvenementEleve = function(poEleve, poEvenement) {
            let otCours = $scope.currentEleve.courss.findWhere({id : $scope.currentCours.id});
            let otPlage = $scope.currentEleve.plages.findWhere({heure : parseInt(moment(otCours.timestamp_dt).format('HH'))});

            otPlage.evenements.remove(otPlage.evenements.findWhere({id : poEvenement.id}));
            $scope.safeApply();
        };

        /**
         * Ouverture d'un appel suite à la sélection d'une date
         */
        $scope.selectAppel = function () {
            $scope.currentCours = undefined;
            // par defaut on se positionne sur la vue classique de tous les eleves
            $scope.bClassesVue = false;
            $scope.ouvrirAppel($scope.appel.date);
        };

        $scope.refreshVuesAppel = function() {
            //fermeture ouverture du template pour rafraichir la vue
            template.close('absc_teacher_appel_eleves_container');
            $scope.safeApply();
            template.open('absc_teacher_appel_eleves_container', '../templates/absences/absc_teacher_appel_eleves');
            $scope.safeApply();
        };

        /**
         * Affiche la vue classes ou eleves
         * @param bShowOrHide true affiche la vue classes, false affiche la vue eleves
         */
        $scope.showVueClasses = function(bShowOrHide) {
            $scope.bClassesVue = bShowOrHide;
            $scope.refreshVuesAppel();
        };

        /**
         * Sélection d'un cours : Affiche le panel central de la liste des élèves
         * @param cours l'objet cours sélectionné
         */
        $scope.selectCours = function(cours) {

            $scope.currentCours = cours;

            $scope.currentCours.classe = $scope.structure.classes.findWhere({id : $scope.currentCours.id_classe});

            // réinitialsiation des valeurs pour ne pas afficher le panel detail eleve lorsque l'on change d'appel.
            $scope.currentEleve = undefined;
            $scope.detailEleveOpen.displayed = false;

            // Recuperation de l'appel associé (création en mode Init s'il n'existe pas)
            $scope.currentCours.appel.sync();

            $scope.currentCours.eleves.sync().then(() => {

                $scope.currentCours.nbPresents = 0;
                $scope.currentCours.on('appelSynchronized', function () {
                    $scope.currentCours.eleves.each(function (oEleve) {
                        oEleve.isAbsent = oEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementAbsence, id_appel : $scope.currentCours.appel.id}) !== undefined;
                        oEleve.hasDepart = oEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementDepart, id_appel : $scope.currentCours.appel.id}) !== undefined;
                        oEleve.hasIncident = oEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementIncident, id_appel : $scope.currentCours.appel.id}) !== undefined;
                        oEleve.hasRetard = oEleve.evenements.findWhere({id_type : $scope.oEvtType.giIdEvenementRetard, id_appel : $scope.currentCours.appel.id}) !== undefined;
                        oEleve.plages.sync($scope.currentCours.appel.id, function() {
                            $scope.safeApply();
                        });

                    });
                    $scope.currentCours.nbPresents = $scope.currentCours.eleves.all.length - (($scope.currentCours.eleves.where({isAbsent : true})).length);
                    $scope.currentCours.nbEleves = $scope.currentCours.eleves.all.length;
                    $scope.safeApply();
                });

                // si le cours sélectionné est un groupe d'enseignement, alors, on regroupe les élèves par classe
                if ($scope.currentCours.classe.type_groupe === 1) {
                    $scope.currentCours.classesOfGroup =
                        _.groupBy($scope.currentCours.eleves.all, function (oEleve) {
                            return oEleve.className;
                        });
                    // on positionne sur la vue par classes par défaut
                    $scope.bClassesVue = true;
                }

                $scope.refreshVuesAppel();
            });
        };

        $scope.lightboxAppel = function() {
            template.open('lightbox', '../templates/absences/absc_teacher_help');
            $scope.show.lightbox = true;
        };

        $scope.selectCurrentCours = function() {
            let currentCours = $scope.structure.courss.filter(function(cours) {
                return (moment().diff(moment(cours.timestamp_dt)) > 0) && (moment().diff(moment(cours.timestamp_fn)) < 0);
            });
            if (currentCours.length === 0) { return undefined; }
            else { return currentCours[0]; }
        };

        /**
         * Sélection d'un élève : affiche le panel de droit avec la saisie du retard/depart/punition eleve
         * @param poEleve l'objet eleve sélectionné
         */
        $scope.detailEleveAppel = function(poEleve) {
            // template.close('rightSide_absc_eleve_appel_detail');
            $scope.initEvtTime();
            $scope.detailEleveOpen.displayed = $scope.currentEleve === undefined ||
                ($scope.currentEleve !== undefined && $scope.currentEleve.id_eleve !== poEleve.id_eleve);

            if ($scope.detailEleveOpen.displayed) {
                $scope.currentEleve = poEleve;
            } else {
                $scope.currentEleve = undefined;
                return;
            }

            let oEvenementRetard = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementRetard);
            if (oEvenementRetard === undefined) {
                oEvenementRetard = new Evenement();
                oEvenementRetard.saisie_cpe = false;
                oEvenementRetard.id_cours = $scope.currentCours.id;
                oEvenementRetard.id_eleve = poEleve.id;
                oEvenementRetard.id_appel = $scope.currentCours.appel.id;
                oEvenementRetard.id_type = $scope.oEvtType.giIdEvenementRetard;
                oEvenementRetard.id_motif = $scope.oEvtType.giIdMotifSansMotif;
            } else {
                $scope.oEvtTime.retard = moment(oEvenementRetard.timestamp_arrive).format('HH:mm');
            }

            let oEvenementDepart = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementDepart);
            if (oEvenementDepart === undefined) {
                oEvenementDepart = new Evenement();
                oEvenementDepart.saisie_cpe = false;
                oEvenementDepart.id_cours = $scope.currentCours.id;
                oEvenementDepart.id_eleve = poEleve.id;
                oEvenementDepart.id_appel = $scope.currentCours.appel.id;
                oEvenementDepart.id_type = $scope.oEvtType.giIdEvenementDepart;
                oEvenementDepart.id_motif = $scope.oEvtType.giIdMotifSansMotif;
            } else {
                $scope.oEvtTime.depart = moment(oEvenementDepart.timestamp_depart).format('HH:mm');
            }

            let oEvenementIncident = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementIncident);
            if (oEvenementIncident === undefined) {
                oEvenementIncident = new Evenement();
                oEvenementIncident.saisie_cpe = false;
                oEvenementIncident.id_cours = $scope.currentCours.id;
                oEvenementIncident.id_eleve = poEleve.id;
                oEvenementIncident.id_appel = $scope.currentCours.appel.id;
                oEvenementIncident.id_type = $scope.oEvtType.giIdEvenementIncident;
                oEvenementIncident.id_motif = $scope.oEvtType.giIdMotifSansMotif;
            }

            let oEvenementObservation = $scope.getEvenementEleve(poEleve, $scope.oEvtType.giIdEvenementObservation);
            if (oEvenementObservation === undefined) {
                oEvenementObservation = new Evenement();
                oEvenementObservation.id_cours = $scope.currentCours.id;
                oEvenementObservation.saisie_cpe = false;
                oEvenementObservation.id_eleve = poEleve.id;
                oEvenementObservation.id_appel = $scope.currentCours.appel.id;
                oEvenementObservation.id_type = $scope.oEvtType.giIdEvenementObservation;
                oEvenementObservation.id_motif = $scope.oEvtType.giIdMotifSansMotif;
            }

            $scope.currentEleve.evenementObservation = oEvenementObservation;
            $scope.currentEleve.evenementDepart = oEvenementDepart;
            $scope.currentEleve.evenementRetard = oEvenementRetard;
            $scope.currentEleve.evenementIncident = oEvenementIncident;

            $scope.detailEleveOpen.displayed = true;
        };

        $scope.fermerDetailEleve = function() {
            $scope.currentEleve = undefined;
            // booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
            $scope.detailEleveOpen.displayed = false;
        };

        $scope.initEvtTime = function() {
            $scope.oEvtTime = {
                depart : '--:--',
                retard : '--:--'
            };
        };

        /**
         * Charge un appel
         * @param pdtDate la date du jour souhaitée
         */
        $scope.ouvrirAppel = async function (pdtDate) {

            // formatage en string
            $scope.appel.sDateDebut = moment(pdtDate).format(FORMAT.date);

            // calcul jour suivant
            $scope.appel.sDateFin =  moment(pdtDate).add(1, 'days').format(FORMAT.date);

            // booleen pour savoir si la partie droite de la vue est affichée (saisie retard/depart/punition eleve)
            $scope.detailEleveOpen.displayed = false;

            // chargement des cours de la journée de l'enseignant
            await $scope.structure.courss.sync($scope.appel.sDateDebut, $scope.appel.sDateFin);
            await $scope.structure.creneaus.sync();
            let currentCours = $scope.selectCurrentCours();
            if (currentCours !== undefined) {
                $scope.selectCours(currentCours);
            }

            // $scope.structure.plages.sync();
        };

        /**
         * Passage de l'état d'un appel à "Fait"
         */
        $scope.terminerAppel = function() {
            $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelFait);
            $scope.succesMessage("L'appel a bien été marqué comme terminé", 5000);
        };

        $scope.succesMessage = function (psMessage, piDuration) {
            $scope.messageSuccess = psMessage;
            $scope.showSuccess = true;
            setTimeout(function() {
                $scope.showSuccess = false;
            }, piDuration);
        };

        /**
         * Récupère le libelle d'une classe.
         * @param idClasse l'identifiant de la classe.
         */
        $scope.getLibelleClasse = function (idClasse) {
            let classe = $scope.structure.classes.findWhere({id : idClasse});
            if (classe !== undefined) {
                return classe.name;
            } else {
                console.log("Class not found : " + idClasse);
                return " ";
            }
        };

        /**
         * Change l'état d'un appel.
         * @param piIdEtatAppel l'identifiant de l'état souhaité.
         */
        $scope.changerEtatAppel = function (piIdEtatAppel) {
            $scope.currentCours.appel.id_etat = piIdEtatAppel;
            $scope.currentCours.appel.update();
            $scope.safeApply();
        };

        /**
         * Convertit une date au format local: [FRANCE].
         * @param dateStr.
         */
        $scope.localeDate = function (dateStr) {
            return moment(new Date(dateStr)).format('DD-MM-YYYY');
        };

        let getCurrentAction = function (): string {
            return $route.current.$$route.action;
        };

        let executeAction = function (): void {
            routesActions[getCurrentAction()]($route.current.params);
        };

        presences.structures.sync().then(() => {
            if (!presences.structures.empty()) {
                $scope.structure = presences.structures.first();
                presences.structure = $scope.structure;
                presences.structure.sync().then(() => {
                    $scope.courss = presences.structure.courss;
                    $scope.creneaus = presences.structure.creneaus;
                    $scope.plages = presences.structure.plages;
                    if ($location.path() === '/disabled') {
                        $location.path('/appel');
                        $location.replace();
                    } else {
                        executeAction();
                    }
                });
            } else {
                $location.path() === '/disabled' ?
                    executeAction() :
                    $location.path('/disabled');
                $location.replace();
            }
        });

    }
]);