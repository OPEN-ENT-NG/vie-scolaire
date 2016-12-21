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
        if (searchParams.classe !== '*' && searchParams.classe !== null) {
            tempTable = _.where(output, {id_classe : searchParams.classe.id});
            output = tempTable;
        }
        if (searchParams.matiere !== '*' && searchParams.matiere !== null) {
            tempTable = _.where(output, {id_matiere : searchParams.matiere.id});
            output = tempTable;
        }
        if (searchParams.sousmatiere !== '*' && searchParams.sousmatiere !== null) {
            tempTable = _.where(output, {id_sousmatiere : parseInt(searchParams.sousmatiere.id)});
            output = tempTable;
        }
        if (searchParams.type !== '*' && searchParams.type !== null) {
            tempTable = _.where(output, {id_type : parseInt(searchParams.type.id)});
            output = tempTable;
        }
        if (searchParams.periode !== '*' && searchParams.periode !== null) {
            tempTable = _.where(output, {id_periode : parseInt(searchParams.periode.id  )});
            output = tempTable;
        }
        if (searchParams.name !== "" && searchParams.name !== null) {
            tempTable = _.filter(output, function (devoir){
                var  reg = new RegExp(searchParams.name.toUpperCase());
                return devoir.name.toUpperCase().match(reg) !== null;
            });
            output = tempTable;
        }
        return output;
    };
});