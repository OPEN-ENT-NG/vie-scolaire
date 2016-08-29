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
 * Created by ledunoiss on 11/08/2016.
 */
function Structure () {}
function ReleveNote () {
    this.synchronized = {
        classe : false,
        devoirs : false,
        evaluations : false,
        releve : false
    };
    this.periode = model.periodes.findWhere({id : this.idPeriode});
    this.matiere = model.matieres.findWhere({id : this.idMatiere});
    this.classe = new Classe(JSON.parse(JSON.stringify(model.classes.findWhere({id : this.idClasse}))));
    this.classe.eleves.load(JSON.parse(JSON.stringify(model.classes.findWhere({id : this.idClasse}).eleves.all)));
    this.collection(Devoir, {
        // sync : '/viescolaire/evaluations/devoirs?idPeriode=' + this.idPeriode + '&idMatiere=' + this.idMatiere + '&idEtablissement=' + model.me.structures[0] + '&idClasse=' + this.idClasse
        sync : function () {
            if (!model.synchronized.devoirs) {
                model.devoirs.on('sync', function () {
                    console.log(this);
                });
            } else {
                var _devoirs = model.devoirs.where({idperiode : this.composer.idPeriode, idclasse : this.composer.idClasse, idmatiere : this.composer.idMatiere, idetablissement: this.composer.idEtablissement});
                if (_devoirs.length > 0) {
                    this.load(_devoirs);
                    this.trigger('sync');
                }
            }
        }
    });
}
ReleveNote.prototype = {
    sync : function (callback) {
        var that = this;
        var callFormating = function () {
            if(that.synchronized.devoirs && that.synchronized.classe && that.synchronized.evaluations) that.trigger('format');
        }
        this.devoirs.on('sync', function () {
            if (!that.synchronized.devoirs) {
                that.synchronized.devoirs = true;
                callFormating();
            }
        });
        this.devoirs.sync();
        this.classe.eleves.on('sync', function () {
            that.synchronized.classe = true;
            callFormating();
        });
        if (this.classe.eleves.all.length > 0) {
            that.synchronized.classe = true;
            callFormating();
        }
        http().getJson('/viescolaire/evaluations/releve?idEtablissement='+model.me.structures[0]+'&idClasse='+this.idClasse+'&idMatiere='+this.idMatiere+'&idPeriode='+this.idPeriode)
            .done(function (res) {
                that._tmp = res;
                that.synchronized.evaluations = true;
                callFormating();
            });
        this.one('format', function () {
            that.classe.eleves.each(function (eleve) {
                var _evals = [];
                if(that._tmp && that._tmp.length !== 0) var _t = _.where(that._tmp, {ideleve : eleve.id});
                that.devoirs.each(function (devoir) {
                    if (_t && _t.length !== 0) {
                        var _e = _.findWhere(_t, {iddevoir : devoir.id});
                        if (_e) _evals.push(_e);
                        else {
                            _evals.push(new Evaluation({valeur:"", iddevoir : devoir.id, ideleve : eleve.id, ramenersur : devoir.ramenersur, coefficient : devoir.coefficient}));
                        }
                    } else {
                        _evals.push(new Evaluation({valeur:"", iddevoir : devoir.id, ideleve : eleve.id, ramenersur : devoir.ramenersur, coefficient : devoir.coefficient}));
                    }
                });
                eleve.evaluations.load(_evals);
            });
            that.synchronized.releve = true;
            that.calculStatsDevoirs();
            that.calculMoyennesEleves();
            callback();
        });
    },
    calculMoyennesEleves : function () {
        var that = this;
        var _datas = [];
        this.classe.eleves.each(function (eleve) {
            var _t = eleve.evaluations.filter(function (evaluation) { return evaluation.valeur !== "" });
            if(_t.length > 0) {
                var _evals = [];
                for (var i = 0; i < _t.length; i++) {
                    _evals.push(_t[i].formatMoyenne());
                }
                var _o = {
                    id : eleve.id,
                    evaluations : _evals
                }
                _datas.push(_o);
            }
        });
        if (_datas.length > 0) {
            http().postJson('/viescolaire/evaluations/moyennes', {data : _datas}).done(function (res) {
                _.each(res, function (eleve) {
                    var e = that.classe.eleves.findWhere({id:eleve.id});
                    if (e !== undefined) {
                        e.moyenne = eleve.moyenne;
                    }
                });
            });
        }
    },
    calculStatsDevoirs : function (callback) {
        var that = this;
        var _datas = [];
        that.devoirs.each(function (devoir) {
            var _o = {
                id : String(devoir.id),
                evaluations : []
            };
            that.classe.eleves.each(function (eleve) {
                var _e = eleve.evaluations.findWhere({iddevoir : devoir.id});
                if (_e.valeur !== "" && _e !== undefined) _o.evaluations.push(_e.formatMoyenne());
            });
            if(_o.evaluations.length > 0) _datas.push(_o);
        });
        if (_datas.length > 0 ) {
            http().postJson('/viescolaire/evaluations/moyennes?stats=true', {data : _datas}).done(function (res) {
                _.each(res, function (devoir) {
                    var nbEleves = that.classe.eleves.all.length;
                    var nbN = _.findWhere(_datas, { id : devoir.id });
                    var d = that.devoirs.findWhere({id : parseInt(devoir.id)});
                    if (d !== undefined) {
                        d.statistiques = devoir;
                        if (nbN !== undefined) {
                            d.statistiques.percentDone = Math.round((nbN.evaluations.length/nbEleves)*100);
                            if(callback && typeof(callback) === 'function'){
                                callback();
                            }
                        }
                    }
                });
            });
        }
    }
}

