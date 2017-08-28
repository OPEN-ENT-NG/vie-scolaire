# Appreciation

##### Table of Contents
  * [getAnnotations](#getAnnotations)
  * [create](#create)
  * [update](#update)
  * [delete](#delete)
  
<a name="getAnnotations" />


## getAnnotations

   récupère les annotations d'un établissement.
  
 * **URL**
  
   `/viescolaire/evaluations/annotations?idEtablissement=?`
  
 * **Method:**
    
   `GET` 

 * **Data Params**

     **Required:**           
      `idEtablissement`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
          ```json
         [
           {
              "id":1,
              "libelle":"Dispense",
              "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
              "libelle_court":"DISP"
           },
           {
              "id":2,
              "libelle":"Absence",
              "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
              "libelle_court":"ABSC"
           },
           {
              "id":3,
              "libelle":"Non Noté",
              "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
              "libelle_court":"NN"
           }
        ]
          ```
<a name="update" />

## update
   met à jour une annotation.
  
 * **URL**
  
   `/viescolaire/evaluations/annotation?idDevoir=?`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {
        "id_devoir"      : 'Integer',
        "id_annotation"  : 'Integer',
        "id_eleve"       : 'String'
     }
     ```    
     **Required:**           
         `idDevoir = Integer`
        
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
  Supprime une Annotation donnée.
 
* **URL**
 
  `/viescolaire/evaluations/annotation?idDevoir=?&idEleve=?
 
* **Method:**
   
  `DELETE` 
   
*  **URL Params**
 
    **Required:**
  
   `idDevoir = Integer`
   `idEleve = String`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    {
      "rows":1
    }
    ```