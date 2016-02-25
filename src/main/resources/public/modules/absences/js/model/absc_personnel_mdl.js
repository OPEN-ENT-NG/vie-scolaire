
/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. Responsable : Coordonnées des responsables de l'élève.
 *  2. Evenements : Liste des évènements relatifs à l'élève : Absences, retards, départs, ...
 *  3. Eleve : Objet contenant toutes les informations relatives à un élève. Contient une liste de Responables et d'Evenements.
 *  4. Classe : Objet contenant toutes les informations relatives à une Classe. Contient une liste d'élève.
 *  5. Enseignant : Objet contenant toutes les informations relatives à un enseignant.
 *  6. Matiere : Objet contenant toutes les informations relatives à une matière.
 *  7. Appel : Object contenant toutes les informations relatives à un appel fait en classe ou réalisé par le CPE/Personnel d'éducation.
 *  8. Motif : Contient les différents motifs d'absences relatif à l'établissement.
 */

function Responsable(){};
function Justificatif(){};
function Evenement(){};
Evenement.prototype = {
    update : function(){
        http().putJson('/viescolaire/absences/evenement/'+this.evenement_id, { evenement : this }).done(function(resp){
           console.log(resp);
        });
    }
};

function Eleve(){
    this.collection(Responsable);
};
function Classe(){
    this.collection(Eleve);
};
function Enseignant(){};
function Matiere(){};
function Appel(){};

function Motif(){};

model.build = function(){
    this.makeModels([Responsable, Evenement, Eleve, Classe, Enseignant, Matiere, Appel, Motif, Justificatif]);
    this.collection(Classe, {
        sync : "/viescolaire/classes/etablissement"
    });
    this.collection(Enseignant, {
        sync : "/viescolaire/enseignants/etablissement"
    });
    this.collection(Appel);
    //sync : '/viescolaire/absences/appels/'+moment(new Date(2016, 01, 10)).format('YYYY-MM-DD')+'/'+moment(new Date()).format('YYYY-MM-DD')
    this.appels.sync = function(pODateDebut, pODateFin){
        if(pODateDebut !== undefined && pODateFin !== undefined){
            http().getJson('/viescolaire/absences/appels/'+moment(pODateDebut).format('YYYY-MM-DD')+'/'+moment(pODateFin).format('YYYY-MM-DD')).done(function(data){
                this.load(data);
            }.bind(this));
        }
    };
    this.collection(Evenement);
    this.evenements.sync = function(psDateDebut, psDateFin){
        if(psDateDebut !== undefined && psDateDebut !== undefined){
            http().getJson('/viescolaire/absences/eleves/evenements/'+moment(psDateDebut).format('YYYY-MM-DD')+'/'+moment(psDateFin).format('YYYY-MM-DD')).done(function(data){
                this.load(data);
            }.bind(this));
        }
    };

    this.collection(Motif, {
        sync : function(){
            http().getJson('/viescolaire/absences/motifs').done(function(motifs){
                this.load(motifs);
                model.motifs.map(function(motif){
                    motif.motif_justifiant_libelle = motif.motif_justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                    return motif;
                });
            }.bind(this));
        }
    });
    this.collection(Justificatif, {
       sync : '/viescolaire/absences/justificatifs'
    });
};
