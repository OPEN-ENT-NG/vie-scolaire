/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. WAbsSansMotifs : Objet Widget contenant la liste des absences sans motifs.
 *  2. WAppelsOublies : Objet Widget contenant la liste des appels oubliés.
 *  3. WMotVsco: Objet Widget contenant la liste des mots pour la vie scolaire.
 *  4. Widget : Objet contenant la liste des widgets de la page d'accueil CPE. Contient des listes de WAbsSansMotifs, WAppelsOublies et WMotVsco.
 */
function Evenement(){}
function Appel(){}
function WAbsSansMotifs(){
    this.collection(Evenement);
}
WAbsSansMotifs.prototype = {
    sync : function(){
        http().getJson("/viescolaire/absences/sansmotifs/"+moment(new Date(2016, 01, 10)).format('YYYY-MM-DD')+"/"+moment(new Date(2016, 01, 10)).format('YYYY-MM-DD'))
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
        http().getJson("/viescolaire/absences/appels/noneffectues/"+moment(new Date(2016, 01, 10)).format('YYYY-MM-DD')+"/"+moment(new Date(2016, 01, 10)).format('YYYY-MM-DD'))
            .done(function(data){
                this.appels.load(data);
            }.bind(this));
    }
};
function WMotVsco(){}
function Widget(){}

model.build = function(){
    this.makeModels([WAbsSansMotifs, WAppelsOublies, WMotVsco, Widget, Evenement, Appel]);
    this.widget = new Widget();
    this.widget.WAbsSansMotifs = new WAbsSansMotifs();
    this.widget.WAbsSansMotifs.sync();
    this.widget.WAppelsOublies = new WAppelsOublies();
    this.widget.WAppelsOublies.sync();
    this.widget.WMotVsco = new WMotVsco();
};
