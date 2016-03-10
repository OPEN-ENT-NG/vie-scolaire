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
            callback(data.evenement_id, true);
        });
    },
    update : function(callback){
        http().putJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/evenement', this).done(function(data){
            callback(data.evenement_id, false);
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
    this.collection(Cours);
    this.evenementsJour = new Collection(Evenement);
    this.evenementsJour.composer = this.evenementsJour.model = this;
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

    this.collection(Plage);
    this.plages.sync = function(piIdAppel, cb) {
        // Evenements du jours
        var otEvt = this.composer.evenementsJour;
        // Liste des cours
        var otCours = this.composer.courss;
        var that = this;
        // On copie les plages dans un tableau
        that.load(JSON.parse(JSON.stringify(model.plages)));
        that.map(function(plage){
            plage.evenements = new Collection(Evenement);
            plage.evenements.composer = plage.evenements.model = this;
        });
        /**
         * Pour chaque plage, on récupere le cours correspondant, puis pour la plage, on ajoute au tableau evenements
         * la liste des evenements relatifs à la plage horaire.
         */
        otEvt.each(function(evenement){
            var otCurrentCours = otCours.findWhere({cours_id : evenement.cours_id});
            var otCurrentPlage = that.filter(function(plage){
                var dt = parseInt(moment(otCurrentCours.cours_timestamp_dt).format('HH'));
                return plage.heure === dt;
            })[0];
            otCurrentPlage.evenements.push(evenement, false);
        });
        /**
         * Si il y a des absences previsionnelles, on les rajoutes dans le tableau d'évènements
         */
        if(this.composer.absencePrevs.all.length > 0){
            this.composer.absencePrevs.each(function(abs){
               abs.fk_type_evt_id = 'abs-prev';
                var dt = parseInt(moment(abs.absence_prev_timestamp_dt).format('HH'));
                var fn = parseInt(moment(abs.absence_prev_timestamp_fn).format('HH'));
                var oIndex = {
                    dt : undefined,
                    fn : undefined
                };
                oIndex.dt = that.indexOf(that.findWhere({heure : dt}));
                oIndex.fn = that.indexOf(that.findWhere({heure : fn}));
                if(oIndex.dt !== -1 && oIndex.fn !== -1){
                    for(var i = oIndex.dt; i < oIndex.fn; i++){
                        that.all[i].evenements.push(abs);
                    }
                }
            });
        }
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
            _.map(data, function(eleve){
               eleve.cours = cours;
            });
            this.load(data);
            this.loadEvenements();
        }.bind(this));
    };

    this.eleves.loadEvenements = function(){
        /**
         * On recupere tous les évènements de l'élève de la journée, quelque soit le cours puis on la disperse en 2 listes :
         * - evenementsJours qui nous permettera d'afficher l'historique.
         * - evenements qui va nous permettre de gérer les évènements de l'appel en cours.
         */
        http().getJson('/'+gsPrefixVieScolaire+'/'+gsPrefixAbsences+'/evenement/classe/'+cours.fk_classe_id+'/periode/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD'))
            .done(function(data){
                this.each(function(eleve){
                    eleve.evenementsJour.load(_.where(data, {fk_eleve_id : eleve.eleve_id}));
                    eleve.evenements.load(eleve.evenementsJour.where({cours_id : cours.cours_id}));
                });
                this.loadAbscPrev();
            }.bind(this));
    };

    this.eleves.loadAbscPrev = function(){
        http().getJson('/'+gsPrefixVieScolaire+'/'+gsPrefixAbsences+'/absencesprev/classe/'+cours.fk_classe_id+'/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD')).done(function(data){
            this.each(function(eleve){
                eleve.absencePrevs.load(_.where(data, {fk_eleve_id : eleve.eleve_id}));
            });
            this.loadAbscLastCours();

        }.bind(this));
    }

    this.eleves.loadAbscLastCours = function(){
        http().getJson('/'+gsPrefixVieScolaire+'/'+gsPrefixAbsences+'/precedentes/classe/'+this.model.fk_classe_id+'/cours/'+this.model.cours_id).done(function(data){
            var that = this;
            _.each(data, function(absc){
                var eleve = that.findWhere({eleve_id : absc.fk_eleve_id});
                if(eleve !== undefined){
                    eleve.absc_precedent_cours = true;
                }
            });
            this.loadCoursClasse();
        }.bind(this));
    };

    this.eleves.loadCoursClasse = function(){
        http().getJson('/'+gsPrefixVieScolaire+'/'+cours.fk_classe_id+'/cours/'+moment(cours.cours_timestamp_dt).format('YYYY-MM-DD')+'/'+moment(cours.cours_timestamp_fn).format('YYYY-MM-DD')).done(function(data){
            this.each(function(eleve){
                eleve.courss.load(data);
            });
            this.trigger("appelSynchronized");
        }.bind(this));
    };
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
