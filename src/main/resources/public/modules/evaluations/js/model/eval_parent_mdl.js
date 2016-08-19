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
function Devoir(){}

function Eleve(){
    this.collection(Periode, {
        sync: function(idEleve){
            if(idEleve !== undefined){
                http().getJson('/viescolaire/evaluations/periodes?idEtablissement='+model.me.structures[0]).done(function(periodes){
                    this.load(periodes);
                }.bind(this));
            }
        }
    });
    this.periodes.on('sync', function(){
        this.trigger('syncPeriodes');
    })
}

function Periode(){
    this.collection(Devoir, {
        sync : function(structureId, userId){
            http().get('/viescolaire/evaluations/devoirs/periode/'+this.model.id+'?idEtablissement='+structureId+'&idUser='+userId).done(
                function(devoirs){
                    this.load(devoirs);
                }.bind(this));
        }
    });
}

Periode.prototype = {
    getReleve : function(idPeriode, idUser) {
        location.replace('/viescolaire/evaluations/releve/pdf?idEtablissement='+model.me.structures[0]+'&idPeriode='+idPeriode+'&idUser='+idUser);
    }
};

function Matiere(){}

model.build = function(){
    angularFilters.addFilters();
    if(this.me.type === 'PERSRELELEVE'){
        this.makeModels([Eleve,Devoir,Periode, Matiere]);
        this.collection(Eleve,{
            sync: function(){
                http().get('/viescolaire/evaluations/enfants?userId='+model.me.userId).done(
                    function(eleves){
                        var listeEleves = [];
                        for(var i = 0; i < eleves.length; i++){
                            listeEleves.push({id:eleves[i]["n.id"], displayName: eleves[i]["n.displayName"], structures:[eleves[i]["s.id"]]});
                        }
                        this.load(listeEleves);
                    }.bind(this));
            }
        });

    }else{
        this.makeModels([Devoir,Periode, Matiere]);
        this.collection(Periode, {
            sync : function(){
                http().getJson('/viescolaire/evaluations/periodes?idEtablissement='+model.me.structures[0]).done(function(periodes){
                    this.load(periodes);
                }.bind(this));
            }
        });
    }

    this.collection(Matiere);
    this.matieres.sync = function(idEleve){
        if(idEleve !== undefined){
            http().getJson('/viescolaire/evaluations/matieres/eleve/'+idEleve).done(function(matieres){
                this.load(JSON.parse(matieres));
            }.bind(this));
        }
    };
};
