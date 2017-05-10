**GetAbsencesPrevClassePeriode**
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
 