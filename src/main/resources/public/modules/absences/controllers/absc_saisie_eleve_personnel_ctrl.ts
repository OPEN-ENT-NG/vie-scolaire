/**
 * Created by rahnir on 08/06/2017.
 */
import {template, ng, idiom as lang} from 'entcore/entcore';
import * as utils from '../utils/personnel';
import {} from '../../entcore/template';
import {AbsencePrev} from '../models/personnel/AbsencePrev';
import {Evenement} from '../models/personnel/Evenement';
import {safeApply} from '../../utils/functions/safeApply';
import {Eleve} from '../models/personnel/Eleve';
import {PLAGES} from '../constants/plages';
import {notify} from '../../entcore/notify';



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

        $scope.getClassTimelineCours = (cours) => {
            let classRightMagnet = 'right-magnet';
            let classCoursHasAbsence = 'cours-has-absence';
            let classIsFutur = 'cours-futur';
            if (cours.absence !== undefined && cours.isFutur) {
                return classRightMagnet + ' ' + classCoursHasAbsence + ' ' + classIsFutur;
            } else if (cours.absence === undefined && cours.isFutur) {
                return classRightMagnet + ' ' + classIsFutur;
            } else if (cours.absence !== undefined && !cours.isFutur) {
                return classCoursHasAbsence;
            }
        };

        // Selection d'un élève dans la barre de recherche
        $scope.selectEleve = async () => {
            $scope.selectingEleve = true;
            try {
                $scope.selected.dateDb = moment().format('YYYY-MM-DD');
                $scope.selected.dateFn = moment().format('YYYY-MM-DD');
                $scope.selected.timeDb = moment().hour(PLAGES.heureDebut).minute(0).format('HH:mm');
                $scope.selected.timeFn = moment().hour(PLAGES.heureFin).minute(0).format('HH:mm');
                $scope.selected.motif = $scope.structure.motifs.all.find(m => m.id == null);

                $scope.structure.isWidget = false;
                $scope.display.calendar = false;

                $scope.selected.eleve = await $scope.syncEleveCours($scope.selected.eleve);
                $scope.selected.eleve.courss ? $scope.display.calendar = true : $scope.display.calendar = false;
                // On regarde si l'utilisateur connecté peut communiquer avec l'élève
                await $scope.selected.eleve.canCommunicate();
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
            } catch (e) {
                console.log(e);
            }
        };

        // sync les [classes - groupes - cours ] d'eleve
        $scope.syncEleveCours = async (eleve) => {
            let i = _.indexOf($scope.structure.eleves.all, eleve);
            if ($scope.structure.eleves.all[i].synchronized.className === false ) {
                await $scope.structure.eleves.all[i].syncClasseGroupName($scope.structure.classes.all, 'classe');
            }
            if ($scope.structure.eleves.all[i].synchronized.groupName === false ) {
                await  $scope.structure.eleves.all[i].syncClasseGroupName($scope.structure.classes.all, 'group');
            }
            // await $scope.selectPeriode() ;
            let startMoment = $scope.getSelectedPeriodStartMoment();
            let endMoment = $scope.getSelectedPeriodEndMoment();
            $scope.selected.eleve.structureId = $scope.structure.id;
            await $scope.selected.eleve.courss.sync(startMoment, endMoment, $scope.structure);

            return $scope.structure.eleves.all[i] ;
        };

        /**
         * Retourne les absences et absences prev mises en forme pour la partie 'Dernière absences saisies'
         * @returns {any}
         */
        $scope.getAbsencesToShow = () => {
            // Mise en forme des absence
            $scope.selected.eleve.absences.forEach(function (event) {
                event.motif = $scope.structure.motifs.find(motif => motif.id === event.id_motif);
                event.startMoment = moment(event.timestamp_dt);
                event.endMoment = moment(event.timestamp_fn);
            });

            // Mise en forme des absences prev + lien entre absences prev et absences normales
            $scope.selected.eleve.abscprev.forEach(function (abscprev) {
                // On ajoute un tableau pour y mettre les absences contenues dans la période de l'absence prévisionelle
                abscprev.absences = [];

                // Boolean permettant de différencier l'absence prev de l'absence normal pour un affichage dynamique
                abscprev.isAbsencePrev = true;

                // Mise en forme de la date
                abscprev.startMoment = moment(abscprev.timestamp_dt);
                abscprev.endMoment = moment(abscprev.timestamp_fn);
                abscprev.motif = $scope.structure.motifs.find(motif => motif.id === abscprev.id_motif);

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
            let absencesAlone = $scope.selected.eleve.absences.filter(event => !event.alreadyInAbscPrev);
            let absencesToShow = $scope.selected.eleve.abscprev.concat(absencesAlone);

            // On retrie par date
            absencesToShow.sort(function (a, b) {
                return new Date(b.timestamp_dt).getTime() - new Date(a.timestamp_dt).getTime() ;
            });

            absencesToShow.forEach(absc => {
                if (absc.isAbsencePrev && absc.absences) {
                    absc.absences.sort(function (a, b) {
                        return new Date(a.timestamp_dt).getTime() - new Date(b.timestamp_dt).getTime() ;
                    });
                }
            });

            return absencesToShow;
        };

        $scope.refreshTheCalendar = async () => {
            $scope.isRefreshingCalendar = true;
            let startMoment = $scope.getSelectedPeriodStartMoment();
            let endMoment = $scope.getSelectedPeriodEndMoment();

            await $scope.selected.eleve.syncEvenement(startMoment, endMoment);
            await $scope.selected.eleve.courss.sync(startMoment, endMoment, $scope.structure);

            let firstweek = moment($scope.selected.dateDb);
            model.calendar.setDate(firstweek);
            $scope.loadItems();
            $scope.checkNavigDate();
            $scope.calculConflit();
            $scope.isRefreshingCalendar = false;
            $scope.inputHasChanged = false;
        };

        $scope.$watch('selected.dateDb',  (newVal, oldVal) => {
            if ($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve) {
                return;
            }

            let newDateDebut = $scope.getSelectedPeriodStartMoment();
            let dateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateDebut > dateFin) {
                $scope.selected.dateDb = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.$watch('selected.timeDb',  (newVal, oldVal) => {
            if ($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve) {
                return;
            }

            let newDateDebut = $scope.getSelectedPeriodStartMoment();
            let dateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateDebut > dateFin) {
                $scope.selected.timeDb = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.$watch('selected.timeFn',  (newVal, oldVal) => {
            if ($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve) {
                return;
            }

            let dateDebut = $scope.getSelectedPeriodStartMoment();
            let newDateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateFin < dateDebut) {
                $scope.selected.timeFn = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.$watch('selected.dateFn',  (newVal, oldVal) => {
            if ($scope.selectingEleve || !$scope.datesPeriodAreLoaded() || !$scope.selected.eleve) {
                return;
            }

            let dateDebut = $scope.getSelectedPeriodStartMoment();
            let newDateFin = $scope.getSelectedPeriodEndMoment();

            if (newDateFin < dateDebut) {
                $scope.selected.dateFn = oldVal;
            } else {
                $scope.inputHasChanged = true;
            }
        });

        $scope.selectMotif = () => {
            if ($scope.selected.motif == null) {
                $scope.selected.motif = {id: null};
            }
            $scope.inputHasChanged = true;
            $scope.calculConflit();
        };

        $scope.loadItems = () => {
            if (!$scope.selected.eleve) {
                return;
            }

            let abscPrevItem = $scope.getArrayItemsAP();

            $scope.selected.eleve.items = [];
            if ($scope.selected.eleve.courss.all) {
                $scope.selected.eleve.items = $scope.selected.eleve.items.concat($scope.selected.eleve.courss.all);
            }
            if (abscPrevItem) {
                $scope.selected.eleve.items = $scope.selected.eleve.items.concat(abscPrevItem);
            }

            model.calendar.addScheduleItems($scope.selected.eleve.items);
        };

        $scope.getArrayItemsAP = () => {
            // On vérifie si il y aura une absence prev, si c'est le cas, on lance la calcul des conflits avec les Absences prev
            let dateDebutPeriode = $scope.getSelectedPeriodStartMoment();
            let dateFinPeriode = $scope.getSelectedPeriodEndMoment();

            // Si la date de fin est dans le futur, alors il y a une absence prev
            if (dateFinPeriode > moment()) {
                let startMoment = dateDebutPeriode > moment() ? dateDebutPeriode : moment();
                let endMoment = dateFinPeriode;

                let numEnglobe = 0;
                let numInside = 0;
                let numTouching = 0;
                let numNotTouching = 0;

                // On trie les cours par ordre chronologique
                $scope.selected.eleve.courss.all.sort(function (a, b) {
                    return new Date(a.startMoment).getTime()  - new Date(b.startMoment).getTime();
                });

                // Découpage de l'absence prev en item pour le calendar
                let abscPrevRaw = {
                    dateDebut: startMoment,
                    dateFin: endMoment
                };

                let arrayOfItemAP: any = [];
                arrayOfItemAP.push(abscPrevRaw);

                $scope.selected.eleve.courss.all.forEach(cours => {
                    let lastAP = arrayOfItemAP.pop();
                    cours.dateDebut = moment(cours.startMoment);
                    cours.dateFin = moment(cours.endMoment);

                    if (lastAP.dateDebut < cours.dateDebut && cours.dateFin < lastAP.dateFin) { // Englobe
                        numEnglobe++;

                        let abscPrevPart1 = {
                            dateDebut: lastAP.dateDebut,
                            dateFin: cours.dateDebut
                        };
                        let abscPrevToPart2 = {
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
                    item.endMoment = item.dateFin;
                });

                return arrayOfItemAP;
            }
        };

        $scope.calculConflit = async () => {
            if (!$scope.selected.eleve || !$scope.selected.motif
                || !$scope.selected.dateDb  || !$scope.selected.dateFn
                || !$scope.selected.timeDb  || !$scope.selected.timeFn) {
                return;
            }

            $scope.selected.eleve.coursWithConflit = [];
            $scope.selected.eleve.arrayConflitAbscPrev = [];
            $scope.selected.eleve.arrayAbscPrevToCreate = [];
            $scope.selected.eleve.arrayAbscPrevToDelete = [];
            $scope.selected.eleve.arrayAbscPrevToUpdate = [];
            $scope.selected.eleve.coursPassedWithoutAbsence = [];
            $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent = [];
            $scope.selected.eleve.coursWithDifferentEvenement = [];

            let dateDebut = $scope.getSelectedPeriodStartMoment();
            let dateFin = $scope.getSelectedPeriodEndMoment();
            let idMotif = $scope.selected.motif.id;

            await $scope.structure.appels.sync(dateDebut.format('YYYY-MM-DD'), dateFin.format('YYYY-MM-DD'));

            $scope.numOfConflit = 0;

            // Calcul des conflits avec les cours

            // Lien entre les cours et les absences
            $scope.selected.eleve.courss.all.forEach(cours => {
                cours.appel = $scope.structure.appels.all.find(x => x.id_cours === cours._id);
                if (cours.appel && $scope.selected.eleve.absences) {
                    cours.absence = $scope.selected.eleve.absences.find(absences => absences.id_appel === cours.appel.id);
                }
            });

            // On groupe les cours par date du jour
            $scope.selected.eleve.courss.all.sort(function (a, b) {
                return new Date(a.startMoment).getTime() - new Date(b.startMoment).getTime();
            });

            let group_to_values = $scope.selected.eleve.courss.all.reduce(function (obj, item) {
                let date = item.startMoment.format('YYYY-MM-DD');
                obj[date] = obj[date] || [];
                obj[date].push(item);
                return obj;
            }, {});

            let groups = Object.keys(group_to_values).map(function (key) {
                return {date: moment(key), cours: group_to_values[key]};
            });

            groups.sort(function (a, b) {
                return new Date(a.date).getTime() - new Date(b.date).getTime();
            });

            $scope.selected.eleve.coursGroupByDay = groups;
            if ($scope.selected.eleve.coursGroupByDay === undefined || $scope.selected.eleve.coursGroupByDay.length === 0 ) {
                $scope.timelineIsVisible = false;
            }

            $scope.selected.eleve.coursPassedWithoutAbsence =
                $scope.selected.eleve.courss.all.filter(cours => !$scope.isAfterToday(cours.startMoment) && !cours.absence);

            $scope.selected.eleve.coursWithConflit = $scope.selected.eleve.courss.all.filter((cours) => {
                // Si le cours est passé et : possède une absence avec un motif différent ou des évènements différents.
                return (!$scope.isAfterToday(cours.startMoment) && $scope.hasDifferentEvent(cours))
                    || (!$scope.isAfterToday(cours.startMoment) && cours.absence && !$scope.equalsMotifIdOfPa(cours.absence.motif.id));
            });

            $scope.selected.eleve.coursWithDifferentEvenement =
                $scope.selected.eleve.coursWithConflit.filter(cours => !$scope.isAfterToday(cours.startMoment) && $scope.hasDifferentEvent(cours));

            $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent =
                $scope.selected.eleve.coursWithConflit.filter(cours => !$scope.isAfterToday(cours.startMoment) && cours.absence && !$scope.equalsMotifIdOfPa(cours.absence.motif.id) );



            // On vérifie si il y aura une absence prev, si c'est le cas, on lance la calcul des conflits avec les Absences prev
            let newAbscPrevToCreate;
            // Si la date de fin est dans le futur, alors il y a une absence prev
            let arrayConflitAbscPrev = [];
            if (dateFin > moment()) {
                let startMoment = dateDebut > moment() ? dateDebut.clone() : moment();
                let endMoment = dateFin.clone();

                newAbscPrevToCreate = {
                    dateDebut: startMoment,
                    dateFin: endMoment,
                    id_motif: idMotif
                };

                // CALCUL CONFLIT AVEC LES ABSENCE PREV
                let arrayAbscPrevToUpdate = [];
                let arrayAbscPrevToDelete = [];
                let arrayAbscPrevToCreate = [];

                let isProlonged = false;

                if ($scope.selected.eleve.abscprev) {
                    $scope.selected.eleve.abscprev.forEach(abscPrev => {
                        abscPrev.dateDebut = moment(abscPrev.timestamp_dt);
                        abscPrev.dateFin = moment(abscPrev.timestamp_fn);

                        if (newAbscPrevToCreate.dateDebut < abscPrev.dateDebut && abscPrev.dateFin < newAbscPrevToCreate.dateFin) { // Englobe
                            arrayAbscPrevToDelete.push(abscPrev);
                            arrayConflitAbscPrev.push(abscPrev);
                        } else if (abscPrev.dateDebut < newAbscPrevToCreate.dateDebut && newAbscPrevToCreate.dateFin < abscPrev.dateFin) { // Inside
                            arrayConflitAbscPrev.push(abscPrev);
                            arrayAbscPrevToDelete.push(abscPrev);
                            if (abscPrev.id_motif === idMotif) {
                                // On prolonge la new absc prev
                                isProlonged = true;
                                newAbscPrevToCreate.dateDebut = abscPrev.dateDebut;
                                newAbscPrevToCreate.dateFin = abscPrev.dateFin;
                            } else { // Coupe
                                let abscPrevToCreate1 = {
                                    dateDebut: abscPrev.dateDebut,
                                    dateFin: newAbscPrevToCreate.dateDebut,
                                    id_motif: abscPrev.id_motif
                                };
                                let abscPrevToCreate3 = {
                                    dateDebut: newAbscPrevToCreate.dateFin,
                                    dateFin: abscPrev.dateFin,
                                    id_motif: abscPrev.id_motif
                                };

                                arrayAbscPrevToCreate.push(abscPrevToCreate1);
                                arrayAbscPrevToCreate.push(abscPrevToCreate3);
                            }
                        } else if (newAbscPrevToCreate.dateFin < abscPrev.dateDebut || newAbscPrevToCreate.dateDebut > abscPrev.dateFin) { // Touche pas
                        } else {
                            if (abscPrev.id_motif === idMotif) { // Prolonge
                                arrayConflitAbscPrev.push(abscPrev);
                                arrayAbscPrevToDelete.push(abscPrev);
                                isProlonged = true;
                                newAbscPrevToCreate.dateDebut = abscPrev.dateDebut > newAbscPrevToCreate.dateDebut ? newAbscPrevToCreate.dateDebut : abscPrev.dateDebut;
                                newAbscPrevToCreate.dateFin = abscPrev.dateFin < newAbscPrevToCreate.dateFin ? newAbscPrevToCreate.dateFin : abscPrev.dateFin;
                            } else {
                                let abscPrevToUpdate;
                                if (abscPrev.dateDebut < newAbscPrevToCreate.dateFin) {
                                    abscPrevToUpdate = {
                                        dateDebut: abscPrev.dateDebut,
                                        dateFin: newAbscPrevToCreate.dateDebut,
                                        id: abscPrev.id,
                                        id_motif: abscPrev.id_motif
                                    };
                                } else if (newAbscPrevToCreate.dateDebut < abscPrev.dateFin) {
                                    abscPrevToUpdate = {
                                        dateDebut: newAbscPrevToCreate.dateFin,
                                        dateFin: abscPrev.dateFin,
                                        id: abscPrev.id,
                                        id_motif: abscPrev.id_motif
                                    };
                                }

                                arrayConflitAbscPrev.push(abscPrev);
                                arrayAbscPrevToUpdate.push(abscPrevToUpdate);
                            }
                        }
                    });
                }

                arrayAbscPrevToCreate.push(newAbscPrevToCreate);

                $scope.selected.eleve.arrayConflitAbscPrev = arrayConflitAbscPrev;
                $scope.selected.eleve.arrayAbscPrevToCreate = arrayAbscPrevToCreate;
                $scope.selected.eleve.arrayAbscPrevToDelete = arrayAbscPrevToDelete;
                $scope.selected.eleve.arrayAbscPrevToUpdate = arrayAbscPrevToUpdate;
            }

            $scope.numOfConflit += arrayConflitAbscPrev.length
                + $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent.length
                + $scope.selected.eleve.coursWithDifferentEvenement.filter(cours =>
                    !$scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent.includes(cours)).length; // On ne compte pas les conflits en double
            utils.safeApply($scope);
        };

        $scope.savePeriodeAbsence = () => {

            let idEleve = $scope.selected.eleve.id;
            let idMotif = $scope.selected.motif.id;

            let arrayEventIdToUpdate = [];
            let arrayEventToCreate = [];

            $scope.selected.eleve.arrayAbscPrevToCreate.forEach(abscPrev => {
                abscPrev.dateDebutString = abscPrev.dateDebut.format('YYYY-MM-DD HH:mm');
                abscPrev.dateFinString = abscPrev.dateFin.format('YYYY-MM-DD HH:mm');
            });
            $scope.selected.eleve.arrayAbscPrevToUpdate.forEach(abscPrev => {
                abscPrev.dateDebutString = abscPrev.dateDebut.format('YYYY-MM-DD HH:mm');
                abscPrev.dateFinString = abscPrev.dateFin.format('YYYY-MM-DD HH:mm');
            });
            $scope.selected.eleve.arrayAbscPrevToDelete.forEach(abscPrev => {
                abscPrev.dateDebutString = abscPrev.dateDebut.format('YYYY-MM-DD HH:mm');
                abscPrev.dateFinString = abscPrev.dateFin.format('YYYY-MM-DD HH:mm');
            });

            $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent.forEach(cours => {
                arrayEventIdToUpdate.push(cours.absence.id);
            });

            let arrayCoursToCreate = [];

            $scope.selected.eleve.coursPassedWithoutAbsence.forEach(cours => {
                if (cours.isFromMongo) {
                    arrayCoursToCreate.push({
                        dateDebut: cours.startMoment.format('YYYY-MM-DD HH:mm'),
                        dateFin: cours.endMoment.format('YYYY-MM-DD HH:mm'),
                        salle: cours.salle,
                        id_matiere: cours.subjectId,
                        classeIds: $scope.structure.classes.all.filter(classe => cours.classeNames.includes(classe.name)).map(a => a.id),
                        teacherIds: cours.teacherIds,
                        id_etablissement: $scope.structure.id,
                    });
                } else {
                    arrayEventToCreate.push({
                        id_cours: cours.id
                    });
                }
            });

            Evenement.savePeriodeAbsence(idEleve, idMotif,
                $scope.selected.eleve.arrayAbscPrevToCreate,
                $scope.selected.eleve.arrayAbscPrevToUpdate,
                $scope.selected.eleve.arrayAbscPrevToDelete,
                arrayEventIdToUpdate, arrayEventToCreate, arrayCoursToCreate);

            $scope.selectEleve();
        };

        $scope.display = {
            selection: {
                eleve: false
            },
            coursDetail: false,
            previsCour: false,
            calendarDate: false
        };
        $scope.allowDisplay = {
            prev: false,
            next: false,
        };
        $scope.checkNavigDate = () => {
            (moment($scope.selected.dateFn).hour(0).minute(0)).isSameOrBefore(moment(model.calendar.dayForWeek).startOf('week').add(7, 'day').hour(0).minute(0))
                ? $scope.allowDisplay.next = false : $scope.allowDisplay.next = true;
            (moment($scope.selected.dateDb).hour(0).minute(0)).isSameOrAfter(moment(model.calendar.dayForWeek).hour(0).minute(0))
                ? $scope.allowDisplay.prev = false : $scope.allowDisplay.prev = true;
        };

        $scope.nextWeekButton = function() {
            let next = moment(model.calendar.firstDay).add(7, 'day');
            $scope.checkNavigDate();
            if ($scope.allowDisplay.next === true) {
                model.calendar.setDate(next);
                $scope.loadItems();
                $scope.checkNavigDate();
            }
        };

        $scope.canSaveEvent = function() {
            if ($scope.selected === undefined
                || $scope.selected.eleve === undefined) {
                return false;
            }
            if (
                ($scope.selected.eleve.arrayAbscPrevToCreate === undefined ||  $scope.selected.eleve.arrayAbscPrevToCreate.length === 0)
                && ($scope.selected.eleve.arrayAbscPrevToUpdate === undefined ||  $scope.selected.eleve.arrayAbscPrevToUpdate.length === 0)
                && ($scope.selected.eleve.arrayAbscPrevToDelete === undefined ||  $scope.selected.eleve.arrayAbscPrevToDelete.length === 0)
                && ($scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent === undefined ||  $scope.selected.eleve.coursPassedWithAbsenceWithMotifDifferent.length === 0)
                && ($scope.selected.eleve.coursPassedWithoutAbsence === undefined ||  $scope.selected.eleve.coursPassedWithoutAbsence.length === 0)
                && ($scope.selected.eleve.coursPassedWithoutAbsence === undefined ||  $scope.selected.eleve.coursPassedWithoutAbsence.length === 0)
            ) {
                return false;
            }
            return true;
        };

        $scope.previousWeekButton = function() {
            let prev = moment(model.calendar.firstDay).subtract(7, 'day');
            $scope.checkNavigDate();
            if ($scope.allowDisplay.prev === true) {
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

        $scope.showLightboxConfirm = (state) => {
            $scope.displayLightboxConfirm = state;
        };

        $scope.setTimelineVisible = (state) => {
            $scope.timelineIsVisible = state;
        };

        $scope.setLightboxLegendeLastAbsenceVisible = (state) => {
            $scope.lightboxLegendeLastAbsenceIsVisible = state;
        };

        $scope.setLightboxLegendeCalendarVisible = (state) => {
            $scope.lightboxLegendeCalendarIsVisible = state;
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
            return moment(moment($scope.selected.dateDb).format('YYYY-MM-DD') + ' ' + $scope.selected.timeDb);
        };

        $scope.getSelectedPeriodEndMoment = () => {
            return moment(moment($scope.selected.dateFn).format('YYYY-MM-DD') + ' ' + $scope.selected.timeFn);
        };

        $scope.equalsMotifIdOfPa = (id_motif) => {
            return $scope.selected.motif.id === id_motif;
        };

        $scope.hasDifferentEvent = (cours) => {
            // Si il y'a des evenements autres qu'une absence sur le cours
            if (cours.evenements !== undefined) {
                return cours.evenements.filter(event => event.id_type !== 1).length > 0;
            } else {
                return false;
            }
        };

        $scope.getLibelleConflit = (cours) => {
            let libelle = '';
            let coursHasDifferentEvent = $scope.hasDifferentEvent(cours);
            let coursHasAbsenceWithDiffMotif = cours.absence && !$scope.equalsMotifIdOfPa(cours.absence.motif.id);
            if (coursHasDifferentEvent) {
                libelle += lang.translate('viescolaire.absences.different.event');
            }
            if (coursHasDifferentEvent && coursHasAbsenceWithDiffMotif){
                libelle += ', ';
            }
            if (coursHasAbsenceWithDiffMotif) {
                libelle += lang.translate('viescolaire.absences.motif.different');
            }
            if (!coursHasDifferentEvent && !coursHasAbsenceWithDiffMotif){
               return false;
            }
            return libelle;
        };

        $scope.isAfterToday = (strDate) => {
            return moment(strDate) > moment();
        };

        $scope.getFormattedDate = (momentDate) => {
            return moment(momentDate).format('DD/MM/YYYY');
        };

        $scope.getFormattedTime = (momentDate) => {
            return moment(momentDate).format('HH:mm');
        };

        $scope.getFormattedDateTime = (momentDate) => {
            return moment(momentDate).format('DD/MM/YYYY HH:mm');
        };
    }
]);