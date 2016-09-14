import { model, notify, http, IModel, Model, Collection, BaseModel } from '../../entcore/entcore';

let moment = require('moment');

declare let _:any;

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
        .done((data) => {
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
        .done((data) => {
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
        .done((data) => {
            this.observations.load(data);
        });
    }
}

export class Widget extends Model {
    WAbsSansMotifs : WAbsSansMotifs;
    WAppelsOublies : WAppelsOublies;
    WObservations : WObservations;
}

export class VieScolaire extends Model {
    widget : Widget;

    constructor () {
        super();
        this.widget = new Widget();
        this.widget.WAbsSansMotifs = new WAbsSansMotifs();
        this.widget.WAppelsOublies = new WAppelsOublies();
        this.widget.WObservations = new WObservations();
    }

    sync () {
        this.widget.WAbsSansMotifs.sync();
        this.widget.WAppelsOublies.sync();
        this.widget.WObservations.sync();
    }
}

export let vieScolaire = new VieScolaire();

model.build = function () {
    (this as any).vieScolaire = vieScolaire;
    vieScolaire.sync();
}