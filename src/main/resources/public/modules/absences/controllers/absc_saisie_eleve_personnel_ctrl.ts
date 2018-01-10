/**
 * Created by rahnir on 08/06/2017.
 */
import {template, ng } from 'entcore/entcore';
import * as utils from '../utils/personnel';
import {} from "../../entcore/template";
import {AbsencePrev} from "../models/personnel/AbsencePrev";
import {Evenement} from "../models/personnel/Evenement";
import {safeApply} from "../../utils/functions/safeApply";



let moment = require('moment');
declare let _: any;

export let abscSaisieElevePersonnel = ng.controller('AbscSaisieElevePersonnel', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('abscDetailTimelineTemplate', '../templates/absences/absc-detail-timeline-cours-template');

        // variables d'affichage
        $scope.display.calendar = false;
        $scope.display.showEleveCard = false;
        $scope.display.showLastAbsences = false;

        $scope.showLightboxAllAbsences = (state) => {
            $scope.displayLightboxAllAbsences = state;
        };

        $scope.$watch("selected.dateDb",  (newVal, oldVal) => {
           if( newVal > $scope.selected.dateFn ) {$scope.selected.dateDb = oldVal ; }
           else{
               let firstweek = moment($scope.selected.dateDb);
               model.calendar.setDate(firstweek);
               $scope.checkNavigDate();
               $scope.selectPeriode();
               model.calendar.addScheduleItems($scope.selected.eleve.cours);
           }
        });
        $scope.$watch("selected.dateFn",  (newVal, oldVal) => {
            if( newVal < $scope.selected.dateDb ){$scope.selected.dateFn = oldVal ; }
            else{
                let firstweek = moment($scope.selected.dateDb);
                model.calendar.setDate(firstweek);
                $scope.checkNavigDate();
                $scope.selectPeriode();
                model.calendar.addScheduleItems($scope.selected.eleve.cours);
            }
        });
        $scope.selectPeriode = async () => {
            let i = _.indexOf($scope.structure.eleves.all, $scope.selected.eleve);
            let firstDate = moment($scope.selected.dateDb).hour(0).minute(0).format('YYYY-MM-DD');
            let endDate = moment($scope.selected.dateFn).hour(0).minute(0).format('YYYY-MM-DD');
            if(i>-1){
                $scope.structure.eleves.all[i].cours = [];
                for(let j=0 ; j < $scope.structure.eleves.all[i].className.length; j++){
                    await  $scope.structure.eleves.all[i].syncCoursByStudid($scope.structure.id,firstDate,endDate,$scope.structure.eleves.all[i].className[j]);
                }
                for(let z=0 ; z < $scope.structure.eleves.all[i].groupName.length; z++){
                    await $scope.structure.eleves.all[i].syncCoursByStudid($scope.structure.id,firstDate,endDate,$scope.structure.eleves.all[i].groupName[z])
                }
                await  $scope.structure.eleves.all[i].formatCourses($scope.structure.matieres.all , $scope.structure.enseignants.all,$scope.selected.dateDb,$scope.selected.dateFn);
                $scope.selected.eleve=$scope.structure.eleves.all[i];
                $scope.display.calendar = true  ;
                $scope.checkNavigDate();
                safeApply($scope);
            }
        };

        //sync les [classes - groupes - cours ] d'eleve
        $scope.syncEleveCours = async (eleve) => {
            let i = _.indexOf($scope.structure.eleves.all, eleve);
            if($scope.structure.eleves.all[i].synchronized.className == false ) {
                await $scope.structure.eleves.all[i].syncClasseGroupName($scope.structure.classes.all,'classe');
            }
            if($scope.structure.eleves.all[i].synchronized.groupName == false ){
                await  $scope.structure.eleves.all[i].syncClasseGroupName($scope.structure.classes.all,'group');
            }
            await $scope.selectPeriode() ;
            return $scope.structure.eleves.all[i] ;
        };

        // Selection d'un élève dans la barre de recherche
        $scope.selectEleve = async () => {
            $scope.display.calendar = false;
            $scope.display.selection.eleve = false;
            $scope.checkNavigDate();
             $scope.selected.eleve = await $scope.syncEleveCours($scope.selected.eleve) ;
            $scope.selected.eleve.cours ?  $scope.display.calendar = true :  $scope.display.calendar = false ;
            safeApply($scope);
            // On récupère les absences et les absences prev de l'élève
            await $scope.selected.eleve.syncAllAbsence(false);
            await $scope.selected.eleve.syncAllAbsencePrev();



                    // Mise en forme des absence
                    $scope.selected.eleve.evenements.forEach(function (event) {
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
            dateDb: new moment(model.calendar.dayForWeek).hour(0).minute(0).format('YYYY-MM-DD'),
            dateFn: new moment(model.calendar.dayForWeek).add(6, 'day').hour(0).minute(0).format('YYYY-MM-DD'),
            //dateDbOld : new moment(model.calendar.dayForWeek).hour(0).minute(0).format('YYYY-MM-DD'),
           // dateFnOld : new moment(model.calendar.dayForWeek).add(7, 'day').hour(0).minute(0).format('YYYY-MM-DD'),
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
        $scope.allowDisplay = {
            prev : false,
            next : false,

        };
        $scope.checkNavigDate = () => {
            moment($scope.selected.dateFn).hour(0).minute(0).format('YYYY-MM-DD') <  moment(model.calendar.dayForWeek).add(7,'day').hour(0).minute(0).format('YYYY-MM-DD') ?  $scope.allowDisplay.next=  false :  $scope.allowDisplay.next=true;
            moment($scope.selected.dateDb).hour(0).minute(0).format('YYYY-MM-DD') >=  moment(model.calendar.dayForWeek).hour(0).minute(0).format('YYYY-MM-DD')?   $scope.allowDisplay.prev =   false :  $scope.allowDisplay.prev = true;
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


        $scope.nextWeekButton = function() {
            let next = moment(model.calendar.firstDay).add(7, 'day');
            $scope.checkNavigDate();
            if($scope.allowDisplay.next == true){
                model.calendar.setDate(next);
                model.calendar.addScheduleItems($scope.selected.eleve.cours);
                $scope.checkNavigDate();
            }
        };

        $scope.previousWeekButton = function() {
            let prev = moment(model.calendar.firstDay).subtract(7, 'day');
            $scope.checkNavigDate();
            if($scope.allowDisplay.prev == true){
                model.calendar.setDate(prev);
                model.calendar.addScheduleItems($scope.selected.eleve.cours);
                $scope.checkNavigDate();
            }
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