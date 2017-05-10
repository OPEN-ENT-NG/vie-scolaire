**GetAbsencesDernierCours**
---- 
  Récupère toutes les absences du cours précédent en fonction de l'identifiant de la classe et de l'identifiant du cours.
 
* **URL**
 
  `/precedentes/classe/:classeId/cours/:coursId `
 
* **Method:**
   
  `GET` 
   
*  **URL Params**
 
    **Required:**
  
   `classeId = String`
   
   `coursId = Integer`
   
   
* **Response:**
   
    * **Code:** 200 <br />
    * **Content**:  
    ```json
    [
       {"id_eleve":"3c16a728-763e-4880-8349-069eef2c6a5e"},
       {"id_eleve":"35eecf92-4313-4322-ad21-a2f4900ee5b9"}
    ]
    ```
