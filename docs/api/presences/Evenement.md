*EvenementController*
 -------- 
 ---- 
  **createEvenement**
  ---- 
    Création  d'un évènement.
   
  * **URL**
   
    `/evenement`
   
  * **Method:**
     
    `POST` 
 
  * **Data Params**
     ```json
     
      {
        "commentaire" : "lets",
        "id_appel" : 12,
        "id_eleve" : "35eecf92-4313-4322-ad21-a2f4900ee5b9",
        "id_motif" : 8,  
        "id_type" : 5, 
        "saisie_cpe" : false
      }
      ```
      **Required:**           
             `saisie_cpe`,
             `id_eleve`,
             `id_appel`,
             `id_type`,
             `id_motif`
             
  * **Response:**
     
      * **Code:** 200 <br />
      * **Content**:  
      ```json
      {
       "id":91 
      }
      ```
 * **Notes:**
      
      `id_type = 1 : pour une absence`   
         
      `id_type = 2 : pour un retard`
      
      `id_type = 3 : pour un départ`
      
      `id_type = 5 : pour une observation`
  
   **updateEvenement**
   ---- 
     Mise à jour  d'un évènement.
    
   * **URL**
    
     `/evenement`
    
   * **Method:**
      
     `PUT` 
  
   * **Data Params**
      ```json
      
       {
         "commentaire" : "lets",
         "id_appel" : 12,
         "id_eleve" : "35eecf92-4313-4322-ad21-a2f4900ee5b9",
         "id_motif" : 8,  
         "id_type" : 5, 
         "saisie_cpe" : false
       }
       ```
       **Required:**           
              `saisie_cpe`,
              `id_eleve`,
              `id_appel`,
              `id_type`,
              `id_motif`
              
   * **Response:**
      
       * **Code:** 200 <br />
       * **Content**:  
       ```json
        {"rows":1}
       ```
  * **Notes:**
       
       `id_type = 1 : pour une absence`   
          
       `id_type = 2 : pour un retard`
       
       `id_type = 3 : pour un départ`
       
       `id_type = 5 : pour une observation`
    
    **updateMotifEvenement**
       ---- 
         Mise à jour du motif d'un évènement.
        
       * **URL**
        
         `/evenement/:idEvenement/updatemotif`
        
       * **Method:**
          
         `PUT` 
      
       * **Data Params**
          ```json
          
           {
             "idEvenement" : 12,
             "id_motif" : 8
           }
           ```
           **Required:**           
                  `idEvenement`,
                  `id_motif`
                  
       * **Response:**
          
           * **Code:** 200 <br />
           * **Content**:  
           ```json
            {"rows":1}
           ```    
    
    **deleteEvenement**
    ---- 
      Suppression d'un évènement.
     
    * **URL**
     
      `/evenement`
     
    * **Method:**
       
      `DELETE` 
   
    *  **URL Params**
     
        **Required:**
      
       `evenementId = Integer`
       
    * **Response:**
       
        * **Code:** 200 <br />
        * **Content**:  
        ```
        {"rows":1}
        ```
**getAbsencesDernierCours**
---- 
  Récupère toutes les absences du cours précédent en fonction de l'identifiant de la classe et de l'identifiant du cours.
 
* **URL**
 
  `/precedentes/classe/:classeId/cours/:coursId `
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `classeId = String`
   
   `coursId = Integer`
   
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [
       {"id_eleve":"3c16a728-763e-4880-8349-069eef2c6a5e"},
       {"id_eleve":"35eecf92-4313-4322-ad21-a2f4900ee5b9"}
    ]
    ```

**getEvtClassePeriode**
---- 
  Récupère tous les évènements pour une classe donnée dans une période donnée.
 
* **URL**
 
  `/evenement/classe/:classeId/periode/:dateDebut/:dateFin `
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `classeId = String`
   
   `dateDebut = String`
   
   `dateFin = String`
   
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
  
 