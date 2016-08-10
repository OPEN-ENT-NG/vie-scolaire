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
function Devoir(){
    this.collection(Competence,{
        sync : '/viescolaire/evaluations/competences/devoir/'+this.id
    });
    if(this.composer instanceof ReleveNote){
        this.competences.sync();
    }
    this.collection(Enseignement);
}

Devoir.prototype = {
    toJSON : function(){
        return {
            name            : this.name,
            owner           : this.owner,
            libelle         : this.libelle,
            idclasse        : this.idClasse,
            idsousmatiere   : parseInt(this.idSousMatiere),
            idperiode       : parseInt(this.idPeriode),
            idtype          : parseInt(this.idType),
            idmatiere       : this.idMatiere,
            idetat          : parseInt(this.idEtat),
            datepublication : this.datePublication,
            idetablissement : this.idEtablissement,
            diviseur        : this.diviseur,
            coefficient     : this.coefficient,
            date            : this.dateDevoir,
            ramenersur      : this.ramenerSur,
            competences     : this.competences
        };
    },
    create : function(callback){
        http().postJson('/viescolaire/evaluations/devoir', this.toJSON()).done(function(data){
            callback(data);
        });
    },
    update : function(){
        http().putJson('/viescolaire/evaluations/devoir', this).done(function(data){
            model.devoirs.sync();
        });
    },
    save : function(){
        if(!this.id){
            this.create();
        }else{
            this.update();
        }
    },
    remove : function(){
        http().delete('/viescolaire/evaluations/devoir?idDevoir='+this._id).done(function(){
            model.devoirs.sync();
        });
    }
};


function Note(){}

Note.prototype = {
    toJSON : function(){
        var o = {};
        if(this.id !== null){
            o.id = this.id;
        }
        o.ideleve  = this.ideleve;
        o.iddevoir = parseInt(this.iddevoir);
        o.valeur   = parseFloat(this.valeur);
        o.appreciation = this.appreciation;
        return o;
    },
    create : function(idDevoir, callback){
        http().postJson("/viescolaire/evaluations/note", this.toJSON()).done(function(data) {
            if(callback && (typeof(callback) === 'function')) {
                callback(data.id);
            }
        });
    },
    update : function(callback){
        http().putJson("/viescolaire/evaluations/note",this.toJSON()).done(function(data){
            if(callback && typeof(callback) === 'function'){
                callback(data);
            }
        });
    },
    save : function(idDevoir, callback){
        if(!this.id){
            this.create(idDevoir, callback);
        }else{
            this.update(callback);
        }
    },
    delete : function(idNote){
        http().delete("/viescolaire/evaluations/devoir/note?idNote="+idNote).done(function(data){
            console.log(data);
        });
    },
    castNote : function(object){
        this.id           = object.id;
        this.iddevoir     = object.iddevoir;
        this.iddispense   = object.iddispense;
        this.ideleve      = object.ideleve;
        this.appreciation = object.appreciation;
        this.valeur       = parseFloat(object.valeur);
    },
    toReleveJson : function(){
        return {
            coefficient : this.coefficient,
            ramenerSur : this.ramenersur,
            valeur : parseFloat(this.valeur)
        };
    }
};

function Competence(){
    this.collection(Competence, {
        sync : function(id){
            var that = this;
            http().getJson('/viescolaire/evaluations/competence/'+id+'/competences').done(function(competences){
                competences = _.map(competences, function(competence){ competence.composer = that.composer; return competence;});
                this.load(competences);
            }.bind(this));
        }
    });
    this.competences.sync(this.id);
}

Competence.prototype = {
    selectChildren : function(bool, callback){
        if(this.competences.all.length !== 0){
            this.competences.each(function(child){
                child.selected = bool;
                child.selectChildren(bool, callback);
            });
        }else{
            callback();
        }
    },
    findSelectedChildren : function(){
        if(this.selected === true){
            model.competencesDevoir.push(this.id);
        }
        if(this.competences.all.length !== 0){
            this.competences.each(function(child){
                child.findSelectedChildren();
            });
        }
    }
};

//function Connaissance(){}

