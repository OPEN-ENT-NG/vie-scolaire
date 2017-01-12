import {model, http, IModel, Model, Collection, idiom as lang} from 'entcore/entcore';
import * as utils from '../utils/teacher';

let moment = require('moment');
let $ = require('jquery');
declare let _:any;

export class Structure extends Model {
    classes : any;

    constructor (o? : any) {
        super();
        if (o) this.updateData(o);
        this.collection(Classe);
    }
}
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

    constructor (o? : any) {
        super();
        if (o && o !== undefined) this.updateData(o);
        var that = this;
        this.synchronized = {
            classe : false,
            devoirs : false,
            evaluations : false,
            releve : false
        };
        this.periode = evaluations.periodes.findWhere({id : this.idPeriode});
        this.matiere = evaluations.matieres.findWhere({id : this.idMatiere});
        var c = evaluations.classes.findWhere({id : this.idClasse});
        this.classe = new Classe({id : c.id, name: c.name });
        var _e = $.extend(true, {}, evaluations.classes.findWhere({id : this.idClasse}).eleves.all);
        this.classe.eleves.load($.map(_e, function (el) {
            return el;
        }));

        this.collection(Devoir, {
            sync : function () {
                if (!evaluations.synchronized.devoirs) {
                    evaluations.devoirs.on('sync', function () {
                        console.log(this);
                    });
                } else {
                    var _devoirs = evaluations.devoirs.where({id_periode : this.composer.idPeriode, id_classe : this.composer.idClasse, id_matiere : this.composer.idMatiere, id_etablissement: this.composer.idEtablissement});
                    if (_devoirs.length > 0) {
                        this.load(_devoirs);
                        that.trigger('format');
                        this.trigger('sync');
                    }
                }
            }
        });
    }

    sync () : Promise<any> {
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
            this.on('format', function () {
                _.each(that.classe.eleves.all, function (eleve) {
                    var _evals = [];
                    if(that._tmp && that._tmp.length !== 0) var _t = _.where(that._tmp, {id_eleve : eleve.id});
                    _.each(that.devoirs.all, function (devoir) {
                        if (_t && _t.length !== 0) {
                            var _e = _.findWhere(_t, {id_devoir : devoir.id});
                            if (_e) {
                                _e.oldValeur = _e.valeur;
                                _e.oldAppreciation = _e.appreciation !== undefined ? _e.appreciation : '';
                                _evals.push(_e);
                            }
                            else {
                                _evals.push(new Evaluation({valeur:"", oldValeur : "", appreciation : "", oldAppreciation : "", id_devoir : devoir.id, id_eleve : eleve.id, ramener_sur : devoir.ramener_sur, coefficient : devoir.coefficient}));
                            }
                        } else {
                            _evals.push(new Evaluation({valeur:"", oldValeur : "", appreciation : "", oldAppreciation : "", id_devoir : devoir.id, id_eleve : eleve.id, ramener_sur : devoir.ramener_sur, coefficient : devoir.coefficient}));
                        }
                    });
                    eleve.evaluations.load(_evals);
                });
                resolve();
            });
        });
    }

    calculStatsDevoirs() : Promise<any> {
        return new Promise((resolve, reject) => {
            var that = this;
            var _datas = [];
            _.each(that.devoirs.all, function (devoir) {
                var _o = {
                    id : String(devoir.id),
                    evaluations : []
                };
                _.each(that.classe.eleves.all, function (eleve) {
                    var _e = eleve.evaluations.findWhere({id_devoir : devoir.id});
                    if (_e !== undefined && _e.valeur !== "") _o.evaluations.push(_e.formatMoyenne());
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

    calculMoyennesEleves() : Promise<any> {
        return new Promise((resolve, reject) => {
            var that = this;
            var _datas = [];
            _.each(this.classe.eleves.all, function (eleve) {
                var _t = eleve.evaluations.filter(function (evaluation) {
                    return evaluation.valeur !== "" && evaluation.valeur !== null && evaluation.valeur !== undefined;
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
    name : string;
    suiviCompetenceClasse : Collection<SuiviCompetenceClasse>;
    mapEleves : any;

    get api () {
        return {
            sync : '/directory/class/'+this.id+'/users?type=Student'
        }
    }

    constructor (o? : any) {
        super();
        if (o !== undefined) this.updateData(o);
        this.collection(Eleve, {
            sync : () : Promise<any> => {
                var that = this;
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.sync).done(function (data) {
                        this.eleves.load(data);
                        this.mapEleves = {};
                        for (var i = 0; i < this.eleves.all.length; i++) {
                            this.mapEleves[this.eleves.all[i].id]= this.eleves.all[i];
                        }
                        resolve();
                    }.bind(this));
                });
            }
        });
        this.collection(SuiviCompetenceClasse);
    }
}

export class Eleve extends Model implements IModel{
    moyenne: number;
    evaluations : Collection<Evaluation>;
    evaluation : Evaluation;
    id : string;
    suiviCompetences : Collection<SuiviCompetence>;

    get api() {
        return {
            getMoyenne : '/viescolaire/evaluations/moyenne'
        }
    }

    constructor () {
        super();
        var that = this;
        this.collection(Evaluation);
        this.collection(SuiviCompetence);
    }

    getMoyenne () : Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.evaluations.all.length > 0) {
                var _datas = [];
                for (var i = 0; i < this.evaluations.all.length; i++) {
                    if (this.evaluations.all[i].valeur !== "") _datas.push(this.evaluations.all[i].formatMoyenne());
                }
                if (_datas.length > 0) {
                    http().postJson( this.api.getMoyenne, {notes : _datas}).done(function (res) {
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
    id : number;
    id_eleve : string;
    id_devoir : number;
    valeur : any;
    appreciation : any;
    coefficient : number;
    ramener_sur : boolean;
    competenceNotes : Collection<CompetenceNote>;
    oldValeur : any;
    oldAppreciation : any;

    get api () {
        return {
            create : '/viescolaire/evaluations/note',
            update : '/viescolaire/evaluations/note?idNote=' + this.id,
            delete : '/viescolaire/evaluations/note?idNote=' + this.id
        }
    }

    constructor (o? : any) {
        super();
        if (o) this.updateData(o);
        this.collection(CompetenceNote);
    }

    toJSON () {
        var o = new Evaluation();
        if(this.id !== null) o.id = this.id;
        o.id_eleve  = this.id_eleve;
        o.id_devoir = parseInt(this.id_devoir);
        o.valeur   = parseFloat(this.valeur);
        if (this.appreciation) o.appreciation = this.appreciation;
        delete o.competenceNotes;
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

    update () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.update, this.toJSON()).done(function (data) {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    delete () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete).done(function (data) {
                if(resolve && typeof(resolve) === 'function'){
                    resolve(data);
                }
            });
        });
    }

    formatMoyenne() {
        return {
            valeur : parseFloat(this.valeur),
            coefficient : this.coefficient,
            ramenersur : this.ramener_sur
        }
    }


}

export class Devoir extends Model implements IModel{
    statistiques : any;
    eleves : Collection<Eleve>;
    matiere : Matiere;
    type : Type;
    competences : Collection<Competence>;
    competenceEvaluee : CompetenceNote;

    // DATABASE FIELDS
    id : number;
    id_classe : string;
    ramener_sur : boolean;
    coefficient : number;
    name : string ;
    owner : string;
    libelle : string;
    id_sousmatiere : number;
    id_periode : number;
    id_type : number;
    id_matiere : string;
    id_etat : number;
    date_publication : any;
    id_etablissement : string;
    diviseur : number;
    date : any;
    is_evaluated  : boolean;
    that: any;
    competencesAdd: any;
    competencesRem: any;


    get api () {
        return {
            create : '/viescolaire/evaluations/devoir',
            update : '/viescolaire/evaluations/devoir?idDevoir=',
            delete : '/viescolaire/evaluations/devoir?idDevoir=',
            getCompetencesDevoir : '/viescolaire/evaluations/competences/devoir/',
            getCompetencesLastDevoir : '/viescolaire/evaluations/competences/last/devoir/',
            getNotesDevoir : '/viescolaire/evaluations/devoir/' + this.id + '/notes',
            getStatsDevoir : '/viescolaire/evaluations/moyenne?stats=true',
            getCompetencesNotes : '/viescolaire/evaluations/competence/notes/devoir/',
            saveCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            updateCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            deleteCompetencesNotes : '/viescolaire/evaluations/competence/notes'
        }
    }

    constructor(p? : any) {
        super();
        var that = this;
        this.collection(Enseignement);
        this.collection(Competence, {
            sync : function () : Promise<any> {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.getCompetencesDevoir + that.id).done(function(res) {
                        this.load(res);
                        if(resolve && (typeof(resolve) === 'function')) {
                            resolve();
                        }
                    }.bind(this));
                });
            }
        });
        this.collection(Eleve, {
            sync : function () : Promise<any> {
                return new Promise((resolve, reject) => {
                    var _classe = evaluations.classes.findWhere({id : that.id_classe});
                    // that.eleves.load(JSON.parse(JSON.stringify(_classe.eleves.all)));
                    // that.eleves.load($.extend(true, {}, JSON.stringify(_classe.eleves.all)));
                    var e = $.map($.extend(true, {}, _classe.eleves.all), function (el) {
                        return el;
                    });
                    that.eleves.load(e);
                    http().getJson(that.api.getNotesDevoir).done(function (res) {
                        for (var i = 0; i < res.length; i++) {
                            var _e = that.eleves.findWhere({id : res[i].id_eleve});
                            if (_e !== undefined) {
                                _e.evaluation = new Evaluation(res[i]);
                                _e.evaluation.oldValeur = _e.evaluation.valeur;
                                _e.evaluation.oldAppreciation = _e.evaluation.appreciation !== undefined ? _e.evaluation.appreciation : '';
                                delete _e.evaluations;
                            }
                        }
                        var _t = that.eleves.filter(function (eleve) {
                            delete eleve.evaluations;
                            return (!_.has(eleve, "evaluation"));
                        });
                        for (var j = 0; j < _t.length; j++) {
                            _t[j].evaluation = new Evaluation({valeur:"", oldValeur : "", appreciation : "", oldAppreciation : "", id_devoir : that.id, id_eleve : _t[j].id, ramener_sur : that.ramener_sur, coefficient : that.coefficient});
                        }
                        that.syncCompetencesNotes().then(() => {
                            if(resolve && (typeof(resolve) === 'function')) {
                                resolve();
                            }
                        });
                    });
                });
            }
        });
        if (p) this.updateData(p);
    }

    getLastSelectedCompetence () : Promise<[any]> {
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
            id_classe        : this.id_classe,
            id_sousmatiere   : parseInt(this.id_sousmatiere),
            id_periode       : parseInt(this.id_periode),
            id_type          : parseInt(this.id_type),
            id_matiere       : this.id_matiere,
            id_etat          : parseInt(this.id_etat),
            date_publication : this.date_publication,
            id_etablissement : this.id_etablissement,
            diviseur        : this.diviseur,
            coefficient     : this.coefficient,
            date            : this.date,
            ramener_sur      : this.ramener_sur,
            is_evaluated     : this.is_evaluated,
            competences     : this.competences,
            competenceEvaluee : this.competenceEvaluee,
            competencesAdd : null,
            competencesRem : null
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

    update (addArray, remArray) : Promise<any> {
        return new Promise((resolve, reject) => {
            var devoirJSON = this.toJSON();
            devoirJSON.competencesAdd = addArray;
            devoirJSON.competencesRem = remArray;
            devoirJSON.competences = [];
            http().putJson(this.api.update + this.id, devoirJSON).done(function(data){
                evaluations.devoirs.sync();
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    remove () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete + this.id).done(function(data){
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            })
                .error(function () {
                    reject();
                });
        });
    }

    save (add,rem) : Promise<any> {
        return new Promise((resolve, reject) => {
            if(!this.id){
                this.create().then((data) => {
                    if (resolve && (typeof (resolve) === 'function')) {
                        resolve(data);
                    }
                });
            }else{
                this.update(add, rem).then((data) => {
                    if (resolve && (typeof (resolve) === 'function')) {
                        resolve(data);
                    }
                });
            }
        });
    }

    calculStats (evaluations : any) : Promise<any> {
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

    syncCompetencesNotes() : Promise<any> {
        return new Promise((resolve, reject) => {
            var that = this;
            http().getJson(that.api.getCompetencesNotes + that.id).done(function (res) {
                for (var i = 0; i < that.eleves.all.length; i++) {
                    var _comps = _.where(res, {id_eleve : that.eleves.all[i].id});
                    if (_comps.length > 0) {
                        var _results = [];
                        for (var j = 0; j < that.competences.all.length; j++) {
                            var _c = that.competences.all[j];
                            var _t = _.findWhere(_comps, {id_competence : _c.id_competence});
                            if (_t === undefined) {
                                _results.push(new CompetenceNote({id_competence : _c.id_competence, nom : _c.nom, id_devoir : that.id, id_eleve : that.eleves.all[i].id, evaluation : -1}));
                            } else {
                                _results.push(_t);
                            }
                        }
                        that.eleves.all[i].evaluation.competenceNotes.load(_results);
                    } else {
                        var _results = [];
                        for (var j = 0; j < that.competences.all.length; j++) {
                            _results.push(new CompetenceNote({id_competence : that.competences.all[j].id_competence, nom : that.competences.all[j].nom, id_devoir : that.id, id_eleve : that.eleves.all[i].id, evaluation : -1}));
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
                            var url = that.api.updateCompetencesNotes + "?";
                            for (var i = 0 ; i < _put.length; i++) {
                                url += "id="+_put[i].id+"&";
                            }
                            url = url.slice(0, -1);
                            http().putJson(url, {data : _put}).done(function (res) {
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
                    var url = that.api.updateCompetencesNotes + "?";
                    for (var i = 0 ; i < _put.length; i++) {
                        url += "id="+_put[i].id+"&";
                    }
                    url = url.slice(0, -1);
                    http().putJson(url, {data : _put}).done(function (res) {
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

export class DevoirsCollection {
    all : Devoir[];
    sync : any;

    get api () {
        return {
            get : '/viescolaire/evaluations/devoirs'
        }
    }

    constructor () {
        this.sync = function () {
            http().getJson(this.api.get).done(function (res) {
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
                    evaluations.types.on('sync', function () {
                        evaluations.devoirs.synchronizedDevoirType();
                    });
                }
                evaluations.devoirs.trigger('sync');
            }.bind(this));
        }
    }

    synchronizeDevoirMatiere () {
        for (var i = 0; i < evaluations.devoirs.all.length; i++) {
            var matiere = evaluations.matieres.findWhere({id : evaluations.devoirs.all[i].id_matiere});
            if (matiere) evaluations.devoirs.all[i].matiere = matiere;
        }
    }

    synchronizedDevoirType () {
        for (var i = 0 ; i < evaluations.devoirs.all.length; i++) {
            var type = evaluations.types.findWhere({id : evaluations.devoirs.all[i].id_type});
            if (type) evaluations.devoirs.all[i].type = type;
        }
    }

    getPercentDone () {
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
    }
}

export interface Devoirs extends Collection<Devoir>, DevoirsCollection {}

export class Periode extends Model {
    id : number;
    timestamp_dt : any;
    timestamp_fn : any;
}

export class Enseignement extends Model {
    competences : Collection<Competence>;

    constructor () {
        super();
        this.collection(Competence);
    }
}

export class Domaine extends Model {
    domaines : Collection<Domaine>;
    competences : Collection<Competence>;
    id : number;
    niveau : number;
    id_parent : number;
    moyenne : number;
    libelle : string;
    codification : string;
    composer : any;
    evaluated : boolean;

    constructor (poDomaine?) {
        super();
        var that = this;
        this.collection(Competence);
        this.collection(Domaine);

        if(poDomaine !== undefined) {

            var sousDomaines = poDomaine.domaines;
            var sousCompetences = poDomaine.competences;

            this.updateData(poDomaine);

            if(sousDomaines !== undefined) {
                this.domaines.load(sousDomaines);
            }

            if(sousCompetences !== undefined) {
                this.competences.load(sousCompetences);
            }
        }
    }

}

export class Competence extends Model {
    competences : Collection<Competence>;
    selected : boolean;
    id : number;
    id_competence : number;
    nom : string;
    code_domaine : string;
    composer : any;

    constructor () {
        super();
        this.collection(Competence);
    }

    selectChildren (bool) : Promise<any> {
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

export class Type extends Model {
    id : number;
}
export class Matiere extends Model {

    constructor () {
        super();
        this.collection(SousMatiere);
    }
}

export class SousMatiere extends Model {}
export class CompetenceNote extends Model implements IModel {
    id: number;
    id_devoir: number;
    id_competence: number;
    evaluation: number;
    id_eleve: string;

    get api() {
        return {
            create: '/viescolaire/evaluations/competence/note',
            update: '/viescolaire/evaluations/competence/note?id=' + this.id,
            delete: '/viescolaire/evaluations/competence/note?id=' + this.id
        }
    }

    constructor(o? : any) {
        super();
        if (o !== undefined) this.updateData(o);
    }

    toJSON() {
        return {
            id: this.id,
            id_devoir: this.id_devoir,
            id_competence: this.id_competence,
            evaluation: this.evaluation,
            id_eleve: this.id_eleve
        }
    }

    create(): Promise<number> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.create, this.toJSON()).done((data) => {
                this.id = data.id;
                if (resolve && (typeof (resolve) === 'undefined')) {
                    resolve(data.id);
                }
            });
        });
    }

    update(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.update, this.toJSON()).done(function (data) {
                if (resolve && (typeof (resolve) === 'undefined')) {
                    resolve();
                }
            });
        });
    }

    delete(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete).done(function (data) {
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
    types : Collection<Type>;
    devoirs : Devoirs;
    enseignements : Collection<Enseignement>;
    matieres : Collection<Matiere>;
    releveNotes : Collection<ReleveNote>;
    classes : Collection<Classe>;
    structures : Collection<Structure>;

    synchronized : any;
    competencesDevoir : any[];

    constructor () {
        super();
        this.synchronized = {
            devoirs : false,
            classes : false,
            matieres : false,
            types : false
        };
    }

    sync () {
        this.collection(Type, {
            sync : function () {
                http().getJson('/viescolaire/evaluations/types?idEtablissement='+model.me.structures[0]).done(function (res) {
                    this.load(res);
                    evaluations.synchronized.types = true;
                }.bind(this));
            }
        });
        this.types.sync();
        this.collection(Devoir, new DevoirsCollection());
        this.devoirs.sync();
        this.collection(Enseignement, {
            // sync : '/viescolaire/evaluations/enseignements'
            sync : function  (idCycle?:any) {
                var uri = '/viescolaire/evaluations/enseignements';
                if (idCycle !== undefined) {
                    uri += '?idCycle='+idCycle;
                }
                http().getJson(uri).done(function (res) {
                    this.load(res);
                    var that = this;
                    this.each(function (enseignement) {
                        enseignement.competences.load(enseignement['competences_1']);
                        _.map(enseignement.competences.all, function(competence) {
                            return competence.composer = enseignement;
                        });
                        enseignement.competences.each(function (competence) {
                            if (competence['competences_2'].length > 0) {
                                competence.competences.load(competence['competences_2']);
                                _.map(competence.competences.all, function (sousCompetence) {
                                    return sousCompetence.composer = competence;
                                });
                            }
                            delete competence['competences_2'];
                        });
                        delete enseignement['competences_1'];
                    });
                }.bind(this));
            }
        });
        this.enseignements.sync();
        this.collection(Matiere, {
            sync : function () {
                http().getJson('/viescolaire/matieres?idEnseignant=' + model.me.userId).done(function (res) {
                    this.load(res);
                    this.each(function (matiere) {
                        matiere.sousMatieres.load(matiere.sous_matieres);
                        evaluations.synchronized.matieres = true;
                        delete matiere.sous_matieres;
                    });
                }.bind(this));
            }
        });
        this.matieres.sync();
        this.collection(Periode, {sync : '/viescolaire/evaluations/periodes?idEtablissement=' + model.me.structures[0]});
        this.collection(ReleveNote);
        this.collection(Classe);
        this.collection(Structure, {
            sync : function () {
                var nb = 0;
                _.each(model.me.structures, function (structureId) {
                    http().getJson('/userbook/structure/' + structureId).done(function (structure) {
                        evaluations.structures.all.push(structure);
                        nb++;
                        if (nb === model.me.structures.length) evaluations.structures.trigger('synchronized')
                    });
                });
            }
        });
        this.structures.on('synchronized', function () {
            var _classes = [];
            _.each(model.me.classes, function (classe) {
                _classes.push(_.findWhere(evaluations.structures.all[0].classes, {id : classe}));
            });
            evaluations.classes.load(_classes);
            evaluations.synchronized.classes = evaluations.classes.all.length;
            for (var i = 0; i < evaluations.classes.all.length; i++) {
                evaluations.classes.all[i].eleves.sync().then(() => {
                    evaluations.synchronized.classes--;
                    if (evaluations.synchronized.classes === 0) {
                        evaluations.classes.trigger('classes-sync');
                    }
                });
            }
        });
        this.structures.sync();
        this.devoirs.on('sync', function () {
            evaluations.synchronized.devoirs = true;
        });
    }

}

export class SuiviCompetenceClasse extends Model implements IModel{
    domaines : Collection<Domaine>;
    competenceNotes : Collection<CompetenceNote>;
    periode : Periode;

    get api() {
        return {
            getCompetencesNotesClasse : '/viescolaire/evaluations/competence/notes/classe/',
            getArbreDomaines : '/viescolaire/evaluations/domaines/classe/'
        }
    }

    constructor (classe : Classe, periode : any) {
        super();
        this.periode = periode;
        var that = this;

        this.collection(Domaine, {
            sync: function () {
                return new Promise((resolve, reject) => {
                    var url = that.api.getArbreDomaines + classe.id;
                    http().getJson(url).done((resDomaines) => {
                        var url = that.api.getCompetencesNotesClasse + classe.id;
                        if (periode !== null && periode !== undefined && periode !== '*') {
                            url += "?idPeriode="+periode.id;
                        }
                        http().getJson(url).done((resCompetencesNotes) => {
                            if(resDomaines) {
                                for(var i=0; i<resDomaines.length; i++) {
                                    var domaine = new Domaine(resDomaines[i]);
                                    that.domaines.all.push(domaine);
                                    setCompetenceNotes(domaine, resCompetencesNotes, this, classe);
                                }
                            }
                        });
                        if (resolve && typeof (resolve) === 'function') {
                            resolve();
                        }
                    });
                });
            }
        });

    }

    findCompetence (idCompetence) {
        for(var i=0; i<this.domaines.all.length; i++) {
            var comp = findCompetenceRec(idCompetence, this.domaines.all[i].competences);
            if(comp !== undefined) {
                return comp;
            } else {
                continue;
            }
        }
        return false;
    }


    sync () : Promise<any> {
        return new Promise((resolve, reject) => {
            resolve();
        });
    }
}


export class SuiviCompetence extends Model implements IModel{
    domaines : Collection<Domaine>;
    competenceNotes : Collection<CompetenceNote>;
    periode : Periode;
    classe : Classe;

    get api() {
        return {
            getCompetencesNotes : '/viescolaire/evaluations/competence/notes/eleve/',
            getArbreDomaines : '/viescolaire/evaluations/domaines/classe/'
        }
    }

    constructor (eleve : Eleve, periode : any, classe : Classe) {
        super();
        this.periode = periode;
        this.classe = classe;
        var that = this;

        this.collection(Domaine, {
            sync: function () {
                return new Promise((resolve, reject) => {
                    var url = that.api.getArbreDomaines + that.classe.id;
                    http().getJson(url).done((resDomaines) => {
                        var url = that.api.getCompetencesNotes + eleve.id;
                        if (periode !== null && periode !== undefined && periode !== '*') {
                            url += "?idPeriode=" + periode.id;
                        }
                        http().getJson(url).done((resCompetencesNotes) => {
                            if (resDomaines) {
                                for (var i = 0; i < resDomaines.length; i++) {
                                    var domaine = new Domaine(resDomaines[i]);
                                    that.domaines.all.push(domaine);
                                    setCompetenceNotes(domaine, resCompetencesNotes, this, null);
                                }
                            }
                            if (resolve && typeof (resolve) === 'function') {
                                resolve();
                            }
                        });
                    });
                });
            }
        });

    }


    /**
     * Calcul la moyenne d'un domaine (moyenne des meilleurs évaluations de chaque compétence)
     *
     */
    setMoyenneCompetences () {
        for(var i=0; i< this.domaines.all.length; i++) {
            var oEvaluationsArray = [];
            var oDomaine = this.domaines.all[i] as Domaine;

            // recherche de toutes les évaluations du domaine et ses sous domaines
            // (uniquement les max de chaque compétence)
            getMaxEvaluationsDomaines(oDomaine, oEvaluationsArray, false);
        }
    }

    findCompetence (idCompetence) {
        for(var i=0; i<this.domaines.all.length; i++) {
            var comp = findCompetenceRec(idCompetence, this.domaines.all[i].competences);
            if(comp !== undefined) {
                return comp;
            } else {
                continue;
            }
        }
        return false;
    }


    sync () : Promise<any> {
        return new Promise((resolve, reject) => {
            resolve();
        });
    }
}

function setSliderOptions(poDomaine) {
    poDomaine.slider = {
        value: parseFloat(poDomaine.moyenne),
        options: {
            disabled: parseFloat(poDomaine.moyenne) === -1,
            floor: 0,
            ceil: 4,
            step: 0.01,
            precision: 2,
            showTicksValues: false,
            showTicks: 1,
            showSelectionBar: true,
            getSelectionBarClass: function(value) {
                if (value <= 1)
                    return 'red';
                if (value <= 2)
                    return 'orange';
                if (value <= 3)
                    return 'yellow';
                if (value <= 4)
                    return 'green';
                return 'grey';
            },
            translate: function(value, sliderId, label) {
                var l = '#label#';
                if (label === 'model') l = '<b>#label#</b>'
                if (value == -1)
                    return l.replace('#label#', lang.translate('evaluations.competence.unevaluated'));
                if (value < 1)
                    return l.replace('#label#', lang.translate('evaluations.competences.poor'));
                if (value < 2)
                    return l.replace('#label#', lang.translate('evaluations.competences.fragile'));
                if (value < 3)
                    return l.replace('#label#', lang.translate('evaluations.competences.satisfying'));
                if (value < 4)
                    return l.replace('#label#', lang.translate('evaluations.competences.proficiency'));
                if (value == 4)
                    return l.replace('#label#', lang.translate('evaluations.competences.proficiency'));
            }

        }
    };
};

function getMaxEvaluationsDomaines(poDomaine, poMaxEvaluationsDomaines, pbMesEvaluations) {
    // si le domaine est évalué, on ajoute les max de chacunes de ses competences
    if(poDomaine.evaluated) {
        for (var i = 0; i < poDomaine.competences.all.length; i++) {
            var competencesEvaluations = poDomaine.competences.all[i].competencesEvaluations;
            var _t = competencesEvaluations;

            // filtre sur les compétences évaluées par l'enseignant
            if (pbMesEvaluations) {
                _t = _.filter(competencesEvaluations, function (competence) {
                    return competence.owner !== undefined && competence.owner === model.me.userId;
                });
            }

            if (_t && _t.length > 0) {
                // TODO récupérer la vrai valeur numérique :
                // par exemple 0 correspond à rouge ce qui mais ça correspond à une note de 1 ou 0.5 ou 0 ?
                poMaxEvaluationsDomaines.push(_.max(_t, function (_t) {
                        return _t.evaluation;
                    }).evaluation + 1);
            }
        }
    }

    // calcul de la moyenne pour les sous-domaines
    if(poDomaine.domaines) {
        for(var i=0; i<poDomaine.domaines.all.length; i++) {
            // si le domaine parent n'est pas évalué, il faut vider pour chaque sous-domaine les poMaxEvaluationsDomaines sauvegardés
            if(!poDomaine.evaluated) {
                poMaxEvaluationsDomaines = [];
            }
            getMaxEvaluationsDomaines(poDomaine.domaines.all[i], poMaxEvaluationsDomaines, pbMesEvaluations);
        }
    }

    // mise à jour de la moyenne
    if (poMaxEvaluationsDomaines.length > 0) {
        poDomaine.moyenne = utils.average(poMaxEvaluationsDomaines);
    } else {
        poDomaine.moyenne = -1;
    }

    setSliderOptions(poDomaine);
}

function findCompetenceRec (piIdCompetence, poCompetences) {
    for (var i = 0; i < poCompetences.all.length; i++) {
        // si compétences trouvée on arrete le traitement
        if(poCompetences[i].id === piIdCompetence) {
            return poCompetences[i];
        } else {
            // recherche dans les sous-compétences
            return findCompetenceRec(piIdCompetence, poCompetences[i].competences);
        }
    }
    return false;
}

function setCompetenceNotes(poDomaine, poCompetencesNotes, object, classe) {
    if(poDomaine.competences) {
        _.map(poDomaine.competences.all, function (competence) {
            competence.competencesEvaluations = _.where(poCompetencesNotes, {
                id_competence: competence.id,
                id_domaine: competence.id_domaine
            });
            // if (object.composer.constructor.name === "SuiviCompetenceClasse"
            //     && classe !== null
            //     && classe.eleves.all.length > 0
            //     && competence.competencesEvaluations.length < classe.eleves.all.length) {
            //     for (var i = 0; i < (classe.eleves.all.length - competence.competencesEvaluations.length); i++) {
            //         var o = new CompetenceNote({id_competence : competence.id, nom : competence.nom, evaluation : -1});
            //         competence.competencesEvaluations.push(o)
            //     }
            // }
            if (object.composer.constructor.name === 'SuiviCompetenceClasse') {
                for (var i = 0; i < classe.eleves.all.length; i++) {
                    var mine = _.findWhere(competence.competencesEvaluations, {id_eleve : classe.eleves.all[i].id, owner : model.me.userId});
                    var others = _.filter(competence.competencesEvaluations, function (evaluation) { return evaluation.owner !== model.me.userId; });
                    if (mine === undefined)
                        competence.competencesEvaluations.push(new CompetenceNote({evaluation : -1, id_competence: competence.id, id_eleve : classe.eleves.all[i].id, owner : model.me.userId}));
                    if (others.length === 0)
                        competence.competencesEvaluations.push(new CompetenceNote({evaluation : -1, id_competence: competence.id, id_eleve : classe.eleves.all[i].id}));
                }
            }
        });
    }

    if( poDomaine.domaines) {
        for (var i = 0; i < poDomaine.domaines.all.length; i++) {
            setCompetenceNotes(poDomaine.domaines.all[i], poCompetencesNotes, object, classe);
        }
    }
}

export let evaluations = new Evaluations();

model.build = function () {
    (this as any).evaluations = evaluations;
    evaluations.sync();
};