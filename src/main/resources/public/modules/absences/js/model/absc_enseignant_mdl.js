var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';
var giHeureDebutPlage = 8;
var giHeureFinPlage = 18;
var gsFormatHeuresMinutes = "HH:mm";

function Plage() {
}

function Creneau() {
}

function Evenement() {
}

Evenement.prototype = {
    create : function(callback){
        http().postJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/evenement', this).done(function(data){
            callback(data.evenement_id);
        });
    },
    update : function(callback){
        http().putJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/evenement', this).done(function(data){
            callback(data.evenement_id);
        });
    },
    save : function(callback) {
        // si l'evenement a deja un identifiant alors il s'agit d'une maj
        if(this.evenement_id){
            this.update(callback);
            // sinon d'une création
        }else{
            this.create(callback);
        }
    },
    delete : function(callback){
        http().delete('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/evenement/' + this.evenement_id).done(function(data){
            callback();
        });
    }
};


function AbsencePrev() {
}

function Eleve() {
    this.collection(Evenement);
    this.evenements.sync = function(psDateDebut, psDateFin){
        http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/eleve/' + this.composer.eleve_id + '/evenements/' + psDateDebut + '/' + psDateFin).done(function(data){
            this.load(data);
        }.bind(this));
    };

    this.collection(AbsencePrev);
    this.absencePrevs.sync = function(psDateDebut, psDateFin) {
        http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/eleve/' + this.composer.eleve_id + '/absencesprev/' + psDateDebut + '/' + psDateFin).done(function(data){
            this.load(data);
        }.bind(this));
    };

    this.collection(Creneau);
    this.creneaus.sync = function(piIdAppel) {
        var oListeCreneauxJson = [];
        var oHeureEnCours;

        // creation d'un objet moment pour la plage du debut de la journée
        var goHeureDebutPlage = moment();
        goHeureDebutPlage.hour(giHeureDebutPlage);
        goHeureDebutPlage.minute(0);
        goHeureDebutPlage = moment(moment(goHeureDebutPlage).format(gsFormatHeuresMinutes), gsFormatHeuresMinutes);

        // creation d'un objet moment pour la plage de fin de journée
        var goHeureFinPlage = moment();
        goHeureFinPlage.hour(giHeureFinPlage);
        goHeureFinPlage.minute(0);
        goHeureFinPlage = moment(moment(goHeureFinPlage).format(gsFormatHeuresMinutes), gsFormatHeuresMinutes);

        if(model.courss !== undefined && model.courss.all.length > 0) {

            // initialsiation heure en cours (1ère heure à placer sur les crenaux)
            oHeureEnCours = goHeureDebutPlage;

            for (var i = 0; i < model.courss.all.length; i++) {

                var oCurrentCours = model.courss.all[i];

                var oHeureDebutCours = moment(moment(oCurrentCours.cours_timestamp_dt).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);
                var oHeureFinCours = moment(moment(oCurrentCours.cours_timestamp_fn).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);

                // si le cours est après le dernier creneau ajouté
                if (oHeureDebutCours.diff(oHeureEnCours) > 0) {

                    // on ajoute un crenau "vide" jusqu'au cours
                    var creneau = {};
                    creneau.heureDebut = oHeureEnCours.format(gsFormatHeuresMinutes);
                    creneau.heureFin = oHeureDebutCours.format(gsFormatHeuresMinutes);
                    creneau.cours = undefined;
                    creneau.duree = oHeureDebutCours.diff(oHeureEnCours, "minute");
                    creneau.style = {
                        "width": (creneau.duree/60) * (1/(giHeureFinPlage-giHeureDebutPlage+1))*100 + "%"
                    };
                    oListeCreneauxJson.push(creneau);
                    oHeureEnCours = oHeureDebutCours;
                }

                // crenau d'un cours
                var creneau = {};
                creneau.heureDebut = oHeureDebutCours.format(gsFormatHeuresMinutes);
                // TODO tester si heureFin = 18h
                creneau.heureFin = oHeureFinCours.format(gsFormatHeuresMinutes);
                creneau.cours = oCurrentCours;
                creneau.duree = oHeureFinCours.diff(oHeureDebutCours, "minute");

                // calcul s'il y a des absences/retard/depart/absences prévisionnelles sur le cours
                creneau.isAbsent = false;
                creneau.hasRetard = false;
                creneau.hasDepart = false;
                creneau.hasAbsencePrev = false;

                // TODO récupérer les évenements du currentCours et non les évenements de l'appel en cours
                if(this.composer.evenements !== undefined && this.composer.evenements.all.length > 0) {
                    creneau.isAbsent = this.composer.evenements.findWhere({fk_eleve_id: this.composer.eleve_id, fk_type_evt_id: 1, fk_appel_id: piIdAppel}) !== undefined;
                    creneau.hasRetard = this.composer.evenements.findWhere({fk_eleve_id: this.composer.eleve_id, fk_type_evt_id: 2, fk_appel_id: piIdAppel}) !== undefined;
                    creneau.hasDepart = this.composer.evenements.findWhere({fk_eleve_id: this.composer.eleve_id, fk_type_evt_id: 3, fk_appel_id: piIdAppel}) !== undefined;
                }

                if(this.composer.absencePrevs !== undefined && this.composer.absencePrevs.all.length > 0) {
                    var iIdEleve = this.composer.eleve_id;
                    var oAbsencePrevs = _.filter(this.composer.absencePrevs.all, function(poAbsencePrev) {
                        var poDebutAbsenceMoment = moment(moment(poAbsencePrev.absence_prev_timestamp_dt).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);
                        var poFinAbsenceMoment = moment(moment(poAbsencePrev.absence_prev_timestamp_fn).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);

                        // l'absence previsionnelle doit englobler le cours pour qu'on indique qu'il y ait eu absence prev sur celui-ci
                        return (iIdEleve === poAbsencePrev.fk_eleve_id) && (oHeureDebutCours.diff(poDebutAbsenceMoment, "minute") >= 0) && (poFinAbsenceMoment.diff(oHeureFinCours, "minute") >= 0);
                    });
                    creneau.hasAbsencePrev = oAbsencePrevs !== undefined && oAbsencePrevs.length > 0;
                }

                creneau.style = {
                    "width": (creneau.duree/60) * (1/(giHeureFinPlage-giHeureDebutPlage+1))*100 + "%"
                };

                oListeCreneauxJson.push(creneau);
                oHeureEnCours = oHeureFinCours;

                // Lors du dernier cours parcouru, on complète par un dernier créneau vide
                // si le cours ne se termine pas à la fin de la journée
                if (i === (model.courss.all.length - 1)) {

                    // si le cours ne termine pas la journée
                    // on ajoute un crenau "vide" jusqu'à la fin de la journée
                    if (goHeureFinPlage.diff(oHeureFinCours) > 0) {

                        var creneau = {};
                        creneau.heureDebut = oHeureFinCours.format(gsFormatHeuresMinutes);
                        creneau.heureFin = goHeureFinPlage.format(gsFormatHeuresMinutes);
                        creneau.cours = undefined;
                        creneau.duree = goHeureFinPlage.diff(oHeureFinCours, "minute");
                        creneau.style = {
                            "width": (creneau.duree/60) * (1/(giHeureFinPlage-giHeureDebutPlage+1))*100 + "%"
                        };
                        oListeCreneauxJson.push(creneau);
                    }
                }
            }
        }
        this.load(oListeCreneauxJson);
    };
}

