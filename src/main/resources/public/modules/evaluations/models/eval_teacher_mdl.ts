import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang } from 'entcore/entcore';

let moment = require('moment');
declare let _:any;

export class Structure extends Model {}
export class ReleveNote extends  Model implements IModel{
    synchronized : any;
    periode : Periode;
    matiere : Matiere;
    classe : Classe;
    devoirs : Collection<Devoir>;

    idClasse: string;
    idMatiere: string;
    idPeriode: number;
    _tmp : any;

    get api () {
        return {
            get : '/viescolaire/evaluations/releve?idEtablissement='+model.me.structures[0]+'&idClasse='+this.idClasse+'&idMatiere='+this.idMatiere+'&idPeriode='+this.idPeriode
        }
    }

    constructor () {
        super();
        this.synchronized = {
            classe : false,
            devoirs : false,
            evaluations : false,
            releve : false
        };
        this.periode = evaluations.periodes.findWhere({id : this.idPeriode});
        this.matiere = evaluations.matieres.findWhere({id : this.idMatiere});
        this.classe = new Classe(JSON.parse(JSON.stringify(evaluations.classes.findWhere({id : this.idClasse}))));
        this.classe.eleves.load(JSON.parse(JSON.stringify(evaluations.classes.findWhere({id : this.idClasse}).eleves.all)));

        this.collection(Devoir, {
            // sync : '/viescolaire/evaluations/devoirs?idPeriode=' + this.idPeriode + '&idMatiere=' + this.idMatiere + '&idEtablissement=' + model.me.structures[0] + '&idClasse=' + this.idClasse
            sync : function () {
                if (!evaluations.synchronized.devoirs) {
                    evaluations.devoirs.on('sync', function () {
                        console.log(this);
                    });
                } else {
                    var _devoirs = evaluations.devoirs.where({idperiode : this.composer.idPeriode, idclasse : this.composer.idClasse, idmatiere : this.composer.idMatiere, idetablissement: this.composer.idEtablissement});
                    if (_devoirs.length > 0) {
                        this.load(_devoirs);
                        this.trigger('sync');
                    }
                }
            }
        });
    }

    sync () : Promise {
        return new Promise((resolve, reject) => {
            var that = this;
            var callFormating = function () {
                if(that.synchronized.devoirs && that.synchronized.classe && that.synchronized.evaluations) that.trigger('format');
            };
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
                _.each(that.classe.eleves, function (eleve) {
                    var _evals = [];
                    if(that._tmp && that._tmp.length !== 0) var _t = _.where(that._tmp, {ideleve : eleve.id});
                    _.each(that.devoirs.all, function (devoir) {
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
                resolve();
            });
        });
    }

    calculStatsDevoirs() : Promise {
        return new Promise((resolve, reject) => {
            var that = this;
            var _datas = [];
            _.each(that.devoirs, function (devoir) {
                var _o = {
                    id : String(devoir.id),
                    evaluations : []
                };
                _.each(that.classe.eleves, function (eleve) {
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
                                if(resolve && typeof(resolve) === 'function'){
                                    resolve();
                                }
                            }
                        }
                    });
                });
            }
        });

    }

    calculMoyennesEleves() : Promise {
        return new Promise((resolve, reject) => {
            var that = this;
            var _datas = [];
            _.each(this.classe.eleves, function (eleve) {
                var _t = eleve.evaluations.filter(function (evaluation) {
                    return evaluation.valeur !== ""
                });
                if (_t.length > 0) {
                    var _evals = [];
                    for (var i = 0; i < _t.length; i++) {
                        _evals.push(_t[i].formatMoyenne());
                    }
                    var _o = {
                        id: eleve.id,
                        evaluations: _evals
                    };
                    _datas.push(_o);
                }
            });
            if (_datas.length > 0) {
                http().postJson('/viescolaire/evaluations/moyennes', {data: _datas}).done(function (res) {
                    _.each(res, function (eleve) {
                        var e = that.classe.eleves.findWhere({id: eleve.id});
                        if (e !== undefined) {
                            e.moyenne = eleve.moyenne;
                            if (resolve && typeof(resolve) === 'function') {
                                resolve();
                            }
                        }
                    });
                });
            }
        });
    }
}
export class Classe extends Model {
    eleves : Collection<Eleve>;
    id : number;

