**GetEvtClassePeriode**
---- 
  Récupère tous les évènements pour une classe donnée dans une période donnée.
 
* **URL**
 
  `/evenement/classe/:classeId/periode/:dateDebut/:dateFin `
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `classeId = [String]`
   
   `dateDebut = [String]`
   
   `dateFin = [String]`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{    
      "id":9,
      "timestamp_arrive":null,
      "timestamp_depart":null,
      "commentaire":null,
      "saisie_cpe":false,
      "id_eleve":"3c16a728-763e-4880-8349-069eef2c6a5e",
      "id_appel":11,
      "id_type":2,
      "id_pj":null,
      "id_motif":8
    }]
    ```
 
* **Notes:**
  `dateDebut et dateFin sont au format : yyyy-mm-dd 
         ex: 2017-05-10 pour le 10 mai 2017.` 
  
 