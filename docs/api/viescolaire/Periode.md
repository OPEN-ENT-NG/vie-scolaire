# PeriodeController

##### Table of Contents


## viewPeriodes
  Retourne les periodes pour un établissement donné.
 
* **URL**
 
  `viescolaire/evaluations/periodes`
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `idEtablissement = String`
   
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [{
      "id": 4,
      "id_etablissement": "7d6b93f1-064c-4a15-88c7-815ebf33815b",
      "libelle": "1er Trimestre",
      "timestamp_dt": "2016-09-05T00:00:00.000",
      "timestamp_fn": "2016-11-20T00:00:00.000",
      "date_fin_saisie": "2016-11-10T00:00:00.000"
     }, {
      "id": 5,
      "id_etablissement": "7d6b93f1-064c-4a15-88c7-815ebf33815b",
      "libelle": "2eme Trimestre",
      "timestamp_dt": "2016-11-21T00:00:00.000",
      "timestamp_fn": "2017-02-26T00:00:00.000",
      "date_fin_saisie": "2017-02-16T00:00:00.000"
    }, {
      "id": 6,
      "id_etablissement": "7d6b93f1-064c-4a15-88c7-815ebf33815b",
      "libelle": "3eme Trimestre",
      "timestamp_dt": "2017-02-27T00:00:00.000",
      "timestamp_fn": "2017-07-07T00:00:00.000",
      "date_fin_saisie": "2017-06-27T00:00:00.000"
    }]
    ```
 