    constructor (o? : any) {
        super();
        this.collection(Eleve, {
            sync : '/directory/class/'+this.id+'/users?type=Student'
        });
    }
}

export class Eleve extends Model {
    moyenne: number;
    evaluations : Collection<Evaluation>;
    evaluation : Evaluation;
    id : string;

    constructor () {
        super();
        this.collection(Evaluation);
    }

    getMoyenne () : Promise {
        return new Promise((resolve, reject) => {
            if (this.evaluations.all.length > 0) {
                var _datas = [];
                for (var i = 0; i < this.evaluations.all.length; i++) {
                    if (this.evaluations.all[i].valeur !== "") _datas.push(this.evaluations.all[i].formatMoyenne());
                }
                if (_datas.length > 0) {
                    http().postJson('/viescolaire/evaluations/moyenne', {notes : _datas}).done(function (res) {
                        if (_.has(res, "moyenne")) {
                            this.moyenne = res.moyenne;
                            if(resolve && typeof(resolve) === 'function'){
                                resolve();
                            }
                        }
                    }.bind(this));
                }
            }
        });
    }
}

export class Evaluation extends Model implements IModel{
    valeur : string;
    id : number;
    ideleve : string;
    iddevoir : number;
    valeur : any;
    appreciation : string;
    coefficient : number;
    ramenersur : boolean;
    competenceNotes : Collection<CompetenceNote>;

    get api () {
        return {
            create : '/viescolaire/evaluations/note',
            update : '/viescolaire/evaluations/note',
            delete : '/viescolaire/evaluations/note?idNote=' + this.id
        }
    }

    constructor (o? : any) {
        super();
        this.collection(CompetenceNote);
    }

    toJson () {
        var o = new Evaluation();
        if(this.id !== null) o.id = this.id;
        o.ideleve  = this.ideleve;
        o.iddevoir = parseInt(this.iddevoir);
        o.valeur   = parseFloat(this.valeur);
        if (this.appreciation) o.appreciation = this.appreciation;
        return o;
    }

