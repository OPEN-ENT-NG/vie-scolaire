**GetClasseCours**
---- 
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
