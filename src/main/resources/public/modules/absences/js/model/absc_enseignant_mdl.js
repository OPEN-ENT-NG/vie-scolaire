var gsPrefixVieScolaire = 'viescolaire';
var gsPrefixNotes = 'notes';
var gsPrefixAbsences = 'absences';

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
    this.makeModels([Eleve, Cours, Evenement, AbsencePrev]);

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
