import { model, notify, http, IModel, Model, Collection, BaseModel } from '../../entcore/entcore';

declare let _:any;
let moment = require('moment');

let gsFormatHeuresMinutes = "HH:mm";
let giHeureDebutPlage = 8;
let giHeureFinPlage = 18;

export class Plage extends Model {
    evenements : Collection<Evenement>;
    heure : number;
    duree : number;
    style : any;
}
export class Creneau extends Model {
    heureDebut : any;
    heureFin : any;
    cours : Cours;
    duree : any;
    style : any;
}
export class Evenement extends Model implements IModel{
    _id : number;

    get api () {
        return {
            post : '/viescolaire/absences/evenement',
            put : '/viescolaire/absences/evenement',
            delete : '/viescolaire/absences/evenement?evenementId='
        }
    }

    create () : Promise<{ id : number, bool : boolean }> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.post, this).done((data) => {
                resolve({id : data.id, bool : true});
            });
        });
    }

    update () : Promise<{ id : number, bool : boolean }> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.put, this).done((data) => {
                resolve({id : data.id, bool : false});
            });
        });
    }

    save () : Promise<any> {
        return new Promise((resolve, reject) => {
            if(this._id){
                this.update().then((data) => {
                    resolve(data);
                });
            }else{
                this.create().then((data) => {
                    resolve(data);
                });
            }
        })
    }

    delete () : Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete + this._id).done(() => {
                resolve();
            });
        });
    }
}

export class AbsencePrev extends Model {}

export class Eleve extends Model implements IModel {
    evenements : Collection<Evenement>;
    courss : Collection<Cours>;
    evenementsJours : any;
    absencePrevs : Collection<AbsencePrev>;
    plages : Collection<Plage>;
    composer : any;
    evenementsJour : any;
    eleve_id : number;
    absc_precedent_cours : boolean;

    get api () {
        return {

        }
    }

    constructor () {
        super();
        this.collection(Evenement, {
            sync : (psDateDebut, psDateFin) => {
                http().getJson('/viescolaire/absences/eleve/' + this.composer.eleve_id + '/evenements/' + psDateDebut + '/' + psDateFin).done((data) => {
                    this.evenements.load(data);
                });
            }
        });
        this.collection(Cours);
        this.evenementsJours = new Collection(Evenement);
        this.collection(Plage, {
            sync : (piIdAppel) => {
                // Evenements du jours
                let otEvt = this.composer.evenementsJour;
                // Liste des cours
                let otCours = this.composer.courss;
                let that = this.plages;
                // On copie les plages dans un tableau
                that.load(JSON.parse(JSON.stringify(this.plages)));
                for (let i = 0; i < that.all.length; i++) {
                    that.all[i].evenements = new Collection<Evenement>(Evenement);
                    that.all[i].evenements.composer = that.all[i].evenements.model = this;
                }
                /**
                 * Pour chaque plage, on récupere le cours correspondant, puis pour la plage, on ajoute au tableau evenements
                 * la liste des evenements relatifs à la plage horaire.
                 */
                otEvt.each((evenement) => {
                    let otCurrentCours = otCours.findWhere({cours_id : evenement.cours_id});
                    let otCurrentPlage = that.filter((plage) => {
                        let dt = parseInt(moment(otCurrentCours.cours_timestamp_dt).format('HH'));
                        return plage.heure === dt;
                    })[0];
                    otCurrentPlage.evenements.push(evenement, false);
                });
                /**
                 * Si il y a des absences previsionnelles, on les rajoutes dans le tableau d'évènements
                 */
                if(this.composer.absencePrevs.all.length > 0){
                    this.composer.absencePrevs.each((abs) => {
                        abs.fk_type_evt_id = 'abs-prev';
                        let dt = parseInt(moment(abs.absence_prev_timestamp_dt).format('HH'));
                        let fn = parseInt(moment(abs.absence_prev_timestamp_fn).format('HH'));
                        let oIndex = {
                            dt : undefined,
                            fn : undefined
                        };
                        oIndex.dt = that.indexOf(that.findWhere({heure : dt}));
                        oIndex.fn = that.indexOf(that.findWhere({heure : fn}));
                        if(oIndex.dt !== -1 && oIndex.fn !== -1){
                            for(let i = oIndex.dt; i < oIndex.fn; i++){
                                that.all[i].evenements.push(abs);
                            }
                        }
                    });
                }
            }
        });
        this.collection(AbsencePrev, {
            sync : (psDateDebut, psDateFin) => {
                http().getJson('/viescolaire/absences/eleve/' + this.composer.eleve_id + '/absencesprev/' + psDateDebut + '/' + psDateFin).done((data) => {
                    this.absencePrevs.load(data);
                });
            }
        });

    }
}

export class Appel extends Model implements IModel{
    id : number;

    get api () {
        return {
            post : '/viescolaire/absences/appel',
            put : '/viescolaire/absences/appel',
            delete : '/viescolaire/absences/appel'
        }
    }

