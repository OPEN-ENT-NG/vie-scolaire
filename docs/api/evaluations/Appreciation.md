# Appreciation

##### Table of Contents
  * [create](#create)
  * [update](#update)
  * [deleteAppreciationDevoir](#deleteAppreciationDevoir)
  
<a name="create" />


## create

   Créé une appreciation.
  
 * **URL**
  
   `viescolaire/evaluations/appreciation`
  
 * **Method:**
    
   `POST` 

 * **Data Params**
    ```json
     {
       "id_devoir" : 164,
       "id_eleve" :"b1e7b1cb-9f40-4520-9bcf-e038c9619912",
       "valeur" : "appreciation"
     }
     ```
     **Required:**           
            `id_devoir`,
                `id_eleve`, 
                `valeur`
            
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
   met à jour une appreciation.
  
 * **URL**
  
   `viescolaire/evaluations/appreciation`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {
       "id" : 11, 
       "id_devoir" : 164,
       "id_eleve" :"b1e7b1cb-9f40-4520-9bcf-e038c9619912",
       "valeur" : "update"
     }
     ```    
     **Required:**           
            `id_personnel`,
                `id_cours`, 
                `id_etat`
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {
       "rows":1
    }
     ```

<a name="deleteAppreciationDevoir" />

## deleteAppreciationDevoir
  Supprime une appréciation donnée.
 
* **URL**
 
  `viescolaire/appreciation`
 
* **Method:**
   
  `DELETE` 
   
*  **URL Params**
 
    **Required:**
  
   `idAppreciation = Integer`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    {
      "rows":1
    }
    ```
   