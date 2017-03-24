import {model, http, IModel, Model, Collection, idiom as lang} from 'entcore/entcore';
import * as utils from '../utils/teacher';
import forEach = require("core-js/fn/array/for-each");

let moment = require('moment');
let $ = require('jquery');
declare let _:any;

export class Structure extends Model implements IModel{
    eleves : Collection<Eleve>;
    devoirs : Devoirs;
    classes : Collection<Classe>;
    synchronized : any;


    get api () {
        return  {
            getEleves : '/viescolaire/etab/eleves/',
            getDevoirs: '/viescolaire/evaluations/etab/devoirs/',
            getClasses: '/viescolaire/evaluations/classes'
        }
    }

    constructor () {
        super();
        this.synchronized = {
            devoirs : false,
            classes : false,
            eleves : false,
        };
        this.collection(Eleve);
        this.collection(Devoir);
        this.collection(Classe);
    }

    syncEleves (idEtab) :  Promise<any> {

        return new Promise((resolve, reject) => {
            var that = this;
            http().getJson(this.api.getEleves+idEtab).done(function(data){
                that.eleves.load(data);
                that.synchronized.Eleve = true;
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
    syncDevoirs () :  Promise<any> {
        this.collection(Devoir, new DevoirsCollection());
        this.devoirs.sync().then((data) =>{
            this.synchronized.devoirs = true;
            this.devoirs.trigger('devoirs-sync');
        });
    }

    syncClasses () : Promise<any>{
        return new Promise((resolve, reject) => {
            var that = this;
            const libelle = {
                CLASSE : 'Classe',
                GROUPE : "Groupe d'enseignement"
            };
            http().getJson(this.api.getClasses).done((res) => {
                _.map(res, (classe) => {
                    let libelleClasse;
                    if(classe.type_groupe_libelle = classe.type_groupe === 0){
                        libelleClasse = libelle.CLASSE;
                    } else {
                        libelleClasse =  libelle.GROUPE;
                    }
                    classe.type_groupe_libelle = libelleClasse;
                    return classe;
                });

                that.classes.load(res);
                that.synchronized.classes = that.classes.all.length;
                for (var i = 0; i < that.classes.all.length; i++) {
                    that.synchronized.classes--;
                    if (that.synchronized.classes === 0) {
                        that.synchronized.classes = true;
                        that.classes.trigger('classes-sync');
                    }
                }

            });
            this.classes.sync();
        });
    }

}

export class ReleveNote extends  Model implements IModel{
    synchronized : any;
    periode : Periode;
    matiere : Matiere;
    classe : Classe;
    devoirs : Collection<Devoir>;
    structure : Structure;
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
        this.structure = evaluations.structure;
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
                    var _devoirs = evaluations.devoirs.where({id_periode : this.composer.idPeriode, id_groupe : this.composer.idClasse, id_matiere : this.composer.idMatiere, id_etablissement: this.composer.idEtablissement});
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
            http().getJson('/viescolaire/evaluations/releve?idEtablissement='+this.structure.id+'&idClasse='+this.idClasse+'&idMatiere='+this.idMatiere+'&idPeriode='+this.idPeriode)
                .done(function (res) {
                    that._tmp = res;
                    that.synchronized.evaluations = true;
                    callFormating();
                });
            this.on('format', function () {
                _.each(that.classe.eleves.all, function (eleve) {
                    var _evals = [];
                    var _t;
                    if(that._tmp && that._tmp.length !== 0)  _t = _.where(that._tmp, {id_eleve : eleve.id});
                    _.each(that.devoirs.all, function (devoir) {
                        if (_t && _t.length !== 0) {
                            var _e = _.findWhere(_t, {id_devoir : devoir.id});
                            if (_e) {
                                _e.oldValeur = _e.valeur;
                                _e.oldAppreciation = _e.appreciation !== undefined ? _e.appreciation : '';
                                _evals.push(_e);
                            }
                            else {
                                _evals.push(new Evaluation({valeur:"", oldValeur : "", appreciation : "",
                                    oldAppreciation : "", id_devoir : devoir.id, id_eleve : eleve.id,
                                    ramener_sur : devoir.ramener_sur, coefficient : devoir.coefficient,
                                    is_evaluated : devoir.is_evaluated}));
                            }
                        } else {
                            _evals.push(new Evaluation({valeur:"", oldValeur : "", appreciation : "",
                                oldAppreciation : "", id_devoir : devoir.id, id_eleve : eleve.id,
                                ramener_sur : devoir.ramener_sur, coefficient : devoir.coefficient,
                                is_evaluated : devoir.is_evaluated}));
                        }
                    });
                    eleve.evaluations.load(_evals);

                });
                this.trigger('noteOK');
                resolve();
            });
        });
    }

    calculStatsDevoirs() : Promise<any> {

        return new Promise((resolve, reject) => {
            this.on('noteOK', function () {
                var that = this;
                var _datas = [];
                _.each(that.devoirs.all, function (devoir) {
                    var _o = {
                        id: String(devoir.id),
                        evaluations: []
                    };
                    _.each(that.classe.eleves.all, function (eleve) {
                        var _e = eleve.evaluations.findWhere({id_devoir: devoir.id});

                        if (_e !== undefined && _e.valeur !== "") _o.evaluations.push(_e.formatMoyenne());
                    });
                    if (_o.evaluations.length > 0) _datas.push(_o);
                });
                if (_datas.length > 0) {
                    http().postJson('/viescolaire/evaluations/moyennes?stats=true', {data: _datas}).done(function (res) {
                        _.each(res, function (devoir) {
                            var nbEleves = that.classe.eleves.all.length;
                            var nbN = _.findWhere(_datas, {id: devoir.id});
                            var d = that.devoirs.findWhere({id: parseInt(devoir.id)});
                            if (d !== undefined) {
                                d.statistiques = devoir;
                                if (nbN !== undefined) {
                                    d.statistiques.percentDone = Math.round((nbN.evaluations.length / nbEleves) * 100);
                                    if (resolve && typeof(resolve) === 'function') {
                                        resolve();
                                    }
                                }
                            }
                        });
                    });
                }
            });
        });
    }

    calculMoyennesEleves() : Promise<any> {
        return new Promise((resolve, reject) => {
            var that = this;
            var _datas = [];
            _.each(this.classe.eleves.all, function (eleve) {
                var _t = eleve.evaluations.filter(function (evaluation) {
                    return evaluation.valeur !== "" && evaluation.valeur !== null && evaluation.valeur !== undefined && evaluation.is_evaluated === true;
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
export class OtherClasse extends Model{
    id : number;
    name : string;

    get api () {
        return {
            nameOfGroupe: '/viescolaire/class/group/'
        }
    }
    constructor(p? : any) {
        super();
    }
    getClasse(idClasse) : Promise<any> {

        return new Promise((resolve, reject) => {
            var that = this;
            http().getJson(this.api.nameOfGroupe+idClasse).done(function(data){
                that.id = data.id;
                that.name = data.name;
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
}
export class OtherClasses extends Model {
    all: OtherClasse[];

    constructor(p?: any) {
        super();
        this.all = [];
    }

}
function isChefEtab () {
  return  model.me.type === 'PERSEDUCNAT' &&
    model.me.functions !== undefined &&
    model.me.functions.DIR !== undefined &&
    model.me.functions.DIR.code === 'DIR';
}
export class Classe extends Model {
    eleves : Collection<Eleve>;
    id : number;
    name : string;
    type_groupe : number;
    type_groupe_libelle : string;
    suiviCompetenceClasse : Collection<SuiviCompetenceClasse>;
    mapEleves : any;

    get api () {
        return {
            syncClasse: '/directory/class/' + this.id + '/users?type=Student',
            syncGroupe : '/viescolaire/groupe/enseignement/users/' + this.id + '?type=Student',
            syncClasseChefEtab : '/viescolaire/classes/'+this.id+'/users'
    }
    }

    constructor (o? : any) {
        super();
        if (o !== undefined) this.updateData(o);
        this.collection(Eleve, {
            sync : () : Promise<any> => {
                return new Promise((resolve, reject) => {
                    this.mapEleves = {};
                    let url;
                    if(isChefEtab()){
                         url = this.type_groupe === 1 ? this.api.syncGroupe : this.api.syncClasseChefEtab;
                    }else {
                         url = this.type_groupe === 1 ? this.api.syncGroupe : this.api.syncClasse;
                    }
                    http().getJson(url).done((data) => {
                        this.eleves.load(data);
                        for (var i = 0; i < this.eleves.all.length; i++) {
                            this.mapEleves[this.eleves.all[i].id] = this.eleves.all[i];
                        }
                        resolve();
                    });
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
    firstName: string;
    lastName: string;
    level: string;
    classes: string;
    suiviCompetences : Collection<SuiviCompetence>;

    get api() {
        return {
            getMoyenne : '/viescolaire/evaluations/moyenne'
        }
    }

    constructor () {
        super();
        this.collection(Evaluation);
        this.collection(SuiviCompetence);
    }
    toString () {
        return this.firstName+" "+this.lastName;
    }

    getMoyenne () : Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.evaluations.all.length > 0) {
                var _datas = [];
                for (var i = 0; i < this.evaluations.all.length; i++) {
                    if (this.evaluations.all[i].valeur !== "" && this.evaluations.all[i].is_evaluated === true ) _datas.push(this.evaluations.all[i].formatMoyenne());
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
    id_appreciation : number;
    valeur : any;
    appreciation : any;
    coefficient : number;
    ramener_sur : boolean;
    competenceNotes : Collection<CompetenceNote>;
    oldValeur : any;
    is_evaluated  : boolean;
    oldAppreciation : any;

    get api () {
        return {
            create : '/viescolaire/evaluations/note',
            update : '/viescolaire/evaluations/note?idNote=' + this.id,
            delete : '/viescolaire/evaluations/note?idNote=' + this.id,
            createAppreciation : '/viescolaire/evaluations/appreciation',
            updateAppreciation : '/viescolaire/evaluations/appreciation?idAppreciation=' + this.id_appreciation,
            deleteAppreciation : '/viescolaire/evaluations/appreciation?idAppreciation=' + this.id_appreciation
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
        //delete o.appreciation;
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

    saveAppreciation () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            if (!this.id_appreciation) {
                this.createAppreciation().then((data) => {
                    resolve(data);
                });
            } else {
                this.updateAppreciation().then((data) =>  {
                    resolve(data);
                });
            }
        });
    }
    create () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            let _noteData = this.toJSON();
            delete _noteData.appreciation;
            delete _noteData.id_appreciation;
            http().postJson(this.api.create, _noteData).done(function (data) {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    update () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            let _noteData = this.toJSON();
            delete _noteData.appreciation;
            delete _noteData.id_appreciation;
            http().putJson(this.api.update, _noteData).done(function (data) {
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
    createAppreciation () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            var _appreciation = {
                id_devoir : this.id_devoir,
                id_eleve  : this.id_eleve,
                valeur    : this.appreciation
            };
            http().postJson(this.api.createAppreciation, _appreciation).done ( function (data) {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            }) ;

        });

    }

    updateAppreciation () : Promise<Evaluation> {
        return new Promise((resolve, reject) => {
            var _appreciation = {
                id : this.id_appreciation,
                id_devoir : this.id_devoir,
                id_eleve  : this.id_eleve,
                valeur    : this.appreciation
            };
            http().putJson(this.api.updateAppreciation, _appreciation).done(function (data) {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });

        });
    }

    deleteAppreciation () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.deleteAppreciation).done(function (data) {
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
export class EvaluationDevoir extends  Model {
    nbreval : number;
    id : string;
    evaluation : number;
    typeeval : string;

    constructor(p? : any) {
        super();
    }

}
export class Devoir extends Model implements IModel{
    statistiques : any;
    eleves : Collection<Eleve>;
    matiere : Matiere;
    type : Type;
    competences : Collection<Competence> | any;
    competenceEvaluee : CompetenceNote;

    // DATABASE FIELDS
    id : number;
    id_groupe : string;
    type_groupe : number;
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
    percent: any;
    evaluationDevoirs : Collection<EvaluationDevoir> ;

    get api () {
        return {
            create : '/viescolaire/evaluations/devoir',
            update : '/viescolaire/evaluations/devoir?idDevoir=',
            delete : '/viescolaire/evaluations/devoir?idDevoir=',
            getCompetencesDevoir : '/viescolaire/evaluations/competences/devoir/',
            getCompetencesLastDevoir : '/viescolaire/evaluations/competences/last/devoir/',
            getNotesDevoir : '/viescolaire/evaluations/devoir/' + this.id + '/notes',
            getAppreciationDevoir: '/viescolaire/evaluations/appreciation/' + this.id + '/appreciations',
            getStatsDevoir : '/viescolaire/evaluations/moyenne?stats=true',
            getCompetencesNotes : '/viescolaire/evaluations/competence/notes/devoir/',
            saveCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            updateCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            deleteCompetencesNotes : '/viescolaire/evaluations/competence/notes',
            isEvaluatedDevoir : '/viescolaire/evaluations/devoirs/evaluations/information?idDevoir='
        }
    }

    constructor(p? : any) {
        super();
        var that = this;
        this.collection(Enseignement);

        this.collection(EvaluationDevoir);


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
                    var _classe = evaluations.classes.findWhere({id : that.id_groupe});
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
        let classe = evaluations.classes.findWhere({id : this.id_groupe});
        let  type_groupe = -1;
        let  id_groupe = null;
        if(classe !== undefined){
            if(classe.type_groupe !== undefined){
                type_groupe = classe.type_groupe;
            }
            id_groupe = this.id_groupe;
        }
        return {
            name            : this.name,
            owner           : this.owner,
            libelle         : this.libelle,
            id_groupe       : id_groupe,
            type_groupe     : type_groupe,
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
    isEvaluatedDevoir (idDevoir) : Promise<any> {

        return new Promise((resolve, reject) => {
            var that = this;
            http().getJson(this.api.isEvaluatedDevoir+idDevoir).done(function(data){


                that.evaluationDevoirs.load(data);
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
            if(devoirJSON.competenceEvaluee == undefined) {
                delete devoirJSON.competenceEvaluee;
            }
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

    save (add? : any,rem? : any) : Promise<any> {
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
    percentDone : boolean;

    get api () {
        return {
            get : '/viescolaire/evaluations/devoirs?idEtablissement=',
            areEvaluatedDevoirs : '/viescolaire/evaluations/devoirs/evaluations/informations?'
        }
    }

    constructor (idEtablissement : String) {
        this.sync =  function () {
            return new Promise((resolve, reject) => {

                http().getJson(this.api.get+idEtablissement).done(function (res) {
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
                    if (resolve && (typeof(resolve) === 'function')) {
                        resolve(res);
                    }
                }.bind(this));
                this.percentDone = false;
            });
        };
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
    areEvaluatedDevoirs (idDevoirs) : Promise<any> {

        return new Promise((resolve, reject) => {

            var URLBuilder = "";

            for (var i=0; i<idDevoirs.length; i++){
                if(i==0)
                    URLBuilder = "idDevoir="+idDevoirs[i];
                else
                    URLBuilder += "&idDevoir="+idDevoirs[i];
            }
            http().getJson(this.api.areEvaluatedDevoirs + URLBuilder  ).done(function(data){

                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
    getPercentDone () : Promise<any> {
        return new Promise((resolve, reject) => {
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
                http().postJson('/viescolaire/evaluations/devoirs/done', {'datas' : _datas})
                    .done((res) => {
                        for (var i = 0; i < this.all.length; i++) {
                            this.all[i].percent = res[this.all[i].id];
                        }
                        model.trigger('apply');
                        this.percentDone = true;
                        resolve();
                    })
                    .error(() => {
                        reject();
                    });
            }
        });
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
    id;

    constructor () {
        super();
        this.collection(Competence);
    }
}

/**
 * Méthode récursive de l'affichage des sous domaines d'un domaine
 *
 * @param poDomaines la liste des domaines
 * @pbMesEvaluations booleen indiquant d'afficher ou non un domaine
 *
 */
function setVisibleSousDomainesRec (poDomaines, pbVisible) {
    if(poDomaines !== null && poDomaines !== undefined) {
        for (var i = 0; i < poDomaines.all.length; i++) {
            var oSousDomaine = poDomaines.all[i];
            oSousDomaine.visible = pbVisible;
            setVisibleSousDomainesRec(oSousDomaine.domaines, pbVisible);
        }
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
    visible : boolean;


    /**
     * Méthode activant l'affichage des sous domaines d'un domaine
     *
     * @pbMesEvaluations booleen indiquant d'afficher ou non un domaine
     *
     */
    setVisibleSousDomaines (pbVisible) {
        setVisibleSousDomainesRec(this.domaines, pbVisible);
    }

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


export class Evaluations extends Model{
    periodes : Collection<Periode>;
    types : Collection<Type>;
    devoirs : Devoirs;
    enseignements : Collection<Enseignement>;
    matieres : Collection<Matiere>;
    releveNotes : Collection<ReleveNote>;
    classes : Collection<Classe>;
    structure : Structure;
    synchronized : any;
    competencesDevoir : any[];
    structures : Collection<Structure>;

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
            sync : function  (idClasse?:any) {
                var uri = '/viescolaire/evaluations/enseignements';
                if (idClasse !== undefined) {
                    uri += '?idClasse='+idClasse;
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


                if(isChefEtab()){
                    http().getJson('/viescolaire/matieres').done(function (res) {
                        this.load(res);
                    }.bind(this));
                }else {
                    http().getJson('/viescolaire/matieres?idEnseignant=' + model.me.userId).done(function (res) {
                        this.load(res);
                        this.each(function (matiere) {
                            matiere.sousMatieres.load(matiere.sous_matieres);
                            evaluations.synchronized.matieres = true;
                            delete matiere.sous_matieres;
                        });
                    }.bind(this));
                }
            }
        });
        this.matieres.sync();
        this.collection(Periode, {
            sync : function () {
                http().getJson('/viescolaire/evaluations/periodes?idEtablissement=' + model.me.structures[0]).done(function (res) {
                    this.load(res);
                    this.trigger('sync');
                }.bind(this));
                }
        });
        this.periodes.sync();
        this.collection(ReleveNote);
        const libelle = {
            CLASSE : 'Classe',
            GROUPE : "Groupe d'enseignement"
        };
        this.collection(Classe, {
            sync : function () {
                http().getJson('/viescolaire/evaluations/classes?idEtablissement=' + this.model.structure.id).done((res) => {
                    _.map(res, (classe) => {
                        let libelleClasse;
                        if(classe.type_groupe_libelle = classe.type_groupe === 0){
                            libelleClasse = libelle.CLASSE;
                        } else {
                            libelleClasse =  libelle.GROUPE;
                        }
                        classe.type_groupe_libelle = libelleClasse;
                        return classe;
                    });
                    evaluations.classes.load(res);


                    evaluations.synchronized.classes = evaluations.classes.all.length;
                    for (let i = 0; i < evaluations.classes.all.length; i++) {
                        // evaluations.classes.all[i].eleves.sync().then(() => {
                            evaluations.synchronized.classes--;
                            if (evaluations.synchronized.classes === 0) {
                                evaluations.classes.trigger('classes-sync');
                            }
                        // });
                    }
                });
            }
        });
        this.classes.sync();
        this.devoirs.on('sync', function () {
            evaluations.synchronized.devoirs = true;resolve();
            });
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
                        var url = that.api.getCompetencesNotesClasse + classe.id+"/"+ classe.type_groupe;
                        if (periode !== null && periode !== undefined && periode !== '*') {
                            if(periode.id !== undefined)url += "?idPeriode="+periode.id;
                        }
                        http().getJson(url).done((resCompetencesNotes) => {
                            if(resDomaines) {
                                for(let i=0; i<resDomaines.length; i++) {
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

    addEvalLibre (eleve){


    }
    findCompetence (idCompetence) {
        for(var i=0; i<this.domaines.all.length; i++) {
            var comp = findCompetenceRec(idCompetence, this.domaines.all[i].competences);
            if(comp !== undefined) {
                return comp;
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

export class TableConversion extends  Model {
    valmin : number;
    valmax : number;
    libelle : string;
    ordre : number;
    couleur : string;

    constructor(p? : any) {
        super();
    }
}
export class SuiviCompetence extends Model implements IModel{
    domaines : Collection<Domaine>;
    competenceNotes : Collection<CompetenceNote>;
    periode : Periode;
    classe : Classe;
    tableConversions : Collection<TableConversion>;
    get api() {
        return {
            getCompetencesNotes : '/viescolaire/evaluations/competence/notes/eleve/',
            getArbreDomaines : '/viescolaire/evaluations/domaines/classe/',
            getCompetenceNoteConverssion : '/viescolaire/evaluations/competence/notes/bilan/conversion'
        }
    }
    that = this;
    constructor (eleve : Eleve, periode : any, classe : Classe) {
        super();
        this.periode = periode;
        this.classe = classe;
        var that = this;
        this.collection(TableConversion);
        this.collection(Domaine, {
            sync: function () {
                return new Promise((resolve, reject) => {
                    var url = that.api.getArbreDomaines + that.classe.id;
                    http().getJson(url).done((resDomaines) => {
                        var url = that.api.getCompetencesNotes + eleve.id;
                        if (periode !== null && periode !== undefined && periode !== '*') {
                            if(periode.id !== undefined)url += "?idPeriode=" + periode.id;
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
            getMaxEvaluationsDomaines(oDomaine, oEvaluationsArray,this.tableConversions.all, false);
        }
    }

    findCompetence (idCompetence) {
        for(var i=0; i<this.domaines.all.length; i++) {
            var comp = findCompetenceRec(idCompetence, this.domaines.all[i]);
            if(comp !== undefined) {
                return comp;
            }
        }
        return false;
    }

    getConversionTable(idetab, idClasse) : Promise<any> {
        return new Promise((resolve, reject) => {
            var that = this;
            http().getJson(this.api.getCompetenceNoteConverssion + '?idEtab='+ idetab+'&idClasse='+idClasse  ).done(function(data){
                that.tableConversions.load(data);

                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
    sync () : Promise<any> {
        return new Promise((resolve, reject) => {
            resolve();
        });
    }
}


function setSliderOptions(poDomaine,tableConversions) {

    poDomaine.slider = {
        options: {
            ticksTooltip: function(value) {
                return String(poDomaine.moyenne);
            },
            disabled: parseFloat(poDomaine.moyenne) === -1,
            floor: _.min(tableConversions, function(Conversions){ return Conversions.ordre; }).ordre - 1,
            ceil: _.max(tableConversions, function(Conversions){ return Conversions.ordre; }).ordre,
            step: 0.01,
            precision: 2,
            showTicksValues: false,
            showTicks: 1,
            showSelectionBar: true,
            hideLimitLabels : true,


        }
    };


    let maConvertion =  undefined;
    for(let i= 0 ; i < tableConversions.length ; i++){
        if((tableConversions[i].valmin <= poDomaine.moyenne && tableConversions[i].valmax > poDomaine.moyenne) && tableConversions[i].ordre !== tableConversions.length ){
            maConvertion = tableConversions[i];
        }else if((tableConversions[i].valmin <= poDomaine.moyenne && tableConversions[i].valmax >= poDomaine.moyenne) && tableConversions[i].ordre === tableConversions.length ){
            maConvertion = tableConversions[i];
        }
    }

    // si ça ne rentre dans aucune case
    if(maConvertion === undefined ){
        poDomaine.slider.value = -1 ;
        poDomaine.slider.options.getSelectionBarClass = function(){ return '#d8e0f3';};
        poDomaine.slider.options.translate = function(value,sliderId,label){
            let l = '#label#';
            if (label === 'model') {

                l = '<b>#label#</b>';
            }
            return l.replace('#label#', lang.translate('evaluations.competence.unevaluated'));
        };

    }else{
        poDomaine.slider.value = maConvertion.ordre ;
        poDomaine.slider.options.getSelectionBarClass = function(value){
            let ConvertionOfValue = _.find(tableConversions,{ordre: value});
            if(ConvertionOfValue !== undefined)
                return ConvertionOfValue.couleur;};
        poDomaine.slider.options.translate = function(value,sliderId,label){
            let l = '#label#';
            if (label === 'model') {

                l = '<b>#label#</b>';
            }
            let libelle = _.find(tableConversions,{ordre: value});
            if(libelle !== undefined)
                return l.replace('#label#', lang.translate(libelle.libelle));
        };
    }
};

function getMaxEvaluationsDomaines(poDomaine, poMaxEvaluationsDomaines,tableConversions, pbMesEvaluations) {
    // si le domaine est évalué, on ajoute les max de chacunes de ses competences
    if(poDomaine.evaluated) {
        for (let i = 0; i < poDomaine.competences.all.length; i++) {
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
            getMaxEvaluationsDomaines(poDomaine.domaines.all[i], poMaxEvaluationsDomaines,tableConversions, pbMesEvaluations);
        }
    }

    // mise à jour de la moyenne
    if (poMaxEvaluationsDomaines.length > 0) {
        poDomaine.moyenne = utils.average(_.without(poMaxEvaluationsDomaines,0) );
    } else {
        poDomaine.moyenne = -1;
    }

    setSliderOptions(poDomaine,tableConversions)

    // Chefs d'établissement


    //Si l'utilisateur n'est pas un chef d'établissement il ne peut pas modifier le slider
    if(!isChefEtab()){
        poDomaine.slider.options.readOnly = true;
    }
}

function findCompetenceRec (piIdCompetence, poDomaine) {
    for (var i = 0; i < poDomaine.competences.all.length; i++) {
        // si compétences trouvée on arrete le traitement
        if(poDomaine.competences.all[i].id === piIdCompetence) {
            return poDomaine.competences.all[i];
        }
    }

    // recherche dans les sous-domaines
    if(poDomaine.domaines) {
        for(var i=0; i<poDomaine.domaines.all.length; i++) {
            let comp = findCompetenceRec(piIdCompetence, poDomaine.domaines.all[i]);
            if(comp !== undefined){
                return comp;
            }
        }
    }
}

function setCompetenceNotes(poDomaine, poCompetencesNotes, object, classe) {
    if(poDomaine.competences) {
        _.map(poDomaine.competences.all, function (competence) {
            competence.competencesEvaluations = _.where(poCompetencesNotes, {
                id_competence: competence.id,
                id_domaine: competence.id_domaine
            });
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


export class Enseignant extends Model{
    id : string;
    displayName : string;

    constructor(p? : any) {
        super();
    }
}

export class Remplacement extends Model implements IModel{

    // DATABASE FIELDS
    titulaire : Enseignant;
    remplacant : Enseignant;
    date_debut : any;
    date_fin : any;
    id_etablissement : string;


    // OTHER FIELDS
    selected : boolean;


    get api () {
        return {
            create : '/viescolaire/evaluations/remplacement/create',
            update : '/viescolaire/evaluations/remplacement/update',
            delete : '/viescolaire/evaluations/remplacement/delete'
        }
    }

    constructor(p? : any) {
        super();
        this.selected = false;

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
            http().postJson(this.api.update, this.toJSON()).done(function(data){
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }


    remove () : Promise<any> {
        var that = this;
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete, this.toJSON()).done(function(data){
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(that);
                }
            })
                .error(function () {
                    reject(that);
                });
        });
    }

    toJSON() {
        return {
            id_titulaire: this.titulaire.id,
            libelle_titulaire : this.titulaire.displayName,
            id_remplacant: this.remplacant.id,
            libelle_remplacant : this.remplacant.displayName,
            date_debut: utils.getFormatedDate(this.date_debut,"YYYY-MM-DD"),
            date_fin: utils.getFormatedDate(this.date_fin,"YYYY-MM-DD"),
            id_etablissement : this.id_etablissement
        }
    }
}

export class GestionRemplacement extends Model implements IModel{
    remplacements : Collection<Remplacement> | any; // liste des remplacements en cours
    selectedRemplacements : Collection<Remplacement> | any; // liste des remplacements sélectionnés
    remplacement : Remplacement; // remplacementen cours d'ajout
    enseignants : Collection<Enseignant>; // liste des enseignants de l'établissment
    sortType : string; // type de tri de la liste des remplaçants
    sortReverse : boolean; // sens de tri de la liste des remplaçants
    showError : boolean; // condition d'affichage d'un message d'erreur
    confirmation : boolean; // condition d'affichage de la popup de confirmation
    selectAll : boolean; // booleen de sélection de tous/aucun remplacement/s

    get api () {
        return {
            deleteMultiple : '/viescolaire/evaluations/remplacements/delete', // TODO A coder
            //enseignants : '/directory/user/admin/list?structureId='+model.me.structures[0]+'&profile=Teacher',
            enseignants : '/viescolaire/evaluations/user/list?structureId='+model.me.structures[0]+'&profile=Teacher',
            remplacements : '/viescolaire/evaluations/remplacements/list'
        }
    }

    constructor(p? : any) {
        super();
        var that = this;

        this.showError = false;
        this.selectAll = false;
        this.confirmation = false;
        this.remplacement = new Remplacement();


        this.remplacement.date_debut = new Date();

        var today = new Date();
        today.setFullYear(today.getFullYear() + 1);
        this.remplacement.date_fin = today;

        this.collection(Enseignant, {
            sync : function () : Promise<any> {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.enseignants).done(function(res) {
                        this.load(res);
                        if(resolve && (typeof(resolve) === 'function')) {
                            resolve();
                        }
                    }.bind(this));
                });
            }
        });

        this.collection(Remplacement, {
            sync : function () : Promise<any> {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.remplacements).done(function(resRemplacements) {

                        this.removeAll();

                        for(var i=0; i<resRemplacements.length; i++) {
                            var remplacementJson = resRemplacements[i];

                            var remplacement = new Remplacement();
                            remplacement.titulaire = new Enseignant();
                            remplacement.titulaire.id = remplacementJson.id_titulaire;
                            remplacement.titulaire.displayName = remplacementJson.libelle_titulaire;

                            remplacement.remplacant = new Enseignant();
                            remplacement.remplacant.id = remplacementJson.id_remplacant;
                            remplacement.remplacant.displayName = remplacementJson.libelle_remplacant;


                            remplacement.date_debut = remplacementJson.date_debut;
                            remplacement.date_fin = remplacementJson.date_fin;
                            remplacement.id_etablissement = remplacementJson.id_etablissement;

                            this.all.push(remplacement);


                        }

                        if(resolve && (typeof(resolve) === 'function')) {
                            resolve();
                        }
                    }.bind(this));
                });
            }
        });

        this.selectedRemplacements = [];
    }

}

export class Structure extends Model {
    id: string;
    libelle: string;
    type: string;

    constructor() {
        super();

    }
}


export let evaluations = new Evaluations();


model.build = function () {
    require('angular-chart.js');
    (this as any).evaluations = evaluations;
    evaluations.sync();
};