    save () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            if (!this.id) {
                this.create().then((data) => {
                    resolve(data);
                });
            } else {
                this.update().then((data) =>  {
                    resolve(data);
                });
            }
        });
    }

    create () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.create, this.toJSON()).done(function (data) {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    update() : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.update, this.toJSON()).done(function (data) {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    formatMoyenne() {
        return {
            valeur : parseFloat(this.valeur),
            coefficient : this.coefficient,
            ramenersur : this.ramenersur
        }
    }


}

export class Devoir extends Model implements IModel{
    statistiques : any;
    eleves : Collection<Eleve>;
    matiere : Matiere;

    id : number;
    idclasse : string;
    ramenersur : boolean;
    coefficient : number;
    name : string ;
    owner : string;
    libelle : string;
    idClasse : string;
    idSousMatiere : number;
    idPeriode : number;
    idType : number;
    idMatiere : string;
    idEtat : number;
    datePublication : any;
    idEtablissement : string;
    diviseur : number;
    dateDevoir : any;
    ramenerSur : boolean;
    competences : [];

    get api () {
        return {
            create : '/viescolaire/evaluations/devoir',
            update : '/viescolaire/evaluations/devoir',
            delete : '/viescolaire/evaluations/devoir',
            getCompetencesDevoir : '/viescolaire/evaluations/competences/devoir/' + this.id,
            getCompetencesLastDevoir : '/viescolaire/evaluations/competences/last/devoir/',
            getNotesDevoir : '/viescolaire/evaluations/devoir/' + this.id + '/notes',
            getStatsDevoir : '/viescolaire/evaluations/moyenne?stats=true',
            getCompetencesNotes : '/viescolaire/evaluations/competence/notes?devoirId=' + this.id,
            saveCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            updateCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            deleteCompetencesNotes : '/viescolaire/evaluations/competence/notes'
        }
    }

    constructor () {
        super();
        var that = this;
        this.collection(Enseignement);
        this.collection(Competence, {
            sync : this.api.getCompetencesDevoir
        });
        this.collection(Eleve, {
            sync : function () : Promise {
                return new Promise((resolve, reject) => {
                    var _classe = evaluations.classes.findWhere({id : that.idclasse});
                    that.eleves.load(JSON.parse(JSON.stringify(_classe.eleves.all)));
                    http().getJson(this.api.getNotesDevoir).done(function (res) {
                        for (var i = 0; i < res.length; i++) {
                            var _e = that.eleves.findWhere({id : res[i].ideleve});
                            if (_e !== undefined) {
                                _e.evaluation = new Evaluation(res[i]);
                                delete _e.evaluations;
                            }
                        }
                        var _t = that.eleves.filter(function (eleve) {
                            delete eleve.evaluations;
                            return (!_.has(eleve, "evaluation"));
                        });
                        for (var j = 0; j < _t.length; j++) {
                            _t[j].evaluation = new Evaluation({valeur:"", iddevoir : that.id, ideleve : _t[j].id, ramenersur : that.ramenersur, coefficient : that.coefficient});
                        }
                        that.syncCompetencesNotes().then(() => {
                            resolve();
                        });
                    });
                });
            }
        })
    }

    getLastSelectedCompetence () : Promise<[]> {
        return new Promise((resolve, reject) => {
            http().getJson(this.api.getCompetencesLastDevoir).done(function(competencesLastDevoirList){
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(competencesLastDevoirList);
                }
            });
        });
    }

    toJSON () {
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
    }

    create () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.create, this.toJSON()).done(function(data){
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    update () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.update, this.toJSON()).done(function(data){
                evaluations.devoirs.sync();
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    remove () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete, this.toJSON()).done(function(data){
                evaluations.devoirs.sync();
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    save () : Promise<any> {
        return new Promise((resolve, reject) => {
            if(!this.id){
                this.create().then((data) => {
                    if (resolve && (typeof (resolve) === 'function')) {
                        resolve(data);
                    }
                });
            }else{
                this.update().then((data) => {
                    if (resolve && (typeof (resolve) === 'function')) {
                        resolve(data);
                    }
                });
            }
        });
    }

    calculStats (evaluations : []) : Promise {
        return new Promise((resolve, reject) => {
            if (evaluations.length > 0) {
                var _datas = [];
                for (var i = 0; i < evaluations.length; i++) {
                    _datas.push(evaluations[i].formatMoyenne());
                }
                if (_datas.length > 0) {
                    http().postJson(this.api.getStatsDevoir, {"notes" : _datas}).done(function (res) {
                        this.statistiques = res;
                        this.statistiques.percentDone = Math.round((_datas.length/this.eleves.all.length)*100);
                        if(resolve && typeof(resolve) === 'function'){
                            resolve();
                        }
                    }.bind(this));
                }
            }
        });
    }

    syncCompetencesNotes() : Promise {
        return new Promise((resolve, reject) => {
            var that = this;
            http().getJson(this.api.getCompetencesNotes).done(function (res) {
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
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve();
                }
            });
        });
    }

    saveCompetencesNotes (_data) {
        var that = this;
        if (_data[0].evaluation !== -1){
            var _post = _.filter(_data, function (competence) {
                return competence.id === undefined;
            });
            if (_post.length > 0) {
                http().postJson(this.api.saveCompetencesNotes, {data : _post}).done(function (res) {
                    if (_post.length === _data.length) {
                        that.syncCompetencesNotes().then(() => {
                            evaluations.trigger('apply');
                        });
                    } else {
                        var _put = _.filter(_data, function (competence) {
                            return competence.id !== undefined;
                        });
                        if (_put.length > 0) {
                            http().putJson(this.api.updateCompetencesNotes, {data : _put}).done(function (res) {
                                that.syncCompetencesNotes().then(() => {
                                    evaluations.trigger('apply');
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
                    http().putJson(this.api.updateCompetencesNotes, {data : _put}).done(function (res) {
                        that.syncCompetencesNotes().then(() => {
                            evaluations.trigger('apply');
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
                http().delete(this.api.deleteCompetencesNotes, {id : _delete}).done(function (res) {
                    that.syncCompetencesNotes().then(() => {
                        evaluations.trigger('apply');
                    });
                });
            }
        }
    }
}

export class DevoirsCollection {}

export class Periode extends Model {}

export class Enseignement extends Model {
    constructor () {
        super();
        this.collection(Competence);
    }
}

export class Competence extends Model {
    competences : Collection<Competence>;
    selected : boolean;
    id : number;

    constructor () {
        super();
        this.collection(Competence);
    }

    selectChildren (bool) : Promise {
        return new Promise((resolve, reject) => {
            if(this.competences.all.length !== 0){
                _.each(this.competences.all, function(child){
                    child.selected = bool;
                    child.selectChildren(bool).then(resolve);
                });
            }else{
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve();
                }
            }
        });
    }

    findSelectedChildren () {
        if(this.selected === true){
            evaluations.competencesDevoir.push(this.id);
        }
        if(this.competences.all.length !== 0){
            _.each(this.competences.all, function(child){
                child.findSelectedChildren();
            });
        }
    }
}

export class Type extends Model {}
export class Matiere extends Model {

    constructor () {
        super();
        this.collection(SousMatiere);
    }
}

export class SousMatiere extends Model {}
export class CompetenceNote extends Model implements IModel {
    id: number;
    iddevoir: number;
    idcompetence: number;
    evaluation: number;
    ideleve: string;

    get api() {
        return {
            create: '/viescolaire/evaluations/competence/note',
            update: '/viescolaire/evaluations/competence/note',
            delete: '/viescolaire/evaluations/competence/note?idNote=' + this.id
        }
    }

    constructor(o? : any) {
        super();
    }

    toJSON() {
        return {
            id: this.id,
            iddevoir: this.iddevoir,
            idcompetence: this.idcompetence,
            evaluation: this.evaluation,
            ideleve: this.ideleve
        }
    }

    create(): Promise<number> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.create, this.toJSON()).done((data) => {
                if (resolve && (typeof (resolve) === 'undefined')) {
                    resolve(data.id);
                }
            });
        });
    }

    update(): Promise {
        return new Promise((resolve, reject) => {
            http().putJson("/viescolaire/evaluations/competence/note", this.toJSON()).done(function (data) {
                if (resolve && (typeof (resolve) === 'undefined')) {
                    resolve();
                }
            });
        });
    }

    delete(): Promise {
        return new Promise((resolve, reject) => {
            http().delete("/viescolaire/evaluations/competence/note?idNote=" + this.id).done(function (data) {
                if (resolve && (typeof (resolve) === 'undefined')) {
                    resolve();
                }
            });
        });
    }

    save(): Promise<any> {
        return new Promise((resolve, reject) => {
            if (!this.id) {
                this.create().then((data) => {
                    if (resolve && (typeof (resolve) === 'undefined')) {
                        resolve(data);
                    }
                });
            } else {
                this.update();
            }
        });
    }
}

export class Evaluations extends Model {
    periodes : Collection<Periode>;
    type : Collection<Type>;
    devoirs : Collection<Devoir>;
    enseignements : Collection<Enseignement>;
    matieres : Collection<Matiere>;
    releveNotes : Collection<ReleveNote>;
    classes : Collection<Classe>;
    structures : Collection<Structure>;

    synchronized : any;
    competencesDevoir : [];

    get api () {
        return {
            getTypes : '/viescolaire/evaluations/types?idEtablissement='+model.me.structures[0]
        }
    }

    constructor () {
        super();
    }

    sync () {
        this.collection(Type, {
            sync : function () {
                http().getJson(this.api.getTypes).done(function (res) {
                    this.load(res);
                    evaluations.synchronized.types = true;
                }.bind(this));
            }
        });
        this.collection(Devoir, {
            // sync : '/viescolaire/evaluations/devoirs'
            sync : function () {
                http().getJson('/viescolaire/evaluations/devoirs').done(function (res) {
                    this.load(res);
                    if (evaluations.synchronized.matieres) {
                        evaluations.devoirs.synchronizeDevoirMatiere();
                    } else {
                        evaluations.matieres.on('sync', function () {
                            evaluations.devoirs.synchronizeDevoirMatiere();
                        });
                    }
                    if (evaluations.synchronized.types) {
                        evaluations.devoirs.synchronizedDevoirType();
                    } else {
                        evaluations.type.on('sync', function () {
                            evaluations.devoirs.synchronizedDevoirType();
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
                        evaluations.synchronized.matieres = true;
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
                        evaluations.structures.push(structure);
                        nb++;
                        if (nb === model.me.structures.length) evaluations.structures.trigger('synchronized')
                    });
                });
            }
        });
        this.structures.on('synchronized', function () {
            var _classes = []
            _.each(model.me.classes, function (classe) {
                _classes.push(_.findWhere(evaluations.structures.all[0].classes, {id : classe}));
            });
            evaluations.classes.load(_classes);
            evaluations.synchronized.classes = evaluations.classes.all.length;
            for (var i = 0; i < evaluations.classes.all.length; i++) {
                evaluations.classes.all[i].eleves.sync();
                evaluations.classes.all[i].eleves.one('sync', function () {
                    evaluations.synchronized.classes--;
                    if (evaluations.synchronized.classes === 0) {
                        evaluations.classes.trigger('classes-sync');
                    }
                });
            }
        });
        this.devoirs.on('sync', function () {
            evaluations.synchronized.devoirs = true;
            evaluations.devoirs.getPercentDone();
        });
        this.devoirs.getPercentDone = function () {
            if (evaluations.synchronized.classes !== 0) {
                evaluations.classes.on('classes-sync', function () {
                    evaluations.devoirs.getPercentDone();
                });
                return;
            }
            if (this.all.length > 0 ) {
                var _datas = {};
                for (var i = 0; i < evaluations.classes.all.length; i++) {
                    _datas[evaluations.classes.all[i].id] = evaluations.classes.all[i].eleves.all.length;
                }
                http().postJson('/viescolaire/evaluations/devoirs/done', {'datas' : _datas}).done(function (res) {
                    for (var i = 0; i < this.all.length; i++) {
                        this.all[i].percent = res[this.all[i].id];
                    }
                    model.trigger('apply');
                }.bind(this));
            }
        };
        this.devoirs.synchronizeDevoirMatiere = function () {
            for (var i = 0; i < evaluations.devoirs.all.length; i++) {
                var matiere = evaluations.matieres.findWhere({id : evaluations.devoirs.all[i].idmatiere});
                if (matiere) evaluations.devoirs.all[i].matiere = matiere;
            }
        };
        this.devoirs.synchronizedDevoirType = function () {
            for (var i = 0 ; i < model.devoirs.all.length; i++) {
                var type = evaluations.types.findWhere({id : evaluations.devoirs.all[i].idtype});
                if (type) evaluations.devoirs.all[i].type = type;
            }
        }
    }

}

export let evaluations = new Evaluations();