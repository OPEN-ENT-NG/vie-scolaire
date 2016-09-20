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
import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang } from 'entcore/entcore';

let moment = require('moment');
declare let _:any;

export class Devoir extends Model {}
export class Eleve extends Model {
    periodes : Collection<Periode>;
    id : any;

    get api () {
        return {
            get : '/viescolaire/evaluations/periodes?idEtablissement='+model.me.structures[0]
        };
    }

    constructor () {
        super();
        this.collection(Periode, {
            sync : () => {
                if (this.id !== undefined) {
                    http().getJson(this.api.get).done(function (perdiodes) {
                        this.periodes.load(perdiodes);
                    });
                }
            }
        });
        this.periodes.on('sync', function () {
            this.trigger('syncPeriodes');
        });
    }
}

export class Periode extends Model {
    devoirs : Collection<Devoir>;
    id : any;
    moyenne : number;

    get api () {
        return {
            calculMoyenne : '/viescolaire/evaluations/moyenne'
        }
    }

    constructor () {
        super();
        this.collection(Devoir, {
            sync : (structureId, userId) => {
                http().getJson('/viescolaire/evaluations/devoirs/periode/' + this.id + '?idEtablissement=' + structureId + '&idUser=' + userId)
                    .done((devoirs) => {
                        this.devoirs.load(devoirs);
                    })
            }
        })
    }

    getReleve (idPeriode, idUser) {
        location.replace('/viescolaire/evaluations/releve/pdf?idEtablissement='+model.me.structures[0]+'&idPeriode='+idPeriode+'&idUser='+idUser);
    }

    calculMoyenne () : Promise<number> {
        return new Promise((resolve, reject) => {
            if (this.devoirs.all.length > 0) {
                http().postJson(this.api.calculMoyenne, {notes : this.devoirs.all}).done((res) => {
                    if (_.has(res, "moyenne")) {
                        this.moyenne = res.moyenne;
                        if(resolve && typeof(resolve) === 'function'){
                            resolve(res.moyenne);
                        }
                    }
                });
            }
        });
    }
}

export class Matiere extends Model {}

export class Evaluations extends Model{
    eleves : Collection<Eleve>;
    matieres : Collection<Matiere>;
    periodes : Collection<Periode>;

    constructor () {
        super();
    }

    sync () {
        if(model.me.type === 'PERSRELELEVE'){
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
    }
}

export let evaluations = new Evaluations();

model.build = function () {
    (this as any).evaluations = evaluations;
    evaluations.sync();
};

