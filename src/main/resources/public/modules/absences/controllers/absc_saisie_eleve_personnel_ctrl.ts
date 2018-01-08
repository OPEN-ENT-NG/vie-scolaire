/**
 * Created by rahnir on 08/06/2017.
 */
import { template, ng } from 'entcore';
import * as utils from '../utils/personnel';
import {AbsencePrev} from "../models/personnel/AbsencePrev";
import {Evenement} from "../models/personnel/Evenement";
import {safeApply} from "../../utils/functions/safeApply";
import {Eleve} from "../models/personnel/Eleve";



let moment = require('moment');
declare let _: any;

export let abscSaisieElevePersonnel = ng.controller('AbscSaisieElevePersonnel', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        // variables d'affichage
        $scope.display.calendar = false;
        $scope.display.creneau = false;

        $scope.display.showEleveCard = false;
        $scope.display.showLastAbsences = false;

        $scope.inputHasChanged = false;
        $scope.isRefreshingCalendar = false;

        $scope.timelineIsVisible = true;
        $scope.numOfConflit = 0;


        // Selection d'un élève dans la barre de recherche
        $scope.selectEleve = async () => {
            $scope.selectingEleve = true;
            try {
                $scope.selected.dateDb = moment(model.calendar.dayForWeek).hour(0).minute(0).format('YYYY-MM-DD');
                $scope.selected.dateFn = moment(model.calendar.dayForWeek).add(6, 'day').hour(0).minute(0).format('YYYY-MM-DD');
                $scope.selected.timeDb = moment().format("HH:mm");
                $scope.selected.timeFn = moment().format("HH:mm");
                $scope.selected.motif = $scope.structure.motifs.all.find(m => m.id==null);

                $scope.structure.isWidget = false;
                $scope.display.calendar = false;

                $scope.selected.eleve = await $scope.syncEleveCours($scope.selected.eleve);
                $scope.selected.eleve.cours ? $scope.display.calendar = true : $scope.display.calendar = false;

                // On récupère les absences et les absences prev de l'élève
                await $scope.selected.eleve.syncAllAbsence(false);
                await $scope.selected.eleve.syncAllAbsencePrev();
                await $scope.selected.eleve.syncEvenement($scope.selected.dateDb, $scope.selected.dateFn);

                // On les mets en formes
                $scope.selected.eleve.absencesToShow = $scope.getAbsencesToShow();

                $scope.display.showEleveCard = true;
                $scope.display.showLastAbsences = true;
                $scope.checkNavigDate();

                $scope.selectingEleve = false;
                $scope.refreshTheCalendar();
                $scope.display.calendar = true;
                $scope.display.creneau = true;
                utils.safeApply($scope);
            }catch(e){
                console.log(e);
            }
        };

        //sync les [classes - groupes - cours ] d'eleve
        $scope.syncEleveCours = async (eleve) => {
            console.log("syncEleveCours");
            let i = _.indexOf($scope.structure.eleves.all, eleve);
            if($scope.structure.eleves.all[i].synchronized.className == false ) {
                await $scope.structure.eleves.all[i].syncClasseGroupName($scope.structure.classes.all,'classe');
            }
            if($scope.structure.eleves.all[i].synchronized.groupName == false ){
                await  $scope.structure.eleves.all[i].syncClasseGroupName($scope.structure.classes.all,'group');
            }
            //await $scope.selectPeriode() ;
            var startMoment = $scope.getSelectedPeriodStartMoment();
            var endMoment = $scope.getSelectedPeriodEndMoment();
            await $scope.selected.eleve.checkRapprochementCours(startMoment, endMoment, $scope.structure);
            return $scope.structure.eleves.all[i] ;
        };


        /**
         * Retourne les absences et absences prev mises en forme pour la partie 'Dernière absences saisies'
         * @returns {any}
         */
        $scope.getAbsencesToShow = () => {
            // Mise en forme des absence
            $scope.selected.eleve.absences.forEach(function (event) {
                event.motif = $scope.structure.motifs.find(motif => motif.id == event.id_motif);
                event.niceDateDebut = moment(event.timestamp_dt).format('DD/MM/YYYY HH:mm');
                event.niceDateFin = moment(event.timestamp_fn).format('DD/MM/YYYY HH:mm');
            });

            // Mise en forme des absences prev + lien entre absences prev et absences normales
            $scope.selected.eleve.abscprev.forEach(function (abscprev) {
                // On ajoute un tableau pour y mettre les absences contenues dans la période de l'absence prévisionelle
                abscprev.absences = [];

                // Boolean permettant de différencier l'absence prev de l'absence normal pour un affichage dynamique
                abscprev.isAbsencePrev = true;

                // Mise en forme de la date
                abscprev.niceDateDebut = moment(abscprev.timestamp_dt).format('DD/MM/YYYY HH:mm');
                abscprev.niceDateFin = moment(abscprev.timestamp_fn).format('DD/MM/YYYY HH:mm');
                abscprev.motif = $scope.structure.motifs.find(motif => motif.id == abscprev.id_motif);

                $scope.selected.eleve.absences.forEach(function (absence) {
                    // Si l'absence normale est à l'intérieur de la période de l'absence prev
                    if (!absence.alreadyInAbscPrev && moment(abscprev.timestamp_dt) < moment(absence.timestamp_dt)
                        && moment(absence.timestamp_fn) < moment(abscprev.timestamp_fn)) {
                        // On ajoute un booléen pour montrer qu'on a déjà rattaché cette absence à une absence prev
                        absence.alreadyInAbscPrev = true;
                        // On ajoute cette absence normale aux absences de l'absence prev
                        abscprev.absences.push(absence);
                    }
                });
            });

            // On rassemble les absences non rattachées aux absences prev afin de les afficher dynamiquement
            var absencesAlone = $scope.selected.eleve.absences.filter(event => !event.alreadyInAbscPrev);
            var absencesToShow = $scope.selected.eleve.abscprev.concat(absencesAlone);

            // On retrie par date
            absencesToShow.sort(function (a, b) {
                return new Date(b.timestamp_dt) - new Date(a.timestamp_dt);
            });

            return absencesToShow;
        };


        $scope.refreshTheCalendar = async () => {
            $scope.isRefreshingCalendar = true;
            var startMoment = $scope.getSelectedPeriodStartMoment();
            var endMoment = $scope.getSelectedPeriodEndMoment();

            await $scope.selected.eleve.checkRapprochementCours(startMoment, endMoment, $scope.structure);

            let firstweek = moment($scope.selected.dateDb);
            model.calendar.setDate(firstweek);
            $scope.loadItems();
            $scope.checkNavigDate();
            $scope.calculConflit();
            $scope.isRefreshingCalendar = false;
            $scope.inputHasChanged = false;
        };


        $scope.$watch("selected.dateDb",  (newVal, oldVal) => {
            if($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve)
                return;

            var newDateDebut = $scope.getSelectedPeriodStartMoment();
            var dateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateDebut > dateFin) {
                $scope.selected.dateDb = oldVal;
            } else{
                $scope.inputHasChanged = true;
            }
        });

        $scope.$watch("selected.timeDb",  (newVal, oldVal) => {
            if($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve)
                return;

            var newDateDebut = $scope.getSelectedPeriodStartMoment();
            var dateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateDebut > dateFin) {
                $scope.selected.timeDb = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.$watch("selected.timeFn",  (newVal, oldVal) => {
            if($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve)
                return;

            var dateDebut = $scope.getSelectedPeriodStartMoment();
            var newDateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateFin < dateDebut) {
                $scope.selected.timeFn = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.$watch("selected.dateFn",  (newVal, oldVal) => {
            console.log("watch selected.dateFn");
            if($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve){
                return;
            }

            var dateDebut = $scope.getSelectedPeriodStartMoment();
            var newDateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateFin < dateDebut){
                $scope.selected.dateFn = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.selectMotif = () => {
            if($scope.selected.motif == null){
                $scope.selected.motif = {id: null};
            }
            $scope.inputHasChanged = true;
            $scope.calculConflit();
        };

        $scope.loadItems = () => {
            if(!$scope.selected.eleve)
                return;

            var abscPrevItem = $scope.getArrayItemsAP();

            $scope.selected.eleve.items = [];
            if($scope.selected.eleve.cours)
                $scope.selected.eleve.items = $scope.selected.eleve.items.concat($scope.selected.eleve.cours);
            if(abscPrevItem)
                $scope.selected.eleve.items = $scope.selected.eleve.items.concat(abscPrevItem);

            model.calendar.addScheduleItems($scope.selected.eleve.items);
        };

        $scope.getArrayItemsAP = () => {
            // On vérifie si il y aura une absence prev, si c'est le cas, on lance la calcul des conflits avec les Absences prev
            var dateDebutPeriode = $scope.getSelectedPeriodStartMoment();
            var dateFinPeriode = $scope.getSelectedPeriodEndMoment();

            // Si la date de fin est dans le futur, alors il y a une absence prev
            if(dateFinPeriode > moment()) {
                var startMoment = dateDebutPeriode > moment() ? dateDebutPeriode : moment();
                var endMoment = dateFinPeriode;

                var numEnglobe = 0;
                var numInside = 0;
                var numTouching = 0;
                var numNotTouching = 0;

                // On trie les cours par ordre chronologique
                $scope.selected.eleve.cours.sort(function (a, b) {
                    return new Date(a.startMoment) - new Date(b.startMoment);
                });

                // Découpage de l'absence prev en item pour le calendar
                var abscPrevRaw = {
                    dateDebut: startMoment,
                    dateFin: endMoment
                };

                var arrayOfItemAP = [];
                arrayOfItemAP.push(abscPrevRaw);

                $scope.selected.eleve.cours.forEach(cours => {
                    var lastAP = arrayOfItemAP.pop();
                    cours.dateDebut = moment(cours.startMoment);
                    cours.dateFin = moment(cours.endMoment);

                    if (lastAP.dateDebut < cours.dateDebut && cours.dateFin < lastAP.dateFin) { // Englobe
                        numEnglobe++;

                        var abscPrevPart1 = {
                            dateDebut: lastAP.dateDebut,
                            dateFin: cours.dateDebut
                        };
                        var abscPrevToPart2 = {
                            dateDebut: cours.dateFin,
                            dateFin: lastAP.dateFin
                        };

                        arrayOfItemAP.push(abscPrevPart1);
                        arrayOfItemAP.push(abscPrevToPart2);

                    } else if (cours.dateDebut < lastAP.dateDebut && lastAP.dateFin < cours.dateFin) { // Inside  // Coupe
                        numInside++;
                    } else if (lastAP.dateFin < cours.dateDebut || lastAP.dateDebut > cours.dateFin) { // Touche pas
                        numNotTouching++;
                        arrayOfItemAP.push(lastAP);
                    } else { // Touche
                        numTouching++;

                        if (cours.dateFin < lastAP.dateFin) {               // Le cours touche le début de l'AP
                            lastAP.dateDebut = cours.dateFin;
                        } else if (cours.dateDebut > lastAP.dateDebut) {    // Le cours touche la fin de l'AP
                            lastAP.dateFin = cours.dateDebut;
                        }
                        arrayOfItemAP.push(lastAP);
                    }
                });

                // Mise en forme des parties de l'absence prev pour le calendar
                arrayOfItemAP.forEach(item => {
                    item.isAbscPrev = true;
                    item.locked = true;
                    item.is_periodic = false;

                    item.startMoment = item.dateDebut;
                    item.startCalendarHour = item.startMoment.seconds(0).millisecond(0).toDate();
                    item.startMomentDate = item.startMoment.format('DD/MM/YYYY');
                    item.startMomentTime = item.startMoment.format('hh:mm');

                    item.endMoment = item.dateFin;
                    item.endCalendarHour = item.endMoment.seconds(0).millisecond(0).toDate();
                    item.endMomentDate = item.endMoment.format('DD/MM/YYYY');
                    item.endMomentTime = item.endMoment.format('hh:mm');
                });

                return arrayOfItemAP;
            }
        };

        $scope.calculConflit = async () => {
            console.log("calculConflit zzzz");
            if(!$scope.selected.eleve || !$scope.selected.motif
                || !$scope.selected.dateDb  || !$scope.selected.dateFn
                || !$scope.selected.timeDb  || !$scope.selected.timeFn)
                return;

            $scope.selected.eleve.arrayConflitAbscPrev = [];
            $scope.selected.eleve.arrayAbscPrevToCreate = [];
            $scope.selected.eleve.arrayAbscPrevToDelete = [];
            $scope.selected.eleve.arrayAbscPrevToUpdate = [];
            $scope.selected.eleve.coursPassedWithoutAbsence = [];
            $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent = [];

            var dateDebut = $scope.getSelectedPeriodStartMoment();
            var dateFin = $scope.getSelectedPeriodEndMoment();
            var idMotif = $scope.selected.motif.id;

            await $scope.structure.appels.sync(dateDebut.format("YYYY-MM-DD"), dateFin.format("YYYY-MM-DD"));

            $scope.numOfConflit = 0;

            // Lien entre les cours et les absences
            $scope.selected.eleve.cours.forEach(cours => {
                cours.appel = $scope.structure.appels.all.find(x => x.id_cours === cours._id);
                if(cours.appel && $scope.selected.eleve.absences)
                    cours.absence = $scope.selected.eleve.absences.find(absences => absences.id_appel === cours.appel.id);
            });

            // Group
            $scope.selected.eleve.cours.sort(function (a, b) {
                return new Date(a.startMoment) - new Date(b.startMoment);
            });

            var group_to_values = $scope.selected.eleve.cours.reduce(function (obj, item) {
                obj[item.startMomentDate] = obj[item.startMomentDate] || [];
                obj[item.startMomentDate].push(item);
                return obj;
            }, {});
            var groups = Object.keys(group_to_values).map(function (key) {
                return {date: key, cours: group_to_values[key]};
            });
            groups.sort(function (a, b) {
                return new Date(a.date) - new Date(b.date);
            });

            $scope.selected.eleve.coursGroupByDay = groups;

            $scope.selected.eleve.coursWithConflit = $scope.selected.eleve.cours.filter((cours) => {
                // Si le cours est passé et n'a pas d'absence, ou si le cours est passé avec une absence avec un motif différent.
                return !$scope.isAfterToday(cours.startMoment) && (!cours.absence || !$scope.equalsMotifIdOfPa(cours.absence.motif.id));
            });

            $scope.selected.eleve.coursPassedWithoutAbsence =
                $scope.selected.eleve.cours.filter(cours => !$scope.isAfterToday(cours.startMoment) && !cours.absence);

            $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent =
                $scope.selected.eleve.cours.filter(cours => !$scope.isAfterToday(cours.startMoment) && cours.absence && !$scope.equalsMotifIdOfPa(cours.absence.motif.id) );

            // On vérifie si il y aura une absence prev, si c'est le cas, on lance la calcul des conflits avec les Absences prev
            var dateDebutPeriode = moment(moment($scope.selected.dateDb).format("YYYY-MM-DD") + " " + $scope.selected.timeDb);
            var dateFinPeriode = moment(moment($scope.selected.dateFn).format("YYYY-MM-DD") + " " + $scope.selected.timeFn);

            var newAbscPrevToCreate;
            // Si la date de fin est dans le futur, alors il y a une absence prev
            var arrayConflitAbscPrev = [];
            if(dateFinPeriode > moment()) {
                var startMoment = dateDebutPeriode > moment() ? dateDebutPeriode : moment();
                var endMoment = dateFinPeriode;

                newAbscPrevToCreate = {
                    dateDebut: startMoment,
                    dateFin: endMoment,
                    id_motif: idMotif
                };

                // ### CALCUL CONFLIT AVEC LES ABSENCE PREV
                var arrayAbscPrevToUpdate = [];
                var arrayAbscPrevToDelete = [];
                var arrayAbscPrevToCreate = [];

                var isInsideExistingAbscPrev = false;
                var isProlonged = false;


                if($scope.selected.eleve.abscprev) {
                    $scope.selected.eleve.abscprev.forEach(abscPrev => {
                        abscPrev.dateDebut = moment(abscPrev.timestamp_dt);
                        abscPrev.dateFin = moment(abscPrev.timestamp_fn);

                        if (dateDebut < abscPrev.dateDebut && abscPrev.dateFin < dateFin) { // Englobe
                            isInsideExistingAbscPrev = true;
                        } else if (abscPrev.dateDebut < dateDebut && dateFin < abscPrev.dateFin) { // Inside
                            arrayAbscPrevToDelete.push(abscPrev);
                            if (abscPrev.id_motif == idMotif) {
                                isProlonged = true;
                                newAbscPrevToCreate.dateDebut = abscPrev.dateDebut;
                                newAbscPrevToCreate.dateFin = abscPrev.dateFin;
                            } else { // Coupe
                                var abscPrevToCreate1 = {
                                    dateDebut: abscPrev.dateDebut,
                                    dateFin: dateDebut,
                                    id_motif: abscPrev.id_motif
                                };
                                var abscPrevToCreate3 = {
                                    dateDebut: dateFin,
                                    dateFin: abscPrev.dateFin,
                                    id_motif: abscPrev.id_motif
                                };

                                arrayAbscPrevToCreate.push(abscPrevToCreate1);
                                arrayAbscPrevToCreate.push(abscPrevToCreate3);

                                arrayConflitAbscPrev.push({
                                    before: abscPrev,
                                    after: [abscPrevToCreate1, abscPrevToCreate3]
                                });
                            }
                        } else if (dateFin < abscPrev.dateDebut || dateDebut > abscPrev.dateFin) { // Touche pas
                        } else {
                            if (abscPrev.id_motif == idMotif) { // Prolonge
                                arrayAbscPrevToDelete.push(abscPrev);
                                isProlonged = true;
                                newAbscPrevToCreate.dateDebut = abscPrev.dateDebut > dateDebut ? dateDebut : abscPrev.dateDebut;
                                newAbscPrevToCreate.dateFin = abscPrev.dateFin < dateFin ? dateFin : abscPrev.dateFin;
                            } else {
                                var abscPrevToUpdate;
                                if (abscPrev.dateDebut < dateFin) {
                                    abscPrevToUpdate = {
                                        dateDebut: abscPrev.dateDebut,
                                        dateFin: dateDebut,
                                        id: abscPrev.id,
                                        id_motif: abscPrev.id_motif
                                    };
                                } else if (dateDebut < abscPrev.dateFin) {
                                    abscPrevToUpdate = {
                                        dateDebut: dateFin,
                                        dateFin: abscPrev.dateFin,
                                        id: abscPrev.id,
                                        id_motif: abscPrev.id_motif
                                    };
                                }

                                arrayAbscPrevToUpdate.push(abscPrevToUpdate);

                                arrayConflitAbscPrev.push({
                                    before: abscPrev,
                                    after: [abscPrevToUpdate]
                                });
                            }
                        }
                    });
                }

                if (!isInsideExistingAbscPrev) {
                    arrayAbscPrevToCreate.push(newAbscPrevToCreate);
                }

                $scope.selected.eleve.arrayConflitAbscPrev = arrayConflitAbscPrev;
                $scope.selected.eleve.arrayAbscPrevToCreate = arrayAbscPrevToCreate
                $scope.selected.eleve.arrayAbscPrevToDelete = arrayAbscPrevToDelete;
                $scope.selected.eleve.arrayAbscPrevToUpdate = arrayAbscPrevToUpdate;
                console.log(arrayAbscPrevToUpdate);
            }

            $scope.numOfConflit += arrayConflitAbscPrev.length
                + $scope.selected.eleve.coursPassedWithoutAbsence.length
                + $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent.length;


            utils.safeApply($scope);
        };

        $scope.saveZoneAbsc = () => {

            var idEleve = $scope.selected.eleve.id;
            var idMotif = $scope.selected.motif.id;

            var arrayEventIdToUpdate = [];
            var arrayEventToCreate = [];

            $scope.selected.eleve.arrayAbscPrevToCreate.forEach(abscPrev => {
                abscPrev.dateDebutString = abscPrev.dateDebut.format("YYYY-MM-DD HH:mm");
                abscPrev.dateFinString = abscPrev.dateFin.format("YYYY-MM-DD HH:mm");
            });
            $scope.selected.eleve.arrayAbscPrevToUpdate.forEach(abscPrev => {
                abscPrev.dateDebutString = abscPrev.dateDebut.format("YYYY-MM-DD HH:mm");
                abscPrev.dateFinString = abscPrev.dateFin.format("YYYY-MM-DD HH:mm");
            });
            $scope.selected.eleve.arrayAbscPrevToDelete.forEach(abscPrev => {
                abscPrev.dateDebutString = abscPrev.dateDebut.format("YYYY-MM-DD HH:mm");
                abscPrev.dateFinString = abscPrev.dateFin.format("YYYY-MM-DD HH:mm");
            });

            $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent.forEach(cours => {
                arrayEventIdToUpdate.push(cours.id);
            });

            var arrayCoursToCreate = [];

            $scope.selected.eleve.coursPassedWithoutAbsence.forEach(cours => {
                if(cours.isFromMongo){
                    arrayCoursToCreate.push({
                        dateDebut: cours.startMoment.format("YYYY-MM-DD HH:mm"),
                        dateFin: cours.endMoment.format("YYYY-MM-DD HH:mm"),
                        salle: cours.salle,
                        id_matiere: cours.subjectId,
                        id_classe: $scope.structure.classes.all.find(classe => classe.name == cours.classes[0]).id,
                        id_personnel: cours.teacherIds[0],
                        id_etablissement: $scope.structure.id,
                    })
                }else{
                    arrayEventToCreate.push({
                        id_cours: cours.id
                    })
                }
            });

            console.log(arrayEventIdToUpdate);
            console.log(arrayEventToCreate);
            console.log(arrayCoursToCreate);
            Evenement.saveZoneAbsence(idEleve, idMotif,
                $scope.selected.eleve.arrayAbscPrevToCreate,
                $scope.selected.eleve.arrayAbscPrevToUpdate,
                $scope.selected.eleve.arrayAbscPrevToDelete,
                arrayEventIdToUpdate, arrayEventToCreate, arrayCoursToCreate);
        };

        $scope.display = {
            selection: {
                eleve: false
            },
            coursDetail: false,
            previsCour :false,
            calendarDate: false
        };
        $scope.allowDisplay = {
            prev : false,
            next : false,

        };
        $scope.checkNavigDate = () => {
            moment($scope.selected.dateFn).hour(0).minute(0).format('YYYY-MM-DD') <  moment(model.calendar.dayForWeek).add(7,'day').hour(0).minute(0).format('YYYY-MM-DD')
                ? $scope.allowDisplay.next=  false :  $scope.allowDisplay.next=true;
            moment($scope.selected.dateDb).hour(0).minute(0).format('YYYY-MM-DD') >=  moment(model.calendar.dayForWeek).hour(0).minute(0).format('YYYY-MM-DD')
                ? $scope.allowDisplay.prev =   false :  $scope.allowDisplay.prev = true;
        };

        $scope.nextWeekButton = function() {
            let next = moment(model.calendar.firstDay).add(7, 'day');
            $scope.checkNavigDate();
            if($scope.allowDisplay.next == true){
                model.calendar.setDate(next);
                $scope.loadItems();
                $scope.checkNavigDate();
            }
        };

        $scope.previousWeekButton = function() {
            let prev = moment(model.calendar.firstDay).subtract(7, 'day');
            $scope.checkNavigDate();
            if($scope.allowDisplay.prev == true){
                model.calendar.setDate(prev);
                $scope.loadItems();
                $scope.checkNavigDate();
            }
        };

        $scope.showLightboxAllAbsences = (state) => {
            $scope.displayLightboxAllAbsences = state;
        };

        $scope.showLightboxCheckConflit = (state) => {
            $scope.displayLightboxCheckConflit = state;
        };

        $scope.setTimelineVisible = (state) => {
            $scope.timelineIsVisible = state;
        };

        /**
         * Retourne true si les dates de la périodes sont bien chargées
         * Vérifie la date début, l'heure début, la date de fin et l'heure de fin
         * @returns {boolean}
         */
        $scope.datesPeriodAreLoaded = () => {
            return $scope.selected && $scope.selected.dateDb && $scope.selected.dateFn && $scope.selected.timeDb && $scope.selected.timeFn;
        };

        $scope.getSelectedPeriodStartMoment = () => {
            return moment(moment($scope.selected.dateDb).format("YYYY-MM-DD") + " " + $scope.selected.timeDb);
        };
        $scope.getSelectedPeriodEndMoment = () => {
            return moment(moment($scope.selected.dateFn).format("YYYY-MM-DD") + " " + $scope.selected.timeFn);
        };

        $scope.equalsMotifIdOfPa = (id_motif) => {
            return $scope.selected.motif.id == id_motif;
        };

        $scope.isAfterToday = (strDate) => {
            return moment(strDate) > moment();
        };

        $scope.niceDateMoment = (momentDate) => {
            return moment(momentDate).format("YYYY-MM-DD");
        };

        $scope.niceTimeMoment = (momentDate) => {
            return moment(momentDate).format("HH:mm");
        };

    }
]);