function Classe () {
    this.collection(Eleve, {sync : '/directory/class/'+this.id+'/users?type=Student'});
}
function Eleve () {
    this.collection(Evaluation);
}
Eleve.prototype = {
    getMoyenne : function (callback) {
        if (this.evaluations.all.length > 0) {
            var _datas = [];
            for (var i = 0; i < this.evaluations.all.length; i++) {
                if (this.evaluations.all[i].valeur !== "") _datas.push(this.evaluations.all[i].formatMoyenne());
            }
            if (_datas.length > 0) {
                http().postJson('/viescolaire/evaluations/moyenne', {notes : _datas}).done(function (res) {
                    if (_.has(res, "moyenne")) {
                        this.moyenne = res.moyenne;
                        if(callback && typeof(callback) === 'function'){
                            callback();
                        }
                    }
                }.bind(this));
            }
        }
    }
}
function Evaluation () {
    this.collection(CompetenceNote);
}
Evaluation.prototype = {
    toJSON : function () {
        var o = {};
        if(this.id !== null) o.id = this.id;
        o.ideleve  = this.ideleve;
        o.iddevoir = parseInt(this.iddevoir);
        o.valeur   = parseFloat(this.valeur);
        if (this.appreciation) o.appreciation = this.appreciation;
        return o;
    },
    save : function (callback) {
        if (!this.id) this.create(callback);
        else this.update(callback);
    },
    create : function (callback) {
        http().postJson('/viescolaire/evaluations/note', this.toJSON()).done(function (data) {
            if(callback && (typeof(callback) === 'function')) {
                callback(data);
            }
        });
    },
    update : function (callback) {
        http().putJson('/viescolaire/evaluations/note', this.toJSON()).done(function (data) {
            if(callback && typeof(callback) === 'function'){
                callback(data);
            }
        });
    },
    delete : function (callback) {
        http().delete('/viescolaire/evaluations/note?idNote=' + this.id).done(function (data) {
            if(callback && typeof(callback) === 'function'){
                callback(data);
            }
        });
    },
    formatMoyenne : function () {
        return {
            valeur : parseFloat(this.valeur),
            coefficient : this.coefficient,
            ramenersur : this.ramenersur
        }
    }
}
function Devoir () {
    var that = this;
    this.collection(Enseignement);
    this.collection(Competence, {
        sync : '/viescolaire/evaluations/competences/devoir/' + this.id
    });
    this.collection(Eleve, {
        sync : function (callback) {
            var _classe = model.classes.findWhere({id : that.idclasse});
            that.eleves.load(JSON.parse(JSON.stringify(_classe.eleves.all)));
            http().getJson('/viescolaire/evaluations/devoir/' + that.id + '/notes').done(function (res) {
                for (var i = 0; i < res.length; i++) {
                    var _e = that.eleves.findWhere({id : res[i].ideleve});
                    if (_e !== undefined) {
                        _e.evaluation = new Evaluation(res[i]);
                        delete _e.evaluations;
                    }
                }
                _t = that.eleves.filter(function (eleve) {
                    delete eleve.evaluations;
                    return (!_.has(eleve, "evaluation"));
                });
                for (var j = 0; j < _t.length; j++) {
                    _t[j].evaluation = new Evaluation({valeur:"", iddevoir : that.id, ideleve : _t[j].id, ramenersur : that.ramenersur, coefficient : that.coefficient});
                }
                that.syncCompetencesNotes(callback);
            });
        }
    });
}
Devoir.prototype = {
    getLastSelectedCompetence : function (callback) {
        http().getJson('/viescolaire/evaluations/competences/last/devoir/').done(function(competencesLastDevoirList){
            callback(competencesLastDevoirList);
        });
    },
    toJSON : function () {
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
    create : function (callback) {
        http().postJson('/viescolaire/evaluations/devoir', this.toJSON()).done(function(data){
            callback(data);
        });
    },
    update : function () {
        http().putJson('/viescolaire/evaluations/devoir', this).done(function(data){
            model.devoirs.sync();
        });
    },
    save : function () {
        if(!this.id){
            this.create();
        }else{
            this.update();
        }
    },
    remove : function () {
        http().delete('/viescolaire/evaluations/devoir?idDevoir=' + this._id).done(function () {
            model.devoirs.sync();
        });
    },
    calculStats : function (evaluations, callback) {
        if (evaluations.length > 0) {
            var _datas = [];
            for (var i = 0; i < evaluations.length; i++) {
                _datas.push(evaluations[i].formatMoyenne());
            }
            if (_datas.length > 0) {
                http().postJson('/viescolaire/evaluations/moyenne?stats=true', {"notes" : _datas}).done(function (res) {
                    this.statistiques = res;
                    this.statistiques.percentDone = Math.round((_datas.length/this.eleves.all.length)*100);
                    if(callback && typeof(callback) === 'function'){
                        callback();
                    }
                }.bind(this));
            }
        }
    },
    syncCompetencesNotes : function (callback) {
        var that = this;
        http().getJson('/viescolaire/evaluations/competence/notes?devoirId=' + this.id).done(function (res) {
            for (var i = 0; i < that.eleves.all.length; i++) {
                var _comps = _.where(res, {ideleve : that.eleves.all[i].id});
                if (_comps.length > 0) {
                    var _results = [];
                    for (var j = 0; j < that.competences.all.length; j++) {
                        var _c = that.competences.all[j];
                        var _t = _.findWhere(_comps, {idcompetence : _c.idcompetence});
                        if (_t === undefined) {
                            _results.push(new CompetenceNote({idcompetence : _c.idcompetence, nom : _c.nom, iddevoir : that.id, ideleve : that.eleves.all[i].id, evaluation : -1}));
                        } else {
                            _results.push(_t);
                        }
                    }
                    that.eleves.all[i].evaluation.competenceNotes.load(_results);
                } else {
                    var _results = [];
                    for (var j = 0; j < that.competences.all.length; j++) {
                        _results.push(new CompetenceNote({idcompetence : that.competences.all[j].idcompetence, nom : that.competences.all[j].nom, iddevoir : that.id, ideleve : that.eleves.all[i].id, evaluation : -1}));
                    }
                    that.eleves.all[i].evaluation.competenceNotes.load(_results);
                }
            }
            if(callback && (typeof(callback) === 'function')) {
                callback();
            }
        });
    },
    saveCompetencesNotes : function (_data) {
        var that = this;
        if (_data[0].evaluation !== -1){
            var _post = _.filter(_data, function (competence) {
                return competence.id === undefined;
            });
            if (_post.length > 0) {
                http().postJson('/viescolaire/evaluations/competence/notes', {data : _post}).done(function (res) {
                    if (_post.length === _data.length) {
                        that.syncCompetencesNotes(function () {
                            model.trigger('apply');
                        })
                    } else {
                        var _put = _.filter(_data, function (competence) {
                            return competence.id !== undefined;
                        });
                        if (_put.length > 0) {
                            http().putJson('/viescolaire/evaluations/competence/notes', {data : _put}).done(function (res) {
                                that.syncCompetencesNotes(function () {
                                    model.trigger('apply');
                                });
                            });
                        }
                    }
                });
            } else {
                var _put = _.filter(_data, function (competence) {
                    return competence.id !== undefined;
                });
                if (_put.length > 0) {
                    http().putJson('/viescolaire/evaluations/competence/notes', {data : _put}).done(function (res) {
                        that.syncCompetencesNotes(function () {
                            model.trigger('apply');
                        });
                    });
                }
            }
        } else {
            var _delete = [];
            for (var i = 0; i < _data.length; i++) {
                if (_data[i].id !== undefined)_delete.push(_data[i].id);
            }
            if (_delete.length > 0) {
                http().delete('/viescolaire/evaluations/competence/notes', {id : _delete}).done(function (res) {
                    that.syncCompetencesNotes(function () {
                        model.trigger('apply');
                    });
                });
            }
        }
    }
}
function Periode () {}
function Enseignement () {
    this.collection(Competence);
}
function Competence () {
    this.collection(Competence);
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

function Type () {}
function Matiere () {
    this.collection(SousMatiere);
}
function SousMatiere () {}
function CompetenceNote() {}
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
    create : function(callback){
        http().postJson("/viescolaire/evaluations/competence/note", this.toJSON()).done(function(data) {
                callback(data.id); // set de l'id sur la CompetenceNote
            }
        );
    },
    update : function(callback){
        http().putJson("/viescolaire/evaluations/competence/note", this.toJSON()).done(function(data) {
                console.log(data);
            }
        );
    },
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
}

model.build = function () {
    this.synchronized = {
        devoirs : false,
        classes : false,
        matieres : false,
        types : false
    }
    angularDirectives.addDirectives();
    angularFilters.addFilters();
    this.makeModels([Structure, ReleveNote, Classe, Eleve, Evaluation, Devoir, Type, Periode, Enseignement, Competence, CompetenceNote, Matiere, SousMatiere]);

    this.collection(Type, {
        // sync : '/viescolaire/evaluations/types?idEtablissement='+model.me.structures[0]
        sync : function () {
            http().getJson('/viescolaire/evaluations/types?idEtablissement='+model.me.structures[0]).done(function (res) {
                this.load(res);
                model.synchronized.types = true;
            }.bind(this));
        }
    });
    this.collection(Devoir, {
        // sync : '/viescolaire/evaluations/devoirs'
        sync : function () {
            http().getJson('/viescolaire/evaluations/devoirs').done(function (res) {
                this.load(res);
                if (model.synchronized.matieres) {
                    model.devoirs.synchronizeDevoirMatiere();
                } else {
                    model.matieres.on('sync', function () {
                        model.devoirs.synchronizeDevoirMatiere();
                    });
                }
                if (model.synchronized.types) {
                    model.devoirs.synchronizedDevoirType();
                } else {
                    model.type.on('sync', function () {
                        model.devoirs.synchronizedDevoirType();
                    });
                }
            }.bind(this));
        }
    });
    this.collection(Enseignement, {
        // sync : '/viescolaire/evaluations/enseignements'
        sync : function  () {
            http().getJson('/viescolaire/evaluations/enseignements').done(function (res) {
                this.load(res);
                this.each(function (enseignement) {
                    enseignement.competences.load(enseignement['competences_1']);
                    enseignement.competences.each(function (competence) {
                        if (competence['competences_2'].length > 0) {
                            competence.competences.load(competence['competences_2']);
                        }
                        delete competence['competences_2'];
                    });
                    delete enseignement['competences_1'];
                });
            }.bind(this));
        }
    });
    this.collection(Matiere, {
        // sync : '/viescolaire/evaluations/matieres?idEnseignant=' + model.me.userId
        sync : function () {
            http().getJson('/viescolaire/evaluations/matieres?idEnseignant=' + model.me.userId).done(function (res) {
                this.load(res);
                this.each(function (matiere) {
                    matiere.sousMatieres.load(matiere.sous_matieres);
                    model.synchronized.matieres = true;
                    delete matiere.sous_matieres;
                });
            }.bind(this));
        }
    });
    this.collection(Periode, {sync : '/viescolaire/evaluations/periodes?idEtablissement=' + model.me.structures[0]});
    this.collection(ReleveNote);
    this.collection(Classe);
    this.collection(Structure, {
        sync : function () {
            var nb = 0;
            _.each(model.me.structures, function (structureId) {
                http().getJson('/userbook/structure/' + structureId).done(function (structure) {
                    model.structures.push(structure);
                    nb++;
                    if (nb === model.me.structures.length) model.structures.trigger('synchronized')
                });
            });
        }
    });
    this.structures.on('synchronized', function () {
        var _classes = []
        _.each(model.me.classes, function (classe) {
            _classes.push(_.findWhere(model.structures.all[0].classes, {id : classe}));
        });
        model.classes.load(_classes);
        model.synchronized.classes = model.classes.all.length;
        for (var i = 0; i < model.classes.all.length; i++) {
            model.classes.all[i].eleves.sync();
            model.classes.all[i].eleves.one('sync', function () {
                model.synchronized.classes--;
                if (model.synchronized.classes === 0) {
                    model.classes.trigger('classes-sync');
                }
            });
        }
    });
    this.devoirs.on('sync', function () {
        model.synchronized.devoirs = true;
        model.devoirs.getPercentDone();
    });
    this.devoirs.getPercentDone = function () {
        if (model.synchronized.classes !== 0) {
            model.classes.on('classes-sync', function () {
                model.devoirs.getPercentDone();
            });
            return;
        }
        if (this.all.length > 0 ) {
            var _datas = {};
            for (var i = 0; i < model.classes.all.length; i++) {
                _datas[model.classes.all[i].id] = model.classes.all[i].eleves.all.length;
            }
            http().postJson('/viescolaire/evaluations/devoirs/done', {'datas' : _datas}).done(function (res) {
                for (var i = 0; i < this.all.length; i++) {
                    this.all[i].percent = res[this.all[i].id];
                }
                model.trigger('apply');
            }.bind(this));
        }
    }
    this.devoirs.synchronizeDevoirMatiere = function () {
        for (var i = 0; i < model.devoirs.all.length; i++) {
            var matiere = model.matieres.findWhere({id : model.devoirs.all[i].idmatiere});
            if (matiere) model.devoirs.all[i].matiere = matiere;
        }
    };
    this.devoirs.synchronizedDevoirType = function () {
        for (var i = 0 ; i < model.devoirs.all.length; i++) {
            var type = model.types.findWhere({id : model.devoirs.all[i].idtype});
            if (type) model.devoirs.all[i].type = type;
        }
    }
}