function Appel() {
}

Appel.prototype = {
    // crée en bdd un appel
    create : function(fn) {
        http().postJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel', this).done(function(data){
            if(fn && (typeof(fn) === 'function')) {
                fn(data);
            }
        });
    },
    //maj en bdd un appel
    update : function() {
        http().putJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel', this).done(function(data){
            console.log("Appel mis à jour. Etat : " + data.fk_etat_appel_id);
        });
    },
    save : function() {
        // si l'appel a deja un identifiant alors il s'agit d'un  maj
        if(this.appel_id){
            this.update();
        // sinon d'un création
        }else{
            this.create();
        }
    }
};

function Cours(){
    var cours = this;
    this.appel = new Appel();
    this.appel.sync = function(){
        http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/appel/cours/' + cours.cours_id).done(function(data){
            this.updateData(data[0]);
            // creation en bdd s'i l'appel n'existe pas encore
            if(this.appel_id === undefined) {
                this.fk_personnel_id = cours.fk_personnel_id;
                this.fk_cours_id = cours.cours_id;
                this.fk_etat_appel_id = 1;
                this.create(function(appel) {
                    cours.appel.appel_id = appel.appel_id;
                });
            }
        }.bind(this));
    };

    this.collection(Eleve);
    this.eleves.sync = function(){
        http().getJson('/' + gsPrefixVieScolaire + '/classe/' + this.composer.fk_classe_id + '/eleves').done(function(data){
            this.load(data);
            this.loadEvenements();
        }.bind(this));
    };

    this.eleves.loadEvenements = function(){
        http().getJson('/'+gsPrefixVieScolaire+'/'+gsPrefixAbsences+'/evenement/classe/'+cours.fk_classe_id+'/cours/'+cours.cours_id).done(function(data){
            this.each(function(eleve){
               eleve.evenements.load(_.where(data, {fk_eleve_id : eleve.eleve_id}));
            });
            this.loadAbscPrev();
        }.bind(this));
    };

    this.eleves.loadAbscPrev = function(){
        http().getJson('/'+gsPrefixVieScolaire+'/'+gsPrefixAbsences+'/absencesprev/classe/'+cours.fk_classe_id+'/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD')).done(function(data){
            this.each(function(eleve){
                eleve.absencePrevs.load(_.where(data, {fk_eleve_id : eleve.eleve_id}));
            });
            this.trigger("appelSynchronized");
        }.bind(this));
    }

    this.eleves.loadAbscLastCours = function(){
        http().getJson('/');
    }
}


