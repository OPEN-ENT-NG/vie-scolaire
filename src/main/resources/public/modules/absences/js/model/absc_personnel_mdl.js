
/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. Responsable : Coordonnées des responsables de l'élève.
 *  2. Evenements : Liste des évènements relatifs à l'élève : Absences, retards, départs, ...
 *  3. Eleve : Objet contenant toutes les informations relatives à un élève. Contient une liste de Responables et d'Evenements.
 *  4. Classe : Objet contenant toutes les informations relatives à une Classe. Contient une liste d'élève.
 *  5. Enseignant : Objet contenant toutes les informations relatives à un enseignant.
 *  6. Matiere : Objet contenant toutes les informations relatives à une matière.
 *  7. Appel : Object contenant toutes les informations relatives à un appel fait en classe ou réalisé par le CPE/Personnel d'éducation.
 */

function Responsable(){};
function Evenement(){};
function Eleve(){
    this.collection(Evenement);
    this.collection(Responsable);
};
function Classe(){
    this.collection(Eleve);
};
function Enseignant(){};
function Matiere(){};
function Appel(){};

model.build = function(){
    this.makeModels([Responsable, Evenement, Eleve, Classe, Enseignant, Matiere, Appel]);
    this.collection(Classe, {
        sync : "/viescolaire/classes/etablissement"
    });
    this.collection(Enseignant, {
        sync : "/viescolaire/enseignants/etablissement"
    });
    this.collection(Appel);
};