function Enseignement(){
    this.collection(Competence, {
        sync : function(id){
            var that = this;
            http().getJson( '/viescolaire/evaluations/enseignement/'+id+'/competences').done(function(competences){
                competences = _.map(competences, function(competence){ competence.composer = that.composer; return competence;});
                this.load(competences);
            }.bind(this));
        }
    });
    this.competences.sync(this.id);
}

function SousMatiere(){}

function Matiere(){
    this.collection(SousMatiere, {
        sync : '/viescolaire/evaluations/matieres/'+this.id+'/sousmatieres'
    });
    this.sousMatieres.sync();
    this.sousMatieres.on('sync', function(){
        this.composer.trigger('sync');
    })
}

function Eleve(){
    if(this.composer instanceof ReleveNote){
        this.collection(Note, {
            sync : function(){
                var that = this;
                http().getJson('/viescolaire/evaluations/releve?idEleve='+this.composer.id+'&idEtablissement='+this.composer.composer.idEtablissement+
                    '&idClasse='+this.composer.composer.idClasse+'&idMatiere='+this.composer.composer.idMatiere+'&idPeriode='+this.composer.composer.idPeriode)
                    .done(function(data){
                        data = _.map(data, function(item){ item.ideleve = that.composer.id; return item;});
                        this.load(data);
                        this.composer.composer.eleves.trigger('calculMoyenne');
                    }.bind(this));
            }
        });
        this.notes.sync();
    }
}

function ReleveNote(){
    this.collection(Devoir, {
        sync: function(idEtablissement, idClasse, idPeriode, idMatiere){
            var that = this;
            http().getJson('/viescolaire/evaluations/devoir?idEtablissement='+idEtablissement+'&idClasse='+idClasse+'&idMatiere='+idMatiere+'&idPeriode='+idPeriode).done(function(data){
                data = _.map(data, function(item){ item.composer= that.composer; return item; });
                this.load(data);
            }.bind(this));
        }
    });
    this.collection(Eleve, {
        sync : function(idClasse){
            var that = this;
            http().getJson('/directory/class/'+idClasse+'/users?type=Student').done(function(data){
                data = _.map(data, function(item){ item.composer= that.composer; return item; });
                this.load(data);
            }.bind(this));
        }
    });
    this.devoirs.sync(this.idEtablissement, this.idClasse, this.idPeriode, this.idMatiere);
    this.eleves.sync(this.idClasse);
    this.sync = function(){
        this.devoirs.sync(this.idEtablissement, this.idClasse, this.idPeriode, this.idMatiere);
        this.eleves.sync(this.idClasse);
    };
}


function CompetenceNote(){}

CompetenceNote.prototype = {
    toJSON : function(){
        return {
            id       : this.id,
            iddevoir  : this.iddevoir,
            idcompetence : this.idcompetence,
            evaluation   : this.evaluation,
            ideleve       : this.ideleve
        };
    },
    // cree l'evaluation d'une competence
    create : function(callback){
        http().postJson("/viescolaire/evaluations/competence/note", this.toJSON()).done(function(data) {
                callback(data.id); // set de l'id sur la CompetenceNote
            }
        );
    },
    // update l'evaluation d'une competence
    update : function(callback){
        http().putJson("/viescolaire/evaluations/competence/note", this.toJSON()).done(function(data) {
                console.log(data);
            }
        );
    },
    //cree ou update l'evaluation d'une competence
    save : function(callback){
        if(!this.id){
            this.create(callback);
        }else{
            this.update();
        }
    },
    delete : function(idCompetenceNote){
        http().delete("/viescolaire/evaluations/competence/note?idNote="+idCompetenceNote).done(function(data){
            console.log(data);
        });
    }
};


model.build = function(){
    angularDirectives.addDirectives();
    angularFilters.addFilters();
    this.makeModels([Devoir, Note, CompetenceNote, Competence, Enseignement, Matiere, SousMatiere, ReleveNote, Eleve]);
    this.collection(Devoir,{
        sync : "/viescolaire/evaluations/devoirs"
    });
    this.collection(Enseignement, {
        sync : "/viescolaire/evaluations/enseignements"
    });
    this.collection(Matiere, {
        sync : "/viescolaire/evaluations/matieres?idEnseignant="+model.me.userId
    });
    this.collection(ReleveNote);
};