    create () : Promise<Appel> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.post, this).done((data) => {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }

    update () : Promise<Appel> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.put, this).done((data) => {
                if(resolve && (typeof(resolve) === 'function')) {
                    resolve(data);
                }
            });
        })
    }

    save () : Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.id) {
                this.update().then(() => {
                    if(resolve && (typeof(resolve) === 'function')) {
                        resolve();
                    }
                });
            } else {
                this.create().then(() => {
                    if(resolve && (typeof(resolve) === 'function')) {
                        resolve();
                    }
                });
            }
        });
    }
}

class AppelElevesCollection {
    sync : any;
    load: (data: Eleve[], cb?: (item: Eleve) => void, refreshView?: boolean) => void;
    findWhere: (props: any) => Eleve;
    all: Eleve[];
    trigger: (eventName: string) => void;
    cours : Cours;
    composer: any;

    constructor () {
        this.sync = () => {
            http().getJson('/viescolaire/classe/' + this.composer.fk_classe_id + '/eleves').done((data) => {
                _.map(data, function(eleve){
                    eleve.cours = this.cours;
                });
                this.load(data);
                this.loadEvenements();
            });
        };
    }



    loadEvenements () {
        /**
         * On recupere tous les évènements de l'élève de la journée, quelque soit le cours puis on la disperse en 2 listes :
         * - evenementsJours qui nous permettera d'afficher l'historique.
         * - evenements qui va nous permettre de gérer les évènements de l'appel en cours.
         */
        http().getJson('/viescolaire/absences/evenement/classe/'+this.cours.fk_classe_id+'/periode/'+moment(this.cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(this.cours.cours_timestamp_dt).format('YYYY-MM-DD'))
            .done((data) => {
                for (let i = 0; i < this.all.length; i++) {
                    this.all[i].evenementsJour.load(_.where(data, {fk_eleve_id : this.all[i].eleve_id}));
                    this.all[i].evenements.load(this.all[i].evenementsJour.where({cours_id : this.cours.cours_id}));
                }
                this.loadAbscPrev();
            });
    }

    loadAbscPrev () {
        http().getJson('/viescolaire/absences/absencesprev/classe/'+this.cours.fk_classe_id+'/'+moment(this.cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(this.cours.cours_timestamp_dt).format('YYYY-MM-DD'))
            .done((data) => {
                for (let i = 0; i < this.all.length; i++) {
                    this.all[i].absencePrevs.load(_.where(data, {fk_eleve_id: this.all[i].eleve_id}));
                }
            });
        this.loadAbscLastCours();
    }

    loadAbscLastCours () {
        http().getJson('/viescolaire/absences/precedentes/classe/'+ this.cours.fk_classe_id+'/cours/'+ this.cours.cours_id)
            .done((data) => {
                _.each(data, function(absc){
                    let eleve = this.findWhere({eleve_id : absc.fk_eleve_id});
                    if(eleve !== undefined){
                        eleve.absc_precedent_cours = true;
                    }
                });
                this.loadCoursClasse();
            });
    }

    loadCoursClasse () {
        http().getJson('/viescolaire/'+ this.cours.fk_classe_id+'/cours/'+moment(this.cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(this.cours.cours_timestamp_fn).format('YYYY-MM-DD'))
            .done((data) => {
                _.each(data, function(eleve){
                    eleve.courss.load(data);
                });
                this.trigger("appelSynchronized");
            });
    }
}

export class Cours extends Model {
    cours : any;
    appel : Appel;
    eleves : Eleves;

    id : number;
    fk_personnel_id : number;
    fk_cours_id : number;
    fk_etat_appel_id : number;
    fk_classe_id : number;
    cours_timestamp_dt : string;
    cours_id : number;
    cours_timestamp_fn : string;

    get api () {
        return {
            getAppel : '/viescolaire/absences/appel/cours/'
        }
    }

    constructor () {
        super();
        this.cours = this;
        this.appel = new Appel();
        this.appel.sync = () => {
            http().getJson(this.api.getAppel + this.cours.cours_id).done((data) => {
                this.updateData(data[0]);
                if(this.id === undefined) {
                    this.fk_personnel_id = this.cours.fk_personnel_id;
                    this.fk_cours_id = this.cours.cours_id;
                    this.fk_etat_appel_id = 1;
                    this.appel.create().then((data) => {
                        this.cours.appel.id = data.id;
                    });
                }
            })
        };

        this.collection(Eleve, new AppelElevesCollection());
    }
}

export class VieScolaire extends Model {
    courss:Collection<Cours>;
    plages:Collection<Plage>;
    creneaus:Collection<Creneau>;

    constructor () {
        super();
        this.collection(Cours, {
            sync : (userId, dateDebut, dateFin) => {
                if(userId !== undefined && dateDebut !== undefined && dateFin !== undefined) {
                    http().getJson('/viescolaire/enseignant/' + userId + '/cours/' + dateDebut + '/' + dateFin)
                        .done((data) => {
                        this.courss.load(data);
                    });
                }
            }
        });

        this.collection(Plage, {
            sync : () => {
                let oListePlages = [];
                for (let heure = giHeureDebutPlage; heure <= giHeureFinPlage; heure++) {
                    let oPlage = new Plage();
                    oPlage.heure = heure;
                    if(heure === giHeureFinPlage) {
                        oPlage.duree = 0; // derniere heure
                    } else{
                        oPlage.duree = 60; // 60 minutes à rendre configurable ?
                    }
                    oPlage.style = {
                        "width" : (1/(giHeureFinPlage-giHeureDebutPlage+1))*100 +"%"
                    };
                    oListePlages.push(oPlage);
                }
                this.plages.load(oListePlages);
            }
        });

        this.collection(Creneau, {
            sync : () => {
                let oListeCreneauxJson = [];
                let oHeureEnCours;

                // creation d'un objet moment pour la plage du debut de la journée
                let goHeureDebutPlage = moment();
                goHeureDebutPlage.hour(giHeureDebutPlage);
                goHeureDebutPlage.minute(0);
                goHeureDebutPlage = moment(moment(goHeureDebutPlage).format(gsFormatHeuresMinutes), gsFormatHeuresMinutes);

                // creation d'un objet moment pour la plage de fin de journée
                let goHeureFinPlage = moment();
                goHeureFinPlage.hour(giHeureFinPlage);
                goHeureFinPlage.minute(0);
                goHeureFinPlage = moment(moment(goHeureFinPlage).format(gsFormatHeuresMinutes), gsFormatHeuresMinutes);

                if(this.courss !== undefined && this.courss.all.length > 0) {

                    // initialsiation heure en cours (1ère heure à placer sur les crenaux)
                    oHeureEnCours = goHeureDebutPlage;

                    for (let i = 0; i < this.courss.all.length; i++) {

                        let oCurrentCours = this.courss.all[i];

                        let oHeureDebutCours = moment(moment(oCurrentCours.cours_timestamp_dt).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);
                        let oHeureFinCours = moment(moment(oCurrentCours.cours_timestamp_fn).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);

                        // si le cours est après le dernier creneau ajouté
                        if (oHeureDebutCours.diff(oHeureEnCours) > 0) {

                            // on ajoute un crenau "vide" jusqu'au cours
                            let creneau = new Creneau();
                            creneau.heureDebut = oHeureEnCours.format(gsFormatHeuresMinutes);
                            creneau.heureFin = oHeureDebutCours.format(gsFormatHeuresMinutes);
                            creneau.cours = undefined;
                            creneau.duree = oHeureDebutCours.diff(oHeureEnCours, "minute");
                            creneau.style = {
                                "height": creneau.duree + "px"
                            };
                            oListeCreneauxJson.push(creneau);
                            oHeureEnCours = oHeureDebutCours;
                        }

                        let creneau = new Creneau();
                        creneau.heureDebut = oHeureDebutCours.format(gsFormatHeuresMinutes);
                        // TODO tester si heureFin = 18h
                        creneau.heureFin = oHeureFinCours.format(gsFormatHeuresMinutes);
                        creneau.cours = oCurrentCours;
                        creneau.duree = oHeureFinCours.diff(oHeureDebutCours, "minute");
                        creneau.style = {
                            "height": creneau.duree + "px"
                        };

                        oListeCreneauxJson.push(creneau);
                        oHeureEnCours = oHeureFinCours;

                        // Lors du dernier cours parcouru, on complète par un dernier créneau vide
                        // si le cours ne se termine pas à la fin de la journée
                        if (i === (this.courss.all.length - 1)) {

                            // si le cours ne termine pas la journée
                            // on ajoute un crenau "vide" jusqu'à la fin de la journée
                            if (goHeureFinPlage.diff(oHeureFinCours) > 0) {

                                let creneau = new Creneau();
                                creneau.heureDebut = oHeureFinCours.format(gsFormatHeuresMinutes);
                                creneau.heureFin = goHeureFinPlage.format(gsFormatHeuresMinutes);
                                creneau.cours = undefined;
                                creneau.duree = goHeureFinPlage.diff(oHeureFinCours, "minute");
                                creneau.style = {
                                    "height": creneau.duree + "px"
                                };
                                oListeCreneauxJson.push(creneau);
                            }
                        }
                    }
                }

                this.creneaus.load(oListeCreneauxJson);
            }
        })
    }

    sync () {
        this.courss.sync();
        this.plages.sync();
        this.creneaus.sync();
    }
}

export interface Eleves extends Collection<Eleve>, AppelElevesCollection {}

export let vieScolaire = new VieScolaire();

model.build = function () {
    (this as any).vieScolaire = vieScolaire;
    vieScolaire.sync();
};