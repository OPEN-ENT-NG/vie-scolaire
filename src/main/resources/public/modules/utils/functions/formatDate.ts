/**
 * Created by ledunoiss on 20/09/2016.
 */
let moment = require('moment');

export function getFormatedDate(date, format){
    return moment(date).format(format);
};