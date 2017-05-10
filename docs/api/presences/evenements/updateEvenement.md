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
 