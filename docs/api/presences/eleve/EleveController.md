*EleveController*
 -------- 
 ---- 
 
**getAbsencesPrevClassePeriode**
---- 
  Récupère les absences prévisionnels d'un ensemble d'élève(s) sur une période donnée.
 
* **URL**
 
  `/absencesprev/eleves`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `id_eleve = String`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
      "id":2,
      "timestamp_dt":"2017-05-10T08:00:00.000",
      "timestamp_fn":"2017-05-10T09:00:00.000",
      "id_motif":8,
      "id_eleve":"73814199-b34b-4b58-a00d-5fdd44d07f8f"
      }]
    ```
 
* **Notes:**

     `Le paramètre id_eleve peut être repété pour définir l'ensemble d'élève sur lequel sont récupérées les 
     absences prévisionnels. ` 
 
 **getAbsencesSansMotifs**
 ---- 
   Récupère les absences Sans Motif.
  
 * **URL**
  
   `/sansmotifs/:dateDebut/:dateFin`
  
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
          "id":8,
          "commentaire":null,
          "saisie_cpe":false,
          "id_eleve":"5aa8e59c-d9c1-4da4-8389-2f5abaed0177",
          "id_motif":8,
          "timestamp_dt":"2017-05-10T10:00:10.918",
          "timestamp_fn":"2017-05-10T11:00:10.918",
          "id_appel":12,
          "id_type":1,
          "id_classe":"cda2cfa0-49cd-472e-99f3-3aa23a338ec9",
          "id_personnel":"89a3d71f-6ba9-47bf-b708-e3f54a345b40",
          "libelle":"Sans motif",
          "justifiant":false
       }]
     ```
  
 * **Notes:**
      
      `dateDebut et dateFin sont au format : yyyy-mm-dd 
              ex: 2017-05-10 pour le 10 mai 2017.`

 **getAbsencesPrevInPeriod**
 ---- 
   Récupère les absences prévisionnelles sur une période.
  
 * **URL**
  
   `/eleve/:idEleve/absencesprev/:dateDebut/:dateFin`
  
 * **Method:**
    
   `GET` 
    
 *  **URL Params**
  
     **Required:**
   
    `dateDebut = String`
    `dateFin = String`
    `idEleve = String`
    
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     [{
          "id":1,
          "timestamp_dt":"2017-05-10T00:00:00.000",
          "timestamp_fn":"2017-05-12T00:00:00.000",
          "id_motif":9,
          "id_eleve":"3c16a728-763e-4880-8349-069eef2c6a5e"
       }]
     ```
  
 * **Notes:**
      
      `dateDebut et dateFin sont au format : yyyy-mm-dd 
              ex: 2017-05-10 pour le 10 mai 2017.`
              
              
 **getAbsences**
  ---- 
    Récupère les absences D'un établissement sur une période.
   
  * **URL**
   
    `/eleves/evenements/:dateDebut/:dateFin`
   
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
          "id":67,
          "id_matiere":"137372-1488276748038",
          "commentaire":null,
          "saisie_cpe":false,
          "id_eleve":"714bfd47-d40c-4908-9240-342034f1d328",
          "id_motif":8,
          "timestamp_dt":"2017-05-10T10:00:10.918",
          "timestamp_fn":"2017-05-10T11:00:10.918",
          "id_personnel":"cf3ef520-bf66-4256-9478-b1d5be02141e",
          "id_classe":"9802ccaa-62ef-460c-b97a-793179e25001",
          "id_appel":8,
          "id_type":1
        }]
      ```
   
  * **Notes:**
       
       `dateDebut et dateFin sont au format : yyyy-mm-dd 
               ex: 2017-05-10 pour le 10 mai 2017.`
               
               
**getAbsencesPrev**
 ---- 
   Récupère les absences prévisionnelles d'un élève.
  
 * **URL**
  
   `/eleve/:idEleve/absencesprev`
  
 * **Method:**
    
   `GET` 
    
 *  **URL Params**
  
     **Required:**
     
    `idEleve = String`
    
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     [{
          "id":1,
          "timestamp_dt":"2017-05-10T00:00:00.000",
          "timestamp_fn":"2017-05-12T00:00:00.000",
          "id_motif":9,
          "id_eleve":"3c16a728-763e-4880-8349-069eef2c6a5e"
       }]
     ```
     
     
 **getEvenements**
 ---- 
   Récupère les événements sur une période d'un élève.
  
 * **URL**
  
   `/eleve/:idEleve/absencesprev/:dateDebut/:dateFin`
  
 * **Method:**
    
   `GET` 
    
 *  **URL Params**
  
     **Required:**
   
    `dateDebut = String`
    `dateFin = String`
    `idEleve = String`
    
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     [{
          "id":1,
          "timestamp_dt":"2017-05-10T00:00:00.000",
          "timestamp_fn":"2017-05-12T00:00:00.000",
          "id_motif":9,
          "id_eleve":"3c16a728-763e-4880-8349-069eef2c6a5e"
       }]
     ```