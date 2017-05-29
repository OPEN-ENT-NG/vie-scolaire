# MotifController

##### Table of Contents
  * [getAbscMotifsAppelEtablissement](#getAbscMotifsAppelEtablissement)
  * [getCategorieAbscMotifsAppelEtablissement](#getCategorieAbscMotifsAppelEtablissement)
  * [createMotifAppel](#createMotifAppel)
  * [updateMotifAppel](#updateMotifAppel)
  

<a name="getAbscMotifsAppelEtablissement" />

## getAbscMotifsAppelEtablissement

Récupère tous les motifs d'appel oublié en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/motifsAppel`

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
    
<a name="getCategorieAbscMotifsAppelEtablissement" />
    
## getCategorieAbscMotifsAppelEtablissement

Récupère toutes les catégories de motifs d'appel oublié en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/motifsAppel/categorie`

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

<a name="createMotifAppel" />

## createMotifAppel

   Créé un Motif d'appel oublié.
  
 * **URL**
  
   `viescolaire/presences/motifAppel`
  
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
    