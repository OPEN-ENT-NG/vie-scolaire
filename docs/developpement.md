# Développement
Cette page est dédiée au développement et aux aides proposées.

## Tâches gulp
La liste suivante regroupe les tâches gulp du projet :

* `copy-local-libs` : Copie les dépendances du coeur pour la compilation du module;
* `drop-cache` : Supprime les dossiers de dépendances [bower](https://bower.io/) et nodeJS;
* `bower` : Récupère les dépendances bowers;
* `update-libs` : Mets à jours les dépendances du coeur de l'ent et les copie dans le module et les dépendances nodeJS;
* `ts` : Lance la mise à jour des dépendances du coeur de l'ent puis lance une compilation [Typescript](https://typescriptlang.org);
* `ts-local` : Compile les fichiers typescripts;
* `webpack` : Lance [Webpack](https://webpack.github.io) compilant l'application Vie Scolaire. Lance au préalable une compilation Typescript et une récupération des dépendances du coeur;
* `webpack-entcore` : Lance webpack compilant le coeur de l'ent. Lance au préalable une compilation Typescript et une récupération des dépendances du coeur;
* `webpack-local` : Lance webpack compilant l'application Vie Scolaire. Lance au préalable une compilation Typescript;
* `webpack-entcore-local` : Lance webpack compilant le coeur de l'ent. Lance au préalable une compilation Typescript;
* `drop-temp` : Supprimer les dossiers `dist` et `temp` dans `/src/main/resources/public`;
* `build` : Compile les applications Vie Scolaire et le coeur de l'ent. Lance au préalable une récupération des dépendances;
* `build-local` : Compile les applications Vie Scolaire et de coeur de l'ent;
* `remove-temp` : Supprime le dossier `temp`dans `/src/main/resources/public`;
* `updateRefs` : Met à jour les références vers les fichiers compilés grâce au manifest;
* `ts::lint::watch` : Lance une watcher lintant les fichiers typescript grâce à [TSLint](https://palantir.github.io/tslint/) lors de l'enregistrement d'un fichier;
* `ts::lint` : Lint les fichiers typescripts.

## TSLint
Afin de garantir la qualité du code front du projet, [TSLint](https://palantir.github.io/tslint/) a été ajouté. Cet outil permet d'analyser le code source
et d'en ressortir des erreurs de syntaxe et de mise en forme. L'objectif de l'outil est d'uniformiser le code source tout en respectant des règles syntaxiques 
définies. Ces règles sont définies dans le fichier `tslint.json` se trouvant à la racine du projet.