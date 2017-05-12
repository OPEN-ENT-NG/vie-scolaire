#Appel

##### Table of Contents
  * [createAppel](#createAppel)
  * [updateAppel](#updateAppel)
  * [getAppelCours](#getAppelCours)
  * [getAppelPeriode](#getAppelPeriode)
  * [getAppelsNonEffectues](#getAppelsNonEffectues)
  
<a name="createAppel" />


 ## createAppel

   Créé un appel.
  
 * **URL**
  
   `/appel`
  
 * **Method:**
    
   `POST` 

 * **Data Params**
    ```json
     {
       "id_cours" : 9,
       "id_etat" : 2,
       "id_justificatif" : null,
       "id_personnel" : "89a3d71f-6ba9-47bf-b708-e3f54a345b40"
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
      "id":11 
     }
     ```
<a name="updateAppel" />

## updateAppel
   met à jour un appel.
  
 * **URL**
  
   `/appel`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {
       "id" : 11, 
       "id_cours" : 9,
       "id_etat" : 2,
       "id_justificatif" : null,
       "id_personnel" : "89a3d71f-6ba9-47bf-b708-e3f54a345b40"
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
    {"rows":1}
     ```

<a name="getAppelCours" />

## getAppelCours
  Récupère l'appel associé à un cours.
 
* **URL**
 
  `/appel/cours/:coursId`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `coursId = Integer`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
      "id":11,
      "id_personnel":"89a3d71f-6ba9-47bf-b708-e3f54a345b40",
      "id_cours":9,
      "id_etat":1,
      "id_justificatif":null
    }]
    ```
    
<a name="getAppelPeriode" />
    
## getAppelPeriode
  Récupère les appels sur une période d'un établissement sur une période.
 
* **URL**
 
  `/appels/:dateDebut/:dateFin`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
    `dateDebut = String`
    `dateFin = String`
    `idEtablissement = String`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
        "timestamp_dt":"2017-05-02T15:03:10.918",
        "timestamp_fn":"2017-05-02T19:03:10.918",
        "id_matiere":"137372-1488276748038",
        "salle":"6",
        "id":5,
        "id_etat":3,
        "id_classe":"cda2cfa0-49cd-472e-99f3-3aa23a338ec9",
        "id_personnel":"89a3d71f-6ba9-47bf-b708-e3f54a345b40"
    }]
    ```

<a name="getAppelsNonEffectues" />

## getAppelsNonEffectues
  Récupère les appels non effectués sur une période d'un établissement sur une période.
 
* **URL**
 
  `/appels/noneffectues/:dateDebut/:dateFin`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
    `dateDebut = String`
    `dateFin = String`
    `idEtablissement = String`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
        "timestamp_dt":"2017-05-02T15:03:10.918",
        "timestamp_fn":"2017-05-02T19:03:10.918",
        "id_matiere":"137372-1488276748038",
        "salle":"6",
        "id":5,
        "id_etat":1,
        "id_classe":"cda2cfa0-49cd-472e-99f3-3aa23a338ec9",
        "id_personnel":"89a3d71f-6ba9-47bf-b708-e3f54a345b40"
    }]
    ```
    
 * **Notes:**
       
       `dateDebut et dateFin sont au format : yyyy-mm-dd 
               ex: 2017-05-10 pour le 10 mai 2017.`