/**
 * Created by rahnir on 09/08/2017.
 */
import { ng } from 'entcore';

export let periodeSearch = ng.filter('periodeSearch', function(){
    return function(classes, searchParams){
        let output = classes;

        if(searchParams.type != null) {
            output = _.filter(output, (classe) => {
                return classe.periodes.length() == searchParams.type;
            });
        }

        if (searchParams.name && searchParams.name !== '*') {
            let regexp = new RegExp('^'+searchParams.name.toUpperCase());
            output = _.filter(output, (num) => {
                return regexp.test(num.name.toUpperCase());
            });
        }
        return output;
    };
});