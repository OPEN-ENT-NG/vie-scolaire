import { model, notify, http, IModel, Model, Collection, BaseModel } from '../../entcore/entcore';
declare let _:any;
let moment = require('moment');

/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. WAbsSansMotifs : Objet Widget contenant la liste des absences sans motifs.
 *  2. WAppelsOublies : Objet Widget contenant la liste des appels oubliés.
 *  3. WMotVsco: Objet Widget contenant la liste des mots pour la vie scolaire.
 *  4. Widget : Objet contenant la liste des widgets de la page d'accueil CPE. Contient des listes de WAbsSansMotifs, WAppelsOublies et WMotVsco.
 */

export class Evenement extends Model {}

export class Appel extends Model {}

export class Observation extends Model {}

export class WAbsSansMotifs extends Model {
    evenements: Collection<Evenement>;

    constructor () {
        super();
        this.collection(Evenement);
    }

    sync () {
        http().getJson("/viescolaire/absences/sansmotifs/"+moment(new Date()).format('YYYY-MM-DD')+"/"+moment(new Date()).format('YYYY-MM-DD'))
        .done(function(data){
            this.evenements.load(data);
        });
    }
}

export class WAppelsOublies extends Model {
    appels : Collection<Appel>;

    constructor () {
        super();
        this.collection(Appel);
    }

    sync () {
        http().getJson("/viescolaire/absences/appels/noneffectues/"+moment(new Date()).format('YYYY-MM-DD')+"/"+moment(new Date()).format('YYYY-MM-DD'))
        .done(function(data){
            this.appels.load(data);
        });
    }
}

export class WObservations extends Model {
    observations : Collection<Observation>;

    constructor () {
        super();
        this.collection(Observation);
    }

    sync ()  {
        http().getJson('/viescolaire/absences/observations/'+moment(new Date()).format('YYYY-MM-DD')+"/"+moment(new Date()).format('YYYY-MM-DD'))
        .done(function(data){
            this.observations.load(data);
        });
    }
}

export class Widget extends Model {
    wAbsSansMotifs : WAbsSansMotifs;
    wAppelsOublies : WAppelsOublies;
    wObservations : WObservations;
}

export class VieScolaire extends Model {
    widget : Widget;

    constructor () {
        super();
        this.widget = new Widget();
        this.widget.wAbsSansMotifs = new WAbsSansMotifs();
        this.widget.wAppelsOublies = new WAppelsOublies();
        this.widget.wObservations = new WObservations();
    }

    sync () {
        this.widget.wAbsSansMotifs.sync();
        this.widget.wAppelsOublies.sync();
        this.widget.wObservations.sync();
    }
}

export let vieScolaire = new VieScolaire();

model.build = function () {
    vieScolaire.sync();
}