/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. Responsable : Coordonnées des responsables de l'élève.
 *  2. Evenements : Liste des évènements relatifs à l'élève : Absences, retards, départs, ...
 *  3. Eleve : Objet contenant toutes les informations relatives à un élève. Contient une liste de Responables et d'Evenements.
 *  4. Classe : Objet contenant toutes les informations relatives à une Classe. Contient une liste d'élève.
 *  5. Enseignant : Objet contenant toutes les informations relatives à un enseignant.
 *  6. Matiere : Objet contenant toutes les informations relatives à une matière.
 *  7. Appel : Object contenant toutes les informations relatives à un appel fait en classe ou réalisé par le CPE/Personnel d'éducation.
 *  8. WAbsSansMotifs : Objet Widget contenant la liste des absences sans motifs.
 *  9. WAppelsOublies : Objet Widget contenant la liste des appels oubliés.
 *  10.WMotVsco: Objet Widget contenant la liste des mots pour la vie scolaire.
 *  11.Widget : Objet contenant la liste des widgets de la page d'accueil CPE. Contient des listes de WAbsSansMotifs, WAppelsOublies et WMotVsco.
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
function WAbsSansMotifs(){};
function WAppelsOublies(){};
function WMotVsco(){};
function Widget(){
    this.collection(WAbsSansMotifs);
    this.collection(WAppelsOublies);
    this.collection(WMotVsco);
};

model.build = function(){
    this.makeModels([Responsable, Evenement, Eleve, Classe, Enseignant, Matiere, Appel, WAbsSansMotifs, WAppelsOublies, WMotVsco, Widget]);
    this.collection(Classe);
    this.collection(Enseignant);
    this.collection(Appel);
};
