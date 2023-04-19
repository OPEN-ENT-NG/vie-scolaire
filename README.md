# À propos de l'application Vie Scolaire  
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Hauts-de-France, Département de l'Essonne, Région Nouvelle Aquitaine
* Développeur(s) : CGI
* Financeur(s) : Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI
* Description : Application de vie scolaire

# Présentation du module

L'application Vie Scolaire implémente des fonctionnalités de gestion des devoirs et de visualisation des résultats des élèves.

#### Evaluation

* Création de devoirs, évaluation possible de compétences, évaluation numérique ou non.
* Edition de devoirs.
* Ajout de notes et d'appréciations.
* Visualisation des devoirs numériques non-notés ou partiellement notés, avec pourcentage de complétion.
* Mise à jour en temps réel des notes minimum, maximum et de la moyenne lors de la notation d'un devoir.
* Gestion des remplacements.

#### Resultat

* Compte-rendus des évaluations de compétences.
* Historique des évaluations de compétences.
* Bilan de fin de cycle, export vers pdf.
* Releve de notes

## Construction
```
gradle copyMod
```

## Configuration

<pre>
{
  "config": {
    ...
    "temporary-directory": "/tmp",
    "exports" : {
       "template-path" : "./public/templates/pdf/"
    },
    "update-classes" : {
       "timeout-transaction" : 1000,
       "enable-date" : "2021-09-20"
    },
    "services": {
        "competences": false,
        "presences": false,
        "edt": false,
        "diary": false,
        "massmailing": false
    },
    "initDefaultSubject": "ÉCOLE"
  }
}

</pre>

Si vous souhaitez avoir accès depuis vie scolaire aux modules présents dans services, vous devez mettre à ***true*** les modules souhaités.