import { template, ng } from 'entcore';
import {Evenement} from '../models/shared/Evenement';
import {FORMAT} from '../constants/formats';
import * as utils from '../utils/shared';
import {Cours} from '../models/shared/Cours';

let moment = require('moment');
declare let _: any;

export let abscAppelController = ng.controller('AbscAppelController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route', '$templateCache',
    async function ($scope, route, model, $rootScope, $location, $route, $templateCache) {

        template.open('AbscFiltreAppel', '../templates/absences/absc_appel_filtre');
        template.open('AbscEleveDetail', '../templates/absences/absc_appel_eleve_detail');
        template.open('AbscEleveAide', '../templates/absences/absc_appel_help');

        $scope.$on('clearAppel', function (event, args) {
            $scope.appel.display = false;
            $scope.showError = false;
            $scope.currentCours = undefined;

            let currentDate = $scope.appel.date;
            $scope.appel.date = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
            $scope.refreshVuesAppel();
            $scope.ouvrirAppel();
        });

        $scope.psFiltreTitre = {
            enseignant: 'Enseignant',
            classe: 'Classe',
            creneau: 'Creneau'
        };

        $scope.etatAppel = {
            giIdEtatAppelInit: 1,
            giIdEtatAppelEnCours: 2,
            giIdEtatAppelFait: 3
        };

        $scope.show = {
            success: false,
            lightbox: false
        };

        $scope.oEvtType = {
            giIdEvenementAbsence: 1,
            giIdEvenementRetard: 2,
            giIdEvenementDepart: 3,
            giIdEvenementIncident: 4,
            giIdEvenementObservation: 5
        };

        $scope.detailEleve = {
            displayed: false,
            evenements: []
        };

        $scope.appel = {
            date: new Date(),
            display: false
        };

        $scope.oEvtTime = {
            depart: '--:--',
            retard: '--:--'
        };

        /**
         * Indique si l'utilisateur est un personnel d'éducation
         * @returns {boolean} true si c'est un personnel d'éducation, false sinon.
         */
        $scope.isResponsable = () => {
            return model.me.type === 'PERSEDUCNAT';
        };

        /**
         * Indique si l'évènement ou l'appel  est éditable par l'utilisateur
         * @param   oEvent  Parametre optionnel. Si fourni, indique si l'évènement est éditable.
         *          Sinon, indique si l'appel est éditable
         * @returns {boolean}   Si poEvent est fourni, retourne true s'il est éditable, false sinon.
         *                      Si rien n'est fourni, retourne true si l'appel est éditable, false sinon.
         */
        $scope.isEditable = (poEvent?) => {
            if (poEvent != null) {
                return model.me.type === 'PERSEDUCNAT' || !poEvent.saisie_cpe;
            } else if ($scope.currentCours != null) {
                return (model.me.type === 'PERSEDUCNAT') || !$scope.currentCours.appel.saisie_cpe === true;
            }
        };

        /**
         * Calcule le nombre d'élèves présents et le renseigne dans $scope.currentCours.nbPresents
         */
        $scope.calcNbElevesPresents = function () {
            let piAbsents = 0;
            $scope.appel.piTotal = 0;
            $scope.currentCours.classes.forEach(classe => {
                $scope.appel.piTotal += classe.eleves.all.length;
                classe.eleves.all.forEach(eleve => {
                    if ($scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementAbsence)) {
                        piAbsents++;
                    }
                });
            });
            $scope.appel.piPresents = $scope.appel.piTotal - piAbsents;
        };

        /**
         * Créer un évènement en fonction de l'élève et du type d'évènement passés en paramètre.
         * Si l'évènement est une absence, supprime les retards et absences placés sur l'élève grâce à une promesse
         * de suppression.
         * @param poEleve       L'élève pour lequel on veut ajouter l'évènement.
         * @param piTypeEvt     Le type d'évènement que l'on souhaite ajouter.
         * @returns {Promise<T>|Promise}    Une promesse de création qui s'achève lorsque toutes les suppressions
         *                                  nécessaires sont achevées.
         */
        $scope.createEvenement = (poEleve, piTypeEvt): Promise<any> => {
            return new Promise(async (resolve, reject) => {
                let poEvt = new Evenement();
                poEvt.saisie_cpe = $scope.isResponsable();
                poEvt.id_eleve = poEleve.id;
                poEvt.id_appel = $scope.currentCours.appel.id;
                poEvt.id_type = piTypeEvt;
                poEvt.id_motif = null;
                poEvt.id_cours = $scope.currentCours.id;

                if (poEvt.id_type === $scope.oEvtType.giIdEvenementAbsence) {
                    let _todo = [];
                    _.each(poEleve.evenements.all, (_evt) => {
                        if ((_evt.id_type === $scope.oEvtType.giIdEvenementRetard ||
                            _evt.id_type === $scope.oEvtType.giIdEvenementDepart)
                            && _evt.id_appel === poEvt.id_appel) {
                            _todo.push($scope.deleteEvenement(_evt));
                        }
                    });
                    $scope.updateDetailEleve();
                    // afin d'éviter d'itérer sur une liste qu'on modifie, on stocke les suppressions pour les
                    // executer une fois la boucle terminee
                    await Promise.all(_todo);
                }
                resolve(poEvt);
            });
        };

        /**
         * Supprime l'évènement passé en paramètre. Si l'évènement est un retard ou un depart, réinitialise la variable
         * qui gère l'affichage de l'heure.
         * Supprime l'évènement de l'historique de l'élève, de son panneau détail, reset l'état de l'appel et recalcul
         * le nombre d'élèves présents.
         *
         * @param poEvt     L'évènement à supprimer.
         * @returns {Promise<Promise<T>|Promise>}   Une promesse qui se résout après une suppression réussie.
         */
        $scope.deleteEvenement = async (poEvt): Promise<any> => {
            return new Promise(async (resolve, reject) => {

                let poEleve = _.findWhere($scope.getListEleveClasses(), {id: poEvt.id_eleve});

                if (poEleve == null) {
                    return;
                }

                poEleve.plages.remEvtPlage(poEvt);
                poEleve.evenements.remEvt(poEvt);
                $scope.updateDetailEleve(poEvt.id_type);
                if (poEvt.id_type === $scope.oEvtType.giIdEvenementRetard || poEvt.id_type === $scope.oEvtType.giIdEvenementDepart) {
                    $scope.setTime(poEvt.id_type);
                }
                await poEvt.delete();
                $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                $scope.calcNbElevesPresents();
                $scope.setEvenementOfEleves(poEleve);
                utils.safeApply($scope);
                resolve();
            });
        };

        /**
         * Lorsque l'utilisateur coche une absence ou un évènement, crée ou supprime cet évènement.
         * @param poEleve       L'élève concerné par la création ou la suppression de l'évènement;
         * @param piTypeEvent   Le type d'évènement à créer ou supprimer.
         */
        $scope.checkEvenement = async (poEleve, piTypeEvent) => {
            let _evt = await $scope.getEvenement(poEleve, piTypeEvent);
            if (_evt) {
                await $scope.deleteEvenement(_evt);
            } else {
                await $scope.updateEvenement(poEleve, piTypeEvent);
            }
        };

        /**
         * Met à jour ou sauvegarde l'évènement spécifié par l'élève et le type d'évènement passés en paramètre.
         * @param poEleve       L'élève sur lequel porte l'évènement.
         * @param piTypeEvent   Le type d'évènement.
         * @returns {Promise<T>|Promise}    Une promesse qui se résout lorsque l'évènement a été mis à jour.
         */
        $scope.updateEvenement = (poEleve, piTypeEvent): Promise<any> => {
            return new Promise(async (resolve, reject) => {

                // Si l'évènement n'existe pas, on le crée
                let poEvent = $scope.getEvenement(poEleve, piTypeEvent);
                if (poEvent == null) {
                    poEvent = await $scope.createEvenement(poEleve, piTypeEvent);
                }
                poEvent.saisie_cpe = $scope.isResponsable();

                // Si l'évènement est une observation qui ne contient aucun texte, il est supprimé
                // Si l'évènement est un retard ou un départ, le timestamp concerné est mis à jour avant sauvegarde.
                // Le "fallthrough" permet d'effectuer la sauvegarde peu importe le type d'évènement, mais de spécifier
                // des traitements additionnels par type d'évènement.
                switch (piTypeEvent) {
                    case $scope.oEvtType.giIdEvenementObservation:
                        if (_.isEmpty($scope.detailEleve.evenements[$scope.oEvtType.giIdEvenementObservation].commentaire)) {
                            if (poEvent.id != null) {
                                await $scope.deleteEvenement(poEvent);
                            }
                            break;
                        } else {
                            poEvent.commentaire = $scope.detailEleve.evenements[$scope.oEvtType.giIdEvenementObservation].commentaire;
                        }
                    case $scope.oEvtType.giIdEvenementDepart:
                    case $scope.oEvtType.giIdEvenementRetard:
                        $scope.updateTime(poEvent);
                        /* FALLTHROUGH */
                    default:
                        let _return = await poEvent.save();
                        if (_return.bool) {
                            poEvent.id = _return.id;
                        }
                        if (poEleve.evenements.addEvt(poEvent)) {
                            poEleve.plages.addEvtPlage(poEvent);
                        }
                        $scope.updateDetailEleve(piTypeEvent);
                        $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelEnCours);
                        $scope.calcNbElevesPresents();
                }
                $scope.setEvenementOfEleves(poEleve);
                utils.safeApply($scope);
                resolve();
            });
        };

        /**
         * Récupère l'évènement concerné par l'élève et le type passés en paramètre. L'évènement doit concerné l'appel
         * en cours et posséder un ID.
         * @param poEleve       L'élève concerné par l'évènement.
         * @param piTypeEvent   Le type d'évènement.
         * @returns     L'évènement s'il existe, null sinon.
         */
        $scope.getEvenement = (poEleve, piTypeEvent) => {
            return poEleve ? poEleve.evenements.find((evt) => {
                return evt.id != null && evt.id_appel === $scope.currentCours.appel.id && evt.id_type === piTypeEvent;
            })
                : null;
        };

        /**
         * Sélection d'un élève : affiche le panel de droit avec la saisie du retard/depart/punition eleve
         * Initialise les valeurs affichées.
         * @param poEleve l'objet eleve sélectionné
         */
        $scope.detailEleveAppel = function (poEleve) {

            if (poEleve !== $scope.currentEleve) {
                $scope.setTime();
                $scope.currentEleve = poEleve;
                $scope.updateDetailEleve();
                $scope.detailEleve.displayed = true;
            } else {
                $scope.detailEleve.displayed = !$scope.detailEleve.displayed;
            }
        };

        /**
         * Met à jour un évènement du panel détail de l'élève, spécifié par le type passé en paramètre.
         * Si aucun type n'est fourni, met à jour tous les évènements.
         * @param piTypeEvt     Le type de l'évènement à mettre à jour.
         */
        $scope.updateDetailEleve = (piTypeEvt?) => {
            if (piTypeEvt != null) {
                // Si l'élève actuel ne possède pas d'évènement du type spécifié, le cas par défaut est sélectionné.
                // Ce cas par défaut initialise les valeurs à null.
                let poEvt = $scope.getEvenement($scope.currentEleve, piTypeEvt);
                let _id = poEvt != null ? poEvt.id_type : 0;

                switch (_id) {
                    case $scope.oEvtType.giIdEvenementObservation:
                        $scope.detailEleve.evenements[piTypeEvt] = {
                            evt: poEvt,
                            commentaire: poEvt.commentaire
                        };
                        break;
                    case $scope.oEvtType.giIdEvenementRetard:
                    case $scope.oEvtType.giIdEvenementDepart:
                        let _timestamp = poEvt.id_type === $scope.oEvtType.giIdEvenementRetard ? poEvt.timestamp_arrive : poEvt.timestamp_depart;
                        $scope.setTime(poEvt.id_type, _timestamp);
                    case $scope.oEvtType.giIdEvenementAbsence:
                    case $scope.oEvtType.giIdEvenementIncident:
                        $scope.detailEleve.evenements[piTypeEvt] = {
                            isEditable: $scope.isEditable(poEvt),
                            evt: poEvt,
                            check: poEvt != null
                        };
                        break;
                    default:
                        $scope.detailEleve.evenements[piTypeEvt] = {
                            isEditable: $scope.isEditable(),
                            check: null,
                            evt: null,
                            commentaire: null
                        };
                }
            } else {
                _.each($scope.oEvtType, (piTypeEvt) => {
                    $scope.updateDetailEleve(piTypeEvt);
                });
            }
        };

        /**
         * Set le timestamp pour l'évènement retard ou départ passé en paramètre, en fonction de l'heure dans oEvtTime
         * et la date du cours.
         * Si le timestamp n'est pas initialisé, initialise oEvtTime.
         * @param poEvent   L'évèment pour lequel on souhaite initialiser l'heure.
         */
        $scope.updateTime = (poEvent) => {
            if (poEvent.id_type === $scope.oEvtType.giIdEvenementDepart) {
                if (poEvent.timestamp_depart == null) {
                    $scope.setTime(poEvent.id_type);
                }
                let _date = moment($scope.currentCours.endMoment).format(FORMAT.date);
                let _hour = $scope.oEvtTime.depart;
                poEvent.timestamp_depart = moment(_date + ' ' + _hour, FORMAT.date + ' ' + FORMAT.heureMinutes).format(FORMAT.timestamp);
            } else if (poEvent.id_type === $scope.oEvtType.giIdEvenementRetard) {
                if (poEvent.timestamp_arrive == null) {
                    $scope.setTime(poEvent.id_type);
                }
                let _date = moment($scope.currentCours.startMoment).format(FORMAT.date);
                let _hour = $scope.oEvtTime.retard;
                poEvent.timestamp_arrive = moment(_date + ' ' + _hour, FORMAT.date + ' ' + FORMAT.heureMinutes).format(FORMAT.timestamp);
            }
        };

        /**
         * Met à jour la variable oEvtTime en fonction du type d'évènement et de l'heure passés en paramètre.
         * @param piTypeEvent   Optionnel   Le type d'évènement à éditer (retard ou départ).
         *                                  Si null, initiliase les temps à '--:--'
         * @param psTime        Optionnel   L'heure à laquelle initialiser les variables de temps.
         *                                  Si null, initialise les temps à l'heure courante.
         */
        $scope.setTime = (piTypeEvent?, psTime?) => {
            if (piTypeEvent != null && psTime != null) {
                let _hour = moment(psTime).format(FORMAT.heureMinutes);
                if (piTypeEvent === $scope.oEvtType.giIdEvenementDepart) {
                    $scope.oEvtTime.depart = _hour;
                } else if (piTypeEvent === $scope.oEvtType.giIdEvenementRetard) {
                    $scope.oEvtTime.retard = _hour;
                }
            } else if (piTypeEvent != null && psTime == null) {
                if (piTypeEvent === $scope.oEvtType.giIdEvenementDepart) {
                    $scope.oEvtTime.depart = $scope.oEvtTime.depart === '--:--' ? moment().format(FORMAT.heureMinutes) : '--:--';
                } else if (piTypeEvent === $scope.oEvtType.giIdEvenementRetard) {
                    $scope.oEvtTime.retard = $scope.oEvtTime.retard === '--:--' ? moment().format(FORMAT.heureMinutes) : '--:--';
                }
            } else {
                $scope.oEvtTime = {
                    depart: '--:--',
                    retard: '--:--'
                };
            }
        };

        $scope.getListEleveClasses = () => {

            let listEleve = [];
            if ($scope.currentCours !== undefined) {
                $scope.currentCours.classes.forEach(classe => classe.eleves.all.forEach(eleve => {
                    listEleve.push(eleve);
                }));
            }
            return listEleve;
        };

        /**
         * Sélection d'un cours : Affiche le panel central de la liste des élèves
         * @param cours l'objet cours sélectionné
         */
        $scope.selectCours = async (cours) => {
            if (cours.isFromMongo) {
                // Si c'est un cours Mongo alors on créer le cours postgres
                cours.structureId = $scope.structure.id;
                await Cours.createCoursPostgres(cours);

                // On re synchronise tout en appelant ouvrirAppel()
                let currentDate = $scope.appel.date;
                $scope.appel.date = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
                await $scope.ouvrirAppel();

                // Une fois synchronisé on récupère le coursPostgres et on le sélectionne
                let coursPostgres = $scope.structure.creneaus.find(creneau => creneau.timestamp_dt === cours.startMoment.format('HH:mm')
                    && creneau.timestamp_fn === cours.endMoment.format('HH:mm') && !creneau.cours.isFromMongo).cours;
                if (coursPostgres !== undefined) {
                    $scope.selectCours(coursPostgres);
                    utils.safeApply($scope);
                }
                return;
            }

            let isTeacher = !$scope.isResponsable() || $scope.poEnseignantRecherche || !$scope.poClasseRecherche;
            $scope.appel = {
                date: $scope.appel.date,
                display: false
            };
            $scope.detailEleve.displayed = false;
            $scope.bClassesVue = false;
            $scope.currentCours = cours;

            $scope.currentCours.classes = $scope.structure.classes.filter(classe => _.contains($scope.currentCours.classeNames, classe.name));

            $scope.currentEleve = undefined;
            $scope.updateDetailEleve();

            // Synchronise les évènements du cours actuel
            await $scope.currentCours.sync(isTeacher);

            // Synchronise l'historique de chaque élève avant de continuer
            let _todo = [];
            $scope.currentCours.classes.forEach(classe => classe.eleves.all.forEach(eleve => {
                _todo.push(eleve.plages.sync());
            }));
            await Promise.all(_todo);

            $scope.calcNbElevesPresents();

            // si il y'a plusieurs classes, alors on regroupe les élèves pour les afficher par classe
            if ($scope.currentCours.classes.length > 0) {
                let listEleve = [];
                $scope.currentCours.classes.forEach(classe => {
                    if (classe.type_groupe === 1) {
                        $scope.bClassesVue = true;
                    }
                    classe.eleves.all.forEach(eleve => {
                        listEleve.push(eleve);
                    });
                });

                let _elevesByGroup = _.groupBy(listEleve, function (oEleve) {
                    return oEleve.classes[0];
                });
                $scope.currentCours.classesOfGroup = [];
                for (let group in _elevesByGroup) {
                    $scope.currentCours.classesOfGroup.push({
                        name: $scope.structure.classes.find(classe => classe.externalId === group).name,
                        eleves: _elevesByGroup[group]
                    });
                }
            }

            if ($scope.currentCours.classes.length > 1) {
                $scope.bClassesVue = true;
            }

            $scope.currentCours.isEditable = $scope.isEditable();
            $scope.currentCours.listAllEleves = $scope.getListEleveClasses();

            $scope.currentCours.listAllEleves.forEach(eleve => {
                eleve.eventAbsence = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementAbsence);
                eleve.eventRetard = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementRetard);
                eleve.eventDepart = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementDepart);
                eleve.eventIncident = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementIncident);
                eleve.eventObservation = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementObservation);

                eleve.eventAbsenceIsEditable = $scope.isEditable(eleve.eventAbsence);
                eleve.eventRetardIsEditable = $scope.isEditable(eleve.eventRetard);
                eleve.eventDepartIsEditable = $scope.isEditable(eleve.eventDepart);
                eleve.eventIncidentIsEditable = $scope.isEditable(eleve.eventIncident);
                eleve.eventObservationIsEditable = $scope.isEditable(eleve.eventObservation);
            });


            // Si l'appel n'est pas éditable, affiche une erreur
            $scope.showError = !$scope.isEditable();

            utils.safeApply($scope);
            $scope.refreshVuesAppel();
            $scope.appel.display = true;
        };

        $scope.setEvenementOfEleves = (eleve) => {
            eleve.eventAbsence = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementAbsence);
            eleve.eventRetard = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementRetard);
            eleve.eventDepart = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementDepart);
            eleve.eventIncident = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementIncident);
            eleve.eventObservation = $scope.getEvenement(eleve, $scope.oEvtType.giIdEvenementObservation);

            eleve.eventAbsenceIsEditable = $scope.isEditable(eleve.eventAbsence);
            eleve.eventRetardIsEditable = $scope.isEditable(eleve.eventRetard);
            eleve.eventDepartIsEditable = $scope.isEditable(eleve.eventDepart);
            eleve.eventIncidentIsEditable = $scope.isEditable(eleve.eventIncident);
            eleve.eventObservationIsEditable = $scope.isEditable(eleve.eventObservation);
        };

        /**
         * Ajoute un retire un jour à la date actuelle
         * @param add   boolean indiquant si le jour doit etre ajouter ou retirer
         */
        $scope.setDate = (add) => {
            let delta = add ? 1 : -1;
            let prevDate = $scope.appel.date;
            $scope.appel.date = new Date(prevDate.getFullYear(), prevDate.getMonth(), prevDate.getDate() + delta);
            $scope.ouvrirAppel();
        };

        /**
         * Charge un appel
         * @param pdtDate la date du jour souhaitée
         */
        $scope.ouvrirAppel = async function (selectedAppel?) {

            $scope.appel.display = false;
            $scope.showError = false;
            $scope.currentCours = undefined;
            $scope.setTime();

            let pdtDate;
            let selectedCours;

            // SelectedAppel indique si l'appel a été ouvert depuis l'écran "appels oubliés" du personnel
            // Si selectedAppel != null, on initialise l'enseignant et la date sélectionnés
            // Si selectedAppel != null, on affiche les cours de la date spécifiée.
            // Si aucune date n'est spécifiée, on sort de la fonction.
            if (selectedAppel != null) {
                pdtDate = moment(selectedAppel.timestamp);

                $scope.poEnseignantRecherche = _.findWhere($scope.structure.enseignants.all, {id: $scope.selectedAppel.id_personnel});
            } else if ($scope.poEnseignantRecherche !== null || $scope.poClasseRecherche !== null || !$scope.isResponsable()) {
                pdtDate = moment($scope.appel.date);
            } else {
                return;
            }

            $scope.detailEleve.displayed = false;

            $scope.pdtDate = pdtDate;

            await $scope.syncDataAppel(pdtDate);

            if (_.isEmpty($scope.structure.courss)) {
                $scope.pbCreneauxOpened = false;
            } else {
                $scope.pbCreneauxOpened = true;

                // Si selectedAppel != null, on ouvre l'appel sélectionné.
                // Sinon, on ouvre l'appel en cours, s'il existe.
                if (selectedAppel != null) {
                    selectedCours = _.findWhere($scope.structure.courss.all, {id: $scope.selectedAppel.id_cours});
                } else {
                    selectedCours = _.find($scope.structure.courss, (cours) => {
                        return (moment().diff(moment(cours.startMoment)) > 0) && (moment().diff(moment(cours.endMoment)) < 0);
                    });
                }

                if (selectedCours !== undefined) {
                    $scope.selectCours(selectedCours);
                }
            }

            utils.safeApply($scope);
        };

        /**
         * Synchronise les cours pour la date passee en parametre
         * @param pdtDate                   date pour laquelle on souhaite visualiser les cours
         * @returns {Promise<T>|Promise}    Promesse attestant de la reussite de la synchronisation
         */
        $scope.syncDataAppel = (pdtDate): Promise<any> => {
            return new Promise(async (resolve, reject) => {
                let sDateDebut = pdtDate.format(FORMAT.date);
                let sDateFin = pdtDate.add(1, 'days').format(FORMAT.date);

                await $scope.structure.classes.sync();
                if ($scope.isResponsable()) {
                    if ($scope.poEnseignantRecherche && !$scope.poClasseRecherche) {
                        await $scope.structure.courss.sync(sDateDebut, sDateFin, $scope.poEnseignantRecherche.id, false
                            , $scope.poEnseignantRecherche.allClasses.map(o => o.name));
                    } else if ($scope.poClasseRecherche && !$scope.poEnseignantRecherche) {
                        await $scope.structure.courss.sync(sDateDebut, sDateFin, $scope.poClasseRecherche.id, true
                            , [$scope.poClasseRecherche.name]);
                    }
                    await $scope.structure.creneaus.sync();
                } else {
                    let arrayClasseName = $scope.structure.classes.filter(classe => model.me.classes.includes(classe.id)).map(o => o.name);
                    await $scope.structure.courss.sync(sDateDebut, sDateFin, model.me.userId, false, arrayClasseName);
                    await $scope.structure.creneaus.sync();
                }
                $scope.structure.creneaus.all.forEach(creneau => {
                    if (creneau.cours !== undefined) {
                        creneau.cours.libelle = $scope.getLibelleClasse(creneau.cours.classeNames);
                    }
                });
                resolve();
            });
        };

        /**
         * Filtre les cours par enseignant ou classe
         * @param isTeacherOrClasse  Indique si l'entite passee en parametre un enseignant ou une classe
         * @param _entity            L'entite utilisee pour filtrer
         */
        $scope.setFiltre = (isTeacherOrClasse, _entity?) => {
            if (isTeacherOrClasse) {
                $scope.poEnseignantRecherche = _entity;
            } else {
                $scope.poClasseRecherche = _entity;
            }
            $scope.ouvrirAppel();
            utils.safeApply($scope);
        };

        /**
         * Rafraichit la liste des élèves entre la vue classe et la vue groupe
         */
        $scope.refreshVuesAppel = function () {
            // fermeture ouverture du template pour rafraichir la vue
            template.close('AbscEleve');
            utils.safeApply($scope);
            template.open('AbscEleve', '../templates/absences/absc_appel_eleve');
            utils.safeApply($scope);
        };

        /**
         * Affiche la vue classes ou eleves
         * @param bShowOrHide true affiche la vue classes, false affiche la vue eleves
         */
        $scope.showVueClasses = function (bShowOrHide) {
            $scope.bClassesVue = bShowOrHide;
            $scope.refreshVuesAppel();
        };

        /**
         * Passage de l'état d'un appel à "Fait"
         */
        $scope.terminerAppel = function () {
            if ($scope.isResponsable()) {
                $scope.currentCours.appel.saisie_cpe = true;
            }
            $scope.changerEtatAppel($scope.etatAppel.giIdEtatAppelFait);
            $scope.succesMessage("L'appel a bien été marqué comme terminé", 5000);
        };

        /**
         * Affichage et temporisation du message de succès de l'appel
         * @param psMessage   Contenu du message
         * @param piDuration  Temps d'affichage du message en millisecondes
         */
        $scope.succesMessage = function (psMessage, piDuration) {
            $scope.messageSuccess = psMessage;
            $scope.showSuccess = true;
            setTimeout(function () {
                $scope.showSuccess = false;
            }, piDuration);
        };

        /**
         * Récupère le libelle d'une classe.
         * @param idClasse l'identifiant de la classe.
         */
        $scope.getLibelleClasse = function (classeNames) {
            classeNames = classeNames.sort((a, b) => a < b);
            return classeNames.join(' - ');
        };

        /**
         * Change l'état d'un appel.
         * @param piIdEtatAppel l'identifiant de l'état souhaité.
         */
        $scope.changerEtatAppel = function (piIdEtatAppel) {
            $scope.currentCours.appel.id_etat = piIdEtatAppel;
            $scope.currentCours.appel.update();
            utils.safeApply($scope);
        };

        /**
         * Convertit une date au format local: [FRANCE].
         * @param dateStr.
         */
        $scope.localeDate = function (dateStr) {
            return moment(new Date(dateStr)).format('DD-MM-YYYY');
        };

        $scope.getFormattedTime = (momentDate) => {
            return moment(momentDate).format('HH:mm');
        };


        if (!$scope.isResponsable()) {
            await $scope.structure.plages.sync();
            await $scope.structure.classes.sync();
            let arrayClasseName = $scope.structure.classes.filter(classe => model.me.classes.includes(classe.id)).map(o => o.name);
            await $scope.structure.courss.sync(moment().format(FORMAT.date), moment().add(1, 'days').format(FORMAT.date)
                , model.me.userId, false, arrayClasseName);

            let currentDate = $scope.appel.date;
            $scope.appel.date = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
            $scope.ouvrirAppel();
        }

        if ($scope.selectedAppel != null) {
            $scope.ouvrirAppel($scope.selectedAppel);
            $scope.appel.date = new Date($scope.selectedAppel.timestamp);
        }
    }
]);