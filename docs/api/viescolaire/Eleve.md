  # EleveController
  
  ##### Table of Contents
    * [getEleveClasse](#getEleveClasse)
    * [getEleveEtab](#getEleveEtab)
    
  <a name="getEleveClasse" />
  
  ## getEleveClasse
  
  Récupère tous les élèves d'une Classe.
  
  * **URL**
  
    `viescolaire/classe/:idClasse/eleves`
  
  * **Method:**
   
    `GET`
    
  * **URL Params** 
    
    **Required:**
    
       `idClasse = String`
  
  
  
  * **Réponse:**
    
     * **Status**: `200`
     * **Content**: 
      ```json
      [{
          "id": "7e28f451-b00f-435d-9f07-1ea737a49e8f",
              "firstName": "Amélie",
              "lastName": "AUBIN",
              "level": "TERMINALE GENERALE & TECHNO YC BT",
              "classes": ["2975$T 4S"]
      }]
      ``` 
      
  
  <a name="getEleveEtab" />
  
  ## getEleveEtab
  
  Recupere tous les élèves d'un etablissment.
  
  * **URL**
  
    `viescolaire/etab/eleves/:idEtab`
  
  * **Method:**
   
    `GET`
    
  * **URL Params** 
    
    **Required:**
    
       
       `idEtab = String`
  
  
  
  * **Réponse:**
    
     * **Status**: `200`
     * **Content**: 
      ```json
      [{
           "id": "e70fc8b2-8837-45f8-93ad-974e39efb1da",
           "firstName": "Arnaud",
           "lastName": "BIGUERAUD",
           "level": "SECONDE GENERALE & TECHNO YC BT",
           "classes": ["e6090f83-e8a0-48ce-be8e-1880c52a014b"]
      }]
      ``` 
