# UtilsController

##### Table of Contents
  * [getIdsStructuresActive](#getIdsStructuresActive)
  
<a name="getIdsStructuresActive" />

## getIdsStructuresActive

  Récupère la liste des structures de l'utilisateur dont le module est activé.

* **URL**

  `/presences/user/structures/actives`

* **Method:**
 
  `GET`
  
*  **URL Params**

   _Aucun paramètre nécessaire_

* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [{
        "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b"
    }]
    ``` 