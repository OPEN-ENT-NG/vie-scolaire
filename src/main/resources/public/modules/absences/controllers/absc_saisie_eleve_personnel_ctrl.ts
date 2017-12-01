/**
 * Created by rahnir on 08/06/2017.
 */
import { template, ng } from 'entcore';
import * as utils from '../utils/personnel';
import {AbsencePrev} from "../models/personnel/AbsencePrev";
import {Evenement} from "../models/personnel/Evenement";



let moment = require('moment');
declare let _: any;

export let abscSaisieElevePersonnel = ng.controller('AbscSaisieElevePersonnel', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('abscDetailTimelineTemplate', '../templates/absences/absc-detail-timeline-cours-template');

        // variables d'affichage

        $scope.display.showEleveCard = false;
        $scope.display.showLastAbsences = false;

        $scope.showLightboxAllAbsences = (state) => {
            $scope.displayLightboxAllAbsences = state;
        };

        $scope.showConfirmDelete = (event) => {
            if(event){
                $scope.selectedEvent = event;
                $scope.displayConfirmDelete = true;
            }else{
                $scope.displayConfirmDelete = false;
            }
        };

        $scope.showLightboxConfirmDelete = (event) => {
            if(event){
                $scope.selectedEvent = event;
                $scope.displayLightboxConfirmDelete = true;
            }else{
                $scope.displayLightboxConfirmDelete = false;
            }
        };

        // Selection d'un élève dans la barre de recherche
        $scope.selectEleve = () => {
            $scope.display.selection.eleve = false;

            // On récupère les absences et les absences prev de l'élève
            $scope.selected.eleve.syncAllAbsence(false).then(()=> {
                $scope.selected.eleve.syncAllAbsencePrev().then(()=> {

                    // Mise en forme des absence
                    $scope.selected.eleve.evenements.forEach(function (event) {
                        event.motif = $scope.structure.motifs.find(motif => motif.id == event.id_motif);
                        event.niceDateDebut = moment(event.timestamp_dt).format('DD/MM/YYYY h:mm');
                        event.niceDateFin = moment(event.timestamp_fn).format('DD/MM/YYYY h:mm');
                    });

                    // Mise en forme des absences prev + lien entre absences prev et absences normales
                    $scope.selected.eleve.abscprev.forEach(function (abscprev) {
                        // On ajoute un tableau pour y mettre les absences contenues dans la période de l'absence prévisionelle
                        abscprev.absences = [];

                        // Boolean permettant de différencier l'absence prev de l'absence normal pour un affichage dynamique
                        abscprev.isAbsencePrev = true;

                        // Mise en forme de la date
                        abscprev.niceDateDebut = moment(abscprev.timestamp_dt).format('DD/MM/YYYY h:mm');
                        abscprev.niceDateFin = moment(abscprev.timestamp_fn).format('DD/MM/YYYY h:mm');
                        abscprev.motif = $scope.structure.motifs.find(motif => motif.id == abscprev.id_motif);

                        $scope.selected.eleve.evenements.forEach(function (absence) {
                            // Si l'absence normale est à l'intérieur de la période de l'absence prev
                            if(!absence.alreadyInAbscPrev && moment(abscprev.timestamp_dt) < moment(absence.timestamp_dt)
                                &&  moment(absence.timestamp_fn) < moment(abscprev.timestamp_fn)) {
                                // On ajoute un booléen pour montrer qu'on a déjà rattaché cette absence à une absence prev
                                absence.alreadyInAbscPrev = true;
                                // On ajoute cette absence normale aux absences de l'absence prev
                                abscprev.absences.push(absence);
                            }
                        });
                    });

                    // On rassemble les absences non rattachées aux absences prev afin de les afficher dynamiquement
                    var absencesAlone = $scope.selected.eleve.evenements.filter(event => !event.alreadyInAbscPrev);
                    var absencesToShow = $scope.selected.eleve.abscprev.concat(absencesAlone);

                    // On retrie par date
                    absencesToShow.sort(function(a,b){
                        return new Date(b.timestamp_dt) - new Date(a.timestamp_dt);
                    });

                    // On les ajoutes au scope
                    $scope.selected.eleve.absencesToShow = absencesToShow;

                    $scope.display.showEleveCard = true;
                    $scope.display.showLastAbsences = true;
                    utils.safeApply($scope);
                }).catch(e => {
                    console.log(e);
                });
            }).catch(e => {
                console.log(e);
            });
        };



        $scope.selected = {
            eleve: '*',
            classe: '*',
            from: '*',
            motif:'*',
            dateDb: new Date,
            dateFn: new Date,
            timeDb:  moment().format("HH:mm"),
            timeFn:  moment().format("HH:mm"),
        };
        $scope.display = {
            selection: {
                eleve: false
            },
            coursDetail: false,
            previsCour :false,
            calendarDate: false
        };
        $scope.syncEleveClasse = () => {
            if ($scope.selected.classe !== '*' && $scope.selected.classe !== null) {
                $scope.display.calendarDate = false;
                $scope.initSelectedDates();
                $scope.display.selection.eleve = false;
                $scope.selected.eleve = '*';
                $scope.selected.classe.eleves.sync().then(() => {
                    $scope.display.selection.eleve = true;
                    utils.safeApply($scope);
                });
            }
        };
        $scope.initSelectedDates = () => {
            $scope.selected.dateDb = new Date();
            $scope.selected.dateFn = new Date();
            $scope.selected.timeDb = moment().format("HH:mm");
            $scope.selected.timeFn = moment().format("HH:mm");
        };


        $scope.searchCours = () => {
            $scope.selected.motif= '*';
            let syncCours = false;
            let syncEvent = false;
            let syncAbscPrev = false;
            if($scope.selected.from === 'Search')
                $scope.selected.eleve.syncCoursByStud($scope.structure.id, $scope.selected.dateDb , $scope.selected.timeDb+":00", $scope.selected.dateFn, $scope.selected.timeFn + ":00")
                    .then((data) => {
                        syncCours = true;
                        utils.safeApply($scope);
                    });
            else if($scope.selected.from === 'Input')
                $scope.selected.eleve.syncCoursByClasseStud($scope.selected.classe.id, $scope.selected.dateDb, $scope.selected.timeDb+":00", $scope.selected.dateFn, $scope.selected.timeFn+":00")
                    .then((data) => {
                        syncCours = true;
                        utils.safeApply($scope);
                    });
            $scope.selected.eleve.syncEvenment($scope.selected.dateDb, $scope.selected.dateFn).then( () =>  syncEvent = true   );
            $scope.selected.eleve.syncAbscPrev($scope.selected.dateDb, $scope.selected.dateFn).then( () =>  syncAbscPrev = true  );
            $scope.$watch(() =>{return syncCours && syncEvent && syncAbscPrev;},(newVal,oldval) =>{
                if(newVal===true){
                    $scope.getCoursEvnt();
                    $scope.display.coursDetail = true;
                    utils.safeApply($scope);
                } });

        };

        $scope.verifierCoursPrev = (index) =>{
            if(index===0) {
                if ($scope.selected.eleve.cours[index].timestamp_fn > moment().format()){
                    $scope.selected.eleve.cours[index].etatcours = 'NOTEND';
                    return true;
                }else{
                    $scope.selected.eleve.cours[index].etatcours = 'END';
                    return false;
                }
            }else {
                if($scope.selected.eleve.cours[index].timestamp_fn > moment().format() &&
                    $scope.selected.eleve.cours[index-1].timestamp_fn < moment().format() ){
                    $scope.selected.eleve.cours[index].etatcours = 'NOTEND';
                    return true;
                }else if($scope.selected.eleve.cours[index].timestamp_fn > moment().format() &&
                    $scope.selected.eleve.cours[index-1].timestamp_fn > moment().format()){
                    $scope.selected.eleve.cours[index].etatcours = 'NOTEND';
                    return false;
                }
                else{$scope.selected.eleve.cours[index].etatcours = 'END';
                    return false;}

            }
        };
        $scope.getNowDate = () => {
            return moment().format('L') + ' '+ moment().format( 'HH:mm');
        };
        $scope.getCoursEvnt = () => {
            _.map($scope.selected.eleve.evenements.all,(event) => {
                return  event.motifs = _.findWhere($scope.structure.motifs.all, { 'id' : event.id_motif });
            });
            _.map($scope.selected.eleve.cours,(c) => {
                _.find($scope.selected.eleve.abscprev, (abs) => {
                    ( moment( abs.timestamp_fn).format('L') >= moment(c.timestamp_dt).format('L') && moment( abs.timestamp_dt).format('L') <= moment(c.timestamp_dt).format('L') ) ||
                    ( moment( abs.timestamp_fn).format('L') >=  moment(c.timestamp_fn).format('L') && moment( abs.timestamp_dt).format('L') <= moment(c.timestamp_fn).format('L') )?
                        c.abcsPrev = true : c.abcsPrev = false;

                });

                if(c.id_appel !== null){

                    let eventCours =  _.where( $scope.selected.eleve.evenements.all, { 'id_appel' : c.id_appel });
                    if(eventCours === undefined || eventCours.length === 0 ){
                        c.isAbsent = false;
                        return  c.evenement = undefined ;
                    }else{
                        let absent =  _.findWhere(eventCours, {'id_type': 1 });
                        if(absent !== undefined )
                        {c.isAbsent = true;
                            return c.evenement = absent ;}
                        else
                        { c.isAbsent = false;
                            return c.evenement = eventCours ; }

                    }
                }else{
                    c.isAbsent = false;
                    return  c.evenement = undefined ;
                }
            });
        };
        $scope.formatDate = function (datedb, datefn) {

            if (moment(datedb).format('L') == moment(datefn).format('L'))
                return moment(datedb).format( 'DD/MM/YYYY') + '\n' + moment(datedb).format( 'HH:mm') + ' -> ' + moment(datefn,).format( 'HH:mm');
            else
                return moment(datedb).format( 'DD/MM/YYYY') + ' ' + moment(datedb).format( 'HH:mm') + ' -> ' + moment(datefn).format( 'DD/MM/YYYY') + ' ' + moment(datefn,).format( 'HH:mm');
        };

        $scope.getColorByEvent = function (evnt) {
            let color;
            if( evnt !== undefined ){
                switch (evnt.id_type) {
                    case 1 : {
                        color = 'red';
                    }
                        break;
                    case 2 : {
                        color = 'blue';
                    }
                        break;
                    case 3 : {
                        color = 'green';
                    }
                        break;
                    case 5 : {
                        color = 'purple';
                    }
                        break;

                    default : {
                        color = 'grey';
                    }
                }
            }else color =  'grey';
            return color;
        };

        $scope.getMatiere = (id_matiere) => {
            let matiere = _.findWhere($scope.structure.matieres.all, {id: id_matiere});
            if (matiere === undefined)
                return 'undefinie';
            else
                return matiere.name;
        };
        $scope.detectDiffMotif = (newVal) => {

            if(newVal!== '' && newVal!== '*' && newVal!== undefined && newVal!== null && typeof(newVal) == "object"){
                _.map($scope.selected.eleve.cours,(c) => {
                    if(c.isAbsent === true){
                        return  c.evenement.motifs.id !== newVal.id ? c.diffMotif = true : c.diffMotif = false;
                    }

                });
                utils.safeApply($scope);
            }else{
                _.map($scope.selected.eleve.cours,(c) => {
                    if(c.isAbsent === true) {
                        return  c.diffMotif = false;
                    }
                });
                utils.safeApply($scope);
            }

        };


        $scope.saveAbsc = () => {
            let coursEND = _.groupBy(_.where($scope.selected.eleve.cours,  {'etatcours' : 'END' }), 'isAbsent');

            let coursWhereIsAbs = coursEND.true;
            let coursWhereIsNotAbs = coursEND.false ;
            let motifid = $scope.selected.motif.id; //TODO <obligatoir>
            let dateDb;
            let dateFn =  moment($scope.selected.dateFn).format();
            //absPrev
            if( moment().format() < moment($scope.selected.dateFn).format() ){
                moment().format() > moment($scope.selected.dateDb).format()?
                    dateDb= moment().format("YYYY/MM/DD"):
                    dateDb= moment($scope.selected.dateDb).format("YYYY/MM/DD");
                let AbscPrev = new AbsencePrev( {
                    timestamp_dt : dateDb,
                    timestamp_fn : moment($scope.selected.dateFn).format("YYYY/MM/DD"),
                    id_eleve : $scope.selected.eleve.id,
                    id_motif : motifid,
                });
                AbscPrev.save();
            }
            //absNormal
            if( moment().format() > moment($scope.selected.dateDb).format() ){
                //Update EVENT
                if(coursWhereIsAbs!==undefined){
                 let eventIds =  _.pluck(coursWhereIsAbs,'id');
                 let Event = new Evenement();
                 //Event.Saves(coursEND, Event);
                }
            }
        }
    }
]);