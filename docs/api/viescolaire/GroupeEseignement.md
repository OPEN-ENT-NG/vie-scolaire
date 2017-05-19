# GroupeEnseignementController
  
##### Table of Contents
  
   * [getGroupesEnseignementUser](#getGroupesEnseignementUser)
   * [getGroupesEnseignementUsers](#getGroupesEnseignementUsers)
   * [getNameOfGroupeClasse](#getNameOfGroupeClasse)
    
  <a name="getGroupesEnseignementUser" />
  
  ## getGroupesEnseignementUser
  
  Liste les groupes d'enseignement d'un utilisateur.
  
  * **URL**
  
    `viescolaire/groupe/enseignement/user/:userid`
  
  * **Method:**
   
    `GET`
    
  * **URL Params** 
    
    **Required:**
    
       `userId = String`
  
  
  
  * **Réponse:**
    
     * **Status**: `200`
     * **Content**: 
      ```json
      [{
          "name": "T 5SP2",
          "externalId": "2975$T 5SP2",
          "id": "91081d10-57af-463d-902c-c57e60c98bc5"
      }]
      ``` 
  
<a name="getGroupesEnseignementUsers" />
  
  ## getGroupesEnseignementUsers
  
  Liste les groupes des utilisateurs d'un groupe.
  
  * **URL**
  
    `viescolaire/groupe/enseignement/users/:groupId`
  
  * **Method:**
   
    `GET`
    
  * **URL Params** 
    
    **Required:**
    
       `userId = String`  
  
  
  * **Réponse:**
    
     * **Status**: `200`
     * **Content**: 
      ```json
      [{
           "lastName": "BAUMARD",
           "firstName": "AURELIEN",
           "id": "f7213c07-c889-4661-ae61-a32bc18a3185",
           "login": "aurelien.baumard",
           "activationCode": null,
           "birthDate": "1979-05-22",
           "blocked": null,
           "source": "AAF"
      }]
      ``` 
  
  
  <a name="getNameOfGroupeClasse" />
  
  ## getNameOfGroupeClasse
  
  Récupère le nom d'un groupe ou d'une classe. 
  
  * **URL**
  
    `viescolaire/class/group/:groupId`
  
  * **Method:**
   
    `GET`
    
  * **URL Params** 
    
    **Required:**
    
           
       `groupId = String`
  
  
  
  * **Réponse:**
    
     * **Status**: `200`
     * **Content**: 
      ```json
      [{
         "id": "72b58a7b-9976-40ea-8078-ec13db868e4a",
         "name": "2EE2GR7"
      }]
      ``` 
