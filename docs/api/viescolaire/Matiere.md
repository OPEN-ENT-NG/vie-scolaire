# MatiereController

##### Table of Contents
  * [viewMatiere](#viewMatiere)
  
<a name="viewMatieresEleve" />

## viewMatieresEleve
  Retourne les matières enseignées par un enseignant donné.
 
* **URL**
 
  `viescolaire/matieres`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `idEtablissement = String`
   
   `idEnseignant = String`
   
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
       "idEtablissement": "0c03ee92-7ca0-4025-9971-df3e63a2ce64",
       "id": "185310-1494411722561",
       "externalId": "062300",
       "name": "PHYSIQUE-CHIMIE",
       "libelleClasses": ["2975$2 8", "2975$T 5S", "2975$T 4SSPE", "2975$T 5S_AP", "2975$T 5S_APA", "2975$T 5SP1", "2975$T 5SP2"],
       "sous_matieres": []
      }]
    ```