///////////////////////
///   MODEL.BUILD   ///
model.build = function(){
    this.makeModels([Appel, Eleve, Cours, Evenement, AbsencePrev, Creneau, Plage]);

    this.collection(Cours);

    this.courss.sync = function(userId, dateDebut, dateFin){
        if(userId !== undefined && dateDebut !== undefined && dateFin !== undefined) {
            http().getJson('/' + gsPrefixVieScolaire + '/enseignant/' + userId + '/cours/' + dateDebut + '/' + dateFin).done(function (data) {
                this.load(data);
            }.bind(this));
        }
    };

    this.collection(Plage);
    this.plages.sync = function() {
        var oListePlages = [];
        for (var heure = giHeureDebutPlage; heure <= giHeureFinPlage; heure++) {
            var oPlage = new Plage();
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
        this.load(oListePlages);
    };


    this.collection(Creneau);
    this.creneaus.sync = function() {
        var oListeCreneauxJson = [];
        var oHeureEnCours;

        // creation d'un objet moment pour la plage du debut de la journée
        var goHeureDebutPlage = moment();
        goHeureDebutPlage.hour(giHeureDebutPlage);
        goHeureDebutPlage.minute(0);
        goHeureDebutPlage = moment(moment(goHeureDebutPlage).format(gsFormatHeuresMinutes), gsFormatHeuresMinutes);

        // creation d'un objet moment pour la plage de fin de journée
        var goHeureFinPlage = moment();
        goHeureFinPlage.hour(giHeureFinPlage);
        goHeureFinPlage.minute(0);
        goHeureFinPlage = moment(moment(goHeureFinPlage).format(gsFormatHeuresMinutes), gsFormatHeuresMinutes);

        if(model.courss !== undefined && model.courss.all.length > 0) {

            // initialsiation heure en cours (1ère heure à placer sur les crenaux)
            oHeureEnCours = goHeureDebutPlage;

            for (var i = 0; i < model.courss.all.length; i++) {

                var oCurrentCours = model.courss.all[i];

                var oHeureDebutCours = moment(moment(oCurrentCours.cours_timestamp_dt).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);
                var oHeureFinCours = moment(moment(oCurrentCours.cours_timestamp_fn).format(gsFormatHeuresMinutes),gsFormatHeuresMinutes);

                // si le cours est après le dernier creneau ajouté
                if (oHeureDebutCours.diff(oHeureEnCours) > 0) {

                    // on ajoute un crenau "vide" jusqu'au cours
                    var creneau = {};
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

                var creneau = {};
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
                if (i === (model.courss.all.length - 1)) {

                    // si le cours ne termine pas la journée
                    // on ajoute un crenau "vide" jusqu'à la fin de la journée
                    if (goHeureFinPlage.diff(oHeureFinCours) > 0) {

                        var creneau = {};
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
        this.load(oListeCreneauxJson);
    };
};
///////////////////////
