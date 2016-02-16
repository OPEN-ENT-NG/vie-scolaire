var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';

function Evenement() {

}

function Eleve() {
    this.collection(Evenement);
    this.evenements.sync = function(idCours){
        http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/eleve/' + this.composer.id + '/evenements/' + dateDebut + '/' + dateFin).done(function(data){
            this.load(data);
        }.bind(this));
    }
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
    this.makeModels([Eleve, Cours]);

    this.collection(Cours);

    this.courss.sync = function(userId, dateDebut, dateFin){
        if(userId !== undefined && dateDebut !== undefined && dateFin !== undefined) {
            http().getJson('/' + gsPrefixVieScolaire + '/enseignant/' + userId + '/cours/' + dateDebut + '/' + dateFin).done(function (data) {
                this.load(data);
            }.bind(this));
        }
    }
};
///////////////////////
