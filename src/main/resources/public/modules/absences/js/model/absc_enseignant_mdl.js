var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';

function Eleve() {
    this.collection(Evenement, {
        sync: function(idCours){
            http().getJson('/' + gsPrefixVieScolaire + '/' + gsPrefixAbsences + '/evenement/list/' + this.id + '/' + idCours).done(function(data){
            }.bind(this));
        }
    });
}

function Cours(){
    this.collection(Eleve,{
        sync: function(){
            http().getJson('/' + gsPrefixVieScolaire + '/' + this.idClasse + '/eleves').done(function(data){
            }.bind(this));
        }
    });
}


///////////////////////
///   MODEL.BUILD   ///
model.build = function(){
    this.makeModels([Eleve, Cours]);

    this.collection(Cours, {
        sync: function(userId, dateDebut, dateFin){
            http().getJson('/' + gsPrefixVieScolaire + '/enseignant/' + userId + '/cours/' + dateDebut + '/' + dateFin).done(function(data){
            }.bind(this));
        }
    });
};

///////////////////////
