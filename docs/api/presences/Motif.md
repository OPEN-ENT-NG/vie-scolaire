# MotifController

##### Table of Contents
  * [getAbscMotifsEtablissement](#getAbscMotifsEtablissement)
  * [getAbscJustificatifsEtablissement](#getAbscJustificatifsEtablissement)
  * [getCategorieAbscMotifsEtablissement](#getCategorieAbscMotifsEtablissement)
  * [createMotifAbs](#createMotifAbs)
  * [updateMotifAbs](#updateMotifAbs)
  * [createCategorieMotifAbs](#createCategorieMotifAbs)
  * [updateCategorieMotifAbs](#updateCategorieMotifAbs)


<a name="getAbscMotifsEtablissement" />

## getAbscMotifsEtablissement

Récupère tous les motifs en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/motifs`

* **Method:**
 
  `GET`
  
*  **URL Params**

   idEtablissement : l'id de l'établissement dont on veut extraire les motifs. 

* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
        "id":9,"id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
        "libelle":"Maladie avec certificat médical",
        "justifiant":true,"commentaire":"Maladie avec certificat médical",
        "defaut":true
    }]
    ``` 
    
<a name="getAbscJustificatifsEtablissement" />
    
## getAbscJustificatifsEtablissement

Récupère tous les justificatifs en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/justificatifs`

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
      "libelle":"Malade",
      "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b"
    }]
    ``` 


<a name="getCategorieAbscMotifsEtablissement" />
    
## getCategorieAbscMotifsEtablissement

Récupère toutes les catégories de motifs d'absences en fonction de l'id de l'établissement.

* **URL**

  `viescolaire/presences/motifs/categorie`

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

<a name="createMotifAbs" />

## createMotifAbs

   Créé un Motif d'absence.
  
 * **URL**
  
   `viescolaire/presences/motif`
  
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
<a name="updateMotifAbs" />

## updateMotifAbs
   met à jour un motif d'absence.
  
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
<a name="createCategorieMotifAbs" />

## createCategorieMotifAbs

   Créé une Catégorie de Motif d'appel d'absence.
  
 * **URL**
  
   `viescolaire/presences/categorie/absence`
  
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
<a name="updateCategorieMotifAbs" />

## updateCategorieMotifAppel
   met à jour une catégorie de motif d'absence.
  
 * **URL**
  
   `viescolaire/categorie/absences`
  
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
    