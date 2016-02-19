/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. WAbsSansMotifs : Objet Widget contenant la liste des absences sans motifs.
 *  2. WAppelsOublies : Objet Widget contenant la liste des appels oubliés.
 *  3. WMotVsco: Objet Widget contenant la liste des mots pour la vie scolaire.
 *  4. Widget : Objet contenant la liste des widgets de la page d'accueil CPE. Contient des listes de WAbsSansMotifs, WAppelsOublies et WMotVsco.
 */

function WAbsSansMotifs(){};
function WAppelsOublies(){};
function WMotVsco(){};
function Widget(){
    this.collection(WAbsSansMotifs);
    this.collection(WAppelsOublies);
    this.collection(WMotVsco);
};

model.build = function(){
    this.makeModels([WAbsSansMotifs, WAppelsOublies, WMotVsco, Widget]);
    this.collection(Widget);
};
