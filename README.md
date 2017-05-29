# À propos de l'application Vie Scolaire
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt)
* Développeur : CGI
* Financeur : Département de Seine et Marne, Région Haut-de-France et Région Nouvelle-Aquitaine
* Description : Service de vie scolaire

# Documentation technique

## Construction
```
gradle copyMod
```

## Déployer dans ent-core


## Configuration

Dans le fichier `/vie-scolaire/deployment/viescolaire/conf.json.template` :

Configurer l'application de la manière suivante :
```    
{
    "name": "fr.openent~viescolaire~0.4.0",
    "config": {
    "main" : "fr.openent.Viescolaire",
    "port" : 8128,
    "sql" : true,
    "app-name" : "Viescolaire",
    "app-address" : "/viescolaire",
    "app-icon" : "Viescolaire-large",
    "db-schema" : "viesco",
    "host": "${host}",
    "ssl" : $ssl,
    "auto-redeploy": false,
    "userbook-host": "${host}",
    "integration-mode" : "HTTP",
    "app-registry.port" : 8012,
    "mode" : "${mode}",
    "entcore.port" : 8009,
    "exports" : {
        "template-path" : "./public/templates/pdf/"
    }
}
```

Associer une route d'entrée à la configuration du module proxy intégré :
```
{
    "location": "/viescolaire",
    "proxy_pass": "http://localhost:8128"
}
```



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
