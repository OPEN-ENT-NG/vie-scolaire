/**
 * Created by ledunoiss on 20/09/2016.
 */
import {ng} from 'entcore/entcore';

let moment = require('moment');
declare let _:any;

export let customSearchFilter = ng.filter('customSearchFilters', function(){
    return function(devoirs, searchParams){
        var output = devoirs;
        var tempTable = [];
        if (searchParams.classe !== '*') {
            tempTable = _.where(output, {id_classe : searchParams.classe.id});
            output = tempTable;
        }
        if (searchParams.matiere !== '*') {
            tempTable = _.where(output, {id_matiere : searchParams.matiere.id});
            output = tempTable;
        }
        if (searchParams.sousmatiere !== '*') {
            tempTable = _.where(output, {id_sousmatiere : parseInt(searchParams.sousmatiere.id)});
            output = tempTable;
        }
        if (searchParams.type !== '*') {
            tempTable = _.where(output, {id_type : parseInt(searchParams.type.id)});
            output = tempTable;
        }
        if (searchParams.periode !== '*') {
            tempTable = _.where(output, {id_periode : parseInt(searchParams.periode.id  )});
            output = tempTable;
        }
        if (searchParams.name !== "") {
            tempTable = _.filter(output, function (devoir){
                var  reg = new RegExp(searchParams.name.toUpperCase());
                return devoir.name.toUpperCase().match(reg) !== null;
            });
            output = tempTable;
        }
        if (moment(searchParams.dateCreation.debut).diff(moment(searchParams.dateCreation.fin)) < 0) {
            tempTable = _.filter(output, function (devoir) {
                return (moment(devoir.date).diff(moment(searchParams.dateCreation.debut)) >= 0) && (moment(devoir.date).diff(moment(searchParams.dateCreation.fin)) <= 0);
            });
            output = tempTable;
        }
        if (moment(searchParams.datePublication.debut).diff(moment(searchParams.datePublication.fin)) < 0) {
            tempTable = _.filter(output, function (devoir) {
                return (moment(devoir.date_publication).diff(moment(searchParams.datePublication.debut)) >= 0) && (moment(devoir.datepublication).diff(moment(searchParams.datePublication.fin)) <= 0);
            });
            output = tempTable;
        }
        return output;
    };
});