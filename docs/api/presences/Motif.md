# MotifController

## getAbscMotifsEtablissement

  Récupère tous les motifs en fonction de l'id de l'établissement

* **URL**

  `/motifs`

* **Method:**
 
  `GET`
  
*  **URL Params**

   Aucun paramètre : prend le premier établissement de l'utilisateur

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
    
## getAbscMotifsEtablissement

  Récupère tous les justificatifs en fonction de l'id de l'établissement

* **URL**

  `/justificatifs`

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