# MotifController

##### Table of Contents
  * [getAbscMotifsAppelEtablissement](#getAbscMotifsAppelEtablissement)
  * [createMotifAppel](#createMotifAppel)
  * [updateMotifAppel](#updateMotifAppel)
  * [getCategorieAbscMotifsAppelEtablissement](#getCategorieAbscMotifsAppelEtablissement)
  * [createCategorieMotifAppel](#createCategorieMotifAppel)
  * [updateCategorieMotifAppel](#updateCategorieMotifAppel)

<a name="getAbscMotifsAppelEtablissement" />

## getAbscMotifsAppelEtablissement

Récupère tous les motifs d'appel oublié en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/motif/appel`

* **Method:**
 
  `GET`
  
*  **URL Params**

   idEtablissement : l'id de l'établissement dont on veut extraire les motifs. 

* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
        "id":9,
        "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
        "libelle":"Maladie avec certificat médical",
        "justifiant":true,
        "commentaire":"Maladie avec certificat médical"
    }]
    ``` 
    
<a name="createMotifAppel" />

## createMotifAppel

   Créé un Motif d'appel oublié.
  
 * **URL**
  
   `viescolaire/presences/motif/appel`
  
 * **Method:**
    
   `POST` 

 * **Data Params**
    ```json
     {  
         "id_etablissement": "0c03ee92-7ca0-4025-9971-df3e63a2ce64",
         "libelle": "update",    
         "justifiant": true,
         "commentaire": "comment updated",
         "id_categorie": 1
     }
     ```
     **Required:**           
            `id_etablissement`,
                `justifiant`, 
                `id_categorie`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     {
      "id":11 
     }
     ```
<a name="updateMotifAppel" />

## updateMotifAppel
   met à jour un motif d'appel oublié.
  
 * **URL**
  
   `viescolaire/presences/motif`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {   "id": 1,
         "id_etablissement": "0c03ee92-7ca0-4025-9971-df3e63a2ce64",
         "libelle": "update",    
         "justifiant": true,
         "commentaire": "comment updated",
         "id_categorie": 1
     }
     ```    
     **Required:**           
            `id`.
                
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {"rows":1}
     ```
   
<a name="getCategorieAbscMotifsAppelEtablissement" />
    
## getCategorieAbscMotifsAppelEtablissement

Récupère toutes les catégories de motifs d'appel oublié en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/motifs/categorie/appel`

* **Method:**
 
  `GET`
  
*  **URL Params**

    **Required:**
  
    `idEtablissement = String`

* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{        
      "id":1,
      "libelle":"defaut",
      "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b"
    }]
    ``` 

<a name="createCategorieMotifAppel" />

## createCategorieMotifAppel

   Créé une Catégorie de Motif d'appel oublié.
  
 * **URL**
  
   `viescolaire/presences/categorie/appels`
  
 * **Method:**
    
   `POST` 

 * **Data Params**
    ```json
     {  
         "id_etablissement": "0c03ee92-7ca0-4025-9971-df3e63a2ce64",
         "libelle": "CATEGORIE"
     }
     ```
     **Required:**           
            `id_etablissement`,
                `libelle`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     {
      "id":11 
     }
     ```
<a name="updateCategorieMotifAppel" />

## updateCategorieMotifAppel
   met à jour une catégorie de motif d'appel oublié.
  
 * **URL**
  
   `viescolaire/categorie/appels`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {   "id": 1,
         "id_etablissement": "0c03ee92-7ca0-4025-9971-df3e63a2ce64",
         "libelle": "update"    
     }
     ```    
     **Required:**           
            `id`.
                
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {"rows":1}
     ```
