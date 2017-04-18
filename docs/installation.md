# Installation

Cette page est consacrée à l'installation du module Vie Scolaire pour une phase de développement.

## Prérequis
* [NodeJS](https://nodejs.org)
* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [Vert.x 2.1](https://bintray.com/vertx/downloads/distribution/2.1)
* [Gradle 1.6](https://downloads.gradle.org/distributions/gradle-1.6-all.zip)

## Installation des dépendances
```npm
    cd <project_path>
    npm install
    npm install -g gulp typescript tslint
```

## Compilation
La première compilation nécessite la récupération des dépendances front du coeur de l'ent. Pour cela, lancer la commande :
```
    gulp build
```

Lors des futures compilations, la commande `gulp build-local` pourra être utilisée puisque les dépendances auront déjà étaient rappatriée. 
La tâche `build` ne sera plus utilisée que pour mettre à jour l'application front.

Une fois le front compilé, il faut maintenant compiler le Java. Pour cela, lancer la commande d'installation via `gradle` : 
```
    gradle install
```

Une fois la compilation finie, vous trouverez dans le dossier `build` les différentes formes de compilations. 
Celle qui nous intéresse ici se trouve dans le dossier `mods` et qui se nomme `fr.openent~viescolaire~<project_version>`