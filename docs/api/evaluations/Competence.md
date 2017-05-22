# Appreciation

##### Table of Contents
  * [create](#create)
  * [update](#update)
  * [delete](#delete)
  * [getBFCsEleve](#getBFCsEleve)
  
<a name="create" />


## create

   Créé un BFC.
  
 * **URL**
  
   `viescolaire/evaluations/bfc`
  
 * **Method:**
    
   `POST` 

 * **Data Params**
    ```json
     {
       "id_domaine" : 3,
       "id_eleve" : "f0c65d0b-11e7-4ae9-83c2-401b1b27c0c6",
       "id_etablissement" : "7d6b93f1-064c-4a15-88c7-815ebf33815b",
       "owner" : "5b275ccb-9b38-4c7b-bc30-282a2aadb318",
       "valeur" :4
     }
     ```
     **Required:**           
            `id_etablissement`,
                `id_eleve`, 
                `id_domaine`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     {
      "id":11 
     }
     ```
<a name="update" />

## update
   met à jour une note pour un domaine du BFC.
  
 * **URL**
  
   `viescolaire/evaluations/bfc`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {
       "id_domaine" : 3,
       "id_eleve" : "f0c65d0b-11e7-4ae9-83c2-401b1b27c0c6",
       "id_etablissement" : "7d6b93f1-064c-4a15-88c7-815ebf33815b",
       "owner" : "5b275ccb-9b38-4c7b-bc30-282a2aadb318",
       "valeur" :4
     }
     ```    
     **Required:**           
            `id_personnel`,
                `id_cours`, 
                `id_etat`,
                `id`
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {
       "rows":1
    }
     ```

<a name="delete" />

## delete
  Supprime une valeur du BFC pour remettre le BFC à sa valeur calculée.
 
* **URL**
 
  `viescolaire/evaluations/bfc`
 
* **Method:**
   
  `DELETE` 
   
*  **URL Params**
 
    **Required:**
  
   `id = Integer`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    {
      "rows":1
    }
    ```
    
    
a name="getBFCsEleve" />    
   ## getBFCsEleve
     Retourne les notes des bfcs pour un élève. 
    
   * **URL**
    
     `viescolaire/evaluations/bfc/eleve/:idEleve`
    
   * **Method:**
      
     `GET` 
      
   *  **URL Params**
    
       **Required:**
     
      `idEleve = String`
      `idEtablissement = String`
      
   * **Response:**
      
       * **Code:** 200 <br />
       * **Content**:  
       ```json
       [{
         "id":556,
         "id_eleve":"f0c65d0b-11e7-4ae9-83c2-401b1b27c0c6",
         "owner":"95dbe1c5-7960-451b-877d-edddc7a6a5a4",
         "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
         "valeur":3.0,
         "id_domaine":16,
         "modified":null,
         "created":null
       }]
       
       ```
      