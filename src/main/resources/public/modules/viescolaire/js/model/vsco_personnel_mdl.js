/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. WAbsSansMotifs : Objet Widget contenant la liste des absences sans motifs.
 *  2. WAppelsOublies : Objet Widget contenant la liste des appels oubliés.
 *  3. WMotVsco: Objet Widget contenant la liste des mots pour la vie scolaire.
 *  4. Widget : Objet contenant la liste des widgets de la page d'accueil CPE. Contient des listes de WAbsSansMotifs, WAppelsOublies et WMotVsco.
 */
function Evenement(){}
function Appel(){}
function Observation(){}
function WAbsSansMotifs(){
    this.collection(Evenement);
}
WAbsSansMotifs.prototype = {
    sync : function(){
        http().getJson("/viescolaire/absences/sansmotifs/"+moment(new Date()).format('YYYY-MM-DD')+"/"+moment(new Date()).format('YYYY-MM-DD'))
            .done(function(data){
                this.evenements.load(data);
            }.bind(this));
    }
};
function WAppelsOublies(){
    this.collection(Appel);
}
WAppelsOublies.prototype = {
    sync : function(){
        http().getJson("/viescolaire/absences/appels/noneffectues/"+moment(new Date()).format('YYYY-MM-DD')+"/"+moment(new Date()).format('YYYY-MM-DD'))
            .done(function(data){
                this.appels.load(data);
            }.bind(this));
    }
};
function WObservations(){
    this.collection(Observation);
}
WObservations.prototype = {
    sync : function(){
        http().getJson('/viescolaire/absences/observations/'+moment(new Date()).format('YYYY-MM-DD')+"/"+moment(new Date()).format('YYYY-MM-DD'))
            .done(function(data){
                this.observations.load(data);
            }.bind(this));
    }
};
function Widget(){}

model.build = function(){
    this.makeModels([WAbsSansMotifs, WAppelsOublies, WObservations, Widget, Evenement, Appel, Observation]);
    this.widget = new Widget();
    var that = this.widget;
    that.WAbsSansMotifs = new WAbsSansMotifs();
    that.WAbsSansMotifs.sync();
    that.WAppelsOublies = new WAppelsOublies();
    that.WAppelsOublies.sync();
    that.WObservations = new WObservations();
    that.WObservations.sync();
};
