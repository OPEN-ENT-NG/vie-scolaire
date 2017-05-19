# ClasseController

##### Table of Contents
  * [getEleveClasse](#getEleveClasse)
  * [getElevesClasse](#getElevesClasse)
  * [getClasses](#getClasses)
  
<a name="getEleveClasse" />

## getEleveClasse

Recupere tous les élèves d'une Classe.

* **URL**

  `viescolaire/classes/:idClasse/users`

* **Method:**
 
  `GET`
  
* **URL Params** 
  
  **Required:**
  
     `idClasse = String`



* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
       "id": "7e28f451-b00f-435d-9f07-1ea737a49e8f",
          "firstName": "Amélie",
          "lastName": "AUBIN",
          "level": "TERMINALE GENERALE & TECHNO YC BT",
          "classes": ["2975$T 4S"]
    }]
    ``` 

<a name="getElevesClasse" />

## getElevesClasse

Recupere tous les élèves d'une liste de classes d'un établissement donné.

* **URL**

  `viescolaire/eleves`

* **Method:**
 
  `GET`
  
* **URL Params** 
  
  **Required:**
  
     `idClasse = String`
     
     `idEtablissement = String`



* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
         "id": "ccee6dde-bdbe-4186-978b-2639ba935b83",
          "displayName": "Adrien GUILLOTON",
          "firstName": "Adrien",
          "lastName": "GUILLOTON",
          "idClasse": "f4ff9e02-77b2-4369-b49a-3988540b4f81"
      
    }]
    ``` 
    
<a name="getClasses" />

## getClasses

Retourne les classes de l'utilisateur.

* **URL**

  `viescolaire/classes`

* **Method:**
 
  `GET`
  
* **URL Params** 
  
  **Required:**
      
     `idEtablissement = String`



* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
            "name": "T 4S",
            "externalId": "2975$T 4S",
            "id": "f4ff9e02-77b2-4369-b49a-3988540b4f81",
            "type_groupe": 0,
            "id_cycle": null,
            "libelle_cycle": null
        
    }]
    ``` 