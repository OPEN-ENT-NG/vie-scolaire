*CoursController*
 -------- 
 ---- 

**GetClasseCours**
---- 
  Récupère tous les cours d'une classe dans une période donnée.
 
* **URL**
 
  `/:idClasse/cours/:dateDebut/:dateFin`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `idClasse = String`
   
   `dateDebut = String`
   
   `dateFin = String`
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
      "id":9,
      "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
      "timestamp_dt":"2017-05-10T08:00:10.918",
      "timestamp_fn":"2017-05-10T09:00:10.918",
      "salle":"5",
      "id_matiere":"137372-1488276748038",
      "edt_classe":null,
      "edt_date":null,
      "edt_salle":null,
      "edt_matiere":null,
      "edt_id_cours":null,
      "id_classe":"cda2cfa0-49cd-472e-99f3-3aa23a338ec9",
      "id_personnel":"89a3d71f-6ba9-47bf-b708-e3f54a345b40"
      }]
    ```
 
* **Notes:**

     `dateDebut et dateFin sont au format : yyyy-mm-dd 
        ex: 2017-05-10 pour le 10 mai 2017.` 
 