/**
 * Created by rahnir on 09/08/2017.
 */
import {ng} from 'entcore/entcore';

let moment = require('moment');
declare let _:any;

export let customSearchClasse = ng.filter('customSearchClasse', function(){
    return function(classes, searchParams){
        let output = classes;
        let tempTable = [];

        if (searchParams.name !== '*' && searchParams.name !== null && searchParams.name !== '') {
            let regexp = new RegExp('^'+searchParams.name.toUpperCase());
            tempTable = _.filter(output, function(num){ return regexp.test(num.name.toUpperCase()) ; });
            output = tempTable;
        }
        return output;
    };
});