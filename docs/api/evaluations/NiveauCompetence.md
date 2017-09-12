# NiveauDeMaitriseController

##### Table of Contents
  * [getMaitriseLevel](#getMaitriseLevel)
  * [getPersoNiveauMaitrise](#getPersoNiveauMaitrise)
  * [create](#create)
  * [ markUserInUsePerso](#markUserInUsePerso)
  * [update](#update)
  * [delete](#delete)
  * [deleteUserFromPerso](#deleteUserFromPerso)

<a name="getMaitriseLevel" />

## getMaitriseLevel

Recupere tous les niveaux de maitrise d'un établissement.

* **URL**

  `viescolaire/evaluations/maitrise/level/:idEtablissement`

* **Method:**
 
  `GET`
  
*  **URL Params**

   idEtablissement : l'id de l'établissement dont on veut extraire les niveaux de maitrise.  

* **Réponse:**
  
   * **Status**: `200`
   * **Content**: 
    ```json
    [  
       {  
          "libelle":"Maîtrise fragile",
          "ordre":2,
          "default":"orange",
          "id_cycle":1,
          "id_niveau":2,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#FF8500",
          "lettre":"MF",
          "id":24,
          "cycle":"cycle 4"
       },
       {  
          "libelle":"Maîtrise satisfaisante",
          "ordre":3,
          "default":"yellow",
          "id_cycle":1,
          "id_niveau":3,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#ecbe30",
          "lettre":"MS",
          "id":22,
          "cycle":"cycle 4"
       },
       {  
          "libelle":"Maîtrise satisfaisante",
          "ordre":3,
          "default":"yellow",
          "id_cycle":2,
          "id_niveau":7,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#ecbe30",
          "lettre":"ms",
          "id":28,
          "cycle":"cycle 3"
       },
       {  
          "libelle":"Maîtrise fragile",
          "ordre":2,
          "default":"orange",
          "id_cycle":2,
          "id_niveau":6,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#FF8500",
          "lettre":"mf",
          "id":30,
          "cycle":"cycle 3"
       },
       {  
          "libelle":"Très Bonne Maîtrise",
          "ordre":4,
          "default":"green",
          "id_cycle":1,
          "id_niveau":4,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#0000ff",
          "lettre":"TB",
          "id":20,
          "cycle":"cycle 4"
       },
       {  
          "libelle":"Très Bonne Maîtrise",
          "ordre":4,
          "default":"green",
          "id_cycle":2,
          "id_niveau":8,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#0080ff",
          "lettre":"tb",
          "id":26,
          "cycle":"cycle 3"
       },
       {  
          "libelle":"Maîtrise insuffisante tot",
          "ordre":1,
          "default":"red",
          "id_cycle":1,
          "id_niveau":1,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#e13a3a",
          "lettre":"  ",
          "id":18,
          "cycle":"cycle 4"
       },
       {  
          "libelle":"Maîtrise insuffisante toto",
          "ordre":1,
          "default":"red",
          "id_cycle":2,
          "id_niveau":5,
          "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
          "couleur":"#e13a3a",
          "lettre":"mi",
          "id":32,
          "cycle":"cycle 3"
       }
    ]
    ``` 
    
<a name="getPersoNiveauMaitrise" />

## getPersoNiveauMaitrise

   Vérifie si un utilisateur utilise la personnification des couleurs de compétence de son établissement.
  
 * **URL**
  
   `viescolaire/evaluations/maitrise/perso/use/:idUser`
  
 * **Method:**
    
   `GET`
    
  *  **URL Params**
  
     idUser : l'id de l'utilisateur dont on veut connaitre le thème.  
  
  * **Réponse:**
    
     * **Status**: `200`
     * **Content**: 
      ```json
      [{
        id_user: '5b275ccb-9b38-4c7b-bc30-282a2aadb318', 
        id: 12
      }]
     ```
     **Required:**           
            `idUser`
            
            
<a name="create" />

## create
   Créer une personnalisation  pour un niveau de maitrise avec les données passées en POST.
  
 * **URL**
  
   `viescolaire/evaluations/maitrise/level`
  
 * **Method:**
    
   `POST` 

 * **Data Params**
    ```json
     {   "couleur":"#0000a0",
         "id":20,
         "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
         "id_niveau":4,
         "lettre":"Tc"
     }
     ```    
     **Required:**           
            `id`.
                
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {"rows":1}
     ```
   
<a name="markUserInUsePerso" />
    
## markUserInUsePerso

Marquer l'utilisateur comme utilisant la personnification du niveau de maitrise de son établissement

* **URL**

  `viescolaire/evaluations/maitrise/perso/use`

* **Method:**
  `POST` 
 
  * **Data Params**
     ```json
          "id_user":'5b275ccb-9b38-4c7b-bc30-282a2aadb318'
      }
      ```    
      **Required:**           
             `id_user`.
                 
         
  * **Response:**
     
      * **Code:** 200 <br />
      * **Content**:  
      ```json
     {"rows":1}
      ```

<a name="update" />

## update

   Modifie un niveau de maitrise avec les données passées en PUT.
  
 * **URL**
  
   `viescolaire/evaluations/maitrise/level`
  
 * **Method:**
    
   `PUT` 

 * **Data Params**
    ```json
     {  
        "couleur":"#0000a0",
        "id":20,
        "id_etablissement":"7d6b93f1-064c-4a15-88c7-815ebf33815b",
        "id_niveau":4,
        "lettre":"Tc"
     }
     ```
     **Required:**           
            `id_etablissement`,
                `id_niveau`
            
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
     {
      "id":20 
     }
     ```
<a name="delete" />

## delete
   Supprimer tous les niveaux de maitrise d'un étbalissement donné.
  
 * **URL**
  
   `viescolaire/evaluations/maitrise/level/:idEtablissement`
  
 * **Method:**
    
   `DELETE` 
   

 * **Data Params**
    ```
         "idEtablissement": "0c03ee92-7ca0-4025-9971-df3e63a2ce64"
     ```    
     **Required:**           
            `idEtablissement`.
                
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {"rows":8}
     ```
     
<a name="deleteUserFromPerso" />

## deleteUserFromPerso
   Permet à un utilisateur de ne plus  utiliser la personnalisation des niveaux de compétences
  
 * **URL**
  
   `viescolaire/evaluations/maitrise/perso/use/:idUser`
  
 * **Method:**
    
   `DELETE` 
   

 * **Data Params**
    ```
         "idUser" = "0c03ee92-7ca0-4025-9971-df3e63a2ce64"
     ```    
     **Required:**           
            `idUser`.
                
        
 * **Response:**
    
     * **Code:** 200 <br />
     * **Content**:  
     ```json
    {"rows":1}
     ```
