/**
 * Created by rahnir on 08/06/2017.
 */
import {template, ng } from 'entcore/entcore';
import * as utils from '../utils/personnel';
import {} from "../../entcore/template";
import {AbsencePrev} from "../models/personnel/AbsencePrev";
import {Evenement} from "../models/personnel/Evenement";



let moment = require('moment');
declare let _: any;

export let abscSaisieElevePersonnel = ng.controller('AbscSaisieElevePersonnel', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('abscDetailTimelineTemplate', '../templates/absences/absc-detail-timeline-cours-template');
        $scope.selected = {
            eleve: '*',
            classe: '*',
            from: '*',
            motif:'*',
            dateDb: new Date(),
            dateFn: new Date(),
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

        $scope.selectEleve = (from) => {
            if (from === 'Search') {
                $scope.selected.classe = '*';
                $scope.display.selection.eleve = false;
            } else if (from === 'Input') {

            }
            $scope.selected.from = from;
            $scope.display.calendarDate = true;
            $scope.initSelectedDates();
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