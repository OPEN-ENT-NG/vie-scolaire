/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

/**
 * Created by ledunoiss on 08/08/2016.
 */
var angularFilters = {
    addFilters : function(){
        module.filter('unique', function() {
            return function(collection, keyname) {
                var output = [],
                    keys = [];

                angular.forEach(collection, function(item) {
                    var key = item[keyname];
                    if(keys.indexOf(key) === -1) {
                        keys.push(key);
                        output.push(item);
                    }
                });
                return output;
            };
        });
        module.filter('customSearchFilters', function(){
            return function(devoirs, searchParams){
                var output = devoirs;
                var tempTable = [];
                if(searchParams.idclasse !== '*'){
                    output = _.where(devoirs, {idclasse : searchParams.idclasse});
                }
                if(searchParams.idmatiere !== '*'){
                    tempTable = _.where(output, {idmatiere : searchParams.idmatiere});
                    output = tempTable;
                }
                if(searchParams.idsousmatiere !== '*'){
                    tempTable = _.where(output, {idsousmatiere : parseInt(searchParams.idsousmatiere)});
                    output = tempTable;
                }
                if(searchParams.idtype !== '*'){
                    tempTable = _.where(output, {idtype : parseInt(searchParams.idtype)});
                    output = tempTable;
                }
                if(searchParams.idperiode !== '*'){
                    tempTable = _.where(output, {idperiode : parseInt(searchParams.idperiode)});
                    output = tempTable;
                }
                if(searchParams.name !== ""){
                    tempTable = _.filter(output, function(devoir){
                        var  reg = new RegExp(searchParams.name);
                        return devoir.name.match(reg) !== null;
                    });
                    output = tempTable;
                }
                return output;
            };
        });
        module.filter('getMatiereParClasse', function(){
            return function(matieres, idClasse, classes){
                if(idClasse !== undefined){
                    var libelleClasse = _.findWhere(classes, {id : idClasse});
                    return _.where(matieres, {libelleClasse : libelleClasse.name});
                }
            };
        });
        module.filter('uniqueMatieres', function() {
            return function(input, collection, keyname, devoirs) {
                var output = [],
                    keys = [];

                angular.forEach(collection, function(item) {
                    var key = item[keyname];
                    if(keys.indexOf(key) === -1) {
                        keys.push(key);
                        output.push(item);
                    }
                });
                var t = _.filter(output, function(item){
                    return devoirs.findWhere({ idmatiere : item.id }) !== undefined;
                });
                return t;
            };
        });
    }
};
