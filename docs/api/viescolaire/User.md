# UtilsController

##### Table of Contents
  * [getActivedStructures](#getIdsStructuresActive)
  * [createActivedStructure](#createStructureActive)
  * [deleteActivatedStructure](#deleteActivatedStructure)
  
<a name="getActivedStructures" />

## getActivedStructures

Retourne la liste des identifiants des structures actives de l'utilisateur pour un module donné.

* **URL**

  `viescolaire/user/structures/actives`

* **Method:**
 
  `GET`
  
* **URL Params** 
  
  **Required:**
  
     `module = String`



* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
        "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b"
    }]
    ``` 
    
* **Notes:**
       
        ` les valeurs possibles du paramètre module
         - "notes"     pour le module évaluations.   
         - "presences" pour présences. `
       

<a name="createActivedStructures" />

## createActivedStructures

Active un module pour une structure donnée.

* **URL**

  `viescolaire/user/structures/actives`

* **Method:**
 
  `POST`
  
* **Data Params**
    ```json
     {
       "module" : "presences",
       "structureId" : "89a3d71f-6ba9-47bf-b708-e3f54a345b40"
     }
     ```
     **Required:**           
            `module`,
            `structureId`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     []
     ``` 
    
* **Notes:**
       
        ` les valeurs possibles du paramètre module
         - "notes"     pour le module évaluations.   
         - "presences" pour présences. `
         
         
<a name="deleteActivedStructures" />

## deleteActivedStructures

Supprime l'activation d'un module pour une structure donnée.

* **URL**

  `viescolaire/user/structures/actives`

* **Method:**
 
  `DELETE`
  
* **URL Params** 
  
  **Required:**
  
     `module = String`



* **Réponse:**
  
 * **Data Params**
    ```json
     []
     ```
     **Required:**           
            `module`,
            `structureId`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     {
      "id_etablissement": "89a3d71f-6ba9-47bf-b708-e3f54a345b40"
     }
     ``` 
    