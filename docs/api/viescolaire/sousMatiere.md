# SousMatiereController

##### Table of Contents
  * [viewSousMatieres](#viewSousMatieres)
  
<a name="viewSousMatieres" />

## viewSousMatieres
  Récupère les sous matières pour une matière donnée.
 
* **URL**
 
  `viescolaire/evaluations/matieres/:id/sousmatieres`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `id = String`
   
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
      "id": 1,
      "libelle": "Ecrit"
    }, {
      "id": 2,
      "libelle": "Oral"
    }]
    ```
 
