var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';

function Creneau() {

}

function Evenement() {
}

Evenement.prototype = {
    create : function(callback){
        //http().postJson("todo", this.toJSON()).done(function(data) {
        //        callback(data.id); // set de l'id sur la CompetenceNote
        //    }
        //);
        //this.composer.isAbsent = true;
        callback();
    },
    delete : function(callback){
        //http().postJson("todo", this.toJSON()).done(function(data) {
        //        callback(data.id); // set de l'id sur la CompetenceNote
        //    }
        //);
        //this.composer.isAbsent = false;
        callback();
    }
};


function AbsencePrev() {
}

function Eleve() {
    this.collection(Evenement);
    this.evenements.sync = function(psDateDebut, psDateFin){
        http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/eleve/' + this.composer.id + '/evenements/' + psDateDebut + '/' + psDateFin).done(function(data){
            this.load(data);
        }.bind(this));
    };
    this.collection(AbsencePrev);
    this.absencePrevs.sync = function(psDateDebut, psDateFin) {
        http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/eleve/' + this.composer.id + '/absencesprev/' + psDateDebut + '/' + psDateFin).done(function(data){
            this.load(data);
        }.bind(this));
    };
}

function Cours(){
    this.collection(Eleve);
    this.eleves.sync = function(){
        http().getJson('/' + gsPrefixVieScolaire + '/classe/' + this.composer.id_classe + '/eleves').done(function(data){
            this.load(data);
        }.bind(this));
    }
}


///////////////////////
///   MODEL.BUILD   ///
model.build = function(){
    this.makeModels([Eleve, Cours, Evenement, AbsencePrev, Creneau]);

    this.collection(Cours);

    this.courss.sync = function(userId, dateDebut, dateFin){
        if(userId !== undefined && dateDebut !== undefined && dateFin !== undefined) {
            http().getJson('/' + gsPrefixVieScolaire + '/enseignant/' + userId + '/cours/' + dateDebut + '/' + dateFin).done(function (data) {
                this.load(data);
            }.bind(this));
        }
    };

    this.collection(Creneau);
    this.creneaus.sync = function() {
        var oListeCreneauxJson = [];

        var iHeureDebutPlage = 8;
        var iHeureFinPlage = 18;

        var oHeureEnCours;

        if(model.courss !== undefined && model.courss.all.length > 0) {

            // creation d'un ojet moment sur la même journée que le créneau
            // pour pouvoir faire une différence d'heures
            var oHeureDebutPlage = moment();
            oHeureDebutPlage.hour(iHeureDebutPlage);
            oHeureDebutPlage.minute(0);
            oHeureDebutPlage = moment(moment(oHeureDebutPlage).format("HH:mm"), "HH:mm");

            oHeureEnCours = oHeureDebutPlage;

            // creation d'un ojet moment sur la même journée que le créneau
            // pour pouvoir faire une différence d'heures
            var oHeureFinPlage = moment();
            oHeureFinPlage.hour(iHeureFinPlage);
            oHeureFinPlage.minute(0);
            oHeureFinPlage = moment(moment(oHeureFinPlage).format("HH:mm"), "HH:mm");



            for (i = 0; i < model.courss.all.length; i++) {

                var oCurrentCours = model.courss.all[i];

                var oHeureDebutCours = moment(moment(oCurrentCours.timestamp_debut).format("HH:mm"),"HH:mm");
                var oHeureFinCours = moment(moment(oCurrentCours.timestamp_fin).format("HH:mm"),"HH:mm");

                // si le cours est après le dernier creneau ajouté
                if (oHeureDebutCours.diff(oHeureEnCours) > 0) {

                    // on ajoute un crenau "vide" jusqu'au cours
                    var creneau = {};
                    creneau.heureDebut = oHeureEnCours.format("HH:mm");
                    creneau.heureFin = oHeureDebutCours.format("HH:mm"); //oHeureDebutCours.subtract(10, "minute").format("HH:mm");
                    creneau.cours = undefined;
                    creneau.duree = oHeureDebutCours.diff(oHeureEnCours, "minute");
                    creneau.style = {
                        "height": creneau.duree + "px"
                    };
                    oListeCreneauxJson.push(creneau);
                    oHeureEnCours = oHeureDebutCours;
                }

                var creneau = {};
                creneau.heureDebut = oHeureDebutCours.format("HH:mm");
                // TODO tester si heureFin = 18h
                creneau.heureFin = oHeureFinCours.format("HH:mm");//oHeureFinCours.subtract(10, "minute").format("HH:mm");
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
                    if (oHeureFinPlage.diff(oHeureFinCours) > 0) {

                        var creneau = {};
                        creneau.heureDebut = oHeureFinCours.format("HH:mm");
                        creneau.heureFin = oHeureFinPlage.format("HH:mm");
                        creneau.cours = undefined;
                        creneau.duree = oHeureFinPlage.diff(oHeureFinCours, "minute");
                        creneau.style = {
                            "height": creneau.duree + "px"
                        };
                        oListeCreneauxJson.push(creneau);
                    }
                }
            }
        }



        //for(i=8; i< 19; i++) {
        //    var creneau = {};
        //    creneau.heure = i;
        //    if(model.courss.all.length > 0) {
        //        var coursDansCetteHeure = _.filter(model.courss.all, function (cours) {
        //            return creneau.heure === parseInt(cours.heure_debut.split(":")[0]);
        //        });
        //        creneau.cours = coursDansCetteHeure[0];
        //        if(creneau.cours !== undefined) {
        //            var oHeureDebutCours = moment(creneau.cours.timestamp_debut);
        //            var oHeureFinCours = moment(creneau.cours.timestamp_fin);
        //            creneau.duree = oHeureFinCours.diff(oHeureDebutCours, "minute");
        //            creneau.style = {
        //                "height" : creneau.duree+"px"
        //            }
        //        }
        //    }
        //
        //    oListeCreneauxJson.push(creneau);
        //}
        this.load(oListeCreneauxJson);
    };
};
///////////////////////
